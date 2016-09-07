package sx.lambda.voxel.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.entity.LivingEntity;
import sx.lambda.voxel.entity.player.Player;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.BlockStorage.CoordinatesOutOfBoundsException;
import sx.lambda.voxel.world.chunk.IChunk;

/**
 * Handles input-based movement, forces, and gravity application.
 */
public class MovementHandler implements RepeatedTask {

    // In meters per milliseconds
    private static final float WALK_SPEED = 0.0032f;

    public MovementHandler(RadixClient game) {
        this.game = game;
    }

    @Override
    public String getIdentifier() {
        return "Movement Handler";
    }

    @Override
    public void run() {
        try {
            long lastMoveCheckMS = System.currentTimeMillis();

            while (!game.isDone()) {
                if (game.getWorld() == null || game.getPlayer() == null) {
                    Thread.sleep(1000);
                    lastMoveCheckMS = System.currentTimeMillis();
                } else {
                    Player player = game.getPlayer();
                    IWorld world = game.getWorld();
                    long moveDiffMS = System.currentTimeMillis() - lastMoveCheckMS;
                    lastMoveCheckMS = System.currentTimeMillis();
                    final boolean threeDMove = false;
                    Vector3 lastPosition = player.getPosition().cpy();
                    if (game.getCurrentScreen().equals(game.getHud())) {
                        float deltaX = 0;
                        float deltaY = 0;
                        float deltaZ = 0;
                        if (Gdx.input.isKeyPressed(Input.Keys.W)) {// Forward TODO Config - Make keys configurable
                            float yaw = player.getRotation().getYaw();
                            float pitch = player.getRotation().getPitch();
                            if (threeDMove) {
                                deltaX += MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw);
                                deltaY += MathUtils.sinDeg(pitch);
                                deltaZ += -MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw);
                            } else {
                                deltaX += MathUtils.sinDeg(yaw);
                                deltaZ += -MathUtils.cosDeg(yaw);
                            }
                        }
                        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                            float yaw = player.getRotation().getYaw();
                            float pitch = player.getRotation().getPitch();
                            if (threeDMove) {
                                deltaX += -MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw);
                                deltaY += -MathUtils.sinDeg(pitch);
                                deltaZ += MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw);
                            } else {
                                deltaX += -MathUtils.sinDeg(yaw);
                                deltaZ += MathUtils.cosDeg(yaw);
                                deltaY += 0;
                            }
                        }
                        if (Gdx.input.isKeyPressed(Input.Keys.A)) {//Strafe left
                            float yaw = player.getRotation().getYaw();

                            deltaX += MathUtils.sinDeg(yaw - 90);
                            deltaZ += -MathUtils.cosDeg(yaw - 90);
                        }
                        if (Gdx.input.isKeyPressed(Input.Keys.D)) {//Strafe right
                            float yaw = player.getRotation().getYaw();

                            deltaX += MathUtils.sinDeg(yaw + 90);
                            deltaZ += -MathUtils.cosDeg(yaw + 90);
                        }

                        float movementMultiplier = moveDiffMS*WALK_SPEED;
                        Vector3 deltaVec = new Vector3(deltaX, deltaY, deltaZ);
                        deltaVec.nor();
                        deltaVec.set(deltaVec.x*movementMultiplier, deltaVec.y*movementMultiplier, deltaVec.z*movementMultiplier);

                        if (!checkDeltaCollision(player, 0, 0, deltaVec.z)) {
                            player.getPosition().offset(0, 0, deltaVec.z);
                        }
                        if (!checkDeltaCollision(player, deltaVec.x, 0, 0)) {
                            player.getPosition().offset(deltaVec.x, 0, 0);
                        }
                        if (!checkDeltaCollision(player, 0, deltaVec.y, 0)) {
                            player.getPosition().offset(0, deltaVec.y, 0);
                        }
                    }

                    int playerX = MathUtils.floor(player.getPosition().getX());
                    int playerZ = MathUtils.floor(player.getPosition().getZ());
                    IChunk playerChunk = world.getChunk(playerX, playerZ);
                    if (playerChunk != null) {
                        int cx = playerX & (game.getWorld().getChunkSize() - 1);
                        int cz = playerZ & (game.getWorld().getChunkSize() - 1);
                        int feetBlockY = MathUtils.floor(player.getPosition().getY() - 0.02f);
                        try {
                            Block blockAtPlayer = playerChunk.getBlock(cx, feetBlockY, cz);
                        if (blockAtPlayer != null && blockAtPlayer.isSolid() &&
                                blockAtPlayer.calculateBoundingBox(playerChunk, cx, feetBlockY, cz)
                                        .contains(player.getPosition().cpy().add(0, -0.02f, 0))) {
                                player.setOnGround(true);
                            } else {
                                player.setOnGround(false);
                            }
                        } catch (CoordinatesOutOfBoundsException ex) {
                            System.out.println(feetBlockY);
                            player.setOnGround(true);
                            ex.printStackTrace();
                        }
                    } else {
                        player.setOnGround(true);
                    }

                    int blockInFeet = player.getBlockInFeet(world);
                    boolean inWater = blockInFeet == BuiltInBlockIds.WATER_ID || blockInFeet == BuiltInBlockIds.WATER_FLOW_ID
                            || blockInFeet == BuiltInBlockIds.LAVA_STILL_ID || blockInFeet == BuiltInBlockIds.LAVA_FLOW_ID;
                    if (inWater) {
                        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                            player.setYVelocity(-3f); // TODO use the same value as mc
                        } else {
                            player.setYVelocity(3f); // TODO use the same value as mc
                        }
                    } else {
                        player.setYVelocity(world.applyGravity(player.getYVelocity(), moveDiffMS));
                    }

                    player.updateMovement(this, moveDiffMS/1000f);

                    player.setMoved(player.getPosition().x != lastPosition.x || player.getPosition().y != lastPosition.y || player.getPosition().z != lastPosition.z);

                    if (player.hasMoved()) {
                        game.getGameRenderer().calculateFrustum();
                        Gdx.graphics.requestRendering();
                    }

                    Thread.sleep(5l);
                }
            }
        } catch (Exception e) {
            game.handleCriticalException(e);
        }
    }

    public boolean checkDeltaCollision(LivingEntity e, float deltaX, float deltaY, float deltaZ) {
        BoundingBox curBB = e.calculateBoundingBox();
        BoundingBox newBB = new BoundingBox(curBB.min.cpy().add(deltaX, deltaY, deltaZ), curBB.max.cpy().add(deltaX, deltaY, deltaZ));

        boolean collideSuccess = false;

        int x = MathUtils.floor(e.getPosition().x);
        int y = MathUtils.floor(e.getPosition().y);
        int z = MathUtils.floor(e.getPosition().z);

        IChunk chunk = game.getWorld().getChunk(x, z);
        if (chunk == null)
            return true;

        int cx = x & (game.getWorld().getChunkSize() - 1);
        int cz = z & (game.getWorld().getChunkSize() - 1);
        try {
            Block block = chunk.getBlock(cx, y, cz);

            for (Vector3 corner : getCorners(newBB)) {
                collideSuccess = collideSuccess || checkCollision(corner);
            }

            return collideSuccess ||
                    (block != null && block.isSolid()
                            && block.calculateBoundingBox(chunk, cx, y, cz).intersects(newBB));
        } catch(CoordinatesOutOfBoundsException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public boolean checkCollision(Vector3 pos) {
        if (pos.y >= RadixClient.getInstance().getWorld().getHeight() || pos.y < 0)
            return false;

        int x = MathUtils.floor(pos.x);
        int y = MathUtils.floor(pos.y);
        int z = MathUtils.floor(pos.z);

        IChunk chunk = game.getWorld().getChunk(x, z);
        if (chunk == null)
            return true;

        int cx = x & (game.getWorld().getChunkSize() - 1);
        int cz = z & (game.getWorld().getChunkSize() - 1);
        try {
            Block block = chunk.getBlock(cx, y, cz);

            return block != null && block.isSolid() && block.calculateBoundingBox(chunk, cx, y, cz).contains(pos);
        } catch (CoordinatesOutOfBoundsException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public void jump() {
        int blockInFeet = game.getPlayer().getBlockInFeet(game.getWorld());
        boolean inWater = blockInFeet == BuiltInBlockIds.WATER_ID || blockInFeet == BuiltInBlockIds.WATER_FLOW_ID
                || blockInFeet == BuiltInBlockIds.LAVA_STILL_ID || blockInFeet == BuiltInBlockIds.LAVA_FLOW_ID;
        if (game.getPlayer().isOnGround()) {
            if(!inWater)
                game.getPlayer().setYVelocity(8.5f);
            game.getPlayer().setOnGround(false);
        }
    }

    private Vector3[] getCorners(BoundingBox bb) {
        return new Vector3[]{bb.getCorner000(new Vector3()), bb.getCorner001(new Vector3()), bb.getCorner010(new Vector3()), bb.getCorner011(new Vector3()), bb.getCorner100(new Vector3()), bb.getCorner101(new Vector3()), bb.getCorner110(new Vector3()), bb.getCorner111(new Vector3())};
    }

    private final RadixClient game;
}

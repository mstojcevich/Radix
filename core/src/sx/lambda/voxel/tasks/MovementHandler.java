package sx.lambda.voxel.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.LivingEntity;
import sx.lambda.voxel.entity.player.Player;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.IChunk;

@CompileStatic
public class MovementHandler implements RepeatedTask {
    public MovementHandler(VoxelGameClient game) {
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
                    DefaultGroovyStaticMethods.sleep(null, 1000);
                    lastMoveCheckMS = System.currentTimeMillis();
                } else {
                    Player player = game.getPlayer();
                    IWorld world = game.getWorld();
                    long moveDiffMS = lastMoveCheckMS - System.currentTimeMillis();
                    float movementMultiplier = moveDiffMS * 0.0043f;
                    final boolean threeDMove = false;
                    EntityPosition lastPosition = player.getPosition().clone();
                    if (game.getCurrentScreen().equals(game.getHud())) {
                        float deltaX = 0;
                        float deltaY = 0;
                        float deltaZ = 0;
                        if (Gdx.input.isKeyPressed(Input.Keys.W)) {// Forward TODO Config - Make keys configurable
                            float yaw = player.getRotation().getYaw();
                            float pitch = player.getRotation().getPitch();
                            if (threeDMove) {
                                deltaX += -MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw) * movementMultiplier;
                                deltaY += -MathUtils.sinDeg(pitch) * movementMultiplier;
                                deltaZ += MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw) * movementMultiplier;
                            } else {
                                deltaX += -MathUtils.sinDeg(yaw) * movementMultiplier;
                                deltaZ += MathUtils.cosDeg(yaw) * movementMultiplier;
                            }
                        }

                        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                            float yaw = player.getRotation().getYaw();
                            float pitch = player.getRotation().getPitch();
                            if (threeDMove) {
                                deltaX += MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw) * movementMultiplier;
                                deltaY += MathUtils.sinDeg(pitch) * movementMultiplier;
                                deltaZ += -MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw) * movementMultiplier;
                            } else {
                                deltaX += MathUtils.sinDeg(yaw) * movementMultiplier;
                                deltaZ += -MathUtils.cosDeg(yaw) * movementMultiplier;
                                deltaY += 0;
                            }
                        }

                        if (Gdx.input.isKeyPressed(Input.Keys.A)) {//Strafe left
                            float yaw = player.getRotation().getYaw();

                            deltaX += -MathUtils.sinDeg((float) yaw - 90) * movementMultiplier;
                            deltaZ += MathUtils.cosDeg((float) yaw - 90) * movementMultiplier;
                        }

                        if (Gdx.input.isKeyPressed(Input.Keys.D)) {//Strafe right
                            float yaw = player.getRotation().getYaw();

                            deltaX += -MathUtils.sinDeg((float) yaw + 90) * movementMultiplier;
                            deltaZ += MathUtils.cosDeg((float) yaw + 90) * movementMultiplier;
                        }


                        if (!checkDeltaCollision(player, 0, 0, deltaZ)) {
                            player.getPosition().offset(0, 0, deltaZ);
                        }

                        if (!checkDeltaCollision(player, deltaX, 0, 0)) {
                            player.getPosition().offset(deltaX, 0, 0);
                        }

                        if (!checkDeltaCollision(player, 0, deltaY, 0)) {
                            player.getPosition().offset(0, deltaY, 0);
                        }
                    }

                    int playerX = MathUtils.floor(player.getPosition().getX());
                    int playerZ = MathUtils.floor(player.getPosition().getZ());
                    IChunk playerChunk = world.getChunk(playerX, playerZ);
                    if (playerChunk != null) {
                        int cx = playerX & (game.getWorld().getChunkSize() - 1);
                        int cz = playerZ & (game.getWorld().getChunkSize() - 1);
                        int feetBlockY = MathUtils.floor(player.getPosition().getY() - 0.05f);
                        Block blockAtPlayer = playerChunk.getBlock(cx, feetBlockY, cz);
                        if (blockAtPlayer != null && blockAtPlayer.isSolid()) {
                            player.setOnGround(true);
                        } else {
                            player.setOnGround(false);
                        }
                    } else {
                        player.setOnGround(true);
                    }

                    int blockInFeet = player.getBlockInFeet(world);
                    if (blockInFeet == BuiltInBlockIds.WATER_ID || blockInFeet == BuiltInBlockIds.WATER_FLOW_ID) {
                        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                            player.setYVelocity(-0.05f);
                        }
                    } else {
                        player.setYVelocity(world.applyGravity(player.getYVelocity(), moveDiffMS));
                    }

                    player.updateMovement(this);

                    if (!(player.getPosition().equals(lastPosition))) {
                        player.setMoved(true);
                    }

                    if (player.hasMoved()) {
                        game.getGameRenderer().calculateFrustum();
                    }

                    lastMoveCheckMS = System.currentTimeMillis();
                    DefaultGroovyStaticMethods.sleep(null, 10);
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

        for (Vector3 corner : getCorners(newBB)) {
            collideSuccess = DefaultGroovyMethods.or(collideSuccess, checkCollision(corner));
        }

        return collideSuccess;
    }

    public boolean checkCollision(Vector3 pos) {
        if (pos.y >= VoxelGameClient.getInstance().getWorld().getHeight() || pos.y < 0)
            return false;

        int x = MathUtils.floor(pos.x);
        int y = MathUtils.floor(pos.y);
        int z = MathUtils.floor(pos.z);

        IChunk chunk = game.getWorld().getChunk(x, z);
        if (chunk == null)
            return true;

        int cx = x & (game.getWorld().getChunkSize() - 1);
        int cz = z & (game.getWorld().getChunkSize() - 1);
        Block block = chunk.getBlock(cx, y, cz);

        if (block != null && block.isSolid()) {
            return block.calculateBoundingBox(chunk, cx, y, cz).contains(pos);
        } else {
            return false;
        }
    }

    public void jump() {
        if (game.getPlayer().isOnGround()) {
            game.getPlayer().setYVelocity(0.115f);
            game.getPlayer().setOnGround(false);
        }
    }

    private Vector3[] getCorners(BoundingBox bb) {
        return new Vector3[]{bb.getCorner000(new Vector3()), bb.getCorner001(new Vector3()), bb.getCorner010(new Vector3()), bb.getCorner011(new Vector3()), bb.getCorner100(new Vector3()), bb.getCorner101(new Vector3()), bb.getCorner110(new Vector3()), bb.getCorner111(new Vector3())};
    }

    private final VoxelGameClient game;
}

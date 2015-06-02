package sx.lambda.voxel.tasks

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.MathUtils
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.BuiltInBlockIds
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.entity.LivingEntity
import sx.lambda.voxel.entity.player.Player
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
class MovementHandler implements RepeatedTask {

    private final VoxelGameClient game

    public MovementHandler(VoxelGameClient game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Movement Handler"
    }

    @Override
    void run() {
        try {
            long lastMoveCheckMS = System.currentTimeMillis()
            while (!game.isDone()) {
                if (game.world == null || game.player == null) {
                    sleep(1000)
                    lastMoveCheckMS = System.currentTimeMillis()
                } else {
                    Player player = game.getPlayer()
                    IWorld world = game.getWorld()
                    long moveDiffMS = lastMoveCheckMS - System.currentTimeMillis()
                    float movementMultiplier = moveDiffMS * 0.0045
                    final boolean threeDMove = false;
                    EntityPosition lastPosition = player.getPosition().clone()
                    if(game.currentScreen == game.hud) {
                        if (Gdx.input.isKeyPressed(Keys.W)) { // Forward TODO Config - Make keys configurable
                            float yaw = player.getRotation().getYaw()
                            float pitch = player.getRotation().getPitch()
                            float deltaX
                            float deltaY
                            float deltaZ
                            if (threeDMove) {
                                deltaX = (float) (-MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw) * movementMultiplier)
                                deltaY = (float) (-MathUtils.sinDeg(pitch) * movementMultiplier)
                                deltaZ = (float) (MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw) * movementMultiplier)
                            } else {
                                deltaX = (float) (-MathUtils.sinDeg(yaw) * movementMultiplier)
                                deltaZ = (float) (MathUtils.cosDeg(yaw) * movementMultiplier)
                                deltaY = 0
                            }

                            if (!checkDeltaCollision(player, 0, 0, deltaZ)) {
                                player.getPosition().offset(0, 0, deltaZ)
                            }
                            if (!checkDeltaCollision(player, deltaX, 0, 0)) {
                                player.getPosition().offset(deltaX, 0, 0)
                            }
                            if (!checkDeltaCollision(player, 0, deltaY, 0)) {
                                player.getPosition().offset(0, deltaY, 0)
                            }
                        }
                        if (Gdx.input.isKeyPressed(Keys.S)) {
                            float yaw = player.getRotation().getYaw()
                            float pitch = player.getRotation().getPitch()
                            float deltaX
                            float deltaY
                            float deltaZ
                            if (threeDMove) {
                                deltaX = (float) (MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw) * movementMultiplier)
                                deltaY = (float) (MathUtils.sinDeg(pitch) * movementMultiplier)
                                deltaZ = (float) (-MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw) * movementMultiplier)
                            } else {
                                deltaX = (float) (MathUtils.sinDeg(yaw) * movementMultiplier)
                                deltaZ = (float) (-MathUtils.cosDeg(yaw) * movementMultiplier)
                                deltaY = 0
                            }

                            if (!checkDeltaCollision(player, deltaX, deltaY, deltaZ)) {
                                player.getPosition().offset(deltaX, deltaY, deltaZ)
                            }
                        }
                        if (Gdx.input.isKeyPressed(Keys.A)) { //Strafe left
                            float deltaX
                            float deltaZ
                            float yaw = player.getRotation().getYaw()

                            deltaX = (float) (-MathUtils.sinDeg((float) yaw - 90) * movementMultiplier)
                            deltaZ = (float) (MathUtils.cosDeg((float) yaw - 90) * movementMultiplier)

                            if (!checkDeltaCollision(player, deltaX, 0, deltaZ)) {
                                player.getPosition().offset(deltaX, 0, deltaZ)
                            }
                        }
                        if (Gdx.input.isKeyPressed(Keys.D)) { //Strafe right
                            float deltaX
                            float deltaZ
                            float yaw = player.getRotation().getYaw()

                            deltaX = (float) (-MathUtils.sinDeg((float) yaw + 90) * movementMultiplier)
                            deltaZ = (float) (MathUtils.cosDeg((float) yaw + 90) * movementMultiplier)

                            if (!checkDeltaCollision(player, deltaX, 0, deltaZ)) {
                                player.getPosition().offset(deltaX, 0, deltaZ)
                            }
                        }
                    }

                    if (world != null && player != null) {
                        int playerX = MathUtils.floor(player.position.x);
                        int playerZ = MathUtils.floor(player.position.z);
                        IChunk playerChunk = world.getChunkAtPosition(playerX, playerZ);
                        player.setOnGround(false)
                        if (playerChunk != null) {
                            Block blockAtPlayer = VoxelGameAPI.instance.getBlockByID(
                                    playerChunk.getBlockId(playerX & (game.world.chunkSize-1),
                                            MathUtils.floor((float)player.position.y-0.2f),
                                            playerZ & (game.world.chunkSize-1)))
                            if (blockAtPlayer != null) {
                                if (blockAtPlayer.isSolid()) {
                                    player.setOnGround(true)
                                }
                            }
                        }

                        int blockInFeet = player.getBlockInFeet(world);
                        if (blockInFeet == BuiltInBlockIds.WATER_ID || blockInFeet == BuiltInBlockIds.WATER_FLOW_ID) {
                            if (!Gdx.input.isKeyPressed(Keys.SPACE)) {
                                player.setYVelocity(-0.05f);
                            }
                        } else {
                            player.setYVelocity(world.applyGravity(player.getYVelocity(), moveDiffMS));
                        }
                        player.updateMovement(this);
                    }

                    if (!(player.position.equals(lastPosition))) {
                        player.setMoved(true);
                    }

                    if (player.hasMoved()) {
                        game.gameRenderer.calculateFrustum()
                    }

                    lastMoveCheckMS = System.currentTimeMillis()
                    sleep(10)
                }
            }
        } catch (Exception e) {
            game.handleCriticalException(e)
        }
    }

    public boolean checkDeltaCollision(LivingEntity e, float deltaX, float deltaY, float deltaZ) {
        float newX = (float)e.getPosition().getX() + deltaX
        float newY = (float)e.getPosition().getY() + deltaY
        float newZ = (float)e.getPosition().getZ() + deltaZ

        boolean collideSuccess = false;

        for(float y = newY - 0.1f; y <= newY + 1.8f; y += 0.2f) {
            for(float x = newX - 0.4; x < newX + 0.4; x += 0.4) {
                for(float z = newZ - 0.4; z < newZ + 0.8; z += 0.4) {
                    collideSuccess |= checkCollision(MathUtils.floor(x), MathUtils.floor(y), MathUtils.floor(z));
                }
            }
        }

        return collideSuccess
    }

    public boolean checkCollision(int x, int y, int z) {
        IChunk newChunk = game.getWorld().getChunkAtPosition(x, z);
        if (newChunk == null) return true

        Block block = newChunk.getBlock(x & (game.world.chunkSize-1), y, z & (game.world.chunkSize-1));

        boolean passed = true
        if (block != null && block.isSolid()) {
            passed = false
        }
        return !passed
    }

    public void jump() {
        if (game.getPlayer().onGround) {
            game.getPlayer().setYVelocity(0.115f)
            game.getPlayer().setOnGround(false)
        }
    }

}
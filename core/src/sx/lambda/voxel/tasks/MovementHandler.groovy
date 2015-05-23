package sx.lambda.voxel.tasks

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
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
                    if (Gdx.input.isKeyPressed(Keys.W)) { // Forward TODO Config - Make keys configurable
                        float yaw = player.getRotation().getYaw()
                        float pitch = player.getRotation().getPitch()
                        float deltaX
                        float deltaY
                        float deltaZ
                        if (threeDMove) {
                            deltaX = (float) (-Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                            deltaY = (float) (-Math.sin(Math.toRadians(pitch)) * movementMultiplier)
                            deltaZ = (float) (Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                        } else {
                            deltaX = (float) (-Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                            deltaZ = (float) (Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                            deltaY = 0
                        }

                        if (!checkCollision(player, deltaX, deltaY, deltaZ)) {
                            player.getPosition().offset(deltaX, deltaY, deltaZ)
                        }
                    }
                    if (Gdx.input.isKeyPressed(Keys.S)) {
                        float yaw = player.getRotation().getYaw()
                        float pitch = player.getRotation().getPitch()
                        float deltaX
                        float deltaY
                        float deltaZ
                        if (threeDMove) {
                            deltaX = (float) (Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                            deltaY = (float) (Math.sin(Math.toRadians(pitch)) * movementMultiplier)
                            deltaZ = (float) (-Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                        } else {
                            deltaX = (float) (Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                            deltaZ = (float) (-Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                            deltaY = 0
                        }

                        if (!checkCollision(player, deltaX, deltaY, deltaZ)) {
                            player.getPosition().offset(deltaX, deltaY, deltaZ)
                        }
                    }
                    if (Gdx.input.isKeyPressed(Keys.A)) { //Strafe left
                        float deltaX
                        float deltaZ
                        float yaw = player.getRotation().getYaw()

                        deltaX = (float) (-Math.sin(Math.toRadians(yaw - 90)) * movementMultiplier)
                        deltaZ = (float) (Math.cos(Math.toRadians(yaw - 90)) * movementMultiplier)

                        if (!checkCollision(player, deltaX, 0, deltaZ)) {
                            player.getPosition().offset(deltaX, 0, deltaZ)
                        }
                    }
                    if (Gdx.input.isKeyPressed(Keys.D)) { //Strafe right
                        float deltaX
                        float deltaZ
                        float yaw = player.getRotation().getYaw()

                        deltaX = (float) (-Math.sin(Math.toRadians(yaw + 90)) * movementMultiplier)
                        deltaZ = (float) (Math.cos(Math.toRadians(yaw + 90)) * movementMultiplier)

                        if (!checkCollision(player, deltaX, 0, deltaZ)) {
                            player.getPosition().offset(deltaX, 0, deltaZ)
                        }
                    }

                    if (world != null && player != null) {
                        int playerX = (int)(player.position.x);
                        int playerZ = (int)(player.position.z);
                        IChunk playerChunk = world.getChunkAtPosition(playerX, playerZ);
                        player.setOnGround(false)
                        if (playerChunk != null) {
                            Block blockAtPlayer = VoxelGameAPI.instance.getBlockByID(
                                    playerChunk.getBlockIdAtPosition(playerX, (int)player.position.y, playerZ))
                            if (blockAtPlayer != null) {
                                if (blockAtPlayer.isSolid()) {
                                    player.setOnGround(true)
                                }
                            }
                        }

                        if (player.getBlockInFeet(world) == BuiltInBlockIds.WATER_ID) {
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

    public boolean checkCollision(LivingEntity e, float deltaX, float deltaY, float deltaZ) {
        int newX = (int) (e.getPosition().getX() + deltaX)
        int newY = (int) (e.getPosition().getY() - 0.1 + deltaY)
        int newY2 = (int) (e.getPosition().getY() + e.getHeight() - 0.1 + deltaY)
        int newZ = (int) (e.getPosition().getZ() + deltaZ)

        IChunk newChunk = game.getWorld().getChunkAtPosition(newX, newZ);

        if (newChunk == null) return true
        Block block1 = VoxelGameAPI.instance.getBlockByID(newChunk.getBlockIdAtPosition(newX, newY, newZ));
        Block block2 = VoxelGameAPI.instance.getBlockByID(newChunk.getBlockIdAtPosition(newX, newY2, newZ));

        boolean passed = true
        if (block1 != null) {
            if (block1.isSolid()) {
                passed = false
            }
        }
        if (block2 != null) {
            if (block2.isSolid()) {
                passed = false
            }
        }

        return !passed
    }

}
package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.entity.player.Player
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.world.IWorld
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

import static org.lwjgl.input.Keyboard.*

@CompileStatic
class MovementHandler implements RepeatedTask {

    private final VoxelGame game

    public MovementHandler(VoxelGame game) {
        this.game = game
    }

    @Override
    String getIdentifier() {
        return "Movement Handler"
    }

    @Override
    void run() {
        long lastMoveCheckMS = System.currentTimeMillis()
        while (!game.isDone()) {
            Player player = game.getPlayer()
            IWorld world = game.getWorld()
            long moveDiffMS = lastMoveCheckMS - System.currentTimeMillis()
            float movementMultiplier = moveDiffMS * 0.0045
            final boolean threeDMove = false;
            EntityPosition lastPosition = player.getPosition().clone()
            if (isKeyDown(KEY_W)) { // Forward TODO Config - Make keys configurable
                float yaw = player.getRotation().getYaw()
                float pitch = player.getRotation().getPitch()
                float deltaX
                float deltaY
                float deltaZ
                if (threeDMove) {
                    deltaX = (float)(-Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                    deltaY = (float)(-Math.sin(Math.toRadians(pitch)) * movementMultiplier)
                    deltaZ = (float)(Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                } else {
                    deltaX = (float)(-Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                    deltaZ = (float)(Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                    deltaY = 0
                }

                if (!checkCollision(deltaX, deltaY, deltaZ)) {
                    player.getPosition().offset(deltaX, deltaY, deltaZ)
                }
            }
            if (isKeyDown(KEY_S)) {
                float yaw = player.getRotation().getYaw()
                float pitch = player.getRotation().getPitch()
                float deltaX
                float deltaY
                float deltaZ
                if (threeDMove) {
                    deltaX = (float)(Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                    deltaY = (float)(Math.sin(Math.toRadians(pitch)) * movementMultiplier)
                    deltaZ = (float)(-Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                } else {
                    deltaX = (float)(Math.sin(Math.toRadians(yaw)) * movementMultiplier)
                    deltaZ = (float)(-Math.cos(Math.toRadians(yaw)) * movementMultiplier)
                    deltaY = 0
                }

                if (!checkCollision(deltaX, deltaY, deltaZ)) {
                    player.getPosition().offset(deltaX, deltaY, deltaZ)
                }
            }
            if (isKeyDown(KEY_A)) { //Strafe left
                float deltaX
                float deltaZ
                float yaw = player.getRotation().getYaw()

                deltaX = (float)(-Math.sin(Math.toRadians(yaw - 90)) * movementMultiplier)
                deltaZ = (float)(Math.cos(Math.toRadians(yaw - 90)) * movementMultiplier)

                if (!checkCollision(deltaX, 0, deltaZ)) {
                    player.getPosition().offset(deltaX, 0, deltaZ)
                }
            }
            if (isKeyDown(KEY_D)) { //Strafe right
                float deltaX
                float deltaZ
                float yaw = player.getRotation().getYaw()

                deltaX = (float)(-Math.sin(Math.toRadians(yaw + 90)) * movementMultiplier)
                deltaZ = (float)(Math.cos(Math.toRadians(yaw + 90)) * movementMultiplier)

                if (!checkCollision(deltaX, 0, deltaZ)) {
                    player.getPosition().offset(deltaX, 0, deltaZ)
                }
            }

            if (world != null && player != null) {
                Vec3i playerPosition = new Vec3i(
                        (int) Math.floor(player.getPosition().getX()),
                        (int) Math.floor(player.getPosition().getY() - 0.2f),
                        (int) Math.floor(player.getPosition().getZ())
                );
                IChunk playerChunk = world.getChunkAtPosition(playerPosition);
                player.setOnGround(false)
                if (playerChunk != null) {
                    if (playerChunk.getBlockAtPosition(playerPosition) != null) {
                        player.setOnGround(true)
                    }
                }

                player.setYVelocity(world.applyGravity(player.getYVelocity(), moveDiffMS));
                player.updateMovement();
                game.calculateFrustum()
            }

            if(!(player.position.equals(lastPosition))) {
                player.setMoved(true);
            }

            lastMoveCheckMS = System.currentTimeMillis()
            sleep(10)
        }
    }

    private boolean checkCollision(float deltaX, float deltaY, float deltaZ) {
        Vec3i newPosition = new Vec3i(
                (int) Math.floor(game.getPlayer().getPosition().getX() + deltaX),
                (int) Math.floor(game.getPlayer().getPosition().getY() - 0.1 + deltaY),
                (int) Math.floor(game.getPlayer().getPosition().getZ() + deltaZ)
        );
        Vec3i newPosition2 = new Vec3i(
                (int) Math.floor(game.getPlayer().getPosition().getX() + deltaX),
                (int) Math.floor(game.getPlayer().getPosition().getY() + game.getPlayer().getHeight() - 0.1 + deltaY),
                (int) Math.floor(game.getPlayer().getPosition().getZ() + deltaZ)
        );
        IChunk newChunk = game.getWorld().getChunkAtPosition(newPosition);
        IChunk newChunk2 = game.getWorld().getChunkAtPosition(newPosition2);
        if (newChunk2 == null) return true
        if (newChunk == null) return true
        return newChunk.getBlockAtPosition(newPosition) != null || newChunk.getBlockAtPosition(newPosition2) != null
    }

}
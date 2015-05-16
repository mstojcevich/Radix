package sx.lambda.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
class WorldLoader implements RepeatedTask {

    private final VoxelGameClient game

    private IChunk currentChunk

    public WorldLoader(VoxelGameClient game) {
        this.game = game
    }

    @Override
    void run() {
        try {
            while (!game.isDone()) {
                if (game.getPlayer() != null && game.getWorld() != null) {
                    int viewDistance = game.getSettingsManager().getVisualSettings().getViewDistance()
                    EntityPosition playerPos = game.getPlayer().getPosition()
                    if (currentChunk == null) {
                        game.getWorld().loadChunks(playerPos, viewDistance)
                        currentChunk = game.getWorld().getChunkAtPosition(new Vec3i((int) playerPos.getX(), (int) playerPos.getY(), (int) playerPos.getZ()));
                    } else {
                        if (Math.abs(currentChunk.getStartPosition().x - game.getPlayer().getPosition().getX()) > game.getWorld().getChunkSize()
                                || Math.abs(currentChunk.getStartPosition().z - game.getPlayer().getPosition().getZ()) > game.getWorld().getChunkSize()) {
                            currentChunk = game.getWorld().getChunkAtPosition(new Vec3i((int) playerPos.getX(), (int) playerPos.getY(), (int) playerPos.getZ()));
                            game.getWorld().loadChunks(playerPos, viewDistance)
                        }
                    }
                }

                sleep(100);
            }
        } catch(Exception e) {
            VoxelGameClient.instance.handleCriticalException(e)
        }
    }

    @Override
    String getIdentifier() {
        return "World Loader"
    }
}

package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk
import sx.lambda.mstojcevich.voxel.entity.EntityPosition
import sx.lambda.mstojcevich.voxel.util.Vec3i

@CompileStatic
class WorldLoader implements RepeatedTask {

    private final VoxelGame game

    private IChunk currentChunk

    public WorldLoader(VoxelGame game) {
        this.game = game
    }

    @Override
    void run() {
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
    }

    @Override
    String getIdentifier() {
        return "World Loader"
    }
}

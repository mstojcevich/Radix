package sx.lambda.voxel.tasks

import com.badlogic.gdx.math.MathUtils
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
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
                        currentChunk = game.getWorld().getChunk(MathUtils.floor(playerPos.getX()), MathUtils.floor(playerPos.getZ()));
                    } else {
                        if (Math.abs(currentChunk.getStartPosition().x - game.getPlayer().getPosition().getX()) > game.getWorld().getChunkSize()
                                || Math.abs(currentChunk.getStartPosition().z - game.getPlayer().getPosition().getZ()) > game.getWorld().getChunkSize()) {
                            currentChunk = game.getWorld().getChunk(MathUtils.floor(playerPos.getX()), MathUtils.floor(playerPos.getZ()));
                            game.getWorld().loadChunks(playerPos, viewDistance)
                        }
                    }
                }

                sleep(100);
            }
        } catch (Exception e) {
            VoxelGameClient.instance.handleCriticalException(e)
        }
    }

    @Override
    String getIdentifier() {
        return "World Loader"
    }
}

package sx.lambda.mstojcevich.voxel.api.events.render;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

/**
 * Event called after a chunk is initially rendered into a display list
 */
public class EventChunkRender implements Event {

    private final IChunk chunk;

    public EventChunkRender(IChunk chunk) {
        this.chunk = chunk;
    }

    /**
     * @return The chunk that was rendered
     */
    public IChunk getChunk() {
        return this.chunk;
    }

}

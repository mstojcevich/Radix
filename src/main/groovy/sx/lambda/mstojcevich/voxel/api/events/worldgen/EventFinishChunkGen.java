package sx.lambda.mstojcevich.voxel.api.events.worldgen;

import pw.oxcafebabe.marcusant.eventbus.Event;
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk;

/**
 * Event fired every time a chunk finishes being generated
 */
public class EventFinishChunkGen implements Event {

    private final IChunk chunk;

    public EventFinishChunkGen(IChunk chunk) {
        this.chunk = chunk;
    }

    public IChunk getChunk() {
        return this.chunk;
    }

}

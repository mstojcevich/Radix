package sx.lambda.mstojcevich.voxel.api

import groovy.transform.CompileStatic
import pw.oxcafebabe.marcusant.eventbus.EventManager
import pw.oxcafebabe.marcusant.eventbus.managers.iridium.IridiumEventManager
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.server.VoxelGameServer

@CompileStatic
class VoxelGameAPI {

    public static final VoxelGameAPI instance = new VoxelGameAPI()

    private final EventManager eventManager

    public VoxelGameAPI() {
        this.eventManager = new IridiumEventManager()
    }

    /**
     * Gets the event manager that is used to fire and listen to API events
     */
    public EventManager getEventManager() { return this.eventManager }

    /**
     * @return Whether the current game is a server
     */
    public boolean isServer() {
        return VoxelGameServer.instance != null
    }

    /**
     * @return Whether the current game is a client
     */
    public boolean isClient() {
        return VoxelGame.instance != null
    }

}

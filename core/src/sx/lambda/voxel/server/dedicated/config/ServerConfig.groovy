package sx.lambda.voxel.server.dedicated.config

import sx.lambda.voxel.VoxelGameClient

class ServerConfig {

    private short port = Short.parseShort(System.getProperty("port", "31173"));
    private String motd = "New $VoxelGameClient.GAME_TITLE Server!"
    /**
     * View distance to load per player, in chunks
     */
    private int viewDistance = 3

    public short getPort() { port }
    public String getMOTD() { motd }
    public int getViewDistance() { viewDistance }

}

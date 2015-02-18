package sx.lambda.mstojcevich.voxel.server.dedicated.config

import sx.lambda.mstojcevich.voxel.VoxelGame

class ServerConfig {

    private short port = Short.parseShort(System.getProperty("port", "31173"));
    private String motd = "New $VoxelGame.GAME_TITLE Server!"
    /**
     * View distance to load per player, in chunks
     */
    private int viewDistance = 3

    public short getPort() { port }
    public String getMOTD() { motd }
    public int getViewDistance() { viewDistance }

}

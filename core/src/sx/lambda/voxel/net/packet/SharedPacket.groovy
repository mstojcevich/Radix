package sx.lambda.voxel.net.packet

import groovy.transform.CompileStatic

@CompileStatic
/**
 * Packet that can be sent and recieved by both the server and the client
 */
abstract interface SharedPacket extends ServerPacket, ClientPacket {}

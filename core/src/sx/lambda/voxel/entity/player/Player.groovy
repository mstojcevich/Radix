package sx.lambda.voxel.entity.player

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.BuiltInBlockIds
import sx.lambda.voxel.entity.EntityRotation
import sx.lambda.voxel.net.packet.shared.PacketPlayerPosition
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.util.gl.ObjModel
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.chunk.IChunk
import sx.lambda.voxel.entity.EntityPosition
import sx.lambda.voxel.entity.LivingEntity

@CompileStatic
class Player extends LivingEntity implements Serializable {

    private static final float WIDTH = 1, HEIGHT = 1.8f
    private static final float REACH = 4

    private transient boolean moved = false

    private static final ObjModel playerModel = new ObjModel(new FileHandle('entity/player.obj'))

    private int itemInHand = BuiltInBlockIds.STONE_ID

    //needed for serialization
    public Player() {
        this(new EntityPosition(0, 0, 0), new EntityRotation())
    }

    public Player(EntityPosition pos, EntityRotation rot) {
        super(playerModel,
                pos, rot, 1, HEIGHT)
    }

    public float getEyeHeight() {
        HEIGHT*0.75
    }

    public float getReach() { REACH }

    public int getItemInHand() { itemInHand }

    public void setItemInHand(int block) { itemInHand = block }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(moved) {
            if(VoxelGameClient.instance.getServerChanCtx() != null) {
                VoxelGameClient.instance.getServerChanCtx().writeAndFlush(new PacketPlayerPosition(this.getPosition()))
            }
            moved = false
        }
    }

    public void setMoved(boolean moved) {
        this.moved = moved
    }

    @Override
    public ObjModel getDefaultModel() {
        playerModel
    }

    public int getBlockInHead(IWorld world) {
        Vec3i eyePosition = new Vec3i(getPosition().x as int, getPosition().y+HEIGHT as int, getPosition().z as int)
        IChunk chunk = world.getChunkAtPosition(eyePosition)
        if(chunk != null) {
            return chunk.getBlockIdAtPosition(getPosition().x as int, getPosition().y+HEIGHT as int, getPosition().z as int)
        } else {
            return 0
        }
    }

}

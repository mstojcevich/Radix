package sx.lambda.mstojcevich.voxel.entity.player

import groovy.transform.CompileStatic
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.block.Block
import sx.lambda.mstojcevich.voxel.entity.EntityRotation
import sx.lambda.mstojcevich.voxel.entity.LivingEntity
import sx.lambda.mstojcevich.voxel.net.packet.shared.PacketPlayerPosition
import sx.lambda.mstojcevich.voxel.util.gl.ObjModel
import sx.lambda.mstojcevich.voxel.entity.EntityPosition

@CompileStatic
class Player extends LivingEntity implements Serializable {

    private static final float WIDTH = 1, HEIGHT = 1.8f
    private static final float REACH = 4

    private transient boolean moved = false

    private static final ObjModel playerModel = new ObjModel(this.class.getResourceAsStream('/models/entity/player.obj'))

    private Block itemInHand = Block.GRASS

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

    public Block getItemInHand() { itemInHand }

    public void setItemInHand(Block block) { itemInHand = block }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(moved) {
            if(VoxelGame.instance.getServerChanCtx() != null) {
                VoxelGame.instance.getServerChanCtx().writeAndFlush(new PacketPlayerPosition(this.getPosition()))
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

}

package sx.lambda.voxel.entity.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import org.spacehq.mc.protocol.data.game.values.entity.player.GameMode;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import sx.lambda.voxel.RadixClient;
import sx.lambda.voxel.api.BuiltInBlockIds;
import sx.lambda.voxel.api.RadixAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.entity.EntityModel;
import sx.lambda.voxel.entity.EntityPosition;
import sx.lambda.voxel.entity.EntityRotation;
import sx.lambda.voxel.entity.LivingEntity;
import sx.lambda.voxel.item.Item;
import sx.lambda.voxel.item.Tool;
import sx.lambda.voxel.util.Vec3i;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.IChunk;

import java.io.Serializable;

public class Player extends LivingEntity implements Serializable {

    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 1.8f;
    private static final float REACH = 4;
    private static Model playerModel;
    private transient boolean moved = false;
    private int itemInHand = BuiltInBlockIds.STONE_ID;
    private GameMode gameMode = GameMode.CREATIVE;
    private float breakPercent;
    private boolean wasBreaking;

    public Player() {
        this(new EntityPosition(0, 0, 0), new EntityRotation());
    }

    public Player(EntityPosition pos, EntityRotation rot) {
        super(EntityModel.getPlayerModel(), EntityModel.getPlayerTexture(), pos, rot, WIDTH, HEIGHT);
    }

    public float getEyeHeight() {
        return HEIGHT * 0.75f;
    }

    public float getReach() {
        return REACH;
    }

    public int getItemInHand() {
        return itemInHand;
    }

    public void setItemInHand(int block) {
        itemInHand = block;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (moved) {
            if (RadixClient.getInstance().getMinecraftConn() != null) {
                ClientPlayerPositionRotationPacket positionRotationPacket =
                        new ClientPlayerPositionRotationPacket(this.isOnGround(),
                        this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ(),
                        (float) 180 - this.getRotation().getYaw(), -this.getRotation().getPitch());
                RadixClient.getInstance().getMinecraftConn().getClient().getSession().send(positionRotationPacket);
            }
            moved = false;
        }

        Vec3i selBlkPos = RadixClient.getInstance().getSelectedBlock();
        try {
            if(selBlkPos != null) {
                int sbcx = selBlkPos.x & (RadixClient.getInstance().getWorld().getChunkSize() - 1);
                int sbcz = selBlkPos.z & (RadixClient.getInstance().getWorld().getChunkSize() - 1);
                Block selectedBlock = RadixClient.getInstance().getWorld().getChunk(selBlkPos.x, selBlkPos.z)
                        .getBlock(sbcx, selBlkPos.y, sbcz);
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { // left mouse button down
                    if (selectedBlock != null && selectedBlock.isSelectable()) {
                        if (breakPercent <= 0) {
                            RadixClient.getInstance().beginBreak();
                        }
                        Item curItem = RadixAPI.instance.getItem(itemInHand);
                        if (selectedBlock.getHardness() <= 0 || gameMode.equals(GameMode.CREATIVE)) {
                            breakPercent = 1;
                        } else {
                            float incr;
                            if (curItem instanceof Tool) {
                                incr = 50f / selectedBlock.getBreakTimeMS((Tool) curItem);
                            } else {
                                incr = 50f / selectedBlock.getBreakTimeMS(null);
                            }
                            breakPercent = MathUtils.clamp(breakPercent + incr, 0, 1);
                            wasBreaking = true;
                        }
                        if (breakPercent >= 1) {
                            RadixClient.getInstance().breakBlock();
                            resetBlockBreak();
                            wasBreaking = false;
                        }
                    }
                } else {
                    if(wasBreaking) {
                        wasBreaking = false;
                        RadixClient.getInstance().cancelBreak();
                    }
                }
            }
        } catch (BlockStorage.CoordinatesOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public boolean hasMoved() {
        return this.moved;
    }

    @Override
    public Model getDefaultModel() {
        return playerModel;
    }

    public int getBlockInHead(IWorld world) {
        int x = MathUtils.floor(getPosition().getX());
        int z = MathUtils.floor(getPosition().getZ());
        int y = MathUtils.floor(getPosition().getY() + HEIGHT);
        IChunk chunk = world.getChunk(x, z);
        if (chunk != null) {
            if (y >= world.getHeight()) return 0;
            try {
                return chunk.getBlockId(x & (world.getChunkSize() - 1), y, z & (world.getChunkSize() - 1));
            } catch (BlockStorage.CoordinatesOutOfBoundsException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * Get the percentage of the current block break
     */
    public float getBreakPercent() {
        return this.breakPercent;
    }

    /**
     * Reset the block break percent
     */
    public void resetBlockBreak() {
        this.breakPercent = 0;
    }

}

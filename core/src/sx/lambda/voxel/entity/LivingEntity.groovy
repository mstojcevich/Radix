package sx.lambda.voxel.entity

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.block.Block
import sx.lambda.voxel.tasks.MovementHandler
import sx.lambda.voxel.world.IWorld
import sx.lambda.voxel.world.chunk.IChunk

@CompileStatic
public abstract class LivingEntity extends Entity implements Serializable {

    private synchronized transient float yVelocity
    private boolean onGround
    private final float width, height

    //Needed for serialization
    public LivingEntity() {
        this(getDefaultModel(), new EntityPosition(0, 0, 0), new EntityRotation())
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot) {
        this(model, pos, rot, 1, 1);
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot, float width, float height) {
        super(model, pos, rot)
        this.width = width
        this.height = height
    }

    public float getYVelocity() { this.yVelocity }

    public boolean isOnGround() { this.onGround }

    public void updateMovement(MovementHandler handler) {
        if (this.onGround) {
            this.yVelocity = Math.max(this.yVelocity, 0)
        }
        if (!handler.checkDeltaCollision(this, 0, yVelocity, 0)) {
            this.getPosition().offset(0, this.yVelocity, 0)
        } else {
            if(yVelocity < 0) { // falling down and failed because we hit the ground
                // prevent overshoot causing the player to not reach the ground
                int x = MathUtils.floor(position.x)
                int y = MathUtils.floor(position.y)
                int z = MathUtils.floor(position.z)
                int cx = x & (VoxelGameClient.instance.world.chunkSize-1)
                int cz = z & (VoxelGameClient.instance.world.chunkSize-1)

                IChunk chunk = VoxelGameClient.instance.world.getChunk(x, z)
                if (chunk != null) {
                    // go directly to ground
                    for(int downY = y; downY > y+yVelocity; downY--) {
                        Block block = chunk.getBlock(cx, downY, cz);
                        if(block != null && block.isSolid()) {
                            getPosition().set(position.x, (float)block.calculateBoundingBox(chunk, cx, downY, cz).max.y + 0.015f, position.z)
                        }
                    }
                }
            }
            yVelocity = 0
        }
    }

    public void setYVelocity(float velocity) {
        synchronized (this) {
            this.yVelocity = velocity
        }
    }

    public int getWidth() { this.width }

    public float getHeight() { this.height }

    public void setOnGround(boolean onGround) {
        synchronized (this) {
            this.onGround = onGround
        }
    }

    public int getBlockInFeet(IWorld world) {
        int x = MathUtils.floor(position.x)
        int y = MathUtils.floor(position.y)
        int z = MathUtils.floor(position.z)
        IChunk chunk = world.getChunk(x, z)
        if (chunk != null && y < world.getHeight()) {
            int cx = x & (world.getChunkSize()-1)
            int cz = z & (world.getChunkSize()-1)
            int id = chunk.getBlockId(
                    cx,
                    y,
                    cz)
            Block block = VoxelGameAPI.instance.getBlockByID(id);
            if(block == null)
                return 0
            BoundingBox blockBox = block.calculateBoundingBox(chunk, cx, y, cz)
            float halfWidth = (float)width/2f
            Vector3 bottomBackLeft = position.cpy().add(-halfWidth, 0, -halfWidth)
            Vector3 bottomBackRight = bottomBackLeft.cpy().add(width, 0, 0)
            Vector3 bottomFrontRight = bottomBackRight.cpy().add(0, 0, width)
            Vector3 bottomFrontLeft = bottomBackLeft.cpy().add(width, 0, 0)

            boolean inFeet = blockBox.contains(bottomBackLeft) || blockBox.contains(bottomBackRight) ||
                    blockBox.contains(bottomFrontLeft) || blockBox.contains(bottomFrontRight)

            return inFeet ? id : 0
        } else {
            return 0
        }
    }

    public BoundingBox calculateBoundingBox() {
        float halfWidth = (float)width/2f;
        return new BoundingBox(getPosition().cpy().add(-halfWidth, 0, -halfWidth), getPosition().cpy().add(halfWidth, height, halfWidth));
    }

}

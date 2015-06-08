package sx.lambda.voxel.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import sx.lambda.voxel.VoxelGameClient;
import sx.lambda.voxel.api.VoxelGameAPI;
import sx.lambda.voxel.block.Block;
import sx.lambda.voxel.tasks.MovementHandler;
import sx.lambda.voxel.world.IWorld;
import sx.lambda.voxel.world.chunk.BlockStorage;
import sx.lambda.voxel.world.chunk.IChunk;

import java.io.Serializable;

public abstract class LivingEntity extends Entity implements Serializable {

    private final float width;
    private final float height;
    private float yVelocity;
    private boolean onGround;

    public LivingEntity() {
        super();
        this.width = 1;
        this.height = 1;
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot) {
        this(model, pos, rot, 1, 1);
    }

    public LivingEntity(Model model, EntityPosition pos, EntityRotation rot, float width, float height) {
        super(model, pos, rot);
        this.width = width;
        this.height = height;
    }

    public float getYVelocity() {
        return this.yVelocity;
    }

    public void setYVelocity(float velocity) {
        this.yVelocity = velocity;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void updateMovement(MovementHandler handler, float seconds) {
        if (this.onGround) {
            this.yVelocity = Math.max(this.yVelocity, 0);
        }

        float deltaY = yVelocity * seconds;
        if (!handler.checkDeltaCollision(this, 0, deltaY, 0)) {
            this.getPosition().offset(0, deltaY, 0);
        } else {
            if (yVelocity < 0) {// falling down and failed because we hit the ground
                // prevent overshoot causing the player to not reach the ground
                int x = MathUtils.floor(getPosition().getX());
                int y = MathUtils.floor(getPosition().getY());
                int z = MathUtils.floor(getPosition().getZ());
                int cx = x & (VoxelGameClient.getInstance().getWorld().getChunkSize() - 1);
                int cz = z & (VoxelGameClient.getInstance().getWorld().getChunkSize() - 1);

                IChunk chunk = VoxelGameClient.getInstance().getWorld().getChunk(x, z);
                if (chunk != null) {
                    // go directly to ground
                    for (int downY = y; downY > y + deltaY; downY--) {
                        try {
                            Block block = chunk.getBlock(cx, downY, cz);
                            if (block != null && block.isSolid()) {
                                getPosition().set(getPosition().getX(),
                                        block.calculateBoundingBox(chunk, cx, downY, cz).max.y + 0.015f,
                                        getPosition().getZ());
                            }
                        } catch (BlockStorage.CoordinatesOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            yVelocity = 0;
        }

    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public int getBlockInFeet(IWorld world) {
        int x = MathUtils.floor(getPosition().getX());
        int y = MathUtils.floor(getPosition().getY());
        int z = MathUtils.floor(getPosition().getZ());
        IChunk chunk = world.getChunk(x, z);
        if (chunk != null && y < world.getHeight()) {
            int cx = x & (world.getChunkSize() - 1);
            int cz = z & (world.getChunkSize() - 1);
            try {
                int id = chunk.getBlockId(cx, y, cz);
                Block block = VoxelGameAPI.instance.getBlockByID(id);
                if (block == null) return 0;
                BoundingBox blockBox = block.calculateBoundingBox(chunk, cx, y, cz);
                float halfWidth = width / 2f;
                Vector3 bottomBackLeft = getPosition().cpy().add(-halfWidth, 0, -halfWidth);
                Vector3 bottomBackRight = bottomBackLeft.cpy().add(width, 0, 0);
                Vector3 bottomFrontRight = bottomBackRight.cpy().add(0, 0, width);
                Vector3 bottomFrontLeft = bottomBackLeft.cpy().add(width, 0, 0);

                boolean inFeet = blockBox.contains(bottomBackLeft) || blockBox.contains(bottomBackRight) || blockBox.contains(bottomFrontLeft) || blockBox.contains(bottomFrontRight);

                return inFeet ? id : 0;
            } catch (BlockStorage.CoordinatesOutOfBoundsException ex) {
                ex.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    public BoundingBox calculateBoundingBox() {
        float halfWidth = width / 2f;
        return new BoundingBox(getPosition().cpy().add(-halfWidth, 0, -halfWidth), getPosition().cpy().add(halfWidth, height, halfWidth));
    }

}

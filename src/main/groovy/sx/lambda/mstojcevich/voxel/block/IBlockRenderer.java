package sx.lambda.mstojcevich.voxel.block;

public interface IBlockRenderer {

    void render(float x, float y, float z,
                boolean shouldRenderTop, boolean shouldRenderBottom,
                boolean shouldRenderLeft, boolean shouldRenderRight,
                boolean shouldRenderFront, boolean shouldRenderBack);

    public void render2d(float x, float y, float width);

    public void prerender();

}

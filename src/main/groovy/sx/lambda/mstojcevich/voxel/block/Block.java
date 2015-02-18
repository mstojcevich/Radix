package sx.lambda.mstojcevich.voxel.block;

public enum Block {
	
	AIR(0), DIRT(1), GRASS(2), STONE(3);
	
	final int id;
	final transient IBlockRenderer renderer;
	
	Block(int id) {
		this(id, new NormalBlockRenderer(id));
	}
	
	Block(int id, IBlockRenderer renderer) {
		this.id = id;
		this.renderer = renderer;
	}
	
	public IBlockRenderer getRenderer() {
		return this.renderer;
	}
	
	public int getID() {
		return this.id;
	}

}

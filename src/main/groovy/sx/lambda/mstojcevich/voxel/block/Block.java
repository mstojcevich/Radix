package sx.lambda.mstojcevich.voxel.block;

public enum Block {
	
	AIR(0), DIRT(1), GRASS(2), STONE(3), WATER(4, new NormalBlockRenderer(4), true, false), SAND(5);
	
	final int id;
	final transient IBlockRenderer renderer;
	final boolean transparent, solid;
	
	Block(int id) {
		this(id, new NormalBlockRenderer(id));
	}
	
	Block(int id, IBlockRenderer renderer) {
		this(id, renderer, false, true);
	}

	Block(int id, IBlockRenderer renderer, boolean transparent, boolean solid) {
		this.id = id;
		this.renderer = renderer;
		this.transparent = transparent;
		this.solid = solid;
	}
	
	public IBlockRenderer getRenderer() {
		return this.renderer;
	}
	
	public int getID() {
		return this.id;
	}

	public boolean isTransparent() {
		return this.transparent;
	}

	public boolean isSolid() {
		return this.solid;
	}

}

package sx.lambda.voxel.block;

import com.badlogic.gdx.files.FileHandle;

import java.net.URL;

public class Block {
	
	// AIR(0), DIRT(1), GRASS(2), STONE(3), WATER(4, new WaterRenderer(4), true, false), SAND(5);
	
	private int id;
	private final transient IBlockRenderer renderer;
	private final boolean transparent, solid;
	private final String humanName;
	private final FileHandle textureLocation;

	Block(int id, String humanName, IBlockRenderer renderer, FileHandle textureLocation, boolean transparent, boolean solid) {
		this.id = id;
		this.renderer = renderer;
		this.transparent = transparent;
		this.solid = solid;
		this.humanName = humanName;
		this.textureLocation = textureLocation;
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

	public void setID(int id) {
		this.id = id;
	}

	public String getHumanName() { return this.humanName; }

	public FileHandle getTextureLocation() { return textureLocation; }
}

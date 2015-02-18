//package pw.lambda.mstojcevich.voxel.block;
//
//public class BlockPosition {
//
//	private final int x, y, z;
//
//	/**
//	 * @param x - North/south position of block
//	 * @param y - Up/down position of block
//	 * @param z - East/west position of block
//	 */
//	public BlockPosition(int x, int y, int z) {
//		this.x = x;
//		this.y = y;
//		this.z = z;
//	}
//
//	public int getX() {
//		return this.x;
//	}
//
//	public int getY() {
//		return this.y;
//	}
//
//	public int getZ() {
//		return this.z;
//	}
//
//    @Override
//    public boolean equals(Object o) {
//        if(o == this) return true;
//        if(!(o instanceof BlockPosition)) return false;
//
//        BlockPosition other = (BlockPosition)o;
//        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ();
//    }
//
//    @Override
//    public int hashCode() {
//        int hashCode;
//        StringBuilder sb = new StringBuilder();
//        sb.append(this.getX()).append(",").append(this.getY()).append(",").append(this.getZ());
//        hashCode = sb.toString().hashCode();
//        return hashCode;
//    }
//
//    public float distance(BlockPosition pos2) {
//        return (float)Math.sqrt(Math.pow(getX() - pos2.getX(), 2) + (Math.pow(getZ() - pos2.getZ(), 2)));
//    }
//
//}

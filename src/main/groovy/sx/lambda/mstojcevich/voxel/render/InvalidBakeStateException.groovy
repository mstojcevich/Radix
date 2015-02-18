package sx.lambda.mstojcevich.voxel.render

/**
 * Thrown when a vbo is either cooked when it shouldn't be or not cooked when it should me.
 */
class InvalidBakeStateException extends Exception {
    private final boolean baked;

    public InvalidBakeStateException(boolean baked) {
        this.baked = baked;
    }

    public boolean wasBaked() {
        return this.baked;
    }
}

package sx.lambda.voxel.item;

public class Item {

    private final String humanName;
    private int id;

    public Item(int id, String humanName) {
        this.id = id;
        this.humanName = humanName;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getHumanName() {
        return humanName;
    }

}

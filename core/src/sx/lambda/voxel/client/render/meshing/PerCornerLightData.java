package sx.lambda.voxel.client.render.meshing;

public class PerCornerLightData {
    public float l00, l01, l10, l11;

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(o instanceof PerCornerLightData) {
            PerCornerLightData p = (PerCornerLightData)o;
            return p.l10 == l10 && p.l11 == l11 && p.l00 == l00 && p.l01 == l01;
        }
        return false;
    }
}


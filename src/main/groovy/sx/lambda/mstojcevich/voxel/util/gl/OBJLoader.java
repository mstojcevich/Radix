package sx.lambda.mstojcevich.voxel.util.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import sx.lambda.mstojcevich.voxel.VoxelGame;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

/**
 * @author Oskar
 */
class OBJLoader {

    private static FloatBuffer reserveData(int size) {
        return BufferUtils.createFloatBuffer(size);
    }

    private static float[] asFloats(Vector3f v) {
        return new float[]{v.x, v.y, v.z};
    }

    public static int[] createVBO(Model model) {
        int vboVertexHandle = glGenBuffers();
        int vboNormalHandle = glGenBuffers();
        // TODO: Implement materials with VBOs
        FloatBuffer vertices = reserveData(model.getFaces().size() * 9);
        FloatBuffer normals = reserveData(model.getFaces().size() * 9);
        for (Model.Face face : model.getFaces()) {
            vertices.put(asFloats(model.getVertices().get(face.getVertexIndices()[0] - 1)));
            vertices.put(asFloats(model.getVertices().get(face.getVertexIndices()[1] - 1)));
            vertices.put(asFloats(model.getVertices().get(face.getVertexIndices()[2] - 1)));
            if(model.hasNormals()) {
                normals.put(asFloats(model.getNormals().get(face.getNormalIndices()[0] - 1)));
                normals.put(asFloats(model.getNormals().get(face.getNormalIndices()[1] - 1)));
                normals.put(asFloats(model.getNormals().get(face.getNormalIndices()[2] - 1)));
            }
        }
        vertices.flip();
        normals.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glNormalPointer(GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return new int[]{vboVertexHandle, vboNormalHandle};
    }

    private static Vector3f parseVertex(String line) {
        String[] xyz = line.split(" ");
        float x = Float.valueOf(xyz[1]);
        float y = Float.valueOf(xyz[2]);
        float z = Float.valueOf(xyz[3]);
        return new Vector3f(x, y, z);
    }

    private static Vector3f parseNormal(String line) {
        String[] xyz = line.split(" ");
        float x = Float.valueOf(xyz[1]);
        float y = Float.valueOf(xyz[2]);
        float z = Float.valueOf(xyz[3]);
        return new Vector3f(x, y, z);
    }

    private static Model.Face parseFace(boolean hasNormals, String line) {
        String[] faceIndices = line.split(" ");
        int[] vertexIndicesArray = {Integer.parseInt(faceIndices[1].split("/")[0]),
            Integer.parseInt(faceIndices[2].split("/")[0]), Integer.parseInt(faceIndices[3].split("/")[0])};
        if (hasNormals) {
            int[] normalIndicesArray = new int[3];
            normalIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[2]);
            normalIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[2]);
            normalIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[2]);
            return new Model.Face(vertexIndicesArray, normalIndicesArray);
        } else {
            return new Model.Face((vertexIndicesArray));
        }
    }

    public static Model loadModel(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Model m = new Model();
        String line;
        while ((line = reader.readLine()) != null) {
            String prefix = line.split(" ")[0];
            if (prefix.equals("#") || line.trim().length() <= 0) {
                continue;
            } else if (prefix.equals("v")) {
                m.getVertices().add(parseVertex(line));
            } else if (prefix.equals("vn")) {
                m.getNormals().add(parseNormal(line));
            } else if (prefix.equals("f")) {
                m.getFaces().add(parseFace(m.hasNormals(), line));
            } else {
                throw new RuntimeException("OBJ file contains line which cannot be parsed correctly: " + line);
            }
        }
        reader.close();
        return m;
    }

    public static int createTexturedDisplayList(Model m) {
        int displayList = glGenLists(1);
        glNewList(displayList, GL_COMPILE);
        {
            glBegin(GL_TRIANGLES);
            for (Model.Face face : m.getFaces()) {
                if (face.hasTextureCoordinates()) {
                    glMaterial(GL_FRONT, GL_DIFFUSE, BufferTools.asFlippedFloatBuffer(face.getMaterial()
                            .diffuseColour[0], face.getMaterial().diffuseColour[1],
                            face.getMaterial().diffuseColour[2], 1));
                    glMaterial(GL_FRONT, GL_AMBIENT, BufferTools.asFlippedFloatBuffer(face.getMaterial()
                            .ambientColour[0], face.getMaterial().ambientColour[1],
                            face.getMaterial().ambientColour[2], 1));
                    glMaterialf(GL_FRONT, GL_SHININESS, face.getMaterial().specularCoefficient);
                }
                if (face.hasNormals()) {
                    Vector3f n1 = m.getNormals().get(face.getNormalIndices()[0] - 1);
                    glNormal3f(n1.x, n1.y, n1.z);
                }
                if (face.hasTextureCoordinates()) {
                    Vector2f t1 = m.getTextureCoordinates().get(face.getTextureCoordinateIndices()[0] - 1);
                    glTexCoord2f(t1.x, t1.y);
                }
                Vector3f v1 = m.getVertices().get(face.getVertexIndices()[0] - 1);
                glVertex3f(v1.x, v1.y, v1.z);
                if (face.hasNormals()) {
                    Vector3f n2 = m.getNormals().get(face.getNormalIndices()[1] - 1);
                    glNormal3f(n2.x, n2.y, n2.z);
                }
                if (face.hasTextureCoordinates()) {
                    Vector2f t2 = m.getTextureCoordinates().get(face.getTextureCoordinateIndices()[1] - 1);
                    glTexCoord2f(t2.x, t2.y);
                }
                Vector3f v2 = m.getVertices().get(face.getVertexIndices()[1] - 1);
                glVertex3f(v2.x, v2.y, v2.z);
                if (face.hasNormals()) {
                    Vector3f n3 = m.getNormals().get(face.getNormalIndices()[2] - 1);
                    glNormal3f(n3.x, n3.y, n3.z);
                }
                if (face.hasTextureCoordinates()) {
                    Vector2f t3 = m.getTextureCoordinates().get(face.getTextureCoordinateIndices()[2] - 1);
                    glTexCoord2f(t3.x, t3.y);
                }
                Vector3f v3 = m.getVertices().get(face.getVertexIndices()[2] - 1);
                glVertex3f(v3.x, v3.y, v3.z);
            }
            glEnd();
        }
        glEndList();
        return displayList;
    }

    public static Model loadTexturedModel(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Model m = new Model();
        Model.Material currentMaterial = new Model.Material();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("mtllib ")) {
                String materialFileName = line.split(" ")[1];
                File materialFile = new File(f.getParentFile().getAbsolutePath() + "/" + materialFileName);
                BufferedReader materialFileReader = new BufferedReader(new FileReader(materialFile));
                String materialLine;
                Model.Material parseMaterial = new Model.Material();
                String parseMaterialName = "";
                while ((materialLine = materialFileReader.readLine()) != null) {
                    if (materialLine.startsWith("#")) {
                        continue;
                    }
                    if (materialLine.startsWith("newmtl ")) {
                        if (!parseMaterialName.equals("")) {
                            m.getMaterials().put(parseMaterialName, parseMaterial);
                        }
                        parseMaterialName = materialLine.split(" ")[1];
                        parseMaterial = new Model.Material();
                    } else if (materialLine.startsWith("Ns ")) {
                        parseMaterial.specularCoefficient = Float.valueOf(materialLine.split(" ")[1]);
                    } else if (materialLine.startsWith("Ka ")) {
                        String[] rgb = materialLine.split(" ");
                        parseMaterial.ambientColour[0] = Float.valueOf(rgb[1]);
                        parseMaterial.ambientColour[1] = Float.valueOf(rgb[2]);
                        parseMaterial.ambientColour[2] = Float.valueOf(rgb[3]);
                    } else if (materialLine.startsWith("Ks ")) {
                        String[] rgb = materialLine.split(" ");
                        parseMaterial.specularColour[0] = Float.valueOf(rgb[1]);
                        parseMaterial.specularColour[1] = Float.valueOf(rgb[2]);
                        parseMaterial.specularColour[2] = Float.valueOf(rgb[3]);
                    } else if (materialLine.startsWith("Kd ")) {
                        String[] rgb = materialLine.split(" ");
                        parseMaterial.diffuseColour[0] = Float.valueOf(rgb[1]);
                        parseMaterial.diffuseColour[1] = Float.valueOf(rgb[2]);
                        parseMaterial.diffuseColour[2] = Float.valueOf(rgb[3]);
                    } else if (materialLine.startsWith("map_Kd")) {
                        parseMaterial.texture = TextureLoader.loadTexture(
                                new FileInputStream(new File(f.getParentFile().getAbsolutePath() + "/" + materialLine
                                        .split(" ")[1])), VoxelGame.getInstance().getTextureManager());
                    } else {
                        System.err.println("[MTL] Unknown Line: " + materialLine);
                    }
                }
                m.getMaterials().put(parseMaterialName, parseMaterial);
                materialFileReader.close();
            } else if (line.startsWith("usemtl ")) {
                currentMaterial = m.getMaterials().get(line.split(" ")[1]);
            } else if (line.startsWith("v ")) {
                String[] xyz = line.split(" ");
                float x = Float.valueOf(xyz[1]);
                float y = Float.valueOf(xyz[2]);
                float z = Float.valueOf(xyz[3]);
                m.getVertices().add(new Vector3f(x, y, z));
            } else if (line.startsWith("vn ")) {
                String[] xyz = line.split(" ");
                float x = Float.valueOf(xyz[1]);
                float y = Float.valueOf(xyz[2]);
                float z = Float.valueOf(xyz[3]);
                m.getNormals().add(new Vector3f(x, y, z));
            } else if (line.startsWith("vt ")) {
                String[] xyz = line.split(" ");
                float s = Float.valueOf(xyz[1]);
                float t = Float.valueOf(xyz[2]);
                m.getTextureCoordinates().add(new Vector2f(s, t));
            } else if (line.startsWith("f ")) {
                String[] faceIndices = line.split(" ");
                int[] vertexIndicesArray = {Integer.parseInt(faceIndices[1].split("/")[0]),
                    Integer.parseInt(faceIndices[2].split("/")[0]), Integer.parseInt(faceIndices[3].split("/")[0])};
                int[] textureCoordinateIndicesArray = {-1, -1, -1};
                if (m.hasTextureCoordinates()) {
                    textureCoordinateIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[1]);
                    textureCoordinateIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[1]);
                    textureCoordinateIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[1]);
                }
                int[] normalIndicesArray = {0, 0, 0};
                if (m.hasNormals()) {
                    normalIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[2]);
                    normalIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[2]);
                    normalIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[2]);
                }
                //                Vector3f vertexIndices = new Vector3f(Float.valueOf(faceIndices[1].split("/")[0]),
                //                        Float.valueOf(faceIndices[2].split("/")[0]),
                // Float.valueOf(faceIndices[3].split("/")[0]));
                //                Vector3f normalIndices = new Vector3f(Float.valueOf(faceIndices[1].split("/")[2]),
                //                        Float.valueOf(faceIndices[2].split("/")[2]),
                // Float.valueOf(faceIndices[3].split("/")[2]));
                m.getFaces().add(new OBJLoader.Model.Face(vertexIndicesArray, normalIndicesArray,
                        textureCoordinateIndicesArray, currentMaterial));
            } else if (line.startsWith("s ")) {
                boolean enableSmoothShading = !line.contains("off");
                m.setSmoothShadingEnabled(enableSmoothShading);
            } else {
                System.err.println("[OBJ] Unknown Line: " + line);
            }
        }
        reader.close();
        return m;
    }

    static class Model {

        private final List<Vector3f> vertices = new ArrayList<Vector3f>();
        private final List<Vector2f> textureCoordinates = new ArrayList<Vector2f>();
        private final List<Vector3f> normals = new ArrayList<Vector3f>();
        private final List<Face> faces = new ArrayList<Face>();
        private final HashMap<String, Material> materials = new HashMap<String, Material>();
        private boolean enableSmoothShading = true;

        public void enableStates() {
            if (hasTextureCoordinates()) {
                glEnable(GL_TEXTURE_2D);
            }
            if (isSmoothShadingEnabled()) {
                glShadeModel(GL_SMOOTH);
            } else {
                glShadeModel(GL_FLAT);
            }
        }

        public boolean hasTextureCoordinates() {
            return getTextureCoordinates().size() > 0;
        }

        public boolean hasNormals() {
            return getNormals().size() > 0;
        }

        public List<Vector3f> getVertices() {
            return vertices;
        }

        public List<Vector2f> getTextureCoordinates() {
            return textureCoordinates;
        }

        public List<Vector3f> getNormals() {
            return normals;
        }

        public List<Face> getFaces() {
            return faces;
        }

        public boolean isSmoothShadingEnabled() {
            return enableSmoothShading;
        }

        public void setSmoothShadingEnabled(boolean smoothShadingEnabled) {
            this.enableSmoothShading = smoothShadingEnabled;
        }

        public HashMap<String, Material> getMaterials() {
            return materials;
        }

        public static class Material {

            @Override
            public String toString() {
                return "Material{" +
                        "specularCoefficient=" + specularCoefficient +
                        ", ambientColour=" + ambientColour +
                        ", diffuseColour=" + diffuseColour +
                        ", specularColour=" + specularColour +
                        '}';
            }

            /** Between 0 and 1000. */
            public float specularCoefficient = 100;
            public float[] ambientColour = {0.2f, 0.2f, 0.2f};
            public float[] diffuseColour = {0.3f, 1, 1};
            public float[] specularColour = {1, 1, 1};
            public int texture;
        }

        /** @author Oskar */
        public static class Face {

            private final int[] vertexIndices = {-1, -1, -1};
            private final int[] normalIndices = {-1, -1, -1};
            private final int[] textureCoordinateIndices = {-1, -1, -1};
            private Material material;

            public Material getMaterial() {
                return material;
            }

            public boolean hasNormals() {
                return normalIndices[0] != -1;
            }

            public boolean hasTextureCoordinates() {
                return textureCoordinateIndices[0] != -1;
            }

            public int[] getVertexIndices() {
                return vertexIndices;
            }

            public int[] getTextureCoordinateIndices() {
                return textureCoordinateIndices;
            }

            public int[] getNormalIndices() {
                return normalIndices;
            }

            public Face(int[] vertexIndices) {
                this.vertexIndices[0] = vertexIndices[0];
                this.vertexIndices[1] = vertexIndices[1];
                this.vertexIndices[2] = vertexIndices[2];
            }

            public Face(int[] vertexIndices, int[] normalIndices) {
                this.vertexIndices[0] = vertexIndices[0];
                this.vertexIndices[1] = vertexIndices[1];
                this.vertexIndices[2] = vertexIndices[2];
                this.normalIndices[0] = normalIndices[0];
                this.normalIndices[1] = normalIndices[1];
                this.normalIndices[2] = normalIndices[2];
            }

            public Face(int[] vertexIndices, int[] normalIndices, int[] textureCoordinateIndices, Material material) {
                this.vertexIndices[0] = vertexIndices[0];
                this.vertexIndices[1] = vertexIndices[1];
                this.vertexIndices[2] = vertexIndices[2];
                this.textureCoordinateIndices[0] = textureCoordinateIndices[0];
                this.textureCoordinateIndices[1] = textureCoordinateIndices[1];
                this.textureCoordinateIndices[2] = textureCoordinateIndices[2];
                this.normalIndices[0] = normalIndices[0];
                this.normalIndices[1] = normalIndices[1];
                this.normalIndices[2] = normalIndices[2];
                this.material = material;
            }
        }
    }

    private static class BufferTools {

        /**
         * @param v the vector that is to be turned into an array of floats
         *
         * @return a float array where [0] is v.x, [1] is v.y, and [2] is v.z
         */
        public static float[] asFloats(Vector3f v) {
            return new float[]{v.x, v.y, v.z};
        }

        /**
         * @param elements the amount of elements to check
         *
         * @return true if the contents of the two buffers are the same, false if not
         */
        public static boolean bufferEquals(FloatBuffer bufferOne, FloatBuffer bufferTwo, int elements) {
            for (int i = 0; i < elements; i++) {
                if (bufferOne.get(i) != bufferTwo.get(i)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @param values the byte values that are to be turned into a readable ByteBuffer
         *
         * @return a readable ByteBuffer
         */
        public static ByteBuffer asByteBuffer(byte... values) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(values.length);
            buffer.put(values);
            return buffer;
        }

        /**
         * @param buffer a readable buffer
         * @param elements the amount of elements in the buffer
         *
         * @return a string representation of the elements in the buffer
         */
        public static String bufferToString(FloatBuffer buffer, int elements) {
            StringBuilder bufferString = new StringBuilder();
            for (int i = 0; i < elements; i++) {
                bufferString.append(" ").append(buffer.get(i));
            }
            return bufferString.toString();
        }

        /**
         * @param matrix4f the Matrix4f that is to be turned into a readable FloatBuffer
         *
         * @return a FloatBuffer representation of matrix4f
         */
        public static FloatBuffer asFloatBuffer(Matrix4f matrix4f) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix4f.store(buffer);
            return buffer;
        }

        /**
         * @param matrix4f the Matrix4f that is to be turned into a FloatBuffer that is readable to OpenGL (but not to you)
         *
         * @return a FloatBuffer representation of matrix4f
         */
        public static FloatBuffer asFlippedFloatBuffer(Matrix4f matrix4f) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix4f.store(buffer);
            buffer.flip();
            return buffer;
        }

        /**
         * @param values the float values that are to be turned into a readable FloatBuffer
         *
         * @return a readable FloatBuffer containing values
         */
        public static FloatBuffer asFloatBuffer(float... values) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
            buffer.put(values);
            return buffer;
        }

        /**
         * @param amountOfElements the amount of elements in the FloatBuffers
         *
         * @return an empty FloatBuffer with a set amount of elements
         */
        public static FloatBuffer reserveData(int amountOfElements) {
            return BufferUtils.createFloatBuffer(amountOfElements);
        }

        /**
         * @param values the float values that are to be turned into a FloatBuffer
         *
         * @return a FloatBuffer readable to OpenGL (not to you!) containing values
         */
        public static FloatBuffer asFlippedFloatBuffer(float... values) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
            buffer.put(values);
            buffer.flip();
            return buffer;
        }
    }
}
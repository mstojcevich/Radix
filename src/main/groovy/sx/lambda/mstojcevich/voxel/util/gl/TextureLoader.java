package sx.lambda.mstojcevich.voxel.util.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import sx.lambda.mstojcevich.voxel.texture.TextureManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TextureLoader {

    public static int loadTexture(InputStream is, TextureManager txman) {
        int texture = GL11.glGenTextures();

        try {
            BufferedImage bf = ImageIO.read(is);
            is.close();

            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(4 * bf.getWidth() * bf.getHeight());
            byte[] imageInByte = (byte[])bf.getRaster().getDataElements(0, 0, bf.getWidth(), bf.getHeight(), null);
            imageBuffer.put(imageInByte);
            imageBuffer.flip();

            int bytesPerPixel = imageInByte.length / (bf.getWidth() * bf.getHeight());
            int colorMode =  bytesPerPixel == 4 ? GL11.GL_RGBA : bytesPerPixel == 3 ? GL11.GL_RGB : GL11.GL_LUMINANCE;

            txman.bindTexture(texture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, colorMode, bf.getWidth(), bf.getHeight(), 0, colorMode, GL11.GL_UNSIGNED_BYTE, imageBuffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return texture;
    }

}

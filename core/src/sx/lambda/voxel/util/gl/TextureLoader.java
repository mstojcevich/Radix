package sx.lambda.voxel.util.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import sx.lambda.voxel.texture.TextureManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TextureLoader {

    public static int loadTexture(InputStream is, TextureManager txman) {
        int texture = Gdx.gl.glGenTexture();

        try {
            BufferedImage bf = ImageIO.read(is);
            is.close();

            ByteBuffer imageBuffer = BufferUtils.newByteBuffer(4 * bf.getWidth() * bf.getHeight());
            byte[] imageInByte = (byte[])bf.getRaster().getDataElements(0, 0, bf.getWidth(), bf.getHeight(), null);
            imageBuffer.put(imageInByte);
            imageBuffer.flip();

            int bytesPerPixel = imageInByte.length / (bf.getWidth() * bf.getHeight());
            int colorMode =  bytesPerPixel == 4 ? GL20.GL_RGBA : bytesPerPixel == 3 ? GL20.GL_RGB : GL20.GL_LUMINANCE;

            txman.bindTexture(texture);
            Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, colorMode, bf.getWidth(), bf.getHeight(), 0, colorMode, GL20.GL_UNSIGNED_BYTE, imageBuffer);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return texture;
    }

}

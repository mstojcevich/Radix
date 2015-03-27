package sx.lambda.mstojcevich.voxel.util.gl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import sx.lambda.mstojcevich.voxel.VoxelGame;


public class FontRenderer {

    public final static int
            ALIGN_LEFT = 0,
            ALIGN_RIGHT = 1,
            ALIGN_CENTER = 2;
    /** Array that holds necessary information about the font characters */
    private IntObject[] charArray = new IntObject[128];

    /** Map of user defined font characters (Character <-> IntObject) */
    private Map customChars = new HashMap();

    /** Boolean flag on whether AntiAliasing is enabled or not */
    private boolean antiAlias;

    /** Font's size */
    private int fontSize = 0;

    /** Font's height */
    private int fontHeight = 0;

    /** Texture used to cache the font 0-255 characters */
    private int fontTextureID;

    /** Default font texture width */
    private int textureWidth = 512;

    /** Default font texture height */
    private int textureHeight = 512;

    /** A reference to Java's AWT Font that we create our font texture from */
    private Font font;

    /** The font metrics for our Java AWT font */
    private FontMetrics fontMetrics;

    private final SpriteBatcher batcher;

    private int correctL = 9, correctR = 8;

    private boolean initialized;

    private BufferedImage jImage;

    private class IntObject {
        /** Character's width */
        public int width;

        /** Character's height */
        public int height;

        /** Character's stored x position */
        public int storedX;

        /** Character's stored y position */
        public int storedY;
    }


    public FontRenderer(Font font, boolean antiAlias, char[] additionalChars) {
        this.batcher = new SpriteBatcher(VoxelGame.getInstance().getTextureManager());

        this.font = font;
        this.fontSize = font.getSize()+3;
        this.antiAlias = antiAlias;

        createSet(additionalChars);

        fontHeight -= 1;
        if (fontHeight <= 0) fontHeight = 1;
    }

    public FontRenderer(Font font, boolean antiAlias) {
        this(font, antiAlias, null);
    }
    public void setCorrection(boolean on) {
        if (on) {
            correctL = 2;
            correctR = 1;
        } else {
            correctL = 0;
            correctR = 0;
        }
    }

    private void createSet( char[] customCharsArray ) {
        System.out.println("Font creating");
        // If there are custom chars then I expand the font texture twice
        if  (customCharsArray != null && customCharsArray.length > 0) {
            textureWidth *= 2;
        }

        // In any case this should be done in other way. Texture with size 512x512
        // can maintain only 256 characters with resolution of 32x32. The texture
        // size should be calculated dynamicaly by looking at character sizes. 

        try {
            jImage = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) jImage.getGraphics();

            g.setColor(new Color(0,0,0,1));
            g.fillRect(0,0,textureWidth,textureHeight);

            if(antiAlias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.setFont(font);
            g.setColor(Color.WHITE);

            fontMetrics = g.getFontMetrics();

            int rowHeight = 0;
            int positionX = 0;
            int positionY = 0;

            int customCharsLength = ( customCharsArray != null ) ? customCharsArray.length : 0;
            for (int i = 32; i < 128 + customCharsLength; i++) {
                // get 0-128 characters and then custom characters
                char ch = ( i < 128 ) ? (char) i : customCharsArray[i-128];

                int charwidth = fontMetrics.charWidth(ch)+8;
                if (charwidth <= 0) {
                    charwidth = 7;
                }
                int charheight = fontMetrics.getHeight()+3;
                if (charheight <= 0) {
                    charheight = fontSize;
                }

                IntObject newIntObject = new IntObject();

                newIntObject.width = charwidth;
                newIntObject.height = charheight;

                if (positionX + newIntObject.width >= textureWidth) {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }
                newIntObject.width += 1; // Extra spacing

                newIntObject.storedX = positionX;
                newIntObject.storedY = positionY;

                if (newIntObject.height > fontHeight) {
                    fontHeight = newIntObject.height;
                }

                if (newIntObject.height > rowHeight) {
                    rowHeight = newIntObject.height;
                }

                g.drawString(String.valueOf(ch), positionX, positionY + fontMetrics.getAscent());

                positionX += newIntObject.width;

                if( i < 128 ) { // standard characters
                    charArray[i] = newIntObject;
                } else { // custom characters
                    customChars.put( new Character( ch ), newIntObject );
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create font.");
            e.printStackTrace();
        }
        System.out.println("Font ready");
    }

    private void drawQuad(int drawX, int drawY, int drawX2, int drawY2,
                          int srcX, int srcY, int srcX2, int srcY2) {
        int drawWidth = drawX2 - drawX;
        int drawHeight = drawY2 - drawY;
        float textureSrcX = (float)srcX / textureWidth;
        float textureSrcY = (float)srcY / textureHeight;
        int srcWidth = srcX2 - srcX;
        int srcHeight = srcY2 - srcY;
        float renderWidth = (float)srcWidth / textureWidth;
        float renderHeight = (float)srcHeight / textureHeight;

        batcher.drawTexturedRect(drawX2, drawY, drawX, drawY2,
                textureSrcX+renderWidth, textureSrcY, textureSrcX, textureSrcY+renderHeight);
    }

    public int getWidth(String whatchars) {
        int totalwidth = 0;
        IntObject intObject = null;
        int currentChar = 0;
        for (int i = 0; i < whatchars.length(); i++) {
            currentChar = whatchars.charAt(i);
            if (currentChar < 128) {
                intObject = charArray[currentChar];
            } else {
                intObject = (IntObject)customChars.get( new Character( (char) currentChar ) );
            }

            if( intObject != null )
                totalwidth += intObject.width;
        }
        return totalwidth/2;
    }

    public int getHeight() {
        return fontHeight;
    }


    public int getHeight(String HeightString) {
        return fontHeight;
    }

    public int getLineHeight() {
        return fontHeight;
    }

    public void drawString(int x, int y,
                           String whatchars) {
        drawString(x, y, whatchars, 1, 1);
    }

    public void drawString(int x, int y,
                           String whatchars, float scaleX, float scaleY) {
        drawString(x,y,whatchars, 0, whatchars.length()-1, scaleX, scaleY, ALIGN_LEFT);
    }
    public void drawString(int x, int y,
                           String whatchars, float scaleX, float scaleY, int format) {
        drawString(x,y,whatchars, 0, whatchars.length()-1, scaleX, scaleY, format);
    }


    public void drawString(int x, int y,
                           String whatchars, int startIndex, int endIndex,
                           float scaleX, float scaleY,
                           int format
    ) {
        if(!this.initialized)init();

        IntObject intObject = null;
        int charCurrent;


        int totalwidth = 0;
        int i = startIndex, d, c;
        int startY = 0;



        switch (format) {
            case ALIGN_RIGHT: {
                d = -1;
                c = correctR;

                while (i < endIndex) {
                    if (whatchars.charAt(i) == '\n') startY -= fontHeight;
                    i++;
                }
                break;
            }
            case ALIGN_CENTER: {
                for (int l = startIndex; l <= endIndex; l++) {
                    charCurrent = whatchars.charAt(l);
                    if (charCurrent == '\n') break;
                    if (charCurrent < 128) {
                        intObject = charArray[charCurrent];
                    } else {
                        intObject = (IntObject)customChars.get( new Character( (char) charCurrent ) );
                    }
                    totalwidth += intObject.width-correctL;
                }
                totalwidth /= -2;
            }
            case ALIGN_LEFT:
            default: {
                d = 1;
                c = correctL;
                break;
            }

        }

        while (i >= startIndex && i <= endIndex) {

            charCurrent = whatchars.charAt(i);
            if (charCurrent < 256) {
                intObject = charArray[charCurrent];
            } else {
                intObject = (IntObject)customChars.get( new Character( (char) charCurrent ) );
            }

            if( intObject != null ) {
                if (d < 0) totalwidth += (intObject.width-c) * d;
                if (charCurrent == '\n') {
                    startY -= fontHeight * d;
                    totalwidth = 0;
                    if (format == ALIGN_CENTER) {
                        for (int l = i+1; l <= endIndex; l++) {
                            charCurrent = whatchars.charAt(l);
                            if (charCurrent == '\n') break;
                            if (charCurrent < 256) {
                                intObject = charArray[charCurrent];
                            } else {
                                intObject = (IntObject)customChars.get( new Character( (char) charCurrent ) );
                            }
                            totalwidth += intObject.width-correctL;
                        }
                        totalwidth /= -2;
                    }
                    //if center get next lines total width/2;
                }
                else {
                    drawQuad((int)(totalwidth * scaleX + x),
                            (int)((startY + intObject.height) * scaleY + y),
                            (int)((totalwidth + intObject.width) * scaleX + x), (int)(startY * scaleY + y),
                            intObject.storedX,
                            intObject.storedY, intObject.storedX + intObject.width,
                            intObject.storedY + intObject.height);
                    if (d > 0) totalwidth += (intObject.width-c) * d ;
                }
                i += d;

            }
        }
        batcher.render(fontTextureID);
    }

    public static int loadImage(BufferedImage bufferedImage) {
        try {
            short width       = (short)bufferedImage.getWidth();
            short height      = (short)bufferedImage.getHeight();
            //textureLoader.bpp = bufferedImage.getColorModel().hasAlpha() ? (byte)32 : (byte)24;
            int bpp = (byte)bufferedImage.getColorModel().getPixelSize();
            ByteBuffer byteBuffer;
            DataBuffer db = bufferedImage.getData().getDataBuffer();
            if (db instanceof DataBufferInt) {
                int intI[] = ((DataBufferInt)(bufferedImage.getData().getDataBuffer())).getData();
                byte newI[] = new byte[intI.length * 4];
                for (int i = 0; i < intI.length; i++) {
                    byte b[] = intToByteArray(intI[i]);
                    int newIndex = i*4;

                    newI[newIndex]   = b[1];
                    newI[newIndex+1] = b[2];
                    newI[newIndex+2] = b[3];
                    newI[newIndex+3] = b[0];
                }

                byteBuffer  = ByteBuffer.allocateDirect(
                        width*height*(bpp/8))
                        .order(ByteOrder.nativeOrder())
                        .put(newI);
            } else {
                byteBuffer  = ByteBuffer.allocateDirect(
                        width*height*(bpp/8))
                        .order(ByteOrder.nativeOrder())
                        .put(((DataBufferByte)(bufferedImage.getData().getDataBuffer())).getData());
            }
            byteBuffer.flip();


            int internalFormat = GL11.GL_RGBA8,
                    format = GL11.GL_RGBA;
            IntBuffer   textureId =  BufferUtils.createIntBuffer(1);
            GL11.glGenTextures(textureId);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId.get(0));


            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);



            GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D,
                    internalFormat,
                    width,
                    height,
                    format,
                    GL11.GL_UNSIGNED_BYTE,
                    byteBuffer);
            return textureId.get(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return -1;
    }
    public static boolean isSupported(String fontname) {
        Font font[] = getFonts();
        for (int i = font.length-1; i >= 0; i--) {
            if (font[i].getName().equalsIgnoreCase(fontname))
                return true;
        }
        return false;
    }

    public static Font[] getFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public void destroy() {
        if(this.initialized) {
            IntBuffer scratch = BufferUtils.createIntBuffer(1);
            scratch.put(0, fontTextureID);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL11.glDeleteTextures(scratch);
            this.batcher.cleanup();
            this.initialized = false;
        }
    }

    private void init() {
        if(!this.initialized) {
            this.batcher.init();
            fontTextureID = loadImage(jImage);
            this.initialized = true;
        }
    }
}
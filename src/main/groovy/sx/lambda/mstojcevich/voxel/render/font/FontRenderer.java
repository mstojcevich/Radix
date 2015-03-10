package sx.lambda.mstojcevich.voxel.render.font;

import java.awt.Font;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

public class FontRenderer  {
    public static boolean betterFontsEnabled = true;
    public static boolean blackShadows = true;
    public StringCache stringCache;
    public boolean dropShadowEnabled = true;
    public boolean enabled = true;

    /** Array of width of all the characters in default.png */
    private int[] charWidth = new int[256];

    /** the height in pixels of default text */
    public int FONT_HEIGHT = 1;
    public Random fontRandom = new Random();

    public Font font;

    /**
     * Array of the start/end column (in upper/lower nibble) for every glyph in
     * the /font directory.
     */
    private byte[] glyphWidth = new byte[65536];

    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16
     * darker version of the same colors for drop shadows.
     */
    private int[] colorCode = new int[32];


    /** Current X coordinate at which to draw the next character. */
    private float posX;

    /** Current Y coordinate at which to draw the next character. */
    private float posY;

    /**
     * If true, strings should be rendered with Unicode fonts instead of the
     * default.png font
     */
    private boolean unicodeFlag;

    /**
     * If true, the Unicode Bidirectional Algorithm should be run before
     * rendering any string.
     */
    private boolean bidiFlag;

    /** Used to specify new red value for the current color. */
    private float red;

    /** Used to specify new blue value for the current color. */
    private float blue;

    /** Used to specify new green value for the current color. */
    private float green;

    /** Used to speify new alpha value for the current color. */
    private float alpha;

    /** Text color of the currently rendering string. */
    private int textColor;

    /** Set if the "k" style (random) is active in currently rendering string */
    private boolean randomStyle;

    /** Set if the "l" style (bold) is active in currently rendering string */
    private boolean boldStyle;

    /** Set if the "o" style (italic) is active in currently rendering string */
    private boolean italicStyle;

    /**
     * Set if the "n" style (underlined) is active in currently rendering string
     */
    private boolean underlineStyle;

    /**
     * Set if the "m" style (strikethrough) is active in currently rendering
     * string
     */
    private boolean strikethroughStyle;
    private static final String __OBFID = "CL_00000660";

    public FontRenderer(Font font, boolean antiAlias) {
        this.font = font;
        this.unicodeFlag = false;

        for (int var5 = 0; var5 < 32; ++var5) {
            int var6 = (var5 >> 3 & 1) * 85;
            int var7 = (var5 >> 2 & 1) * 170 + var6;
            int var8 = (var5 >> 1 & 1) * 170 + var6;
            int var9 = (var5 >> 0 & 1) * 170 + var6;

            if (var5 == 6) {
                var7 += 85;
            }

            if (var5 >= 16) {
                var7 /= 4;
                var8 /= 4;
                var9 /= 4;
            }

            this.colorCode[var5] = (var7 & 255) << 16 | (var8 & 255) << 8
                    | var9 & 255;
        }

        this.stringCache = new StringCache(this.colorCode);

        this.stringCache.setDefaultFont(font, antiAlias);

        this.FONT_HEIGHT = (int) this.stringCache.getHeight();
        if (this.FONT_HEIGHT == 0)
            this.FONT_HEIGHT = 9;
        // this.readGlyphSizes();
    }

    // public FontRenderer(GameSettings par1GameSettings, ResourceLocation
    // par2ResourceLocation, TextureManager par3TextureManager, boolean par4)
    // {
    // this.locationFontTexture = par2ResourceLocation;
    // this.renderEngine = par3TextureManager;
    // this.unicodeFlag = par4;
    // par3TextureManager.bindTexture(this.locationFontTexture);
    //
    // for (int var5 = 0; var5 < 32; ++var5)
    // {
    // int var6 = (var5 >> 3 & 1) * 85;
    // int var7 = (var5 >> 2 & 1) * 170 + var6;
    // int var8 = (var5 >> 1 & 1) * 170 + var6;
    // int var9 = (var5 >> 0 & 1) * 170 + var6;
    //
    // if (var5 == 6)
    // {
    // var7 += 85;
    // }
    //
    // if (par1GameSettings.anaglyph)
    // {
    // int var10 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
    // int var11 = (var7 * 30 + var8 * 70) / 100;
    // int var12 = (var7 * 30 + var9 * 70) / 100;
    // var7 = var10;
    // var8 = var11;
    // var9 = var12;
    // }
    //
    // if (var5 >= 16)
    // {
    // var7 /= 4;
    // var8 /= 4;
    // var9 /= 4;
    // }
    //
    // this.colorCode[var5] = (var7 & 255) << 16 | (var8 & 255) << 8 | var9 &
    // 255;
    // }
    // System.out.println("Starting BetterFonts");
    // if(par2ResourceLocation.getResourcePath().equals("textures/font/ascii.png")
    // && this.stringCache == null)
    // {
    // this.stringCache = new StringCache(this.colorCode);
    //
    // /* Read optional config file to override the default font name/size */
    // ConfigParser config = new ConfigParser();
    // if(config.loadConfig("/config/BetterFonts.cfg"))
    // {
    // String fontName = config.getFontName("SansSerif");
    // int fontSize = config.getFontSize(18);
    // boolean antiAlias = config.getBoolean("font.antialias", false);
    // dropShadowEnabled = config.getBoolean("font.dropshadow", true);
    //
    // this.stringCache.setDefaultFont(fontName, fontSize, antiAlias);
    // System.out.println("BetterFonts configuration loaded");
    // }
    // }
    // this.readGlyphSizes();
    // }

    /**
     * Draws the specified string with a shadow.
     */
    public int drawStringWithShadow(String par1Str, double x, double y,
                                    int color) {
        return this.drawString(par1Str, x, y, color, true);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String par1Str, double d, double e, int par4) {
        return this.drawString(par1Str, d, e, par4, false);
    }

    /**
     * Draws the specified string with a shadow.
     */
    public int drawStringWithShadow(String par1Str, int x, int y,
                                    int color) {
        return this.drawString(par1Str, x, y, color, true);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String par1Str, int d, int e, int par4) {
        return this.drawString(par1Str, d, e, par4, false);
    }

    /**
     * Draws the specified string. Args: string, x, y, color, dropShadow
     */
    public int drawString(String par1Str, double x, double y, int par4,
                          boolean par5) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        this.resetStyles();
        int var6;

        if (par5 && this.dropShadowEnabled) {
            var6 = this.renderString(par1Str, x + 1, y + 1, blackShadows ? 0 : par4, true);
            var6 = Math
                    .max(var6, this.renderString(par1Str, x, y, par4, false));
        } else {
            var6 = this.renderString(par1Str, x, y, par4, false);
        }

        return var6;
    }

    public int drawString(String par1Str, int x, int y, int par4,
                          boolean par5) {
        return this.drawString(par1Str, (double)x, (double)y, par4, par5);
    }

    private String func_147647_b(String p_147647_1_) {
        return p_147647_1_;
    }

    /**
     * Reset all style flag fields in the class to false; called at the start of
     * string rendering
     */
    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    /**
     * Render string either left or right aligned depending on bidiFlag
     */
    private int renderStringAligned(String par1Str, int par2, int par3,
                                    int par4, int par5, boolean par6) {
        if (this.bidiFlag) {
            int var7 = this.getStringWidth(this.func_147647_b(par1Str));
            par2 = par2 + par4 - var7;
        }

        return this.renderString(par1Str, par2, par3, par5, par6);
    }

    /**
     * Render single line string by setting GL color, current (posX,posY), and
     * calling renderStringAtPos()
     */
    private int renderString(String par1Str, double d, double e, int par4,
                             boolean par5) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if (par1Str == null) {
            return 0;
        } else {
            if (this.bidiFlag) {
                par1Str = this.func_147647_b(par1Str);
            }

            if ((par4 & -67108864) == 0) {
                par4 |= -16777216;
            }

            if (par5) {
                par4 = (par4 & 16579836) >> 2 | par4 & -16777216;
            }

            this.red = (par4 >> 16 & 255) / 255.0F;
            this.blue = (par4 >> 8 & 255) / 255.0F;
            this.green = (par4 & 255) / 255.0F;
            this.alpha = (par4 >> 24 & 255) / 255.0F;
            GL11.glColor4f(this.red, this.blue, this.green, this.alpha);
            this.posX = (float) d;
            this.posY = (float) e;
            if (this.stringCache != null) {
                this.posX += stringCache
                        .renderString(par1Str, d, e, par4, par5);
            }
            return (int) this.posX;
        }
    }

    /**
     * Returns the width of this string. Equivalent of
     * FontMetrics.stringWidth(String s).
     */
    public int getStringWidth(String par1Str) {
        return this.stringCache.getStringWidth(par1Str);
    }

    /**
     * Returns the width of this character as rendered.
     */
    public int getCharWidth(char par1) {
        if (par1 == 167) {
            return -1;
        } else if (par1 == 32) {
            return 4;
        } else {
            int var2 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"
                    .indexOf(par1);

            if (par1 > 0 && var2 != -1 && !this.unicodeFlag) {
                return this.charWidth[var2];
            } else if (this.glyphWidth[par1] != 0) {
                int var3 = this.glyphWidth[par1] >>> 4;
                int var4 = this.glyphWidth[par1] & 15;

                if (var4 > 7) {
                    var4 = 15;
                    var3 = 0;
                }

                ++var4;
                return (var4 - var3) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String par1Str, int par2) {
        return this.trimStringToWidth(par1Str, par2, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String par1Str, int par2, boolean par3) {
        return this.stringCache.trimStringToWidth(par1Str, par2, par3);
    }

    /**
     * Remove all newline characters from the end of the string
     */
    private String trimStringNewline(String par1Str) {
        while (par1Str != null && par1Str.endsWith("\n")) {
            par1Str = par1Str.substring(0, par1Str.length() - 1);
        }

        return par1Str;
    }

    /**
     * Splits and draws a String with wordwrap (maximum length is parameter k)
     */
    public void drawSplitString(String par1Str, int par2, int par3, int par4,
                                int par5) {
        this.resetStyles();
        this.textColor = par5;
        par1Str = this.trimStringNewline(par1Str);
        this.renderSplitString(par1Str, par2, par3, par4, false);
    }

    /**
     * Perform actual work of rendering a multi-line string with wordwrap and
     * with darker drop shadow color if flag is set
     */
    private void renderSplitString(String par1Str, int par2, int par3,
                                   int par4, boolean par5) {
        List var6 = this.listFormattedStringToWidth(par1Str, par4);

        for (Iterator var7 = var6.iterator(); var7.hasNext(); par3 += this.FONT_HEIGHT) {
            String var8 = (String) var7.next();
            this.renderStringAligned(var8, par2, par3, par4, this.textColor,
                    par5);
        }
    }

    /**
     * Returns the width of the wordwrapped String (maximum length is parameter
     * k)
     */
    public int splitStringWidth(String par1Str, int par2) {
        return this.FONT_HEIGHT
                * this.listFormattedStringToWidth(par1Str, par2).size();
    }

    /**
     * Set unicodeFlag controlling whether strings should be rendered with
     * Unicode fonts instead of the default.png font.
     */
    public void setUnicodeFlag(boolean par1) {
        this.unicodeFlag = par1;
    }

    /**
     * Get unicodeFlag controlling whether strings should be rendered with
     * Unicode fonts instead of the default.png font.
     */
    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    /**
     * Set bidiFlag to control if the Unicode Bidirectional Algorithm should be
     * run before rendering any string.
     */
    public void setBidiFlag(boolean par1) {
        this.bidiFlag = par1;
    }

    /**
     * Breaks a string into a list of pieces that will fit a specified width.
     */
    public List listFormattedStringToWidth(String par1Str, int par2) {
        return Arrays.asList(this.wrapFormattedStringToWidth(par1Str, par2)
                .split("\n"));
    }

    /**
     * Inserts newline and formatting into a string to wrap it within the
     * specified width.
     */
    @SuppressWarnings("all")
    private String wrapFormattedStringToWidth(String par1Str, int par2) {
        int var3 = this.sizeStringToWidth(par1Str, par2);

        if (par1Str.length() <= var3) {
            return par1Str;
        } else {
            String var4 = par1Str.substring(0, var3);
            char var5 = par1Str.charAt(var3);
            boolean var6 = var5 == 32 || var5 == 10;
            String var7 = getFormatFromString(var4)
                    + par1Str.substring(var3 + (var6 ? 1 : 0));
            return var4 + "\n" + this.wrapFormattedStringToWidth(var7, par2);
        }
    }

    /**
     * Determines how many characters from the string will fit into the
     * specified width.
     */
    private int sizeStringToWidth(String par1Str, int par2) {
        return this.stringCache.sizeStringToWidth(par1Str, par2);
    }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
    private static boolean isFormatColor(char par0) {
        return par0 >= 48 && par0 <= 57 || par0 >= 97 && par0 <= 102
                || par0 >= 65 && par0 <= 70;
    }

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special
     * formatting.
     */
    private static boolean isFormatSpecial(char par0) {
        return par0 >= 107 && par0 <= 111 || par0 >= 75 && par0 <= 79
                || par0 == 114 || par0 == 82;
    }

    /**
     * Digests a string for nonprinting formatting characters then returns a
     * string containing only that formatting.
     */
    private static String getFormatFromString(String par0Str) {
        String var1 = "";
        int var2 = -1;
        int var3 = par0Str.length();

        while ((var2 = par0Str.indexOf(167, var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                char var4 = par0Str.charAt(var2 + 1);

                if (isFormatColor(var4)) {
                    var1 = "\u00a7" + var4;
                } else if (isFormatSpecial(var4)) {
                    var1 = var1 + "\u00a7" + var4;
                }
            }
        }

        return var1;
    }

    /**
     * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should
     * be run before rendering any string
     */
    public boolean getBidiFlag() {
        return this.bidiFlag;
    }

    public int getStringHeight(String title) {
        return this.FONT_HEIGHT;
    }
}
package sx.lambda.mstojcevich.voxel.render.game

import groovy.transform.CompileStatic
import org.lwjgl.opengl.Display
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.render.EventEntityRender
import sx.lambda.mstojcevich.voxel.api.events.render.EventPostWorldRender
import sx.lambda.mstojcevich.voxel.render.Renderer
import sx.lambda.mstojcevich.voxel.entity.Entity
import sx.lambda.mstojcevich.voxel.util.Frustum
import sx.lambda.mstojcevich.voxel.util.Vec3i
import sx.lambda.mstojcevich.voxel.util.gl.FontRenderer
import sx.lambda.mstojcevich.voxel.util.gl.FrameBuffer
import sx.lambda.mstojcevich.voxel.util.gl.SpriteBatcher.StaticRender
import sx.lambda.mstojcevich.voxel.world.chunk.IChunk

import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat

import static org.lwjgl.opengl.GL11.*

@CompileStatic
class GameRenderer implements Renderer {

    private final VoxelGame game
    private FrameBuffer postProcessFbo
    private FontRenderer debugTextRenderer

    private boolean initted, fontRenderReady

    private Frustum frustum = new Frustum()
    private boolean calcFrustum

    private long lastDynamicTextRerenderMS = 0

    // The dynamic static renders
    private StaticRender fpsRender, positionRender, headingRender, chunkposRender, awrtRender, lightlevelRender, activeThreadsRender

    // The 100% never-changing static renders
    private StaticRender glInfoRender

    private long frameCounter

    public GameRenderer(VoxelGame game) {
        this.game = game
    }

    @Override
    void render() {
        if(!initted)init()

        prepareWorldRender()
        if(game.instance.settingsManager.visualSettings.postProcessEnabled) {
            postProcessFbo.bind()
            glClearColor(0.2f, 0.4f, 1, 1)
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
        game.getWorld().render()
        VoxelGameAPI.instance.eventManager.push(new EventPostWorldRender())
        drawBlockSelection()
        renderEntities()
        if(game.instance.settingsManager.visualSettings.postProcessEnabled) {
            postProcessFbo.unbind()
        }
    }

    void draw2d() {
        if(!initted)init()

        if(System.currentTimeMillis() - lastDynamicTextRerenderMS >= 1000) { // Rerender the dynamic texts every second
            createDynamicRenderers()
            lastDynamicTextRerenderMS = System.currentTimeMillis()
        }

        if(game.instance.settingsManager.visualSettings.postProcessEnabled) {
            game.enablePostProcessShader()
            game.postProcessShader.setAnimTime((int) (System.currentTimeMillis() % 100000))
            postProcessFbo.drawTexture(VoxelGame.instance.textureManager, game.getPostProcessShader().getPositionAttrib(), game.getPostProcessShader().getTexCoordAttrib())
        }

        game.enableGuiShader()
        game.getGuiShader().disableColors()
        game.getGuiShader().enableTexturing()

        if(fontRenderReady) {
            if(glInfoRender != null) {
                glInfoRender.render()
            }

            if(fpsRender != null) {
                fpsRender.render()
            }
            if(positionRender != null) {
                positionRender.render()
            }
            if(headingRender != null) {
                headingRender.render()
            }
            if(chunkposRender != null) {
                chunkposRender.render()
            }
            if(awrtRender != null) {
                awrtRender.render()
            }
            if(lightlevelRender != null) {
                lightlevelRender.render()
            }
            if(activeThreadsRender != null) {
                activeThreadsRender.render()
            }

            // Let the texture manager know that we've switched textures
            // drawString binds its own texture and TextureManager still thinks we're on our last texture we used.
            // This manually forces TextureManager to think we're on a different texture
            game.textureManager.bindTexture(0)
        }
    }

    @Override
    void cleanup() {
        if(postProcessFbo != null) {
            postProcessFbo.cleanup()
        }
        debugTextRenderer.destroy()
        glInfoRender.destroy()
        glInfoRender = null

        initted = false
    }

    @Override
    void init() {
        initted = true

        new Thread() {
            @Override
            public void run() {
                InputStream is = GameRenderer.class.getResourceAsStream("/fonts/LiberationMono-Bold.ttf")
                debugTextRenderer = new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f), true)
                VoxelGame.instance.addToGLQueue(new Runnable() {
                    @Override
                    void run() {
                        String glInfoStr = "GL Info: " + glGetString(GL_RENDERER) + " (GL " + glGetString(GL_VERSION) + ")"
                        glInfoRender = debugTextRenderer.drawStringStatic(Display.getWidth(), 2, glInfoStr, FontRenderer.ALIGN_RIGHT)
                    }
                })
                fontRenderReady = true
            }
        }.start()

        if(game.instance.settingsManager.visualSettings.postProcessEnabled) {
            postProcessFbo = new FrameBuffer()
        }
    }

    private void prepareWorldRender() {
        glRotatef(-game.getPlayer().getRotation().getPitch(), 1, 0, 0)
        glRotatef(game.getPlayer().getRotation().getYaw(), 0, 1, 0)
        glTranslatef(-game.getPlayer().getPosition().getX(), (float) -(game.getPlayer().getPosition().getY() + game.getPlayer().getEyeHeight()), -game.getPlayer().getPosition().getZ())

        if (shouldCalcFrustum()) {
            calcFrustum = false
            frustum.calculateFrustum() // TODO do this on another thread?
        }
    }

    private void drawBlockSelection() {
    }

    private void renderEntities() {
        VoxelGame.instance.worldShader.disableTexturing()
        VoxelGame.instance.worldShader.disableLighting()
        for(Entity e : game.world.loadedEntities) {
            if(e != null && e != game.player) {
                e.render()
                VoxelGameAPI.instance.eventManager.push(new EventEntityRender(e))
            }
        }
        VoxelGame.instance.worldShader.enableLighting()
        VoxelGame.instance.worldShader.enableTexturing()
    }

    public void calculateFrustum() {
        this.calcFrustum = true
    }

    public boolean shouldCalcFrustum() { calcFrustum }

    public Frustum getFrustum() { frustum }

    private void destroyDynamicRenderers() {
        if(fpsRender != null) {
            fpsRender.destroy()
            fpsRender = null
        }
        if(positionRender != null) {
            positionRender.destroy()
            positionRender = null
        }
        if(headingRender != null) {
            headingRender.destroy()
            headingRender = null
        }
        if(chunkposRender != null) {
            chunkposRender.destroy()
            chunkposRender = null
        }
        if(awrtRender != null) {
            awrtRender.destroy()
            awrtRender = null
        }
        if(lightlevelRender != null) {
            lightlevelRender.destroy()
            lightlevelRender = null
        }
        if(activeThreadsRender != null) {
            activeThreadsRender.destroy()
            activeThreadsRender = null
        }
    }

    private void createDynamicRenderers() {
        destroyDynamicRenderers()

        if(fontRenderReady) {
            int currentHeight = 2 + debugTextRenderer.getLineHeight() * 1
            // There is 1 text not part of the dynamic texts, offset to make room

            String fpsStr = "FPS: $game.fps"
            fpsRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, fpsStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            int acrt = 0
            if (game.numChunkRenders > 0) {
                acrt = (int) (game.chunkRenderTimes / game.numChunkRenders)
            }
            String lcrtStr = "AWRT: $acrt ns"
            awrtRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, lcrtStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            DecimalFormat posFormat = new DecimalFormat("#.00");
            String coordsStr = String.format("(x,y,z): %s,%s,%s",
                    posFormat.format(game.player.position.x),
                    posFormat.format(game.player.position.y),
                    posFormat.format(game.player.position.z))
            positionRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, coordsStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            String chunk = String.format("Chunk (x,z): %s,%s",
                    game.world.getChunkPosition(game.player.position.x),
                    game.world.getChunkPosition(game.player.position.z))
            chunkposRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, chunk, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            String headingStr = String.format("(yaw,pitch): %s,%s",
                    posFormat.format(game.player.rotation.yaw),
                    posFormat.format(game.player.rotation.pitch))
            headingRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, headingStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            Vec3i playerPosVec = new Vec3i(Math.floor(game.player.position.x) as int, Math.floor(game.player.position.y) as int, Math.floor(game.player.position.z) as int)
            IChunk playerChunk = game.world.getChunkAtPosition(playerPosVec)
            if (playerChunk != null) {
                String llStr = String.format("Light Level @ Feet: " + playerChunk.getSunlight(playerPosVec.x, playerPosVec.y, playerPosVec.z))
                lightlevelRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, llStr, FontRenderer.ALIGN_RIGHT)
                currentHeight += debugTextRenderer.getLineHeight()
            }

            String threadsStr = "Active threads: " + Thread.activeCount()
            activeThreadsRender = debugTextRenderer.drawStringStatic(Display.getWidth(), currentHeight, threadsStr, FontRenderer.ALIGN_RIGHT)
        }
    }

}

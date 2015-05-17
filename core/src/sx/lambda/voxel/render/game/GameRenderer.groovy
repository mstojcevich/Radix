package sx.lambda.voxel.render.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Frustum
import com.badlogic.gdx.math.Vector3
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.events.render.EventEntityRender
import sx.lambda.voxel.api.events.render.EventPostWorldRender
import sx.lambda.voxel.render.Renderer
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.world.chunk.IChunk
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.entity.Entity
import sx.lambda.voxel.util.gl.FontRenderer
import sx.lambda.voxel.util.gl.FrameBuffer
import sx.lambda.voxel.util.gl.SpriteBatcher

import java.awt.*
import java.text.DecimalFormat

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
class GameRenderer implements Renderer {

    private final VoxelGameClient game
    private FrameBuffer postProcessFbo
    private FontRenderer debugTextRenderer

    private boolean initted, fontRenderReady

    private boolean calcFrustum

    private long lastDynamicTextRerenderMS = 0

    // The dynamic static renders
    private SpriteBatcher.StaticRender fpsRender, positionRender, headingRender, chunkposRender, awrtRender, lightlevelRender, activeThreadsRender

    // The 100% never-changing static renders
    private SpriteBatcher.StaticRender glInfoRender

    private long frameCounter

    private Frustum frustum

    public GameRenderer(VoxelGameClient game) {
        this.game = game
    }

    @Override
    void render() {
        if(!initted)init()

        prepareWorldRender()
        if(game.instance.settingsManager.visualSettings.postProcessEnabled) {
            postProcessFbo.bind()
            Gdx.gl.glClearColor(0.2f, 0.4f, 1, 1)
            Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
            postProcessFbo.drawTexture(VoxelGameClient.instance.textureManager, game.getPostProcessShader().getPositionAttrib(), game.getPostProcessShader().getTexCoordAttrib())
        }

        game.enableGuiShader()
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

        frustum = game.camera.frustum

        new Thread() {
            @Override
            public void run() {
                InputStream is = Gdx.files.internal("fonts/LiberationMono-Bold.ttf").read()
                debugTextRenderer = new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f), true)
                VoxelGameClient.instance.addToGLQueue(new Runnable() {
                    @Override
                    void run() {
                        String glInfoStr = "GL Info: " + Gdx.gl.glGetString(GL_RENDERER) + " (GL " + Gdx.gl.glGetString(GL_VERSION) + ")"
                        glInfoRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), 2, glInfoStr, FontRenderer.ALIGN_RIGHT)
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
        //TODO game camera
        game.camera.position.set(game.player.position.x, game.player.position.y + game.player.eyeHeight, game.player.position.z)
        game.camera.up.set(0, 1, 0);
        game.camera.direction.set(0, 0, -1);
        game.camera.rotate(game.player.rotation.pitch, 1, 0, 0)
        game.camera.rotate(-game.player.rotation.yaw, 0, 1, 0)

        if (shouldCalcFrustum()) {
            calcFrustum = false
            game.camera.update(true)
        } else {
            game.camera.update()
        }
    }

    private void drawBlockSelection() {
    }

    private void renderEntities() {
        VoxelGameClient.instance.worldShader.disableTexturing()
        VoxelGameClient.instance.worldShader.disableLighting()
        for(Entity e : game.world.loadedEntities) {
            if(e != null && e != game.player) {
                e.render()
                VoxelGameAPI.instance.eventManager.push(new EventEntityRender(e))
            }
        }
        VoxelGameClient.instance.worldShader.enableLighting()
        VoxelGameClient.instance.worldShader.enableTexturing()
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
            fpsRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, fpsStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            int acrt = 0
            if (game.numChunkRenders > 0) {
                acrt = (int) (game.chunkRenderTimes / game.numChunkRenders)
            }
            String lcrtStr = "AWRT: $acrt ns"
            awrtRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, lcrtStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            DecimalFormat posFormat = new DecimalFormat("#.00");
            String coordsStr = String.format("(x,y,z): %s,%s,%s",
                    posFormat.format(game.player.position.x),
                    posFormat.format(game.player.position.y),
                    posFormat.format(game.player.position.z))
            positionRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, coordsStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            String chunk = String.format("Chunk (x,z): %s,%s",
                    game.world.getChunkPosition(game.player.position.x),
                    game.world.getChunkPosition(game.player.position.z))
            chunkposRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, chunk, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            String headingStr = String.format("(yaw,pitch): %s,%s",
                    posFormat.format(game.player.rotation.yaw),
                    posFormat.format(game.player.rotation.pitch))
            headingRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, headingStr, FontRenderer.ALIGN_RIGHT)
            currentHeight += debugTextRenderer.getLineHeight()

            Vec3i playerPosVec = new Vec3i(Math.floor(game.player.position.x) as int, Math.floor(game.player.position.y) as int, Math.floor(game.player.position.z) as int)
            IChunk playerChunk = game.world.getChunkAtPosition(playerPosVec)
            if (playerChunk != null) {
                String llStr = String.format("Light Level @ Feet: " + playerChunk.getSunlight(playerPosVec.x, playerPosVec.y, playerPosVec.z))
                lightlevelRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, llStr, FontRenderer.ALIGN_RIGHT)
                currentHeight += debugTextRenderer.getLineHeight()
            }

            String threadsStr = "Active threads: " + Thread.activeCount()
            activeThreadsRender = debugTextRenderer.drawStringStatic(Gdx.graphics.getWidth(), currentHeight, threadsStr, FontRenderer.ALIGN_RIGHT)
        }
    }

}

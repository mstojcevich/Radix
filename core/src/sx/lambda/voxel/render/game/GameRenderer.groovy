package sx.lambda.voxel.render.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Frustum
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.events.render.EventEntityRender
import sx.lambda.voxel.api.events.render.EventPostWorldRender
import sx.lambda.voxel.render.Renderer
import sx.lambda.voxel.util.Vec3i
import sx.lambda.voxel.world.chunk.IChunk
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.entity.Entity
import sx.lambda.voxel.util.gl.FrameBuffer

import java.text.DecimalFormat

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
class GameRenderer implements Renderer {

    private final VoxelGameClient game
    private FrameBuffer postProcessFbo
    private BitmapFont debugTextRenderer

    private SpriteBatch batch

    private boolean initted = false, fontRenderReady = true

    private boolean calcFrustum

    private long lastDynamicTextRerenderMS = 0

    private Frustum frustum

    private GlyphLayout fpsRender, glInfoRender, positionRender, headingRender, chunkposRender, awrtRender, lightlevelRender, activeThreadsRender

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

        if(fontRenderReady) {
            batch.begin()
            float currentHeight = 2
            if(glInfoRender != null) {
                debugTextRenderer.draw(batch, glInfoRender, Gdx.graphics.width-glInfoRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }

            if(fpsRender != null) {
                debugTextRenderer.draw(batch, fpsRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(positionRender != null) {
                debugTextRenderer.draw(batch, positionRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(headingRender != null) {
                debugTextRenderer.draw(batch, headingRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(chunkposRender != null) {
                debugTextRenderer.draw(batch, chunkposRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(awrtRender != null) {
                debugTextRenderer.draw(batch, awrtRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(lightlevelRender != null) {
                debugTextRenderer.draw(batch, lightlevelRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            if(activeThreadsRender != null) {
                debugTextRenderer.draw(batch, activeThreadsRender, Gdx.graphics.width-fpsRender.width, currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }
            batch.end()
        }
    }

    @Override
    void cleanup() {
        if(postProcessFbo != null) {
            postProcessFbo.cleanup()
        }
        debugTextRenderer.dispose()

        batch.dispose();

        initted = false
    }

    @Override
    void init() {
        initted = true

        batch = new SpriteBatch()
        batch.setTransformMatrix(VoxelGameClient.instance.hudCamera.combined)

        frustum = game.camera.frustum

        debugTextRenderer = new BitmapFont()

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

    private void createDynamicRenderers() {
        if(fontRenderReady) {
            batch.begin()
            float currentHeight = 2 + debugTextRenderer.getLineHeight() * 1
            // There is 1 text not part of the dynamic texts, offset to make room

            String fpsStr = "FPS: $game.fps"
            fpsRender = debugTextRenderer.draw(batch, fpsStr, Gdx.graphics.getWidth(), currentHeight)
            currentHeight += debugTextRenderer.getLineHeight()

            int acrt = 0
            if (game.numChunkRenders > 0) {
                acrt = (int) (game.chunkRenderTimes / game.numChunkRenders)
            }
            String lcrtStr = "AWRT: $acrt ns"
            awrtRender = debugTextRenderer.draw(batch, lcrtStr, Gdx.graphics.getWidth(), currentHeight)
            currentHeight += debugTextRenderer.getLineHeight()

            DecimalFormat posFormat = new DecimalFormat("#.00");
            String coordsStr = String.format("(x,y,z): %s,%s,%s",
                    posFormat.format(game.player.position.x),
                    posFormat.format(game.player.position.y),
                    posFormat.format(game.player.position.z))
            positionRender = debugTextRenderer.draw(batch, coordsStr, Gdx.graphics.getWidth(), currentHeight)
            currentHeight += debugTextRenderer.getLineHeight()

            String chunk = String.format("Chunk (x,z): %s,%s",
                    game.world.getChunkPosition(game.player.position.x),
                    game.world.getChunkPosition(game.player.position.z))
            chunkposRender = debugTextRenderer.draw(batch, chunk, Gdx.graphics.getWidth(), currentHeight)
            currentHeight += debugTextRenderer.getLineHeight()

            String headingStr = String.format("(yaw,pitch): %s,%s",
                    posFormat.format(game.player.rotation.yaw),
                    posFormat.format(game.player.rotation.pitch))
            headingRender = debugTextRenderer.draw(batch, headingStr, Gdx.graphics.getWidth(), currentHeight)
            currentHeight += debugTextRenderer.getLineHeight()

            Vec3i playerPosVec = new Vec3i(Math.floor(game.player.position.x) as int, Math.floor(game.player.position.y) as int, Math.floor(game.player.position.z) as int)
            IChunk playerChunk = game.world.getChunkAtPosition(playerPosVec)
            if (playerChunk != null) {
                String llStr = String.format("Light Level @ Feet: " + playerChunk.getSunlight(playerPosVec.x, playerPosVec.y, playerPosVec.z))
                lightlevelRender = debugTextRenderer.draw(batch, llStr, Gdx.graphics.getWidth(), currentHeight)
                currentHeight += debugTextRenderer.getLineHeight()
            }

            String threadsStr = "Active threads: " + Thread.activeCount()
            headingRender = debugTextRenderer.draw(batch, threadsStr, Gdx.graphics.getWidth(), currentHeight)
            batch.end()
        }
    }

}

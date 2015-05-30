package sx.lambda.voxel.render.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.BitmapFontCache
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Frustum
import com.badlogic.gdx.math.MathUtils
import groovy.transform.CompileStatic
import sx.lambda.voxel.VoxelGameClient
import sx.lambda.voxel.api.VoxelGameAPI
import sx.lambda.voxel.api.events.render.EventEntityRender
import sx.lambda.voxel.api.events.render.EventPostWorldRender
import sx.lambda.voxel.entity.Entity
import sx.lambda.voxel.render.Renderer
import sx.lambda.voxel.world.chunk.IChunk

import java.text.DecimalFormat

import static com.badlogic.gdx.graphics.GL20.*

@CompileStatic
class GameRenderer implements Renderer {

    private static final float mouseSensitivity = 0.03f //TODO Config - allow changeable mouse sensitivity

    private final VoxelGameClient game
    private BitmapFont debugTextRenderer

    private boolean initted = false

    private boolean calcFrustum

    private long lastDynamicTextRerenderMS = 0

    private Frustum frustum

    private BitmapFontCache fpsRender, glInfoRender, positionRender, headingRender, chunkposRender, awrtRender, lightlevelRender, activeThreadsRender

    public GameRenderer(VoxelGameClient game) {
        this.game = game
    }

    @Override
    void render() {
        if (!initted) init()

        prepareWorldRender()
        game.getWorld().render()
        VoxelGameAPI.instance.eventManager.push(new EventPostWorldRender())
        drawBlockSelection()
        renderEntities()
    }

    void draw2d(SpriteBatch batch) {
        if (!initted) init()

        if (System.currentTimeMillis() - lastDynamicTextRerenderMS >= 1000) { // Rerender the dynamic texts every second
            createDynamicRenderers(batch)
            lastDynamicTextRerenderMS = System.currentTimeMillis()
        }

        glInfoRender.draw(batch)
        fpsRender.draw(batch)
        positionRender.draw(batch)
        headingRender.draw(batch)
        chunkposRender.draw(batch)
        awrtRender.draw(batch)
        lightlevelRender.draw(batch)
        activeThreadsRender.draw(batch)
    }

    @Override
    void cleanup() {
        debugTextRenderer.dispose()

        initted = false
    }

    @Override
    void init() {
        initted = true

        frustum = game.camera.frustum

        debugTextRenderer = new BitmapFont()
        fpsRender = debugTextRenderer.newFontCache()
        awrtRender = debugTextRenderer.newFontCache()
        glInfoRender = debugTextRenderer.newFontCache()
        positionRender = debugTextRenderer.newFontCache()
        headingRender = debugTextRenderer.newFontCache()
        chunkposRender = debugTextRenderer.newFontCache()
        lightlevelRender = debugTextRenderer.newFontCache()
        activeThreadsRender = debugTextRenderer.newFontCache()
    }

    private void prepareWorldRender() {
        if (shouldCalcFrustum()) {
            game.camera.position.set(game.player.position.x, game.player.position.y + game.player.eyeHeight, game.player.position.z)
            game.camera.up.set(0, 1, 0);
            game.camera.direction.set(0, 0, -1);
            game.camera.rotate(game.player.rotation.pitch, 1, 0, 0)
            game.camera.rotate(-game.player.rotation.yaw, 0, 1, 0)

            game.camera.update(true)
            calcFrustum = false
        } else {
            game.camera.update()
        }
    }

    private void drawBlockSelection() {
    }

    private void renderEntities() {
        for (Entity e : game.world.loadedEntities) {
            if (e != null && e != game.player) {
                e.render()
                VoxelGameAPI.instance.eventManager.push(new EventEntityRender(e))
            }
        }
    }

    public void calculateFrustum() {
        this.calcFrustum = true
    }

    public boolean shouldCalcFrustum() { calcFrustum }

    public Frustum getFrustum() { frustum }

    private void createDynamicRenderers(SpriteBatch batch) {
        float currentHeight = 2

        String glInfoStr = String.format("%s (%s) [%s]",
                Gdx.gl.glGetString(GL_RENDERER),
                Gdx.gl.glGetString(GL_VERSION),
                Gdx.gl.glGetString(GL_VENDOR))
        GlyphLayout glGl = glInfoRender.setText(glInfoStr, 0, 0)
        glInfoRender.setPosition(Gdx.graphics.width - glGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        String fpsStr = "FPS: $Gdx.graphics.framesPerSecond"
        GlyphLayout fpsGl = fpsRender.setText(fpsStr, 0, 0)
        fpsRender.setPosition(Gdx.graphics.width - fpsGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        int acrt = 0
        if (game.numChunkRenders > 0) {
            acrt = (int) (game.chunkRenderTimes / game.numChunkRenders)
        }
        String lcrtStr = "AWRT: $acrt ns"
        GlyphLayout awrtGl = awrtRender.setText(lcrtStr, 0, 0)
        awrtRender.setPosition(Gdx.graphics.width - awrtGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        DecimalFormat posFormat = new DecimalFormat("#.00");
        String coordsStr = String.format("(x,y,z): %s,%s,%s",
                posFormat.format(game.player.position.x),
                posFormat.format(game.player.position.y),
                posFormat.format(game.player.position.z))
        GlyphLayout posGl = positionRender.setText(coordsStr, 0, 0)
        positionRender.setPosition(Gdx.graphics.width - posGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        String chunk = String.format("Chunk (x,z): %s,%s",
                game.world.getChunkPosition(game.player.position.x),
                game.world.getChunkPosition(game.player.position.z))
        GlyphLayout chunkGl = chunkposRender.setText(chunk, 0, 0)
        chunkposRender.setPosition(Gdx.graphics.width - chunkGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        String headingStr = String.format("(yaw,pitch): %s,%s",
                posFormat.format(game.player.rotation.yaw),
                posFormat.format(game.player.rotation.pitch))
        GlyphLayout headingGl = headingRender.setText(headingStr, 0, 0)
        headingRender.setPosition(Gdx.graphics.width - headingGl.width, (float) (Gdx.graphics.height - currentHeight))
        currentHeight += debugTextRenderer.getLineHeight()

        int playerX = MathUtils.floor(game.player.position.x);
        int playerZ = MathUtils.floor(game.player.position.z);
        IChunk playerChunk = game.world.getChunkAtPosition(playerX, playerZ)
        if (playerChunk != null) {
            String llStr = String.format("Light Level @ Feet: "
                    + playerChunk.getSunlight(playerX, MathUtils.floor(game.player.position.y), playerZ))
            GlyphLayout llGl = lightlevelRender.setText(llStr, 0, 0)
            lightlevelRender.setPosition(Gdx.graphics.width - llGl.width, (float) (Gdx.graphics.height - currentHeight))
            currentHeight += debugTextRenderer.getLineHeight()
        }

        String threadsStr = "Active threads: " + Thread.activeCount()
        GlyphLayout threadsGl = activeThreadsRender.setText(threadsStr, 0, 0)
        activeThreadsRender.setPosition(Gdx.graphics.width - threadsGl.width, (float) (Gdx.graphics.height - currentHeight))
    }

}

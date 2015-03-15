package sx.lambda.mstojcevich.voxel.render.game

import groovy.transform.CompileStatic
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import org.newdawn.slick.Color
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.api.VoxelGameAPI
import sx.lambda.mstojcevich.voxel.api.events.render.EventEntityRender
import sx.lambda.mstojcevich.voxel.api.events.render.EventPostWorldRender
import sx.lambda.mstojcevich.voxel.render.Renderer
import sx.lambda.mstojcevich.voxel.entity.Entity
import sx.lambda.mstojcevich.voxel.util.gl.FrameBuffer

import static org.lwjgl.opengl.GL11.*

@CompileStatic
class GameRenderer implements Renderer {

    private final VoxelGame game
    private int sphereList = -1
    private FrameBuffer postProcessFbo;

    public GameRenderer(VoxelGame game) {
        this.game = game
    }

    @Override
    void render() {
        prepareWorldRender()
        game.getWorld().render()
        VoxelGameAPI.instance.eventManager.push(new EventPostWorldRender())
        glPushMatrix()
        drawBlockSelection()
        glPopMatrix()
        renderEntities()
    }

    void draw2d() {
        //postProcessFbo.drawTexture(VoxelGame.instance.textureManager)
    }

    @Override
    void init() {
        sphereList = glGenLists(1)
        glNewList(sphereList, GL_COMPILE)
        Sphere sp = new Sphere()
        sp.setDrawStyle GLU.GLU_SILHOUETTE
        sp.draw(0.5f, 50, 50)
        glEndList()

        postProcessFbo = new FrameBuffer()
    }

    private void prepareWorldRender() {
        glRotatef(-game.getPlayer().getRotation().getPitch(), 1, 0, 0)
        glRotatef(game.getPlayer().getRotation().getYaw(), 0, 1, 0)
        glTranslatef(-game.getPlayer().getPosition().getX(), (float) -(game.getPlayer().getPosition().getY() + game.getPlayer().getEyeHeight()), -game.getPlayer().getPosition().getZ())
        glLight(GL_LIGHT0, GL_POSITION, game.getLightPosition())

        if (game.shouldCalcFrustum()) {
            game.dontCalcFrustum()
            game.getFrustum().calculateFrustum()
        }
    }

    private void drawBlockSelection() {
        if (game.getSelectedBlock() != null) {
            glDisable GL_DEPTH_TEST
            VoxelGame.instance.shaderManager.disableTexturing()
            VoxelGame.instance.shaderManager.disableLighting()
            if (game.getSelectedBlock() != null) {
                glTranslatef((float) (game.getSelectedBlock().x + 0.5f), (float) (game.getSelectedBlock().y + 0.5f), (float) (game.getSelectedBlock().z + 0.5f))
                glRotatef((float) ((System.currentTimeMillis() % 720) / 2f), 1, 1, 1)
                try {
                    Color incr = new Color(java.awt.Color.HSBtoRGB((float)((((int)(System.currentTimeMillis()*0.1)) % 360) / 360.0f), 1, 1));
                    glColor3f(incr.r, incr.g, incr.b)
                } catch (Exception e) {
                    e.printStackTrace()
                }
                glCallList(sphereList)
            }
            VoxelGame.instance.shaderManager.enableLighting()
            VoxelGame.instance.shaderManager.enableTexturing()
            glEnable GL_DEPTH_TEST
        }
    }

    private void renderEntities() {
        VoxelGame.instance.shaderManager.disableTexturing()
        VoxelGame.instance.shaderManager.disableLighting()
        glColor3f(1, 1, 1)
        for(Entity e : game.world.loadedEntities) {
            if(e != null && e != game.player) {
                e.render()
                VoxelGameAPI.instance.eventManager.push(new EventEntityRender(e))
            }
        }
        VoxelGame.instance.shaderManager.enableLighting()
        VoxelGame.instance.shaderManager.enableTexturing()
    }

}

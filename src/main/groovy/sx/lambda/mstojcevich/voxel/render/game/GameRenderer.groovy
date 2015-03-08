package sx.lambda.mstojcevich.voxel.render.game

import groovy.transform.CompileStatic
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import org.newdawn.slick.Color
import sx.lambda.mstojcevich.voxel.VoxelGame
import sx.lambda.mstojcevich.voxel.render.Renderer
import sx.lambda.mstojcevich.voxel.entity.Entity

import static org.lwjgl.opengl.GL11.*

@CompileStatic
class GameRenderer implements Renderer {

    private final VoxelGame game
    private int sphereList = -1

    public GameRenderer(VoxelGame game) {
        this.game = game
    }

    @Override
    void render() {
        prepareWorldRender()
        game.getWorld().render()
        glPushMatrix()
        drawBlockSelection()
        glPopMatrix()
        renderEntities()
    }

    @Override
    void init() {
        sphereList = glGenLists(1)
        glNewList(sphereList, GL_COMPILE)
        Sphere sp = new Sphere()
        sp.setDrawStyle GLU.GLU_SILHOUETTE
        sp.draw(0.5f, 50, 50)
        glEndList()
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
            glDisable GL_TEXTURE_2D
            glDisable GL_LIGHTING
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
            glEnable GL_LIGHTING
            glEnable GL_TEXTURE_2D
            glEnable GL_DEPTH_TEST
        }
    }

    private void renderEntities() {
        glDisable GL_TEXTURE_2D
        glDisable GL_LIGHTING
        glColor3f(1, 1, 1)
        for(Entity e : game.world.loadedEntities) {
            if(e != null && e != game.player) {
                e.render()
            }
        }
        glEnable GL_LIGHTING
        glEnable GL_TEXTURE_2D
    }

}

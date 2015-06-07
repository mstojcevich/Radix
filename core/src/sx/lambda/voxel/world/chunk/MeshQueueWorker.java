package sx.lambda.voxel.world.chunk;

import com.badlogic.gdx.Gdx;

import java.util.Queue;

/**
 * Meshes chunks off of the main thread
 */
public class MeshQueueWorker extends Thread {

    private final Queue<Runnable> meshQueue;

    public MeshQueueWorker(Queue<Runnable> meshQueue) {
        super("Mesh Queue Worker");
        this.meshQueue = meshQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                while (!meshQueue.isEmpty()) {
                    meshQueue.poll().run();
                }

                Gdx.graphics.requestRendering();

                synchronized (meshQueue) {
                    meshQueue.wait();
                }
            }
        } catch (InterruptedException e) {}
    }

}

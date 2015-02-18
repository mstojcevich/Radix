package sx.lambda.mstojcevich.voxel.tasks

import groovy.transform.CompileStatic

@CompileStatic
public interface RepeatedTask extends Runnable {

    public String getIdentifier()

}
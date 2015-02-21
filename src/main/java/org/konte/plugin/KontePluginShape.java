/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.plugin;

import org.konte.lang.Language;
import org.konte.lang.UtBuilder;
import org.konte.misc.Matrix4;

/**
 *
 * @author pt
 */
public abstract class KontePluginShape {

    protected UtBuilder builder = UtBuilder.getUtBuilder();

    public KontePluginShape() throws Exception {
        builder.name(this.getName());

        for (int i=0; i < getShapes().length; i++) {
            builder.addShape(getShapes()[i]);
            if (isCurved()) 
                builder.addControlPoints(getControlPoints()[i]);
        }
        Language.addUntransformable(builder.build());
    }
    public abstract String getName();
    public abstract Matrix4[][] getShapes();
    public abstract boolean isCurved();
    public abstract Matrix4[][][] getControlPoints();
    
}



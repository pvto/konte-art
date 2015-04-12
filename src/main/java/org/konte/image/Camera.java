
package org.konte.image;

import org.konte.misc.Matrix4;
import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Transform;
import org.konte.parse.ParseException;


/**
 *
 * @author pvto
 */
public interface Camera {

    public Point2 mapTo2D(Vector3 v);
    
    public Vector3 getTarget();
    public float distMetric(Matrix4 matrix);
    public Vector3 getPosition();
    public void setPosition(Transform pos) throws ParseException;
    public String getName();
    public void setName(String name);
    
    public void setCanvas(Canvas canvas);

    public void lookat(Vector3 vector3);
    
}

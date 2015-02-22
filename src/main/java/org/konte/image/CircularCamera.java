
package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author pvto
 */
public class CircularCamera extends SimpleCamera {

    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = Vector3.sub(v, position);
        d = cameraRotationMatrix.multiply(d);
        float alfa = (float)Math.atan2(d.x, d.y);
        if (d.z <= 0f)
        {
            return new Point2(0f,0f);
        }
        float dist = 1f / d.z;
        return new Point2((float)Math.cos(alfa) * dist, (float)Math.sin(alfa) * dist);
    }
}

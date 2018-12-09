
package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author pvto
 */
public class StereographicCamera extends SimpleCamera {

    public float scale = 1f;

    public StereographicCamera(float scale) {
        this.scale = scale;
    }
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = Vector3.sub(v, position);
        d = cameraRotationMatrix.multiply(d);
        if (d.z >= 2f) {
            return new Point2(d.x * 1000f, d.y * 1000f);
        }
        float divisor = (2f - d.z) * scale;
        return new Point2(0.5f * d.x / divisor, 0.5f * d.y / divisor);
    }
}

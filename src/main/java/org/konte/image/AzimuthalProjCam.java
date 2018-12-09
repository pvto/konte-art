
package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 *
 * @author pvto
 */
public class AzimuthalProjCam extends SimpleCamera {

    public float scale = 1f;

    public AzimuthalProjCam(float scale) {
        this.scale = scale;
    }
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = Vector3.sub(v, position);
        d = cameraRotationMatrix.multiply(d);
        float longitude = (float)Math.atan2(d.y, d.x);
        float lat = d.z;
        float roo = lat / (float)Math.PI;
        float theta = longitude;
        return new Point2(roo * (float)Math.cos(theta) * scale, roo * (float)Math.sin(theta) * scale);
    }
}

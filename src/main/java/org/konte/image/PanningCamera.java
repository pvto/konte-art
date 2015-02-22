
package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Transform;
import org.konte.parse.ParseException;



/**
 *
 * @author pvto
 */
public class PanningCamera extends SimpleCamera {

    Vector3 moveDirection;
    Vector3 tmpv;
    private float h;
    private float w;
    private float threshold = 0.45f;
    private float panningBase = -0.9f + 2.1f;
    Vector3 tmp2 = new Vector3();

    public PanningCamera() {
        super();
    }

    public PanningCamera(Transform position) throws ParseException {
        super(position);
    }

    public void setBoundaryThreshold(float threshold) {
        this.threshold = Math.abs(threshold);
        panningBase = 2.1f - 2f*threshold;

    }

    @Override
    public void setCanvas(Canvas canvas) {
        super.setCanvas(canvas);
        h = canvas.getHeight() / 2;
        w = canvas.getWidth() / 2;
    }
    
    @Override
    public void setPosition(Transform posT) throws ParseException {
        super.setPosition(posT);
        float unitL = 1/(float)Math.sqrt(3);
        moveDirection = new Vector3();
        moveDirection = this.cameraRotationMatrix.multiply(new Vector3(0f,0f,-unitL));
        System.out.println("PAN " + getName() + " moveDir: " + moveDirection);
    }

    float prevest = 0f;
    @Override
    public Point2 mapTo2D(Vector3 v) {
        Point2 p = super.mapTo2D(v);
        if (p.x >= -threshold && p.x <= threshold && p.y >= -threshold && p.y <= threshold)
            return p;
        // if tmp out of bounds, move backward 
        int i = 0;
        while (i++ < 100) {
            float estimate = Math.abs(p.x) + Math.abs(p.y) + panningBase;
            if (Float.isNaN(estimate) || Float.isInfinite(estimate)) {
                return p;
            }
            tmpv = moveDirection.mul(estimate);
            Vector3.add(position, tmpv, tmp2);
            position.set(tmp2);
//            System.out.println("PAN pos: " + tmp2 + " - est. " + estimate);
            p = super.mapTo2D(v);
            if (p.x >= -threshold && p.x <= threshold && p.y >= -threshold && p.y <= threshold)
                return p;
            if (prevest == estimate)    // prevent eternal panning if movement is asymptotic
                return p;
            prevest = estimate;
        }
        return p;
    }

    @Override
    public String toString() {
        return super.toString() + " +PANNING-THR-" + threshold;
    }



    
}

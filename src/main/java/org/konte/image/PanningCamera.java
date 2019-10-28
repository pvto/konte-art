
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
    Vector3 tmpv = new Vector3();
    private float h;
    private float w;
    private float threshold = 0.45f;
    private float panningBase = -0.9f + 2.1f;
    Vector3 tmp2 = new Vector3();

    @Override public float primingRate() {
        return 1f;
    }

    public PanningCamera()
    {
        super();
    }

    public PanningCamera(Transform position) throws ParseException
    {
        super(position);
    }

    public void setBoundaryThreshold(float threshold)
    {
        this.threshold = Math.abs(threshold);
        panningBase = 2.1f - 2f*threshold;

    }

    @Override
    public void setCanvas(Canvas canvas)
    {
        super.setCanvas(canvas);
        setWh();
    }

    private void setWh() {
        h = canvas.getHeight() / 2f / (float)canvas.getWidth();
        w = 0.5f;
    }

    private int iii = 0;

    @Override
    public void setPosition(Transform posT) throws ParseException
    {
        super.setPosition(posT);
        moveDirection = Vector3.sub(position, target).normalize();
    }

    private boolean inBounds(Point2 p) {
        if (Math.abs(p.x) > w) return false;
        if (Math.abs(p.y) > h) return false;
        return true;
    }
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        if (w == 0)
            setWh();
        Point2 p = super.mapTo2D(v);
        if (inBounds(p)) {
            return p;
        }
        // if tmp out of bounds, move backward
        // iterate linear estimate

        int i = 0;
        while (i++ < 20)
        {
            float maxd = (float)Math.max(
                Math.abs(p.x) - w,
                Math.abs(p.y) - h
            );
            float estimate = maxd / 100f / w;
            if (Float.isNaN(estimate) || Float.isInfinite(estimate)) {
                return p;
            }
            estimate = (float)Math.max(estimate, 0.05f);

            tmpv = moveDirection.mul((float)Math.sqrt(estimate), tmpv);
            Vector3.add(position, tmpv, tmp2);
            position.set(tmp2);
            //System.out.println("PAN pos: " + tmp2 + " p= " + p + " - est. " + estimate + " d=" + tmpv + " md=" + moveDirection);
            p = super.mapTo2D(v);
            if (inBounds(p))
                return p;
        }
        return p;
    }

    @Override
    public String toString()
    {
        return super.toString() + " +PANNING-THR-" + threshold;
    }




}

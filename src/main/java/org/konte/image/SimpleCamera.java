
package org.konte.image;

import org.konte.misc.Matrix3;
import org.konte.misc.Matrix4;
import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.DrawingContext;
import org.konte.model.Transform;
import org.konte.model.TransformModifier;
import org.konte.parse.ParseException;
import org.konte.generate.Runtime;
import org.konte.misc.Matrix4Red;
import org.konte.model.systems.DistanceMetric;

/**
 *
 * @author pvto
 */
public class SimpleCamera implements Camera {

    protected float viewerDfromCamera = 1;
    protected Matrix3 cameraRotationMatrix;
    protected Vector3 position;
    protected Vector3 target;

    protected String name;
    protected Canvas canvas;
    protected DistanceMetric metric;

    public SimpleCamera() {}

    public SimpleCamera(Transform pos) throws ParseException
    {
        setPosition(pos);
    }
    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        Vector3 d = cameraRotationMatrix.multiply(Vector3.sub(v, position));
        if (d.z >= 1f)
            return new Point2(d.x / d.z, d.y / d.z);
        float fct = 2f-d.z;
        return new Point2(d.x*fct, d.y*fct);
        // do something to this

    }
    @Override
    public String getName()
    {
        return (name == null ? "" : name);
    }
    @Override
    public void setName(String name)
    {
        this.name = name;
    }
    private static final float toRad = 3.14159265f / 180f ;
    @Override
    public void setPosition(Transform posT) throws ParseException
    {
        DrawingContext pos = posT.transform(DrawingContext.ZERO);
        position = new Vector3(pos.getx(),-pos.gety(),pos.getz());
        float rx = 0f;
        float ry = 0f;
        float rz = 0f;
        if (target != null)
        {
            Vector3 iniPos = this.initialRotation();
            rx = iniPos.x;
            ry = iniPos.y;
            rz = iniPos.z;
            Runtime.sysoutln("fov " + this.getName() + " initial dir: " + iniPos, 10);
        }

        for(TransformModifier tr : posT.acqTrs)
        {
            if (tr instanceof TransformModifier.rx)
                rx += tr.evaluateAll()[0] * toRad;
            else
            if (tr instanceof TransformModifier.ry)
                ry += tr.evaluateAll()[0] * toRad;
            else
            if (tr instanceof TransformModifier.rz)
                rz += tr.evaluateAll()[0] * toRad;

        }
        Matrix3 zrotm = Matrix3.rotation(0,0,rz);
        Matrix3 xyrotm = Matrix3.rotation(rx,ry,0);
        cameraRotationMatrix = new Matrix3();
        Matrix3.multiply(zrotm, xyrotm, cameraRotationMatrix);
        target = cameraRotationMatrix.multiply(new Vector3(0f,0f,-1f));
    }

    @Override
    public Vector3 getPosition()
    {
        return position;
    }

    public Matrix3 getCameraRotationMatrix()
    {
        return cameraRotationMatrix;
    }

    @Override
    public void setCanvas(Canvas canvas)
    {
        this.canvas = canvas;
    }

//    public float distMetric(Matrix4 matrix)
//    {
//        float xs = (position.x-matrix.m03);
//        float ys = (position.y-matrix.m13);
//        float zs = (position.z-matrix.m23);
//        return xs*xs+ys*ys+zs*zs;
//    }

    @Override
    public void setDistanceMetric(DistanceMetric metric) {
        this.metric = metric;
    }

    private Vector3 v1 = new Vector3();
    private Vector3 v2 = new Vector3();
    @Override
    public float distMetric(Matrix4Red matrix)
    {
        if (metric != null) {
            v1.x = matrix.m03;
            v1.y = -matrix.m13;
            v1.z = matrix.m23;
            v2.x = position.x;
            v2.y = position.y;
            v2.z = position.z;
            return metric.distance(v1, v2);
        }
        float xs = (position.x-matrix.m03);
        float ys = (position.y+matrix.m13);
        float zs = (position.z-matrix.m23);
        return xs*xs+ys*ys+zs*zs;
    }

    public Vector3 getTarget()
    {
        return target;
    }

    @Override
    public void lookat(Vector3 target)
    {
        setTarget(target);
    }

    public void setTarget(Vector3 trgt)
    {
        target = trgt;
    }

    protected Vector3 initialRotation() {
        Vector3 diff = Vector3.sub(target, position);
        float rx = 0f;
        if (diff.y == 0f)
        {
            rx = 0;
        }
        else
        {
            rx = -(float)( Math.atan2(-diff.y, diff.z) );
        }
        float ry = 0f;
        if (diff.x == 0f)
        {

            if ( diff.z < 0f)
                ry = (float)Math.PI;
            else
                ry = 0;
        }
        else
        {
            ry = -(float)( Math.atan2(diff.x, diff.z) );
        }
        float rz = 0f;
        return new Vector3(rx, ry, rz);
    }


    @Override
    public String toString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append(getName()).append(" [").append(position).append("] R*\n").
                append(cameraRotationMatrix);
        return bd.toString();
    }


}

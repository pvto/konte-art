
package org.konte.parse;

import org.konte.expression.Expression;
import org.konte.expression.ExpressionFunction;
import org.konte.expression.Value;
import org.konte.image.*;
import org.konte.lang.CameraProperties;
import org.konte.lang.Language;
import org.konte.misc.Vector3;
import org.konte.model.Transform;
import org.konte.model.TransformModifier;

import java.util.ArrayList;
import java.util.List;


public class CameraBuilder {

    private String name;
    private Transform position;
    private final List<CameraProperties> properties = new ArrayList<>();
    private final List<Object> extra = new ArrayList<>();
    public ExpressionFunction lookat;

    public CameraBuilder() { }

    public CameraBuilder(Transform position)
    {
        setPosition(position);
    }



    public CameraBuilder setDefault() throws ParseException
    {
        Transform camPos = new Transform(0,0);
        ArrayList<Expression> exp = new ArrayList<>();
        exp.add(new Value(-1f));
        camPos.setShapeTransform(Language.z, exp);

        return this.setPosition(camPos).setName("MAIN");
    }

    public CameraBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getName()
    {
        return name;
    }


    public CameraBuilder setPosition(Transform position)
    {
        this.position = position;
        return this;
    }

    public CameraBuilder addProperty(CameraProperties prop)
    {
        properties.add(prop);
        return this;
    }

    public Camera build() throws ParseException
    {
        Camera c = null;
        int flag = 0;
        for (CameraProperties p : properties)
        {
            if (flag >= 16)
            {
                throw new ParseException("Inconsistent camera properties: " + properties);
            }

            switch (p)
            {
                case PANNING:
                    flag |= 16;
                    break;
                case ORTOGRAPHIC:
                    flag |= 32;
                    break;
                case CIRCULAR:
                    flag |= 64;
                    break;
                case CABINET:
                    flag |= 128;
                    break;
                case FISH:
                    flag |= 256;
                    break;
                case FISHEYE:
                    flag |= 512;
                    break;
                case ZPOW:
                    flag |= 1024;
                    break;
                case BEZIER2:
                    flag |= 2048;
                    break;
                case STEREOGRAPHIC:
                    flag |= 4096;
                    break;
            }
        }

        if ((flag & 4096) != 0)
        {
            float exp[] = { 1f };
            int step = 0;
            for(Object o : extra)
            {
                if (step > 1)
                    throw new ParseException("too many arguments ("+extra.size()+") to STEREOGRAPHIC camera(scale) - " + o);
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        exp[step++] = tmp;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new ParseException("can't evaluate STEREOGRAPHIC f: " + e.getMessage());
                    }
                }
            }
            c = new StereographicCamera(exp[0]);
        }
        else if ((flag & 2048) != 0)
        {
            float w = 1f, bb = 0.4f;
            float exp[] = new float[] {
                bb,-w, 0,-w, -bb,-w,
                -w,-bb, -w,0, -w,bb,
                -bb,w, 0,w, bb,w,
                w,bb, w,0, w,-bb,
                3f, 1f
            };
            int step = 0;
            for(Object o : extra)
            {
                if (step > 25)
                    throw new ParseException("too many arguments ("+extra.size()+") to BEZIER2 camera(cx,cxy,x1,y1,cx,cy, cx,cy,x2,y2,cx,cy, xc,xy,x3,y3,cx,cy, xc,yc,x4,y4,xc,yc, ease,baseform) - " + o);
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        exp[step++] = tmp;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new ParseException("can't evaluate BEZIER2 f: " + e.getMessage());
                    }
                }
            }
            c = new Bezier2Camera(exp[0], exp[1], exp[2], exp[3], exp[4], exp[5],
                    exp[6], exp[7], exp[8], exp[9], exp[10], exp[11],
                    exp[12], exp[13], exp[14], exp[15], exp[16], exp[17],
                    exp[18], exp[19], exp[20], exp[21], exp[22], exp[23],
                    exp[24], exp[25]
            );
        }
        else if ((flag & 1024) != 0) {
            float exp[] = {2f, 0f, 0f};
            int step = 0;
            for(Object o : extra)
            {
                if (step > 2)
                    throw new ParseException("too many arguments ("+extra.size()+") to POW camera(z-exponent, x_tr, y_tr) - " + o);
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        exp[step++] = tmp;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new ParseException("can't evaluate ZPOW f: " + e.getMessage());
                    }
                }
            }
            c = new ZPowCamera(exp[0], exp[1], exp[2]);
        }
        else if ((flag & 512) != 0) {
            float exp[] = {0.5f, 0f, 1f, 1f, 0f, 0f};
            int step = 0;
            for(Object o : extra)
            {
                if (step > 5)
                    throw new ParseException("too many arguments ("+extra.size()+") to camera{FISHEYE f, type{0,1,2,3}, opticalBlindSpotDist, r_exp, x_tr, y_tr}");
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        exp[step++] = tmp;
                    }
                    catch (Exception e)
                    {
                        throw new ParseException("can't evaluate FISHEYE f: " + e.getMessage());
                    }
                }
            }
            c = new FishLensCamera(exp[0], exp[1], exp[2], exp[3], exp[4], exp[5]);
        }
        else if ((flag & 256) != 0) {
            float exp[] = {0.5f, 1f, 2f};
            int step = 0;
            for(Object o : extra)
            {
                if (step > 2)
                    throw new ParseException("too many arguments ("+extra.size()+") to FISH camera");
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        exp[step++] = tmp;
                    }
                    catch (Exception e)
                    {
                        throw new ParseException("can't evaluate Fish curvature: " + e.getMessage());
                    }
                }
            }
            c = new FishCamera(exp[0], exp[1], exp[2]);
        }
        else if ((flag & 128) != 0)
        {
            float angle = (float)Math.PI / 6f;
            float zContraction = 0.5f;
            int step = 0;
            for(Object o : extra)
            {
                if (step > 2)
                    throw new ParseException("too many arguments to CABINET camera: " + extra.size());
                if (o instanceof Expression)
                {
                    try
                    {
                        float tmp = ((Expression)o).evaluate();
                        if (step == 0)
                        {
                            angle = tmp / 180f * (float)Math.PI;
                        }
                        else if (step == 1)
                        {
                            zContraction = tmp;
                        }
                        step++;
                    }
                    catch (Exception e)
                    {
                        throw new ParseException("can't evaluate Cabinet perspective angle: " + e.getMessage());
                    }
                }
            }
            c = new CabinetCamera(angle, zContraction);
        }
        else if ((flag & 64) != 0)
        {
            float exp = 1f;
            if (extra.size() > 1)
            {
                throw new ParseException("too many arguments to CIRCULAR camera: " + extra.size());
            }
            if (extra.size() > 0)
            {
                exp = ((Expression)extra.get(0)).evaluate();
            }
            c = new CircularCamera(exp);
        }
        else if ((flag & 32) != 0)
        {
            c = new OrtographicCamera();
        }
        else if ((flag & 16) != 0)
        {
            c = new PanningCamera();
            for(Object o : extra)
            {
                if (o instanceof Expression)
                {
                    try
                    {
                        float threshold = ((Expression)o).evaluate();
                        ((PanningCamera)c).setBoundaryThreshold(threshold);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        else
        {
            c = new SimpleCamera();
        }

        if (!position.hasTransformType(TransformModifier.z.class))
        {
            ArrayList<Expression> tmp = new ArrayList<Expression>();
            if (c instanceof CabinetCamera)
            {
                tmp.add(new Value(-0f));
            }
            else
            {
                tmp.add(new Value(-1f));
            }
            position.setShapeTransform(Language.z, tmp);
        }

        position.getTransformMatrix();

        c.setName(name);

        if (lookat != null)
        {
            Expression[] A = lookat.getArgs();
            c.lookat(new Vector3(A[0].evaluate(), A[1].evaluate(), A[2].evaluate()));
        }

        c.setPosition(position);

        return c;
    }

    void addExtra(Expression value)
    {
        extra.add(value);
    }
}

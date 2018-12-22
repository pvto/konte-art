
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


    private float[] evalExps(float[] base) throws ParseException
    {
        return evalExps(base, "[" + base.length + "]");
    }

    private float[] evalExps(float[] base, String argNames) throws ParseException
    {
        CameraProperties p = properties.size() > 0 ? properties.get(0) : CameraProperties.SIMPLE;
        int step = 0;
        for(Object o : extra)
        {
            if (step > base.length)
                throw new ParseException("too many arguments ("+extra.size()+") to " + p +" camera("+argNames+") - " + o);
            if (o instanceof Expression)
            {
                try
                {
                    float tmp = ((Expression)o).evaluate();
                    base[step++] = tmp;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new ParseException("can't evaluate " + p + " f: " + e.getMessage());
                }
            }
        }
        return base;
    }

    public Camera build() throws ParseException
    {
        Camera c = null;
        int flag = 0;
        CameraProperties CM = CameraProperties.SIMPLE;
        for (CameraProperties p : properties)
        {
            if (flag >= CameraProperties.PANNING.ENC)
            {
                throw new ParseException("Inconsistent camera properties: " + properties);
            }

            flag |= p.ENC;
            CM = p;
        }

        if (CM == CameraProperties.AZIMUTHAL)
        {
            c = new AzimuthalProjCam(evalExps(new float[]{ 1f }, "scale-factor")[0]);
        }
        else if (CM == CameraProperties.STEREOGRAPHIC)
        {
            c = new StereographicCamera(evalExps(new float[]{1f}, "scale-factor")[0]);
        }
        else if (CM == CameraProperties.BEZIER2)
        {
            float w = 1f, bb = 0.4f;
            float exp[] = new float[] {
                bb,-w, 0,-w, -bb,-w,
                -w,-bb, -w,0, -w,bb,
                -bb,w, 0,w, bb,w,
                w,bb, w,0, w,-bb,
                3f, 1f
            };
            evalExps(exp, "cx,cxy,x1,y1,cx,cy, cx,cy,x2,y2,cx,cy, xc,xy,x3,y3,cx,cy, xc,yc,x4,y4,xc,yc, ease,baseform");
            c = new Bezier2Camera(exp[0], exp[1], exp[2], exp[3], exp[4], exp[5],
                    exp[6], exp[7], exp[8], exp[9], exp[10], exp[11],
                    exp[12], exp[13], exp[14], exp[15], exp[16], exp[17],
                    exp[18], exp[19], exp[20], exp[21], exp[22], exp[23],
                    exp[24], exp[25]
            );
        }
        else if (CM == CameraProperties.ZPOW) {
            float[] exp = {2f, 0f, 0f};
            evalExps(exp, "z-exponent, x_tr, y_tr");
            c = new ZPowCamera(exp[0], exp[1], exp[2]);
        }
        else if (CM == CameraProperties.FISHEYE) {
            float exp[] = {0.5f, 0f, 1f, 1f, 0f, 0f};
            evalExps(exp, "f, type{0,1,2,3}, opticalBlindSpotDist, r_exp, x_tr, y_tr");
            c = new FishLensCamera(exp[0], exp[1], exp[2], exp[3], exp[4], exp[5]);
        }
        else if (CM == CameraProperties.FISH) {
            float exp[] = {0.5f, 1f, 2f};
            evalExps(exp);
            c = new FishCamera(exp[0], exp[1], exp[2]);
        }
        else if (CM == CameraProperties.CABINET)
        {
            float[] exp = { (float)Math.PI / 6f, 0.5f };
            evalExps(exp, "angle, zContraction");
            c = new CabinetCamera(exp[0] / 180f * (float)Math.PI, exp[1]);
        }
        else if (CM == CameraProperties.CIRCULAR)
        {
            c = new CircularCamera(evalExps(new float[]{1f})[0]);
        }
        else if (CM == CameraProperties.ORTOGRAPHIC)
        {
            c = new OrtographicCamera();
        }
        else if ((flag & CameraProperties.PANNING.ENC) != 0)
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

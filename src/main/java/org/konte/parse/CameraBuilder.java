
package org.konte.parse;

import java.util.ArrayList;
import java.util.List;
import org.konte.expression.Expression;
import org.konte.expression.ExpressionFunction;
import org.konte.expression.Value;
import org.konte.image.CabinetCamera;
import org.konte.image.Camera;
import org.konte.image.CircularCamera;
import org.konte.image.OrtographicCamera;
import org.konte.image.PanningCamera;
import org.konte.image.SimpleCamera;
import org.konte.lang.Language;
import org.konte.model.Transform;
import org.konte.model.TransformModifier;
import org.konte.lang.CameraProperties;
import org.konte.misc.Vector3;


public class CameraBuilder {

    private String name;
    private Transform position;
    private final List<CameraProperties> properties = new ArrayList<>();
    private final List<Object> extra = new ArrayList<>();

    public CameraBuilder() { }

    public CameraBuilder(Transform position)
    {
        setPosition(position);
    }


    
    public CameraBuilder setDefault() throws ParseException
    {
        Transform camPos = new Transform();
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
                   
            }
        }
        
        if ((flag & 128) != 0)
        {
            if (extra.size() > 2)
            {
                throw new ParseException("too many arguments to CABINET camera: " + extra.size());
            }
            float angle = (float)Math.PI / 6f;
            float zContraction = 0.5f;
            int step = 0;
            for(Object o : extra)
            {
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
        for(int i = 0; i < this.extra.size(); i++)
        {
            Object o = extra.get(i);
            if (o instanceof ExpressionFunction && ((ExpressionFunction)o).getToken() == Language.lookat)
            {
                ExpressionFunction ef = (ExpressionFunction)o;
                Expression[] A = ef.getArgs();
                c.lookat(new Vector3(A[0].evaluate(), A[1].evaluate(), A[2].evaluate()));
            }
        }
        
        c.setPosition(position);

        return c;
    }

    void addExtra(Expression value)
    {
        extra.add(value);
    }
}

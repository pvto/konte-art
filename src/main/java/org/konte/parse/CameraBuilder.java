
package org.konte.parse;

import java.util.ArrayList;
import java.util.List;
import org.konte.expression.Expression;
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

    public String getName() {
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
            float angle = (float)Math.PI / 6f;
            for(Object o : extra)
            {
                if (o instanceof Expression)
                {
                    try
                    {
                        angle = ((Expression)o).evaluate() / 180f * (float)Math.PI;
                    }
                    catch (Exception e)
                    {
                        throw new ParseException("can't evaluate Cabinet perspective angle: " + e.getMessage());
                    }
                }
            }
            c = new CabinetCamera(angle);
        }
        else if ((flag & 64) != 0)
        {
            c = new CircularCamera();
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

        c.setPosition(position);
        c.setName(name);

        return c;
    }

    void addExtra(Expression value)
    {
        extra.add(value);
    }
}

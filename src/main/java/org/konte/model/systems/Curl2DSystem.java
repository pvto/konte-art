package org.konte.model.systems;

import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Curl2DSystem implements GreyBoxSystem {

    GreyBoxSystem noise;
    float eps = 0.00001f;

    private float[] tmp = new float[] { 0f,0f,0f };

    /** returns curl as a polar angle */
    public float curl(float x, float y)
    {
        float n1, n2, a, b;



        tmp[1]=x; tmp[2]=y + eps;    n1 = noise.read(tmp);
        tmp[1]=x; tmp[2]=y - eps;    n2 = noise.read(tmp);
        a = (n1 - n2)/(2 * eps);
        tmp[1]=x + eps; tmp[2]=y;    n1 = noise.read(tmp);
        tmp[1]=x - eps; tmp[2]=y;    n2 = noise.read(tmp);
        b = (n1 - n2)/(2 * eps);

        return (float)Math.atan2(b, a);
    }

    @Override
    public GreyBoxSystem newInstance() {
        return new Curl2DSystem();
    }

    @Override
    public void initialize(Object[] args)
    {
        Model model = (Model) args[args.length - 1];
        int ind = 0;
        try {
            ind = ((Float) args[0]).intValue();
            noise = model.greyBoxSystems.get(ind);
        } catch (Exception ex) {
            throw new RuntimeException("Curl2 creation failed. Referred noise function (" + args[0] + ") not initialised.");
        }
        if (args.length > 2)
        {
            eps = (Float)args[1];
        }
    }

    @Override
    public void evaluate(float[] args) { /*noop*/ }

    @Override
    public float read(float[] args)
    {
        float x = args[1];
        float y = args[2];
        return curl(x, y);
    }

    @Override
    public void write(float[] args) {
        throw new UnsupportedOperationException("Curl model is immutable. Try altering its noise system..");
    }


}

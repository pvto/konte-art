package org.konte.model.systems;

import java.util.ArrayList;
import java.util.List;
import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class Particle3DSystem implements GreyBoxSystem {

    public float timeIncrement = 1f;
    public List<Particle> particles = new ArrayList<Particle>();
    /**  By default, Newton's universal law of gravitation with distance polynomial x²+0x+0 expressed by factors {1,0,0},
     *   yielding  G = (1 / d^2) * m1 * m2 */
    public float[] gravityPolynomial = { 1f, 0f, 0f };
    private float[][] netg;
    public Model model;
    private boolean fullyInitialized = false;
    
    public static class Particle {
        float x, y, z;
        float xv = 0f, yv = 0f, zv = 0f;
        float mass;
    }
    @Override
    public GreyBoxSystem newInstance() { return new Particle2DSystem(); }

    public float
            radius,
            massMin,
            massMax
            ;
    @Override
    public void initialize(Object[] args)
    {
        int n = ((Float) args[0]).intValue();
        radius = (Float) args[1];
        massMin = (Float) args[2];
        massMax = (Float) args[3];
        if (args.length > 5)
        {
            int order = args.length - 5;
            gravityPolynomial = new float[order];
            for(int i = 4; i < args.length - 1; i++)
            {
                gravityPolynomial[i - 4] = (Float) args[i];
            }
        }
        netg = new float[n][3];
        this.model = (Model)args[args.length - 1];
    }

    private void initInternal()
    {
        if (fullyInitialized) 
            return;
        for (int i = 0; i < netg.length; i++)
        {
            float r0 = radius * (float)model.getRandomFeed().get(); //(float) Math.random();
            float angle = (float) (Math.PI * 2.0 * model.getRandomFeed().get()); // Math.random());
            float angle2 = (float) (Math.PI * 2.0 * model.getRandomFeed().get()); // Math.random());
            Particle p = new Particle();
            particles.add(p);
            p.x = (float) Math.cos(angle) * r0;
            p.y = (float) Math.sin(angle) * r0;
            p.z = (float) Math.sin(angle2) * r0;
            p.mass = (massMax - massMin) * (float)model.getRandomFeed().get() + massMin;
        }
        fullyInitialized = true;
    }
    
    @Override
    public void evaluate(float[] args)
    {
        
        initInternal();
        for (int i = 0; i < particles.size(); i++)
        {
            Particle p = particles.get(i);
            
            netg[i][0] = 0f;
            netg[i][1] = 0f;
            netg[i][2] = 0f;

            // apply change in velocity due to attraction to other particles
            
            for (int j = 0; j < particles.size(); j++)
            {
                if (i == j) { continue; }
                
                Particle other = particles.get(j);

                float xd = other.x - p.x;
                float yd = other.y - p.y;
                float zd = other.z - p.z;
                float distance = (float) Math.sqrt(xd*xd + yd*yd + zd*zd);
                float angle = (float) Math.atan2(yd, xd);
                float angle2 = (float) Math.atan2(zd, xd);
                
                float g = 0f;
                int order = gravityPolynomial.length - 1;
                for (int k = 0; k < gravityPolynomial.length; k++, order--)
                {
                    if (gravityPolynomial[k] == 0f)
                        continue;
                    g += gravityPolynomial[k] * (float) Math.pow(distance, order);
                }
                if (g == 0f) { // propel not from the state of equilibrium
                    continue;
                }
                g = 1 / g * other.mass;
                netg[i][0] += (float) Math.cos(angle) * g;
                netg[i][1] += (float) Math.sin(angle) * g;
                netg[i][2] += (float) Math.sin(angle) * g;
            }
            p.xv += timeIncrement * netg[i][0];
            p.yv += timeIncrement * netg[i][1];
            p.zv += timeIncrement * netg[i][2];
        }
        
        for (int i = 0; i < particles.size(); i++)
        {
            Particle p = particles.get(i);
            p.x += p.xv;
            p.y += p.yv;
            p.z += p.zv;
        }
    }

    public static final int 
            X = 1,
            Y = 2,
            Z = 3,
            XV = 10,
            YV = 20,
            ZV = 30,
            MASS = 100,
            TIMEINCR = 0
            ;
    
    @Override
    public float read(float[] args)
    {
        int particle = normalize( (int) args[1] );
        int property = (int) args[2];
        Particle p = particles.get(particle);
        switch (property) {
            case X: return p.x;
            case Y: return p.y;
            case Z: return p.z;
            case XV: return p.xv;
            case YV: return p.yv;
            case ZV: return p.zv;
            case MASS: return p.mass;
            case TIMEINCR: return timeIncrement;
        }
        return 0f;
    }

    public int normalize(int particle)
    {
        return (particle % particles.size() + particles.size()) % particles.size();
    }
    
    @Override
    public void write(float[] args)
    {
        initInternal();
        int particle = normalize( (int) args[1] );
        int property = (int) args[2];
        float value = args[3];
        Particle p = particles.get(particle);
        switch (property) {
            case X: p.x = value;  break;
            case Y: p.y = value;  break;
            case Z: p.z = value;  break;
            case XV: p.xv = value;  break;
            case YV: p.yv = value;  break;
            case ZV: p.zv = value;  break;
            case MASS: p.mass = value;  break;
            case TIMEINCR: timeIncrement = value;  break;
        }
    }

}

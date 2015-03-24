
package org.konte.model;

import org.konte.expression.Expression;
import org.konte.expression.Name;
import org.konte.misc.Mathc3;
import org.konte.misc.Matrix4;
import org.konte.misc.Vector3;
import org.konte.model.ColorSpace.RGBA;

public class PhongLight implements Light {

    private final Expression x;
    private final Expression y;
    private final Expression z;
    private final Expression diffuse;
    private final Expression specular;
    private final Expression alpha;
    private final RGBA rgba;

    public int ambientId = -1,
            diffuseId = -1,
            specularId = -1
            ;
    
    private Matrix4 ORTHOG = new Matrix4(1f,0f,0f,0f, 0f,1f,0f,0f, 0f,0f,1f,1f, 0f,0f,0f,1f);
    Vector3 R = new Vector3();
    private final Expression strength;
    
    @Override
    public float[] lightObject(DrawingContext point) {
        try {
            Matrix4 p = point.matrix;
            Vector3 L = new Vector3(x.evaluate()-p.m03, y.evaluate()-p.m13, z.evaluate()-p.m23).normalize();
            Matrix4 m = p.multiply(ORTHOG);
            Vector3 N = new Vector3(p.m03-m.m03, p.m13-m.m13, p.m23-m.m23).normalize();
            Vector3 R = new Vector3(N).negate();
            Vector3.add(R.mul(2f), L, R);  // perfectly reflected ray; R =  N - (L-N)  =  2 * N - L
            R = R.normalize();
            Model mdl = Name.model;
            Vector3 fov = mdl.cameras.get(point.fov).getPosition();
            Vector3 d = new Vector3(fov.x-p.m03, fov.y-p.m13, fov.z-p.m23);
            Vector3 V = d.normalize();

            float ka = Mathc3.bounds1(point.getDef(this.ambientId, 0.5f));
            float kd = Math.max(0f, Math.min(1f-ka, point.getDef(this.diffuseId, 0.5f)));
            float ks = 1f - kd - ka;
            
            float iDiff = this.diffuse.evaluate();
            float iSpec = this.specular.evaluate();
            float alpha = this.alpha.evaluate();
            float iR = rgba.R.evaluate(),// * factor,
                    iG = rgba.G.evaluate(),// * factor,
                    iB = rgba.B.evaluate();// * factor;

            float r = point.getR() * kd * Vector3.dot(L, N) * iR * iDiff
                    + ks * (float)Math.pow(Vector3.dot(R, V), alpha) * iR * iSpec;
            float g = point.getG() * kd * Vector3.dot(L, N) * iG * iDiff 
                    + ks * (float)Math.pow(Vector3.dot(R, V), alpha) * iG * iSpec;
            float b = point.getB() * kd * Vector3.dot(L, N) * iB * iDiff 
                    + ks * (float)Math.pow(Vector3.dot(R, V), alpha) * iB * iSpec;;
            r = Math.max(0f, r);
            g = Math.max(0f, g);
            b = Math.max(0f, b);
            return new float[]{r,g,b,point.getA()};
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return new float[]{1f,1f,1f,1f};
    }
    
    public PhongLight(Expression x, Expression y, Expression z, Expression strength,
            Expression diffuse, Expression specular, Expression alpha,
            RGBA rgba) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.diffuse = diffuse;
        this.specular = specular;
        this.alpha = alpha;
        this.rgba = rgba;
    }
}

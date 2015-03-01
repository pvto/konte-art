package org.konte.model;


import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import org.konte.image.OutputShape;
import org.konte.misc.Matrix4;
import org.konte.parse.ParseException;
import static org.konte.misc.Mathc3.bounds1;
import static org.konte.misc.Mathc3.roll1;

/**
 *
 * @author pvto
 */
public class DrawingContext implements Serializable {

    public Matrix4 matrix;
    public float R;
    public float G;
    public float B;
    public float A = 1f;
    public float layer;
    public short fov = 0;
    public Untransformable shape;

    public byte isDrawPhase = 0;
    public int d = -1;
    /** stores all definition-->value mappings for this instance */
    public Def[] defs;
    public int[] pushstack;
//    public short bitmap = -1;
    public short shading = -1;
    public float col0;






    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException 
           {
        out.writeObject(matrix);
        out.writeFloat(R);
        out.writeFloat(G);
        out.writeFloat(B);
        out.writeFloat(A);
        out.writeFloat(layer);
        out.writeShort(fov);
        out.writeObject(shape);

        out.writeByte(isDrawPhase);
        if (isDrawPhase == 0)
        {
            out.writeInt(d);
            out.writeObject(defs);
            out.writeObject(pushstack);
//            out.writeShort(bitmap);
            out.writeShort(shading);
            if (shading != -1)
            {
                out.writeFloat(col0);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException 
           {
        matrix = (Matrix4)in.readObject();
        R = in.readFloat();
        G = in.readFloat();
        B = in.readFloat();
        A = in.readFloat();
        layer = in.readFloat();
        fov = in.readShort();
        shape = (Untransformable)in.readObject();

        isDrawPhase = in.readByte();
        if (isDrawPhase == 0)
        {
            d = in.readInt();
            defs = (Def[])in.readObject();
            pushstack = (int[])in.readObject();
//            bitmap = in.readShort();
            shading = in.readShort();
            if (shading != -1)
            {
                col0 = in.readFloat();
            }
        }
    }
    
    public DrawingContext()
    {
    }

    public OutputShape toOutputShape()
    {
        OutputShape s = new OutputShape();
        s.matrix = matrix;
//        s.R = R;
//        s.G = G;
//        s.B = B;
//        s.A = A;
        s.col = (int)(A * 255) << 24;
        s.col |= (int)(R * 255) << 16;
        s.col |= (int)(G * 255) << 8;
        s.col |= (int)(B * 255);
        s.fov = fov;
        s.layer = layer;
        s.shape = shape;
        return s;
    }
    
    public static transient DrawingContext ZERO;
    static {
        ZERO = new DrawingContext();
        ZERO.matrix = Matrix4.IDENTITY;
    }
    
    public static class Def implements Serializable, Comparable<Def> {   
        public int nameid;
        public float defval;
        public Def(int nameid, float defval)
        {
            this.nameid = nameid;
            this.defval = defval;
        }

        public int compareTo(Def o)
        {
            return nameid - o.nameid;
        }
        public String toString()
        {
            return nameid + " " + defval;
        }
    }

    private static transient Def search = new Def(0,0);
    
    public float getDef(int id)
    {
        if (defs == null) return 0f;
        search.nameid = id;
        int i = Arrays.binarySearch(defs, search);
        return (i < 0 ? 0f : defs[i].defval);
    }
    public float getR()
    {
        return R;
    }
    public float getG()
    {
        return G;
    }
    public float getB()
    {
        return B;
    }
    public float getA()
    {
        return A;
    }

    public float getshading()
    {
        return (float)shading;
    }
    public float getcol0()
    {
        return col0;
    }
    public void applyShading(Model model) throws ParseException 
    {
        if (shading != -1)
        {
            ColorSpace sp = model.colorSpaces.get(shading);
            float[] ret = sp.getValue(col0);
            float nega = 1f-ret[4];
            R = bounds1(R*nega + ret[0]*ret[4]);
            G = bounds1(G*nega + ret[1]*ret[4]);
            B = bounds1(B*nega + ret[2]*ret[4]);
            A = bounds1(A*nega + ret[3]*ret[4]);
            shading = -1;       // cleanup to decrease serialize size!
        } 
    }
    
    public float getHue()
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        return tmpvals[0];
    }
    private static transient float[] tmpvals = new float[3];
    private void changeHSL()
    {
        if (tmpvals[1]>1f) tmpvals[1] = 1f;
        if (tmpvals[2]>1f) tmpvals[2] = 1f;
        int col = Color.HSBtoRGB(tmpvals[0],tmpvals[1],tmpvals[2]);
        R = (col >> 16 & 0xFF)/256f;
        G = (col >> 8 & 0xFF)/256f;
        B = (col & 0xFF)/256f;
    }
    public void changeHue(float delta)
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        tmpvals[0] = roll1(tmpvals[0]+delta/360);
        changeHSL();
    }
    
    public float getL()
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        return tmpvals[2];
    }

    public void changeLighness(float delta)
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        tmpvals[2] = bounds1(tmpvals[2]+delta);
        changeHSL();
    }
    
    
    public float getSat()
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        return tmpvals[1];
    }


    
    public void changeSat(float delta)
    {
        Color.RGBtoHSB((int)(R*256), (int)(G*256), (int)(B*256), tmpvals);
        if (tmpvals[1] > 1f) tmpvals[1] = 1f;
        tmpvals[1] = tmpvals[1] + delta;
        changeHSL();
    }
    
    public Color getColor()
    {
        return new Color(R,G,B,A);
    }

    public float getlayer()
    {
        return layer;
    }

    public int getfov()
    {
        return fov;
    }

    
    private static final transient float PI = (float)Math.PI;
    private static final transient float toDeg = 180f / 3.14159265f;
    
    public float getrx()
    {
        float l = getLengthFactor();
        float zt = -matrix.m21/matrix.m22;
        float yt = (matrix.m11)/matrix.m11;
        //return toDeg * ((float)Math.atan2(zt,yt));
        return toDeg *( (float)Math.atan2(zt,yt));
    }
/* 
 * 
 */public float getLengthFactor()
 {
     return (float)Math.sqrt(matrix.m00*matrix.m00 + matrix.m11*matrix.m11 + matrix.m22*matrix.m22);
 }
    public float getry()
    {
        float l = getLengthFactor();
        float zt = matrix.m22/matrix.m22;
        float xt = -matrix.m02/matrix.m00;
        return toDeg * ((float)Math.atan2(xt,zt) );
    }

    public float getrz()
    {
        float l = getLengthFactor();
        float xt = matrix.m00/matrix.m00;
        float yt = matrix.m10/matrix.m11;
        return toDeg * ((float)Math.atan2(yt,xt));
    }

    public float getskewx()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public float getskewy()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public float getskewz()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public float getsx()
    {
        return matrix.m00;
    }

    public float getsy()
    {
        return matrix.m11;
    }

    public float getsz()
    {
        return matrix.m22;
    }

    public float getx()
    {
        return matrix.m03;
    }

    public float gety()
    {
        return matrix.m13;
    }

    public float getz()
    {
        return matrix.m23;
    }

    public float getMinWidth()
    {
        return Math.min(Math.abs(matrix.m00) + Math.abs(matrix.m01) + Math.abs(matrix.m02),
                Math.min(Math.abs(matrix.m10) + Math.abs(matrix.m11) + Math.abs(matrix.m12),
                Math.abs(matrix.m20) + Math.abs(matrix.m21) + Math.abs(matrix.m22)));
    }
    public float getMaxWidth()
    {
        return Math.max(Math.abs(matrix.m00) + Math.abs(matrix.m01) + Math.abs(matrix.m02),
                Math.max(Math.abs(matrix.m10) + Math.abs(matrix.m11) + Math.abs(matrix.m12),
                Math.abs(matrix.m20) + Math.abs(matrix.m21) + Math.abs(matrix.m22)));
    }

    public String toString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append(matrix).append("+\n");
        bd.append(String.format("R %s G %s B %s A %s layer %s d %s shad %s sh-x %s",
                R,G,B,A,layer,d,shading,col0));
        
        return bd.toString();
    }
    
}

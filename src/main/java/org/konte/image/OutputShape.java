
package org.konte.image;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import org.konte.misc.Matrix4Red;
import org.konte.model.Untransformable;

/**
 *
 * @author pvto
 */
public class OutputShape implements Serializable {
    public Matrix4Red matrix;
    public int col;
    public float layer;
    public short fov = 0;
    public Untransformable shape;

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException 
           {
//        out.writeObject(matrix);
        out.writeFloat(matrix.m00);  out.writeFloat(matrix.m01);  out.writeFloat(matrix.m02);  out.writeFloat(matrix.m03);
        out.writeFloat(matrix.m10);  out.writeFloat(matrix.m11);  out.writeFloat(matrix.m12);  out.writeFloat(matrix.m13);
        out.writeFloat(matrix.m20);  out.writeFloat(matrix.m21);  out.writeFloat(matrix.m22);  out.writeFloat(matrix.m23);
        out.writeInt(col);
        out.writeFloat(layer);
        out.writeShort(fov);
        out.writeObject(shape);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException 
           {
        matrix = new Matrix4Red();
        matrix.m00 = in.readFloat();  matrix.m01 = in.readFloat();  matrix.m02 = in.readFloat();  matrix.m03 = in.readFloat();
        matrix.m10 = in.readFloat();  matrix.m11 = in.readFloat();  matrix.m12 = in.readFloat();  matrix.m13 = in.readFloat();
        matrix.m20 = in.readFloat();  matrix.m21 = in.readFloat();  matrix.m22 = in.readFloat();  matrix.m23 = in.readFloat();
        col = in.readInt();
        layer = in.readFloat();
        fov = in.readShort();
        shape = (Untransformable)in.readObject();
    }

    public OutputShape()
    {
    }

    public Color getColor()
    {
        return new Color((col >> 16) & 0xFF, (col >> 8) & 0xFF, col & 0xFF, (col >> 24) & 0xFF);
    }
    public float getR() { return ((col >> 16) & 0xFF) / 255f; }
    public float getG() { return ((col >> 8) & 0xFF) / 255f; }
    public float getB() { return (col & 0xFF) / 255f; }
    public float getA() { return ((col >> 24) & 0xFF) / 255f; }
    
    public float getMinWidth()
    {
        return Math.min(Math.abs(matrix.m00) + Math.abs(matrix.m01) + Math.abs(matrix.m02),
                Math.min(Math.abs(matrix.m10) + Math.abs(matrix.m11) + Math.abs(matrix.m12),
                Math.abs(matrix.m20) + Math.abs(matrix.m21) + Math.abs(matrix.m22)));
    }

    public float getAvgWidth()
    {
        return (Math.abs(matrix.m00) + Math.abs(matrix.m01) + Math.abs(matrix.m02)
                + Math.abs(matrix.m10) + Math.abs(matrix.m11) + Math.abs(matrix.m12)
                + Math.abs(matrix.m20) + Math.abs(matrix.m21) + Math.abs(matrix.m22))
                / 3.0f;
    }

}

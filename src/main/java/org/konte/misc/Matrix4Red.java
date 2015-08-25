
package org.konte.misc;

/**
 * @author pvto https://github.com/pvto
 */
public class Matrix4Red {

/*
    public Vector3 setXyz(Matrix4 orig, Matrix4 b) {
        x = orig.m00 * b.m03 + orig.m01 * b.m13 + orig.m02 * b.m23 + orig.m03 * b.m33;
        y = -(orig.m10 * b.m03 + orig.m11 * b.m13 + orig.m12 * b.m23 + orig.m13 * b.m33);
        z = orig.m20 * b.m03 + orig.m21 * b.m13 + orig.m22 * b.m23 + orig.m23 * b.m33;
        return this;
    }
    */
    
    public float 
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23
            ;
    
    public Matrix4Red()
    {
    }
    
    public Matrix4Red(Matrix4 m)
    {
        m00 = m.m00;
        m01 = m.m01;
        m02 = m.m02;
        m03 = m.m03;
        m10 = m.m10;
        m11 = m.m11;
        m12 = m.m12;
        m13 = m.m13;
        m20 = m.m20;
        m21 = m.m21;
        m22 = m.m22;
        m23 = m.m23;
    }
}

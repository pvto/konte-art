/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.misc;



/**<p>Derived from org.sunflow.misc.Matrix4
 *
 * @author pto
 */
public class Matrix3 {
    // matrix elements, m(row,col)
    // - row major
    public float m00;
    public float m01;
    public float m02;
    public float m10;
    public float m11;
    public float m12;
    public float m20;
    public float m21;
    public float m22;
    
    public Matrix3() { }
    public Matrix3(float n00,float n01, float n02, float n10, float n11, float n12, float n20, float n21, float n22) { 
        m00 = n00; m01 = n01; m02 = n02; m10 = n10; m11 = n11; m12 = n12; m20 = n20; m21 = n21; m22 = n22;
    }
    public static Matrix3 rotationX(float theta) {
        Matrix3 m = new Matrix3();
        m.m00 = 1f;
        m.m11 = (float) Math.cos(-theta);
        m.m12 = (float) Math.sin(-theta);
        m.m21 = (float) -Math.sin(-theta);
        m.m22 = (float) Math.cos(-theta);
        return m;
    }
    public static Matrix3 rotationY(float theta) {
        Matrix3 m = new Matrix3();
        m.m11 = 1f;
        m.m00 = (float) Math.cos(-theta);
        m.m02 = (float) -Math.sin(-theta);
        m.m20 = (float) Math.sin(-theta);
        m.m22 = (float) Math.cos(-theta);
        return m;
    }
    public static Matrix3 rotationZ(float theta) {
        Matrix3 m = new Matrix3();
        m.m22 = 1f;
        m.m00 = (float) Math.cos(-theta);
        m.m01 = (float) Math.sin(-theta);
        m.m10 = (float) -Math.sin(-theta);
        m.m11 = (float) Math.cos(-theta);
        return m;
    }
    public static Matrix3 rotation(float thetax, float thetay, float thetaz) {
        Matrix3 m1 = rotationX(thetax);
        Matrix3 m2 = rotationY(thetay);
        Matrix3 m3 = new Matrix3();
        multiply(m1,m2,m3);
        m1 = rotationZ(thetaz);
        return multiply(m3,m1,m2);
    }
    public static Matrix3 nmul(Matrix3... ms) {
        if (ms == null) return null;
        Matrix3 res = ms[0];
        for (int i = 1; i < ms.length; i++) {
            Matrix3 tmp = new Matrix3();
            multiply(res, ms[i], tmp);
            res = tmp;
        }
        return res;
    }
    
    public static Matrix3 multiply(Matrix3 m1, Matrix3 m2, Matrix3 dest) {
        dest.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20;
        dest.m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21;
        dest.m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22;

        dest.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20;
        dest.m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21;
        dest.m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22;

        dest.m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20;
        dest.m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21;
        dest.m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22;
        
        return dest;
    }

    public Vector3 multiply(Vector3 v) {
        Vector3 dest = new Vector3(
            m00 * v.x + m01 * v.y + m02 * v.z,
            m10 * v.x + m11 * v.y + m12 * v.z,
            m20 * v.x + m21 * v.y + m22 * v.z);
        return dest;
    }
    
    public Matrix4 toMatrix4() {
        Matrix4 m = new Matrix4(m00,m01,m02,0f,m10,m11,m12,0f,m20,m21,m22,0f,0f,0f,0f,1f);
        return m;
    }
    
    
   public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append(String.format("[%s\t%s\t%s\n %s\t%s\t%s\n %s\t%s\t%s\n]",
                m00,m01,m02,m10,m11,m12,m20,m21,m22));
        
        return bd.toString();        
    }
     
}

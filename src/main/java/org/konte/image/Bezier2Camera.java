package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;
import org.konte.model.Transform;
import org.konte.parse.ParseException;

/**
 * Created by paavoto on 26.6.2017.
 */
public class Bezier2Camera extends SimpleCamera {

    private float 
            p1ax, p1ay, x1, y1, p1bx, p1by,
            p2ax, p2ay, x2, y2, p2bx, p2by,
            p3ax, p3ay, x3, y3, p3bx, p3by,
            p4ax, p4ay, x4, y4, p4bx, p4by,
            k13x,
            k24y,
            cx, cy,
            p1ang, p2ang, p3ang, p4ang,
            Aradr, Bradr, Cradr, Dradr,
            ease = 7f,
            square = 1f
    ;
    
    private double normang(double ang) {
        if (ang < 0.0) {
            if (ang < -2.0 * Math.PI) {
                ang = ang % 2.0 * Math.PI;
            }
            ang = ang + 2.0 * Math.PI;
        }
        else if (ang >= 2.0 * Math.PI) {
            ang = ang % 2.0 * Math.PI;
        }
        return ang;
    }
    
    public Bezier2Camera(float p1ax, float p1ay, float x1, float y1, float p1bx, float p1by, 
                         float p2ax, float p2ay, float x2, float y2, float p2bx, float p2by,
                         float p3ax, float p3ay, float x3, float y3, float p3bx, float p3by,
                         float p4ax, float p4ay, float x4, float y4, float p4bx, float p4by,
                         float ease, float baseform
                         ) {
        this.p1ax = p1ax; this.p1ay = p1ay; this.x1 = x1; this.y1 = y1; this.p1bx = p1bx; this.p1by = p1by;
        this.p2ax = p2ax; this.p2ay = p2ay; this.x2 = x2; this.y2 = y2; this.p2bx = p2bx; this.p2by = p2by;
        this.p3ax = p3ax; this.p3ay = p3ay; this.x3 = x3; this.y3 = y3; this.p3bx = p3bx; this.p3by = p3by;
        this.p4ax = p4ax; this.p4ay = p4ay; this.x4 = x4; this.y4 = y4; this.p4bx = p4bx; this.p4by = p4by;
        
        this.ease = ease;
        this.square = baseform;
        
        k13x = (y3-y1==0? 0 : (x3-x1) / (y3-y1));
        k24y = (x4-x2==0? 0 : (y4-y2) / (x4-x2));
        cx = (x1 + x2 + x3 + x4) / 4f;
        cy = (y1 + y2 + y3 + y4) / 4f;
        
        p1ang = (float) normang(Math.atan2(y1-cy, x1-cx));
        p2ang = (float) normang(Math.atan2(y2-cy, x2-cy));
        p3ang = (float) normang(Math.atan2(y3-cy, x3-cy));
        p4ang = (float) Math.atan2(y4-cy, x4-cx);
        Aradr = (float) Math.abs(p1ang - p2ang);
        Bradr = (float) Math.abs(p2ang - p3ang);
        Cradr = (float) Math.abs(p3ang - p4ang);
        Dradr = (float) Math.abs(p4ang - 2.0*Math.PI + p1ang);
        
/*        System.out.println(x3 + "," + y3 + " " + p3ax + "," + p3ay + " " + p2bx + "," + p2by + " " + x2 + "," + y2 + " "); 
        for(float ff = 0f; ff < 1.57f; ff+=0.1f) {
            float t = ff / 1.57f;
            float x0 = pow(1f-t, 3)*x3  +  pow(1f-t, 2)*t*p3ax  +  (1f-t)*t*t*p2bx  +  t*t*t*x2;
            float y0 = pow(1f-t, 3)*y3  +  pow(1f-t, 2)*t*p3ay  +  (1f-t)*t*t*p2by  +  t*t*t*y2;
            System.out.println(x0 + "," + y0);
        } */
    }
    
    boolean isAB(float x, float y) {
        return x < x1 + k13x * (y - y1);
    }
    boolean isAD(float x, float y) {
        return y < y2 + k24y * (x - x2);
    }

    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        float 
                x = v.x,
                y = v.y;

        
        /*

               P   
              -3-   
             _..._    
        |  / B | C \  |
       P2 (---------) P4
        |  \ A | D /  |
             -...-    
              -P-   
               1  

        */
        
        
        

        float dist = (float) Math.sqrt(pow(x - cx, 2f) + pow(y - cy, 2f));
        float ang = (float) Math.atan2((y-cy), x-cx);
        float t, x0, y0, dist0;
        float p0d, p0x, p0y; // point p0 on approximately square bezier curve, mapping is between this and the user defined curve 
        if (isAD(x, y)) {
            if (isAB(x, y)) {
                // A
                t = ((float)normang(ang) - p2ang) / Aradr;
                x0 = pow(1f-t, 3)*x2  +  3f * pow(1f-t, 2)*t*p2ax  +  3f * (1f-t)*t*t*p1bx  +  t*t*t*x1;
                y0 = pow(1f-t, 3)*y2  +  3f * pow(1f-t, 2)*t*p2ay  +  3f * (1f-t)*t*t*p1by  +  t*t*t*y1;
                p0x = pow(1f-t, 3)*x2  +  3f * pow(1f-t, 2)*t*x2  +  3f * (1f-t)*t*t*(x1-(x1-x2)*square)  +  t*t*t*x1;
                p0y = pow(1f-t, 3)*y2  +  3f * pow(1f-t, 2)*t*(y2-(y2-y1)*square)  +  3f * (1f-t)*t*t*y1  +  t*t*t*y1;
            } else {
                // D
                t = (float)(p4ang - ang) / Dradr;
                x0 = pow(1f-t, 3)*x4  +  3f * pow(1f-t, 2)*t*p4bx  +  3f * (1f-t)*t*t*p1ax  +  t*t*t*x1;
                y0 = pow(1f-t, 3)*y4  +  3f * pow(1f-t, 2)*t*p4by  +  3f * (1f-t)*t*t*p1ay  +  t*t*t*y1;
                p0x = pow(1f-t, 3)*x4  +  3f * pow(1f-t, 2)*t*x4  +  3f * (1f-t)*t*t*(x1+(x4-x1)*square)  +  t*t*t*x1;
                p0y = pow(1f-t, 3)*y4  +  3f * pow(1f-t, 2)*t*(y4-(y4-y1)*square)  +  3f * (1f-t)*t*t*y1  +  t*t*t*y1;
            }
        } else {
            if (isAB(x, y)) {
                // B
                t = (float)(normang(ang) - p3ang) / Bradr;
                x0 = pow(1f-t, 3)*x3  +  3f * pow(1f-t, 2)*t*p3ax  +  3f * (1f-t)*t*t*p2bx  +  t*t*t*x2;
                y0 = pow(1f-t, 3)*y3  +  3f * pow(1f-t, 2)*t*p3ay  +  3f * (1f-t)*t*t*p2by  +  t*t*t*y2;
                p0x = pow(1f-t, 3)*x3  +  3f * pow(1f-t, 2)*t*(x3-(x3-x2)*square)  +  3f * (1f-t)*t*t*x2  +  t*t*t*x2;
                p0y = pow(1f-t, 3)*y3  +  3f * pow(1f-t, 2)*t*y3  +  3f * (1f-t)*t*t*(y2+(y3-y2)*square)  +  t*t*t*y2;
            } else {
                // C
                t = (float)(normang(ang) - p4ang) / Cradr;
                x0 = pow(1f-t, 3)*x4  +  3f * pow(1f-t, 2)*t*p4ax  +  3f * (1f-t)*t*t*p3bx  +  t*t*t*x3;
                y0 = pow(1f-t, 3)*y4  +  3f * pow(1f-t, 2)*t*p4ay  +  3f * (1f-t)*t*t*p3by  +  t*t*t*y3;
                p0x = pow(1f-t, 3)*x4  +  3f * pow(1f-t, 2)*t*x4  +  3f * (1f-t)*t*t*(x3+(x4-x3)*square)  +  t*t*t*x3;
                p0y = pow(1f-t, 3)*y4  +  3f * pow(1f-t, 2)*t*(y4+(y3-y4)*square)  +  3f * (1f-t)*t*t*y3  +  t*t*t*y3;
            }
        }
        dist0 = (float) Math.sqrt(x*x + y*y);
        p0d = (float) Math.sqrt(pow(p0x-cx, 2f) + pow(p0y-cy, 2f));
        p0d = (0.5f + pow((dist0 / p0d - 0.5f) * 2f, ease) * 0.5f);
        x0 = cx + (x0 - cx) * p0d; 
        y0 = cy + (y0 - cy) * p0d;
        /*dist0 = (float) Math.sqrt(pow(x0-cx, 2f) + pow(y0-cy, 2f));
        ang = (float) normang((float)Math.atan2(y0 - cy, x0 - cx));
        x0 = cx + (float) Math.cos(ang) * dist0 * p0d;
        y0 = cy + (float) Math.sin(ang) * dist0 * p0d;*/
        return super.mapTo2D(new Vector3(x0, y0, v.z));
    }

    private float pow(float x, float exp) {
        return (float)Math.pow(x, exp);
    }
    private float min(float a, float b) { return b < a ? b : a; }
    private float abs(float x) { return x<0f? -x : x; }
    
    private float PI = (float) Math.PI;
    
    @Override
    public void setPosition(Transform posT) throws ParseException {
        super.setPosition(posT);
        float x = posT.getTransformMatrix().m03;
        float y = posT.getTransformMatrix().m13;
        
        p1ax += x; p1bx += x; p2ax += x; p2bx += x; p3ax += x; p3bx += x; p4ax += x; p4bx += x; 
        p1ay += y; p1by += y; p2ay += y; p2by += y; p3ay += y; p3by += y; p4ay += y; p4by += y; 
        x1 += x; x2 += x; x3 += x; x4 += x;
        y1 += y; y2 += y; y3 += y; y4 += x;
        cx += x; cy += y;
    }
    
    
    
}

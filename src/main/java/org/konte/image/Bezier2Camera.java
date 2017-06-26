package org.konte.image;

import org.konte.misc.Point2;
import org.konte.misc.Vector3;

/**
 * Created by paavoto on 26.6.2017.
 */
public class Bezier2Camera extends SimpleCamera {

    private float 
            p1ax, p1ay, x1, y1, p1bx, p1by,
            p2ax, p2ay, x2, y2, p2bx, p2by,
            p3ax, p3ay, x3, y3, p3bx, p3by,
            p4ax, p4ay, x4, y4, p4bx, p4by
    ;
    
    public Bezier2Camera(float p1ax, float p1ay, float x1, float y1, float p1bx, float p1by, 
                         float p2ax, float p2ay, float x2, float y2, float p2bx, float p2by,
                         float p3ax, float p3ay, float x3, float y3, float p3bx, float p3by,
                         float p4ax, float p4ay, float x4, float y4, float p4bx, float p4by
                         ) {
        this.p1ax = p1ax; this.p1ay = p1ay; this.x1 = x1; this.y1 = y1; this.p1bx = p1bx; this.p1by = p1by;
        this.p2ax = p2ax; this.p2ay = p2ay; this.x2 = x2; this.y2 = y2; this.p2bx = p2bx; this.p2by = p2by;
        this.p3ax = p3ax; this.p3ay = p3ay; this.x3 = x3; this.y3 = y3; this.p3bx = p3bx; this.p3by = p3by;
        this.p4ax = p4ax; this.p4ay = p4ay; this.x4 = x4; this.y4 = y4; this.p4bx = p4bx; this.p4by = p4by;
    }

    @Override
    public Point2 mapTo2D(Vector3 v)
    {
        float 
                x = v.x,
                y = v.y;

        // f(t), t in [0,1]:  (1-t)^3*P0 + (1-t)^2*t*P1 + (1-t)*t^2*P2 + t^3*P3
        
        /*

            b    a
          P2      P3
         a   _.._   b
           /      \
    PL--- (--------) ---PR
           \      /
         b   -..-   a
          P1      P4
            a    b

        */

        float PLx = pow(1-y, 3) * x1  +  pow(1-y, 2) * y * p1bx  +  (1-y) * y*y * p2ax  +  y*y*y * x2;
        float PLy = pow(1-y, 3) * y1  +  pow(1-y, 2) * y * p1by  +  (1-y) * y*y * p2ay  +  y*y*y * y2;
        float PLbx = PLx  -  (1-y) * (p1bx - x1)  -  y * (p2ax - x2);
        float PLby = PLy  -  (1-y) * (p1by - y1)  -  y * (p2ay - y2);

        float PRx = pow(1-y, 3) * x4  +  pow(1-y, 2) * y * p4ax  +  (1-y) * y*y * p3bx  +  y*y*y * x3;
        float PRy = pow(1-y, 3) * y4  +  pow(1-y, 2) * y * p4ay  +  (1-y) * y*y * p3by  +  y*y*y * y3;
        float PRax = PRx  -  (1-y) * (p4ax - x4)  -  y * (p3bx - x3);
        float PRay = PRy  -  (1-y) * (p4ay - y4)  -  y * (p3by - y3);

        float PCx = pow(1-x, 3) * PLx  +  pow(1-x, 2) * x * PLbx  +  (1-x) * x*x * PRax  +  x*x*x * PRx;
        float PCy = pow(1-x, 3) * PLy  +  pow(1-x, 2) * x * PLby  +  (1-x) * x*x * PRay  +  x*x*x * PRy;

        return super.mapTo2D(new Vector3(PCx, PCy, v.z));
    }

    private float pow(float x, float exp) {
        return (float)Math.pow(x, exp);
    }
}

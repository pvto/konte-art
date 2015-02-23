
package org.konte.model;

import org.konte.lang.*;
import org.konte.misc.Matrix4;

/**
 *
 * @author pvto
 */
public class UtConstants {

    public UtConstants(UtBuilder utb)
    {
        this.utb = utb;
    }

    private static float cf_ = (float)Math.sqrt(2f)/5f;
    // SQUARE CORNERS
    private static Matrix4 PATH_TOPL = Matrix4.translation(-0.5f, 0.5f, 0f);
    private static Matrix4 PATH_TOPR = Matrix4.translation(0.5f, 0.5f, 0f);
    private static Matrix4 PATH_BOTL = Matrix4.translation(-0.5f, -0.5f, 0f);
    private static Matrix4 PATH_BOTR = Matrix4.translation(0.5f, -0.5f, 0f);  
    // + TRIANGLE TOP CORNER + CIRCLE
    private static Matrix4 PATH_TOPC = Matrix4.translation(0f, 0.5f, 0f); 
    private static Matrix4 PATH_BOTC = Matrix4.translation(0f, -0.5f, 0f); 
    private static Matrix4 PATH_LFTC = Matrix4.translation(-0.5f, 0f, 0f); 
    private static Matrix4 PATH_RGTC = Matrix4.translation(0.5f, 0f, 0f); 
    // CIRCLE BEZIERS
    private static Matrix4[][] CIRC_BEZ = new Matrix4[4][2];
    
    // BOX CORNERS
    private static Matrix4 PATH_TLF = Matrix4.translation(-0.5f, 0.5f, -0.5f);
    private static Matrix4 PATH_TRF = Matrix4.translation(0.5f, 0.5f, -0.5f);
    private static Matrix4 PATH_BLF = Matrix4.translation(-0.5f, -0.5f, -0.5f);
    private static Matrix4 PATH_BRF = Matrix4.translation(0.5f, -0.5f, -0.5f);
    private static Matrix4 PATH_TLB = Matrix4.translation(-0.5f, 0.5f, 0.5f);
    private static Matrix4 PATH_TRB = Matrix4.translation(0.5f, 0.5f, 0.5f);
    private static Matrix4 PATH_BLB = Matrix4.translation(-0.5f, -0.5f, 0.5f);
    private static Matrix4 PATH_BRB = Matrix4.translation(0.5f, -0.5f, 0.5f);


    private UtBuilder utb;

    public void run()
    {

        // CIRCLE BEZIER POINTS
        for (int i=0; i < 4; i++)
        {
            float x1 = (i % 2 == 0 ? cf_ : 0.5f);
            float y1 = (i % 2 == 1 ? cf_ : 0.5f);
            float x2 = y1;
            float y2 = x1;
            if (i > 1)
            {
                x1 = -x1;
                x2 = -x2;
            }
            if (i == 1 || i == 2)
            {
                y1 = -y1;
                y2 = -y2;
            }
            CIRC_BEZ[i][0] = Matrix4.translation(x1,y1,0f);
            CIRC_BEZ[i][1] = Matrix4.translation(x2,y2,0f);
        }
        float S1 = 0.0f;
        float S2 = cf_;

        try {
            Language.SQUARE      = Language.addUntransformable(utb.name("SQUARE").
                    addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL).
                    build());
            Language.CIRCLE    = Language.addUntransformable(utb.name("CIRCLE").clearShapes().
                    addShape(PATH_TOPC,PATH_RGTC,PATH_BOTC,PATH_LFTC).
                    addControlPoints(CIRC_BEZ).
                    build());
            Language.TRIANGLE    = Language.addUntransformable(utb.name("TRIANGLE").clearShapes().
                    addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC).
                    build());
            Language.RTRIANGLE    = Language.addUntransformable(utb.name("RTRIANGLE").clearShapes().
                    addShape(PATH_BOTR,PATH_BOTL,PATH_TOPL).
                    build());
            Language.BOX         = Language.addUntransformable(utb.name("BOX").clearShapes().
                    addShape(PATH_TLB,PATH_TRB,PATH_BRB,PATH_BLB).
                    addShape(PATH_TLB,PATH_TRB,PATH_TRF,PATH_TLF).
                    addShape(PATH_BLB,PATH_BRB,PATH_BRF,PATH_BLF).
                    addShape(PATH_TLB,PATH_TLF,PATH_BLF,PATH_BLB).
                    addShape(PATH_TRB,PATH_TRF,PATH_BRF,PATH_BRB).
                    addShape(PATH_TLF,PATH_TRF,PATH_BRF,PATH_BLF).
                    build());
            Language.SPHERE        = Language.addUntransformable(utb.name("SPHERE").clearShapes().build());
            Language.MESH        = Language.addUntransformable(utb.name("MESH").clearShapes().build());
            utb.name("PIPE").clearShapes();
            float nn = 16;
            Matrix4 step = Matrix4.scale((float)Math.sin(Math.PI/nn), 1f, 1f).multiply(
                    Matrix4.translation(0f, 0f, -(float)Math.cos(Math.PI/nn)/2f));
            Matrix4 base = Matrix4.IDENTITY;
            for(int i = 0; i < nn; i++)
            {
                base = base.multiply(Matrix4.rotateY((float)Math.PI*2f/(float)nn));
                Matrix4 c = base.multiply(step);
                utb.addShape(
                    c.multiply(PATH_TOPL),
                    c.multiply(Matrix4.translation(0.52f, 0.5f, 0f)),
                    c.multiply(Matrix4.translation(0.52f, -0.5f, 0f)),
                    c.multiply(PATH_BOTL) );
            }
            Language.PIPE    = Language.addUntransformable(utb.build());
            utb.name("CONE").clearShapes();
            base = Matrix4.IDENTITY;
            for(int i = 0; i < nn; i++)
            {
                base = base.multiply(Matrix4.rotateY((float)Math.PI*2f/(float)nn));
                Matrix4 c = base.multiply(step);
                utb.addShape(
                    Matrix4.translation(0f, 0.5f, 0f),
                    c.multiply(Matrix4.translation( 0.52f, -0.5f, 0f)),
                    c.multiply(Matrix4.translation(-0.52f, -0.5f, 0f)) );
            }
            Language.CONE = Language.addUntransformable(utb.build());
            Language.CSQU    = Language.addUntransformable(utb.name("CSQU").clearShapes().
                    addShape(CIRC_BEZ[0][0],CIRC_BEZ[0][1],CIRC_BEZ[1][0],CIRC_BEZ[1][1],
                    CIRC_BEZ[2][0],CIRC_BEZ[2][1],CIRC_BEZ[3][0],CIRC_BEZ[3][1]).
                    build());
            Language.RSQU    = Language.addUntransformable(utb.name("RSQU").clearShapes().
                    addShape(PATH_TOPC,PATH_RGTC,PATH_BOTC,PATH_LFTC).
                    addControlPoints(new Matrix4[][] {
                        {PATH_TOPR,PATH_TOPR},{PATH_BOTR,PATH_BOTR},{PATH_BOTL,PATH_BOTL},{PATH_TOPL,PATH_TOPL}
                    }).
                    build());
        } catch(Exception e) { e.printStackTrace(); }

    }
}

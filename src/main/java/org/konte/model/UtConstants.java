
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

    private static final float 
            cf_ = .275f;
    private static final Matrix4 
            PATH_TOPL = Matrix4.translation(-0.5f, 0.5f, 0f),
            PATH_TOPR = Matrix4.translation(0.5f, 0.5f, 0f),
            PATH_BOTL = Matrix4.translation(-0.5f, -0.5f, 0f),
            PATH_BOTR = Matrix4.translation(0.5f, -0.5f, 0f),
            PATH_TOPC = Matrix4.translation(0f, 0.5f, 0f),
            PATH_BOTC = Matrix4.translation(0f, -0.5f, 0f),
            PATH_LFTC = Matrix4.translation(-0.5f, 0f, 0f),
            PATH_RGTC = Matrix4.translation(0.5f, 0f, 0f)
            ;
    private static final Matrix4[][] CIRC_BEZ = 
            new Matrix4[4][2];
    private static final Matrix4
            PATH_TLF = Matrix4.translation(-0.5f, 0.5f, -0.5f),
            PATH_TRF = Matrix4.translation(0.5f, 0.5f, -0.5f),
            PATH_BLF = Matrix4.translation(-0.5f, -0.5f, -0.5f),
            PATH_BRF = Matrix4.translation(0.5f, -0.5f, -0.5f),
            PATH_TLB = Matrix4.translation(-0.5f, 0.5f, 0.5f),
            PATH_TRB = Matrix4.translation(0.5f, 0.5f, 0.5f),
            PATH_BLB = Matrix4.translation(-0.5f, -0.5f, 0.5f),
            PATH_BRB = Matrix4.translation(0.5f, -0.5f, 0.5f)
            ;
    private static final Matrix4[] HEXP = new Matrix4[6];
    private static final Matrix4[] _32GONP = new Matrix4[32];

    private final UtBuilder utb;

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
        // create xGON POINTS
        for(Matrix4[] trg : new Matrix4[][] { HEXP, _32GONP})
        {
            for (int i = 0; i < trg.length; i++)
            {
                float angle = (float) (Math.PI / 2.0 + Math.PI * 2.0 / trg.length * i);
                float radius = (float) (0.5 / Math.cos(Math.PI / trg.length));
                trg[i] = Matrix4.translation((float)Math.cos(angle)*radius, (float)(Math.sin(angle)*radius), 0f);
            }
        }

        try {
            Language.SQUARE      = Language.addUntransformable(utb.name("SQUARE").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .build());
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
            Language.HEXAGON    = Language.addUntransformable(utb.name("HEX").clearShapes()
                    .addShape(HEXP)
                    .build());
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
                base = base.multiply(Matrix4.rotateY(2f * (float)Math.PI / (float)nn));
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
            Language.BLUR_SQUARE      = Language.addUntransformable(utb.name("GBLUR").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.GBLUR)
                    .build());
            Language.BLUR_TRIANGLE    = Language.addUntransformable(utb.name("GBLURTRI").clearShapes()
                    .addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC)
                    .effect(Effects.GBLUR)
                    .build());
            Language.BLUR_HEXAGON    = Language.addUntransformable(utb.name("GBLURHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.GBLUR)
                    .build());
            Language.BLUR_32GON    = Language.addUntransformable(utb.name("GBLUR32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.GBLUR)
                    .build());

            Language.RADBLUR_SQUARE      = Language.addUntransformable(utb.name("RADBLUR").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.RADBLUR)
                    .build());
            Language.RADBLUR_HEXAGON      = Language.addUntransformable(utb.name("RADBLURHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.RADBLUR)
                    .build());
            Language.RADBLUR_32GON      = Language.addUntransformable(utb.name("RADBLUR32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.RADBLUR)
                    .build());

            Language.RADTR_SQUARE      = Language.addUntransformable(utb.name("RADTR").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.RADTR)
                    .build());
            Language.RADTR_HEXAGON      = Language.addUntransformable(utb.name("RADTRHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.RADTR)
                    .build());
            Language.RADTR_32GON      = Language.addUntransformable(utb.name("RADTR32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.RADTR)
                    .build());

            Language.MIX_SQUARE      = Language.addUntransformable(utb.name("MIX").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.MIX)
                    .build());
            Language.MIX_TRIANGLE    = Language.addUntransformable(utb.name("MIXTRI").clearShapes()
                    .addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC)
                    .effect(Effects.MIX)
                    .build());
            Language.MIX_HEXAGON    = Language.addUntransformable(utb.name("MIXHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.MIX)
                    .build());
            Language.MIX_32GON    = Language.addUntransformable(utb.name("MIX32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.MIX)
                    .build());
            
            Language.RUBBER_SQUARE      = Language.addUntransformable(utb.name("RUBBER").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.RUBBER)
                    .build());
            Language.RUBBER_TRIANGLE    = Language.addUntransformable(utb.name("RUBBERTRI").clearShapes()
                    .addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC)
                    .effect(Effects.RUBBER)
                    .build());
            Language.RUBBER_HEXAGON    = Language.addUntransformable(utb.name("RUBBERHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.RUBBER)
                    .build());
            Language.RUBBER_32GON    = Language.addUntransformable(utb.name("RUBBER32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.RUBBER)
                    .build());
            
            Language.GAMEOFLIFE      = Language.addUntransformable(utb.name("GAMEOFLIFE").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.GOL)
                    .build());
            
            Language.EDGE_SQUARE      = Language.addUntransformable(utb.name("EDGE").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.EDGE)
                    .build());
            Language.EDGE_TRIANGLE      = Language.addUntransformable(utb.name("EDGETRI").clearShapes()
                    .addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC)
                    .effect(Effects.EDGE)
                    .build());
            Language.EDGE_HEXAGON    = Language.addUntransformable(utb.name("EDGEHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.EDGE)
                    .build());
            Language.EDGE_32GON    = Language.addUntransformable(utb.name("EDGE32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.EDGE)
                    .build());

            Language.BRIGHTNESS_SQUARE    = Language.addUntransformable(utb.name("BRIGHTNESS").clearShapes()
                    .addShape(PATH_TOPL,PATH_TOPR,PATH_BOTR,PATH_BOTL)
                    .effect(Effects.BRIGHTNESS)
                    .build());
            Language.BRIGHTNESS_TRIANGLE    = Language.addUntransformable(utb.name("BRIGHTNESSTRI").clearShapes()
                    .addShape(PATH_BOTR,PATH_BOTL,PATH_TOPC)
                    .effect(Effects.BRIGHTNESS)
                    .build());
            Language.BRIGHTNESS_HEXAGON    = Language.addUntransformable(utb.name("BRIGHTNESSHEX").clearShapes()
                    .addShape(HEXP)
                    .effect(Effects.BRIGHTNESS)
                    .build());
            Language.BRIGHTNESS_32GON    = Language.addUntransformable(utb.name("BRIGHTNESS32").clearShapes()
                    .addShape(_32GONP)
                    .effect(Effects.BRIGHTNESS)
                    .build());
            
        } catch(Exception e) { e.printStackTrace(); }

    }
}

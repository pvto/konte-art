
package org.konte.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.konte.expression.Name;
import org.konte.generate.RuleWriter;
import org.konte.lang.Tokenizer;
import org.konte.lang.Tokenizer.TokenizerString;
import org.konte.misc.Matrix4;
import org.konte.model.ColorSpace;
import org.konte.model.ColorSpace.RGBA;
import org.konte.model.DrawingContext;
import org.konte.model.Model;
import org.konte.parse.ParseException;
import org.konte.parse.Parser;

/**
 *
 * @author pvto
 */
public class ColSpacePanel extends JPanel {
    private ColorSpace cspace;
    private int lastActive = -1;
    private DrawingContext cpoint;
    private RuleWriter rw;
    public ColSpacePanel() {
        init();
    }
    private Long avgDrawTime;
    private int curStep = 1;

    
    public ColSpacePanel(ColorSpace cspace0) {
        this();
        this.cspace = cspace0;
    }
    private void init() {
        cpoint = new DrawingContext();
        cpoint.matrix = Matrix4.IDENTITY;
        try {
            rw = new RuleWriter(null);
            rw.model = new Model();
            rw.model.context = cpoint;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                try {
                    float[][] bounds = cspace.getBounds();
                    float fuzzy = (bounds[1][0] - bounds[0][0]) / 100f;
                    int pivot = getPivot(e, fuzzy);
                    if (pivot != -1) {
                        setLastActive(pivot);
                        repaint();
                        System.out.println("push at " + pivot);
                    }
                } catch(Exception ex) { }
            }

        });
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                avgDrawTime = null;
                curStep = 1;
            }
        });
    }

    
    public ColorSpace getColorSpace() { 
        return cspace;                  }
    public void setColorSpace(ColorSpace cspace) {
        this.cspace = cspace;
    }
    public void setLastActive(int index) { this.lastActive = index; }
    public int getLastActive() { return lastActive; }
    @Override
    public void paint(Graphics g) {
//        super.paint(g);
        if (cspace != null)
        try {
            Name.gene = rw; // for backreference
            g.clearRect(0, 0, getWidth(), getHeight());
            float width = getWidth() - 4;
            float height = getHeight() - 8;
            float[][] bounds = cspace.getBounds();
            float xmul = width/(bounds[1][0] - bounds[0][0]);
            float ymul = height/(bounds[1][1] - bounds[0][1]);
            if (cspace.getDimension() == 1) {
                for (int i = 0; i < width; i++) {
                    float[] col = cspace.getValue(new float[] {
                        i/xmul,
                        0
                    });
                    g.setColor(new Color(col[0],col[1],col[2],col[3]));
                    g.fillRect(i, 0, 1, (int)height);
                }

            } else {
                long startTime = System.currentTimeMillis();
                int it0 = 0, it = 0;
                int step = curStep;
                if (avgDrawTime != null) {
                    if (avgDrawTime > 100) {
                        step = ++curStep;
                    }
                }
                BufferedImage bim = new BufferedImage((int)width,(int)height+step-1,BufferedImage.TYPE_INT_ARGB);
                WritableRaster r = bim.getWritableTile(0, 0);
                DataBuffer d = r.getDataBuffer();
                for (int j = 0; j < height-step+1; j+= step) {
                    for (int i = 0; i < width-step+1; i+= step) {
                        float[] col = cspace.getValue(new float[] {
                            i/xmul,
                            j/ymul
                        });

                        int intval = (((int)(Math.min(1f,(col[3]))*255f)) << 24) +
                                (((int)(Math.min(1f,col[0])*255f)) << 16) +
                                (((int)(Math.min(1f,col[1])*255f)) << 8) +
                                (((int)(Math.min(1f,col[2])*255f)));
                        int it1 = it;
                        for(int ys = 0; ys < step; ys++) {
                            for(int xs = 0; xs < step; xs++) {
                                d.setElem(it, intval);
                                it++;
                            }
                            it += width - step;
                        }
                        it = it1 + step;
                    }
                    it = it0 = it0 + step*(int)width;
                }
                while (!g.drawImage(bim, 2, 4, null));
                bim.releaseWritableTile(0, 0);
                long span = System.currentTimeMillis() - startTime;
                if (avgDrawTime == null)
                    avgDrawTime = span;
                else
                    avgDrawTime = (avgDrawTime + span) / 2;
            }

            int it = 0;
            for(RGBA rgba : cspace.getPivots()) {
                int bwidth = 4;
                float x = 2f + rgba.point.get(0).evaluate()*width/(bounds[1][0] - bounds[0][0]);
                float y1 = rgba.point.size() > 1 ? 
                    4f + rgba.point.get(1).evaluate()*height/(bounds[1][1] - bounds[0][1])
                    : bwidth/2;
                float y2 = y1;
                if (cspace.getDimension() == 1) {
                    y2 = getHeight()-1 - bwidth/2;
                }

                g.setColor(it == lastActive ? Color.GRAY : Color.WHITE);
                g.fillRect((int)x - bwidth/2+1, (int)y1 - bwidth/2+1, bwidth-2, (int)(y2-y1+bwidth-2));
                g.drawLine((int)(x - bwidth), (int)(y1 - bwidth), (int)(x + bwidth), (int)(y1 + bwidth));
                g.drawLine((int)(x - bwidth), (int)(y1 + bwidth), (int)(x + bwidth), (int)(y1 - bwidth));
                g.setColor(Color.BLACK);
                g.drawRect((int)x - bwidth/2, (int)y1 - bwidth/2, bwidth, (int)(y2-y1+bwidth));
                it++;
            }
        } catch (ParseException ex) {

        }
    }

    public void setColorSpace(String grammar) throws Exception{
        grammar = grammar + "\n rule shading_editor_def { }";  // no rules throws a parse exception
        ArrayList<TokenizerString> tokens = Tokenizer.retrieveTokenStrings(new StringBuilder(grammar));
        Parser parser = new Parser();
        Model model = parser.parse(tokens);
        model.initForGenerate();
        setColorSpace(model.colorSpaces.get(0));
        repaint();
    }

    
    float getColSpaceX(int x) {
        try {
            float[][] bounds = cspace.getBounds();
            float res = (x - 2f) / (getWidth() - 4f) * (bounds[1][0] - bounds[0][0]);
            return res;
        } catch (ParseException ex) {
            return Float.MIN_VALUE;
        }
    }
    float getColSpaceY(int y) {
        try {
            float[][] bounds = cspace.getBounds();
            float res = (y - 4f) / (getHeight() - 8f) * (bounds[1][1] - bounds[0][1]);
            return res;
        } catch (ParseException ex) {
            return Float.MIN_VALUE;
        }
    }

    public int getPivot(MouseEvent e, float fuzzy) {
        int ret = -1;
        float fuzzyMark = Float.MAX_VALUE;
        int it = 0;
        for(RGBA rgba : cspace.getPivots()) {
            try {
                float xsp = rgba.point.get(0).evaluate();
                float xpnl = getColSpaceX(e.getX());
                float nearness0 = Math.abs(xpnl - xsp);
                if (nearness0 <= fuzzy &&
                        (nearness0 < fuzzyMark || lastActive == it)) {
                    float ypnl = getColSpaceY(e.getY());
                    float ysp = cspace.getDimension() == 1 ? ypnl : rgba.point.get(1).evaluate();
                    float nearness = Math.abs(ypnl - ysp);
                    if (nearness <= fuzzy &&
                            (nearness < fuzzyMark || lastActive == it)) {
                        ret = it;
                        fuzzyMark = nearness0;
                    }
                }
                it++;
            } catch (Exception ex) {
            }
        }
        return ret;
    }

}

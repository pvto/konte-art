package org.konte.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.konte.model.Model;
import org.konte.model.Untransformable.EffectApply;

/**
 *
 * @author pvto
 */
public interface Canvas {

    public void initLayer(Model model, float layer);
    
    public void drawCurve(Camera camera, OutputShape shape);

    public void drawMeshPiece(Camera camera, OutputShape shape);

    public void drawPolygon(Camera camera, OutputShape shape);

    public void drawSphere(Camera camera, OutputShape shape);
    
    public void drawEffect(Camera camera, OutputShape shape, EffectApply how);

    public void applyEffects(Model model, float layer);
    
    public void init(int width, int height);
    
    public void finish();
    /**Clears this canvas by setting all pixels to the given background color.
     * 
     * @param color
     */
    public void setBackground(Color color);
    public int getWidth();
    public int getHeight();

    public BufferedImage getImage();

}


package org.konte.ui;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author pvto
 */
public class ColSpaceEditFrame extends EditFrame {
    private ColSpaceEditPane cspane;

    
    public ColSpaceEditFrame()
    {
        super(new FileNameExtensionFilter(
                "c3dg shading files", "c3dg"));
        cspane = new ColSpaceEditPane();
        this.add(cspane);
        cspane.getSaveShadingButton().addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                save();
            }
        });
    }

    @Override
    public String getText0()
    {
        return cspane.getTextArea().getText();
    }


    
}

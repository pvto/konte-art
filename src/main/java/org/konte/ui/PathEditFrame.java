
package org.konte.ui;

import javax.swing.filechooser.FileNameExtensionFilter;
import org.konte.image.SimpleCamera;
import org.konte.parse.ParseException;

/**
 *
 * @author pvto
 */
public class PathEditFrame extends EditFrame {

    private PathEditPane ppane;

    public PathEditFrame() throws ParseException 
    {
        super(new FileNameExtensionFilter(
                "c3dg path files", "c3dg"));
        ppane = new PathEditPane();
        ppane.getPathPanel().setCamera(new SimpleCamera());
        this.add(ppane);
        ppane.getSaveButton().addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                save();
            }
        });
        ppane.getJComboBox1().setSelectedIndex(1);
        ppane.changeToNewForm();
    }

    @Override
    public String getText0()
    {
        return ppane.getText();
    }

    public void setVisible(boolean b)
    {
        super.setVisible(b);
        if (b)
        {
            ppane.getPathPanel().init();
            ppane.repaint();
        }
    }

    public static void main(String[] args) throws ParseException 
    {
        PathEditFrame fr = new PathEditFrame();
        fr.setSize(700,600);
        fr.setVisible(true);
    }
}

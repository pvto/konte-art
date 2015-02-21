
package org.konte.ui;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.konte.generate.RandomFeed;


public interface EditViewInterface {


    public String getScriptText();

    public RandomFeed getRandomFeed();

    public void setCaretPosition(int caretPos);

    public void updateRenderInfo(RandomFeed randomFeed);

    public String suggestExportFileName();

    public JLabel getView();

    public BufferedImage getDisplayImage();
    public void setDisplayImage(BufferedImage image);

    public Component getEditor();

}

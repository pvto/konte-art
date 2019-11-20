package org.konte.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.konte.generate.RandomFeed;
import org.konte.misc.CommandLine;

/**<p>A split pane with text editor to left and empty panel to right.
 * <p>Plugs to Ui actions (key handling in edit TextArea).
 *
 * @author pvto
 */
public abstract class EditViewCombo extends JSplitPane implements EditViewInterface {

    static {
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/c3dg", "org.konte.ui.KonteRSTATokenMaker");
    }
    
    //JTextArea edit = new Edit();
    RSyntaxTextArea edit = new RSyntaxTextArea(30, 400);
    {
        edit.setSyntaxEditingStyle("text/c3dg");
        edit.setCodeFoldingEnabled(true);
    }

    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JLabel view = new JLabel();
    File file;
    File exportFile;
    BufferedImage image;
    
    String lastRenderKey;
    private RandomFeed randomFeed;

    public BufferedImage getImage()
    {
        return image;
    }

    public String suggestExportFileName()
    {
        return exportFile.getPath();
    }

    public void setExportFile(File exportFile)
    {
        this.exportFile = exportFile;
    }

    abstract void finishRender();

    void setText(String text)
    {
        edit.setText(text);
    }

    void updateExportFile(RandomFeed rnd)
    {
        if (getFile() != null)
        {
            String destfile = getFile().getName().replaceAll("(\\.\\w+)$", ".png").replaceAll("\\\\", "/");
            if (!destfile.endsWith(".png"))
            {
                destfile += ".png";
            }
            destfile = CommandLine.makeExportFileName(rnd.getKey(), destfile);
            setExportFile(new File(destfile));
        }

    }

    public void updateRenderInfo(RandomFeed randomFeed)
    {
        lastRenderKey = randomFeed.getKey();
        updateExportFile(randomFeed);
    }


    private String replicate(String s, int n)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
        {
            sb.append(s);
        }
        return sb.toString();
    }

    public EditViewCombo(String name, Dimension d)
    {
        super();
        this.setName(name);
        this.setDividerLocation((int) (d.getWidth() / 2.7));
        this.setDividerSize(3);

        int bWIDTH = 6;
        edit.setBorder(new javax.swing.border.EmptyBorder(bWIDTH, bWIDTH, bWIDTH, bWIDTH));
        jScrollPane1.setViewportView(edit);

        this.setLeftComponent(jScrollPane1);

        view.setBorder(new javax.swing.border.LineBorder(Color.BLACK, 1));
        jScrollPane2.setViewportView(view);
        this.setRightComponent(jScrollPane2);

    }



    public void changeFontSize(float delta)
    {
        Font f = edit.getFont();
        float newSize = f.getSize() + delta;
        Font f2 = f.deriveFont(newSize);
        edit.setFont(f2);
    }

    public float getFontSize() {
        return edit.getFont().getSize();
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public JLabel getView()
    {
        return view;
    }

    public Component getEditor()
    {
        return edit;
    }

    public String getScriptText()
    {
        return edit.getText();
    }

    public void setCaretPosition(int pos)
    {
        edit.setCaretPosition(pos);
    }

    public void setDisplayImage(BufferedImage image)
    {
        this.image = image;
        view.setIcon(new ImageIcon(image));
        view.repaint();
    }

    public BufferedImage getDisplayImage()
    {
        return image;
    }

    public RandomFeed getRandomFeed()
    {
        return this.randomFeed;
    }

    public void setRandomFeed(RandomFeed randomFeed)
    {
        this.randomFeed = randomFeed;
    }

    
}

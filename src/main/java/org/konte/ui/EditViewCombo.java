package org.konte.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.konte.generate.RandomFeed;
import org.konte.misc.CommandLine;

/**<p>A split pane with text editor to left and empty panel to right.
 * <p>Plugs to Ui actions (key handling in edit TextArea).
 *
 * @author pvto
 */
public abstract class EditViewCombo extends JSplitPane implements EditViewInterface {

    JTextArea edit = new Edit();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JLabel view = new JLabel();
    File file;
    File exportFile;
    BufferedImage image;
    
    String lastRenderKey;
    private RandomFeed randomFeed;

    public BufferedImage getImage() {
        return image;
    }

    public String suggestExportFileName() {
        return exportFile.getPath();
    }

    public void setExportFile(File exportFile) {
        this.exportFile = exportFile;
    }

    abstract void finishRender();

    void setText(String text) {
        edit.setText(text);
    }

    void updateExportFile(RandomFeed rnd) {
        if (getFile() != null) {
            String destfile = getFile().getName().replaceAll("(\\.\\w+)$", ".png").replaceAll("\\\\", "/");
            if (!destfile.endsWith(".png")) {
                destfile += ".png";
            }
            destfile = CommandLine.makeExportFileName(rnd.getKey(), destfile);
            setExportFile(new File(destfile));
        }

    }
    UndoManager undo;
    public void undo() {
        try {
            if (undo.canUndo()) {
                undo.undo();
            }
        } catch (CannotUndoException e) {
        }
    }
    public void redo() {
        try {
            if (undo.canRedo()) {
                undo.redo();
            }
        } catch (CannotRedoException e) {
        }        
    }

    public void updateRenderInfo(RandomFeed randomFeed) {
        lastRenderKey = randomFeed.getKey();
        updateExportFile(randomFeed);
    }

    private void setUndo(JTextArea edit) {
        JTextComponent textcomp = (JTextComponent) edit;
        Document doc = textcomp.getDocument();
        
        undo = new UndoManager();
        // Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener() {

            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });

        // Create an undo action and add it to the text component
        textcomp.getActionMap().put("Undo",
                new AbstractAction("Undo") {

                    public void actionPerformed(ActionEvent evt) {
                        undo();
                    }
                });

        // Bind the undo action to ctl-Z
        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        // Create a redo action and add it to the text component
        textcomp.getActionMap().put("Redo",
                new AbstractAction("Redo") {

                    public void actionPerformed(ActionEvent evt) {
                        redo();
                    }
                });

        // Bind the redo action to ctl-Y
        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

    }

    private class Edit extends JTextArea {

        @Override
        public void setText(String t) {
            t = t.replaceAll("(^\r)\n", "\r\n");
            int ind = -1;
            int cto = getCaretPosition();
            while ((ind = t.indexOf("\t")) >= 0) {
                int i0 = ind;
                while (i0 > 0 && t.charAt(i0-1) != '\n') {
                    i0--;
                }
                int add = 4 - (ind-i0+4) % 4;
                StringBuilder bd = new StringBuilder();
                for (int i = 0; i < add; i++) {
                    bd.append(" ");
                }
                t = t.replaceFirst("\t", bd.toString());
                if (getCaretPosition() == ind+1) {
                    cto = getCaretPosition()+add-1;
                }
            }
            super.setText(t);
            setCaretPosition(cto);
        }
    }

    private String replicate(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public EditViewCombo(String name, Dimension d) {
        super();
        this.setName(name);
        this.setDividerLocation((int) (d.getWidth() / 2.7));
        this.setDividerSize(3);

        edit.setColumns(400);
        edit.setRows(30);
        int bWIDTH = 6;
        edit.setBorder(new javax.swing.border.EmptyBorder(bWIDTH, bWIDTH, bWIDTH, bWIDTH));
        edit.setFont(new java.awt.Font("Courier new", Font.PLAIN, 11));

        setUndo(edit);
        jScrollPane1.setViewportView(edit);

        this.setLeftComponent(jScrollPane1);

        view.setBorder(new javax.swing.border.LineBorder(Color.BLACK, 1));
        //JPanel toRight = new JPanel();
        //BorderLayout b = new BorderLayout();
        //toRight.setLayout(b);
        jScrollPane2.setViewportView(view);
        //b.addLayoutComponent(jScrollPane2, BorderLayout.CENTER);
        this.setRightComponent(jScrollPane2);

        setHandlers();
    }

    private void setHandlers() {
        KeyListener listo = new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (!e.isActionKey() && e.getKeyChar() == '\t') {
                    edit.setText(edit.getText());
                    e.consume();
                }
            }

            public void keyPressed(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {

            }
        };
        edit.addKeyListener(listo);
        this.view.addKeyListener(listo);
    }


    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public JLabel getView() {
        return view;
    }

    public Component getEditor() {
        return edit;
    }
    
    public String getScriptText() {
        return edit.getText();
    }

    public void setCaretPosition(int pos) {
        edit.setCaretPosition(pos);
    }

    public void setDisplayImage(BufferedImage image) {
        this.image = image;
        view.setIcon(new ImageIcon(image));
        view.repaint();
    }

    public BufferedImage getDisplayImage() {
        return image;
    }

    public RandomFeed getRandomFeed() {
        return this.randomFeed;
    }

    public void setRandomFeed(RandomFeed randomFeed) {
        this.randomFeed = randomFeed;
    }

    
}

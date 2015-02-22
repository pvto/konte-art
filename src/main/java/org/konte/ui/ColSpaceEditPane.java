
/*
 * ColSpaceEditPane.java
 *
 * Created on 12.9.2009, 21:49:52
 */

package org.konte.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.konte.expression.Value;
import org.konte.model.ColorSpace;
import org.konte.model.ColorSpace.RGBA;
import org.konte.parse.ParseException;

/**
 *
 * @author pvto
 */
public class ColSpaceEditPane extends javax.swing.JPanel {

    private ColSpacePanel colSpacePanel1;

    /** Creates new form ColSpaceEditPane */
    public ColSpaceEditPane() {
        initComponents();
        colSpacePanel1 = new ColSpacePanel();
        add(colSpacePanel1, BorderLayout.CENTER);
        colSpacePanel1.addMouseMotionListener(new MouseMotionListener() {
            private int grabbedPoint = -1;
            public void mouseDragged(MouseEvent e) {
                try {
                    if (grabbedPoint == -1) {
                        ColorSpace cspace = colSpacePanel1.getColorSpace();
                        float[][] bounds = cspace.getBounds();
                        float fuzzy = (bounds[1][0] - bounds[0][0]) / 120f;
                        int pivot = colSpacePanel1.getPivot(e, fuzzy);
                        if (pivot != -1) {
                            grabbedPoint = pivot;
                            System.out.println("drag at " + pivot);
                        }
                    } else {
                        ColorSpace cspace = colSpacePanel1.getColorSpace();
                        RGBA rgba = cspace.getPivots().get(grabbedPoint);
                        float x = colSpacePanel1.getColSpaceX(e.getX());
                        float y = colSpacePanel1.getColSpaceY(e.getY());
                        rgba.point.set(0, new Value(x));
                        if (cspace.getDimension() > 1) {
                            rgba.point.set(1, new Value(y));
                        }
                        updatePointFromModel(rgba, grabbedPoint);
                        colSpacePanel1.repaint();

                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            private int i = 0;
            public void mouseMoved(MouseEvent e) {
                grabbedPoint = -1;
                if (i++ % 20 == 0)
                    updateColorspacePanel();
            }
        });
        colSpacePanel1.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    ColorSpace cspace;
                    try {
                        cspace = colSpacePanel1.getColorSpace();
                        float[][] bounds = cspace.getBounds();
                            float fuzzy = (bounds[1][0] - bounds[0][0]) / 80f;
                        int pivot = colSpacePanel1.getPivot(e, fuzzy);
                        if (pivot == -1) {
                            float[] coords = new float[] {
                                colSpacePanel1.getColSpaceX(e.getX()),
                                colSpacePanel1.getColSpaceY(e.getY())
                            };
                            insertPivot(coords);
                        } else {
                            colSpacePanel1.setLastActive(pivot);
                            changePivotColor();
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        });
    }

    private void copyToClipboard() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        ClipboardOwner owner = new ClipboardOwner() {
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
            }
        };
        StringSelection contents = new StringSelection(jTextArea1.getText());
        cb.setContents(contents, owner);
    }

    private void insertPivot() throws ParseException {
        float[] coords = new float[3];
        ColorSpace cspace = colSpacePanel1.getColorSpace();
        if (cspace != null) {
            float[][] bounds = cspace.getBounds();
            float step = 1f;
            if (cspace.getPivots().size() > 1)
                step = (bounds[1][0] - bounds[0][0])/(cspace.getPivots().size() - 1f);
            coords[0] = bounds[1][0] + step;
            if (cspace.getDimension() > 1) {
                step = 1f;
                if (cspace.getPivots().size() > 1)
                    step = (bounds[1][1] - bounds[0][1])/(cspace.getPivots().size() - 1f);
                coords[1] = bounds[1][1] + step;
            }
        }
        insertPivot(coords);
    }
    private void insertPivot(float[] coords) {
        String gram = jTextArea1.getText();
        int dim = 1;
        if (colSpacePanel1.getColorSpace() == null) {
            dim = JOptionPane.showOptionDialog(null, "Select dimension", "Insert shading control point", 0, JOptionPane.PLAIN_MESSAGE,
                null, new Object[] { " 1", " 2" }, null);
        } else {
            dim = colSpacePanel1.getColorSpace().getDimension() - 1;
        }

        if (dim >= 0) {
            dim++;
            StringBuilder point = new StringBuilder();
            point.append("    point(");
            for(int i=0; i < dim; i++) {
                point.append(coords[i]);
                if (i < dim-1)
                    point.append(", ");
            }
            point.append(") { A 1\n        RGB 0 0 0");
            if (dim > 1) {
                point.append("  s 1");
            }
            point.append(" }\n");
            gram = gram.replaceAll("\\}\\s*\\Z", point.toString() + "}");
            jTextArea1.setText(gram);
        }
    }

    private String numeric = "([0-9]+|[0-9]*\\.[0-9]+)";
    
    private void updateColorFromModel(int lastActive, float[] color) {
        String text = jTextArea1.getText();
        Pattern pattern = Pattern.compile(
                String.format("(RGB\\s+)%s(\\s+)%s(\\s+)%s([\\s/\\}])",
                numeric,numeric,numeric)
                );
        Matcher m = pattern.matcher(text);
        int count = 0;
        while(m.find()) {
            if (count++ == lastActive) {
                text = text.substring(0, m.start()) +
                            m.group(0).replaceFirst(pattern.pattern(),
                            String.format(
                                Locale.ENGLISH,
                                "$1%.3f$3%.3f$5%.3f$7",
                                color[0], color[1], color[2])).replaceAll("0.000","0").replaceAll("(\\d\\.\\d)00","$1") +
                            text.substring(m.end());
                jTextArea1.setText(text);
                return;
            }
        }
    }

    private void updatePointFromModel(RGBA rgba, int ind) throws ParseException {
        String text = jTextArea1.getText();
        String pts = numeric;
        if (this.colSpacePanel1.getColorSpace().getDimension() == 2)
            pts = numeric+"\\s*,\\s*"+numeric;
        Pattern pattern = Pattern.compile(
                String.format("(point\\s*\\(\\s*)%s(\\s*\\)\\s*\\{)",pts)
                );
        Matcher m = pattern.matcher(text);
        int ind0 = 0;
        while(m.find()) {
            if (ind0++ != ind) {
                continue;
            }
            if (colSpacePanel1.getColorSpace().getDimension() == 1) {
                text = String.format("%s%s%s",
                        text.substring(0, m.start()),

                        m.group(0).replaceFirst(pattern.pattern(),
                            String.format(Locale.ENGLISH, "$1%.3f$3", rgba.point.get(0).evaluate())),

                        text.substring(m.end()));
                jTextArea1.setText(text);
                return;
            } else if (colSpacePanel1.getColorSpace().getDimension() == 2) {
                text = String.format("%s%s%s",
                        text.substring(0, m.start()),

                        m.group(0).replaceFirst(pattern.pattern(),
                            String.format(Locale.ENGLISH, "$1%.3f, %.3f$4",
                                rgba.point.get(0).evaluate(),
                                rgba.point.get(1).evaluate())),

                        text.substring(m.end()));
                jTextArea1.setText(text);
                return;
            }
            break;
        }
    }
    private void changePivotColor() {
        if (colSpacePanel1.getLastActive() != -1) {
            RGBA rgba = colSpacePanel1.getColorSpace().getPivots().get(colSpacePanel1.getLastActive());

            Color initialColor;
            try {
                initialColor = new Color(rgba.R.evaluate(), rgba.G.evaluate(), rgba.B.evaluate(), rgba.A.evaluate());
                Color color = JColorChooser.showDialog(this, "Select color (alpha value will remain!)", initialColor);
                if (color != null) {
                    float[] cols = color.getColorComponents(null);
                    rgba.R = new Value(cols[0]);
                    rgba.G = new Value(cols[1]);
                    rgba.B = new Value(cols[2]);
//                    rgba.A = new Value(cols[3]);
                    updateColorFromModel(colSpacePanel1.getLastActive(), cols);
                    colSpacePanel1.repaint();
                }
            } catch (ParseException ex) {
            }

        } else {
            jTextField1.setText("Select a pivot first");
        }
    }

    public void updateColorspacePanel() {
        try {
            String text = jTextArea1.getText();
            colSpacePanel1.setColorSpace(text);
            jTextField1.setText("");
        } catch(Exception ex) {
            jTextField1.setText(ex instanceof NullPointerException ?
                "NullPointerException at " + ex.getStackTrace()[0].toString() :
                ex.getMessage());
        }

    }

    public JTextArea getTextArea() { return jTextArea1; }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea() {
            public void setText(String text) {
                super.setText(text);
                updateColorspacePanel();
            }
        };
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        saveShadingButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(187, 203, 209));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(240, 424));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText("shading mycolors {\n}");
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTextArea1MousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTextField1.setBackground(new java.awt.Color(187, 203, 209));
        jTextField1.setEditable(false);
        jPanel1.add(jTextField1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBackground(new java.awt.Color(187, 203, 209));

        jButton1.setBackground(new java.awt.Color(187, 203, 209));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/Hardware ad alt.png"))); // NOI18N
        jButton1.setToolTipText("Add point to model");
        jButton1.setBorder(null);
        jButton1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/Hardware ad alt.png"))); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton1MousePressed(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jButton2.setBackground(new java.awt.Color(187, 203, 209));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/drop.png"))); // NOI18N
        jButton2.setToolTipText("Select color");
        jButton2.setBorder(null);
        jButton2.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/drop.png"))); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton2MousePressed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton3.setBackground(new java.awt.Color(187, 203, 209));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/applications.png"))); // NOI18N
        jButton3.setToolTipText("Change shading name");
        jButton3.setBorder(null);
        jButton3.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/applications.png"))); // NOI18N
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton3MousePressed(evt);
            }
        });
        jPanel2.add(jButton3);

        jButton4.setBackground(new java.awt.Color(187, 203, 209));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/clipping_unknow.png"))); // NOI18N
        jButton4.setToolTipText("Copy to clipboard");
        jButton4.setBorder(null);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/clipping_unknow.png"))); // NOI18N
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton4MousePressed(evt);
            }
        });
        jPanel2.add(jButton4);

        saveShadingButton.setBackground(new java.awt.Color(187, 203, 209));
        saveShadingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/floppy.png"))); // NOI18N
        saveShadingButton.setToolTipText("Save shading");
        saveShadingButton.setBorder(null);
        saveShadingButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/ui/24/hl/floppy.png"))); // NOI18N
        saveShadingButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveShadingButtonMousePressed(evt);
            }
        });
        jPanel2.add(saveShadingButton);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MousePressed
        String gram = jTextArea1.getText();
        Pattern pattern = Pattern.compile("(shading\\s+)([a-zA-Z0-9\\.\\-_]*)(\\s*\\{)");
        Matcher matcher = pattern.matcher(gram);
        String name = "";
        if (matcher.find()) {
            name = matcher.group(2);
        }

        String name2 = JOptionPane.showInputDialog(null, "Type new name for the shading", name);
        if (name2 != null && !name.isEmpty()) {
            gram = matcher.replaceFirst(String.format("$1%s$3",name2));
            jTextArea1.setText(gram);
        }

    }//GEN-LAST:event_jButton3MousePressed

    private void jButton1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MousePressed
        try {
            insertPivot();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButton1MousePressed

    private void jTextArea1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MousePressed
        updateColorspacePanel();
    }//GEN-LAST:event_jTextArea1MousePressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void colSpacePanel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colSpacePanel1MousePressed
        updateColorspacePanel();
    }//GEN-LAST:event_colSpacePanel1MousePressed

    private void jButton2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MousePressed
        changePivotColor();
    }//GEN-LAST:event_jButton2MousePressed

    private void jButton4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MousePressed
        copyToClipboard();
    }//GEN-LAST:event_jButton4MousePressed

    private void saveShadingButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveShadingButtonMousePressed
        // TODO add your handling code here:
}//GEN-LAST:event_saveShadingButtonMousePressed


    public JButton getSaveShadingButton() {
        return saveShadingButton;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton saveShadingButton;
    // End of variables declaration//GEN-END:variables


    public static void main(String[] args) {
        JFrame fr = new JFrame();
        fr.setSize(800,600);
        ColSpaceEditPane csep = new ColSpaceEditPane();
        fr.add(csep);
        fr.setVisible(true);
    }
}

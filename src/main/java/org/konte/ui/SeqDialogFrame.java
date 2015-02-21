/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SeqDialogFrame.java
 *
 * Created on 25.12.2009, 17:38:11
 */

package org.konte.ui;

import java.text.ParseException;
import java.util.Properties;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

/**
 *
 * @author pto
 */
public class SeqDialogFrame extends javax.swing.JFrame {

    private Properties presets;
    
    /** Creates new form SeqDialogFrame */
    public SeqDialogFrame(Properties presets) {
        initComponents();
        this.presets = presets;
        loadProp(jTextField1, "seq_prefix");
        loadProp(jFormattedTextField1, "seq_frequency");
        loadProp(jFormattedTextField2, "seq_width");
        loadProp(jFormattedTextField3, "seq_height");
        loadProp(jCheckBox1, "seq_is_phase_generate");
        loadProp(jCheckBox2, "seq_is_phase_final");
    }

    private void saveAndFire() {
        saveProp(jTextField1, "seq_prefix");
        saveProp(jFormattedTextField1, "seq_frequency");
        saveProp(jFormattedTextField2, "seq_width");
        saveProp(jFormattedTextField3, "seq_height");
        saveProp(jCheckBox1, "seq_is_phase_generate");
        saveProp(jCheckBox2, "seq_is_phase_final");
        firePropertyChange("GenerateSequence", null, presets);
    }

    private void loadProp(JTextField f, String key) {
        Object val = presets.get(key);
        if (val != null)
            f.setText(val.toString());
    }

    private void loadProp(JCheckBox b, String key) {
        Object val = presets.get(key);
        if (val != null)
            b.setSelected(val.toString().toUpperCase().equals("TRUE")?true:false);
    }

    private void saveProp(JTextField f, String key) {
        if (f instanceof JFormattedTextField)
            try {
            ((JFormattedTextField) f).commitEdit();
        } catch (ParseException ex) {
            return;
        }
        String s = f.getText();
        if (s.length() > 0) {
            presets.setProperty(key, s);
        }
    }

    private void saveProp(JCheckBox b, String key) {
        presets.setProperty(key, b.isSelected()?"TRUE":"FALSE");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jFormattedTextField2 = new javax.swing.JFormattedTextField();
        jPanel18 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jFormattedTextField3 = new javax.swing.JFormattedTextField();
        jPanel19 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jPanel22 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Generate a sequence of images");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridLayout(6, 2, 12, 6));

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Filename prefix");
        jPanel8.add(jLabel1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel8);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jTextField1.setText("seq");
        jPanel3.add(jTextField1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("\"generate\" frames");
        jPanel5.add(jCheckBox1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5);

        jPanel6.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jPanel6);

        jPanel9.setLayout(new java.awt.BorderLayout());

        jCheckBox2.setSelected(true);
        jCheckBox2.setText("\"order\" frames");
        jPanel9.add(jCheckBox2, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(jPanel9);

        jPanel10.setLayout(new java.awt.BorderLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Frames per second");
        jPanel10.add(jLabel2, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel10);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel11.add(jFormattedTextField1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel11);

        jPanel12.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jPanel12);

        jPanel13.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jPanel13);

        jPanel14.setLayout(new java.awt.BorderLayout());

        jButton1.setText("Ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel14);

        jPanel15.setLayout(new java.awt.BorderLayout());

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel15.add(jButton2, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel15);

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanel7.setLayout(new java.awt.GridLayout(6, 2, 12, 6));

        jPanel16.setLayout(new java.awt.BorderLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("width (px)");
        jPanel16.add(jLabel3, java.awt.BorderLayout.PAGE_END);

        jPanel7.add(jPanel16);

        jPanel17.setLayout(new java.awt.BorderLayout());

        jFormattedTextField2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel17.add(jFormattedTextField2, java.awt.BorderLayout.CENTER);

        jPanel7.add(jPanel17);

        jPanel18.setLayout(new java.awt.BorderLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("height (px)");
        jPanel18.add(jLabel4, java.awt.BorderLayout.CENTER);

        jPanel7.add(jPanel18);

        jPanel20.setLayout(new java.awt.BorderLayout());

        jFormattedTextField3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel20.add(jFormattedTextField3, java.awt.BorderLayout.CENTER);

        jPanel7.add(jPanel20);

        jPanel19.setLayout(new java.awt.BorderLayout());
        jPanel7.add(jPanel19);

        jPanel21.setLayout(new java.awt.BorderLayout());
        jPanel7.add(jPanel21);

        jPanel22.setLayout(new java.awt.BorderLayout());
        jPanel7.add(jPanel22);

        jPanel23.setLayout(new java.awt.BorderLayout());
        jPanel7.add(jPanel23);

        jPanel1.add(jPanel7, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveAndFire();
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JFormattedTextField jFormattedTextField2;
    private javax.swing.JFormattedTextField jFormattedTextField3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}

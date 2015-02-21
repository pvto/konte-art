/*
 * About.java
 *
 * Created on 27. lokakuuta 2008, 23:17
 */

package org.konte.ui;

import org.konte.lang.Language;

/**
 *
 * @author  Paavo
 */
public class About extends javax.swing.JFrame {
    
    /** Creates new form About */
    public About() {
        initComponents();
        version.setText("v. " + Language.version);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        version = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(187, 203, 209));
        setLocationByPlatform(true);
        setResizable(false);
        setUndecorated(true);

        version.setBackground(new java.awt.Color(187, 203, 209));
        version.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        version.setText("jLabel2");
        version.setAlignmentX(0.5F);
        getContentPane().add(version, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(187, 203, 209));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBackground(new java.awt.Color(187, 203, 209));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setFocusable(false);
        jScrollPane1.setOpaque(false);

        jTextPane1.setBackground(new java.awt.Color(187, 203, 209));
        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jTextPane1.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n<p>Some Rights reserved (c) 2009-2011<br>\n Paavo Toivanen.\n    </p>\n<p>Licensed under GNU LGPL\r</p>\n<p><b>Attributions</b>\n<p>MersenneTwisterFast (Java) is by Michael Lecuyer and distributed under GNU GPL.\n<p>Mersenne Twister algorithm is due to M. Matsumoto and T. Nishimura.\n<p>'albook extended blue' icon set is by (http://stopdreaming.deviantart.com).\n<p>Albook concept is by Laurent Baumann (http://lbaumann.com/) and distributed under a Creative Common 2.5 license (http://creativecommons.org/licenses/by/2.5/).\n  </body>\r\n</html>\r\n");
        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBackground(new java.awt.Color(187, 203, 209));
        jPanel2.setAlignmentY(1.0F);
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(187, 203, 209));
        jPanel3.setFocusable(false);
        jPanel3.setMinimumSize(new java.awt.Dimension(10, 30));
        jPanel3.setLayout(new java.awt.GridBagLayout());
        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jButton1.setBackground(new java.awt.Color(187, 203, 209));
        jButton1.setText("Close");
        jButton1.setAlignmentY(1.0F);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel1.setBackground(new java.awt.Color(187, 203, 209));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/konte/resources/images/k_logo400x100.png"))); // NOI18N
        jLabel1.setText("konte");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 3, true));
        jLabel1.setIconTextGap(10);
        jPanel4.add(jLabel1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new About().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel version;
    // End of variables declaration//GEN-END:variables
    
}

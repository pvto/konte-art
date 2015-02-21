/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.konte.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.konte.generate.RandomFeed;
import org.konte.misc.Misc;

/**
 *
 * @author pto
 */
public class RndCharSliderBar extends JPanel {

    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField jTextField1;
    private int rndCharN;

    public RndCharSliderBar() {
        super();
        this.setLayout(new GridLayout(1, 2));
        this.setMaximumSize(new Dimension(140, 48));
        jTextField1 = new javax.swing.JTextField();
        jSlider1 = new javax.swing.JSlider();
        //this.setRollover(true);

        jTextField1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                changeRndCharSlider(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyReleased(java.awt.event.KeyEvent evt) {
                changeRndCharN(evt);
            }
        });
        jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                System.out.println(e);
                if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                    JTextField f = (JTextField) e.getSource();
                    int n = f.getText().length();
                    String s = "";
                    for (int i = 0; i < n; i++) {
                        s += (char) ('A' + Math.floor(Math.random() * 25));
                    }
                    f.setText(s);
                    setRndCharSlider(f.getText());
                }
            }
        });
        this.add(jTextField1);

        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                changeRndChars(evt);
            }
        });
        jSlider1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int val = jSlider1.getValue();
                    int add =
                            (e.getX() > jSlider1.getWidth() * (val - jSlider1.getMinimum()) / (jSlider1.getMaximum() - jSlider1.getMinimum()) ? 1 : -1);
                    addToSlider(add);
                }
            }
        });
        jSlider1.setSize(new Dimension(80,14));
        jSlider1.setPreferredSize(new Dimension(80,14));
        this.add(jSlider1);

        jTextField1.setFont(new java.awt.Font("Aharoni", 0, 11));
        jTextField1.setText("AAA");
        jTextField1.setPreferredSize(new Dimension(60,18));
        change();
    }

    public void addToSlider(int add) {
        String orig = jTextField1.getText();
        String newst = orig;
        int val = jSlider1.getValue();
        while (orig.equals(newst)) {
            val += add;
            newst = RandomFeed.toConvertKey(val);
        }
        val -= add;
        if (val==jSlider1.getValue())
            val += add;
        jSlider1.setValue(val);
        changeRndChars(val);

    }

    private void changeRndCharN(java.awt.event.KeyEvent evt) {
        change();
    }

    private void changeRndCharSlider(java.beans.PropertyChangeEvent evt) {
        change();
    }

    private void change() {
        JTextField f = jTextField1;
        int car = jTextField1.getCaretPosition();
        f.setText(f.getText().replaceAll("[^\\w]", "").toUpperCase());
        car = Math.min(car, f.getText().length());
        setRndCharSlider(f.getText());
        try {
            jTextField1.setCaretPosition(car);
        } catch(Exception eee) { } 
    }

    private void changeRndChars(javax.swing.event.ChangeEvent evt) {
        changeRndChars(jSlider1.getValue());
    }

    private void changeRndChars(int value) {
        this.jTextField1.setText(RandomFeed.toConvertKey(value));
    }

    private void setRndCharSlider(String s) {
        this.rndCharN = s.length();
        this.jSlider1.setMinimum((int) RandomFeed.convertKey(Misc.replicate("A", rndCharN)));
        this.jSlider1.setMaximum((int) RandomFeed.convertKey(Misc.replicate("Z", rndCharN)));
        this.jSlider1.setValue((int) RandomFeed.convertKey(s));
    }

    public String getKeyCode() {
        return this.jTextField1.getText();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (jSlider1 != null)
            jSlider1.setBackground(bg);
    }


}

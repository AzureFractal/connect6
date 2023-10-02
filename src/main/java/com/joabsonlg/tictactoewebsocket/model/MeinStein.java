/*
 * MeinStein Connect6 Copyright (c) 2007-2009 by Theo van der Storm
 *
 * This code is published on the CSVN website (http://www.csvn.nl)
 * to commemorate Theo. This is done with consent of Theo's family.
 *
 * The software is AS IS and under GPL v3, see below.
 *
 * This file is part of MeinStein Connect6.
 *
 * MeinStein Connect6 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeinStein Connect6 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeinStein Connect6.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.joabsonlg.tictactoewebsocket.model;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;

/**
 * MeinStein.java is a Java 1.4 Swing program.
 * It plays the games "EinStein wuerfelt nicht." and CONNECT-6.
 * Copyright (c) 2005, 2006 by Theo van der Storm
 *
 * @author      Theo van der Storm
 * @version     1.10.01
 */
public class MeinStein {

    final static int bSize = 19,  bSquare = bSize * bSize;
    final static int sqHalf = 15,  sqWidth = sqHalf * 2 + 1;

    private static void createAndShowGUI() throws IOException {
        MeinCtrl ctrl;
        MeinDisplay disp;
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame fdf = new JFrame("MeinStein Connect-6 v1.10.01");
        disp = new MeinDisplay();
        if (disp == null) {
            System.err.println("disp null");
        }
        disp.init();
        fdf.getContentPane().add(disp);
        fdf.setSize(bSize * sqWidth + 10, bSize * sqWidth + 33);
        fdf.setVisible(true);
        JFrame fcf = new JFrame("MeinStein Controls");
        fcf.setLocation(600,0);
        fcf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ctrl = new MeinCtrl(disp);
        if (ctrl == null) {
            System.err.println("ctrl null");
        }
        fcf.getContentPane().add(ctrl);
        fcf.pack();
        fcf.setVisible(true);

        ctrl.resetBoard();

        int central = (bSquare - 1) / 2;
        // Diag close, Mickey Mouse, Long Gun, Short Gun, Opp Diag, Waterfall
        int[] openingsSq1 = {central - 1, central + bSize - 1, central + bSize - 1, central + bSize - 1, central - bSize - 1, central + bSize - 1};
        int[] openingsSq2 = {central - bSize, central + bSize + 1, central + 2, central + 1, central + bSize + 1, central + 2 * bSize + 1};

//        for (int o = 0; o < openingsSq1.length; o++) {
//            for (int i = 700; i <= 1000; i+=300) {
//                for (int j = 700; j <= 1000; j+=300) {
//                    if (i==j) {
//                        continue;
//                    }
//                    ctrl.resetBoard();
//                    ctrl.tryMove(openingsSq1[o], openingsSq2[o], 0);
//                    ctrl.tryMove(openingsSq2[o], openingsSq1[o], 0);
//                    ctrl.depth0 = 3;
//                    ctrl.depth1 = 3;
//                    ctrl.oScoreNumerator0 = i;
//                    ctrl.oScoreNumerator1 = j;
//                    int score = ctrl.playEngineGame();
//                    System.out.println("YH Score (" + i + "," + j + "," + ctrl.depth0 + "," + ctrl.depth1 + "):" + score);
//
//                    String fileName = "./competitionScores14.txt";
//                    String str = i + "," + j + "," + ctrl.depth0 + "," + ctrl.depth1 + "," + score + "," + ctrl.timeBlack + "," + ctrl.timeWhite + "\r\n";
//                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
//                    writer.append(str);
//
//                    writer.close();
//                }
//            }
//        }
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                System.out.println("Your java.version is: " +
                    System.getProperty("java.version"));
                try {
                    createAndShowGUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

/**
 *
 * @author theo
 */
class MeinDisplay extends JPanel implements MouseListener {

    final static int bSize = 19,  bSquare = bSize * bSize;
    final static int sqHalf = 15,  sqWidth = sqHalf * 2 + 1;
    final static int white = 1,  empty = 0,  black = -1;
    private static final long serialVersionUID = -2992518955242272222L;
    BufferedImage img = new BufferedImage(
        bSize * sqWidth, bSize * sqWidth, BufferedImage.TYPE_INT_BGR);
    MeinCtrl ctrl;
    Color myBlue = new Color(20, 20, 200);
    Color myOran = new Color(240, 180, 120);
    Color myWood = new Color(222, 172, 82);
    Color darkStone = myBlue, darkSquare = Color.darkGray;
    Color lightStone = myOran, lightSquare = Color.gray;
    Font myFont = new Font(null, Font.BOLD, sqWidth / 2);
    int p1, p2 = -1;
    boolean rotatedBoard = false;
    int currentSquare = -1;

    public void flipBoard(boolean mode) {
        rotatedBoard = mode;
    }

    public void setColStone(boolean colStone) {
        if (colStone) {	// Coloured stones with dark appearance
            darkStone = myBlue;
            darkSquare = Color.darkGray;
            lightStone = myOran;
            lightSquare = Color.gray;
        } else {		// Black & White stones with light appearance
            darkStone = Color.black;
            darkSquare = Color.gray;
            lightStone = Color.white;
            lightSquare = Color.lightGray;
        }
    }

    public void drawSquare(boolean x, int num, int c, int r) {
        drawSquare(x, num, c, r, false);
    }

    public void drawSquare(boolean x, int num, int c, int r, boolean highLight) {
        // x true means white, false means black.
        Graphics2D g = img.createGraphics();
        byte[] fig = new byte[2];
        p2 = -1;
        if (rotatedBoard) {
            r = bSize - 1 - r;
            c = bSize - 1 - c;
        }
        if (highLight) {
            g.setColor(Color.green);
        } else {
            g.setColor(myWood);
        }
        g.fill(new Rectangle(c * sqWidth, r * sqWidth, sqWidth, sqWidth));
        g.setColor(Color.black);
        g.drawLine(c * sqWidth + (c == 0 ? sqHalf : 0), r * sqWidth + sqHalf, (c + 1) * sqWidth - 1 - (c == bSize - 1 ? sqHalf : 0), r * sqWidth + sqHalf);
        g.drawLine(c * sqWidth + sqHalf, r * sqWidth + (r == 0 ? sqHalf : 0), c * sqWidth + sqHalf, (r + 1) * sqWidth - 1 - (r == bSize - 1 ? sqHalf : 0));
        if (num == 0) {
            if (c % 6 == 3 && r % 6 == 3) {
                int rad = 2;
                g.fillRoundRect(c * sqWidth + sqHalf - rad, r * sqWidth + sqHalf - rad, rad + rad + 1, rad + rad + 1, rad + 1, rad + 1);
            }
            g.setColor(Color.gray);
            g.setFont(myFont);
            if (c == 0 || c == bSize - 1) {
                fig[0] = (byte) ('0' + (bSize - r) % 10);
                g.drawBytes(fig, 0, 1, c == 0 ? 0 : c * sqWidth + sqWidth * 15 / 20, r * sqWidth + sqWidth * 7 / 10);
            }
            if (r == 0 || r == bSize - 1) {
                fig[0] = (byte) ('a' + c);
                g.drawBytes(fig, 0, 1, c * sqWidth + sqWidth * 7 / 20 + 1, r == 0 ? 12 : (r + 1) * sqWidth - 3);
            }
        } else {
            String sn = String.valueOf(num);
            g.setColor(x ? lightStone : darkStone);
            g.fillOval(c * sqWidth + 1, r * sqWidth + 1, sqWidth - 2, sqWidth - 2);
            g.setColor(Color.gray);
            g.setFont(myFont);
            g.drawString(sn, c * sqWidth + 18 - 5 * sn.length(), r * sqWidth + 21);
        }
        g.dispose();
        if (highLight) {
            now();
        }
        repaint();
    }

    public void now() {
        paintImmediately(0, 0, getWidth(), getHeight());
    }

    public void mousePressed(MouseEvent e) {
        if (e.getX() >= bSize * sqWidth || e.getY() >= bSize * sqWidth) {
            return;
        }
        int sq = e.getX() / sqWidth + bSize * (e.getY() / sqWidth);
        if (rotatedBoard) {
            sq = bSquare - 1 - sq;
        }
        currentSquare = ctrl.tryMove(currentSquare, sq, e.getButton()); // 0?, 1(left), 2, 3(right)
    }

    public void refCtrl(MeinCtrl c) {
        ctrl = c;
    }

    public void init() {
        setColStone(false);
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
} // MeinDisplay

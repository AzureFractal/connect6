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
import java.awt.datatransfer.*;
import javax.swing.*;		// Use swing Timer
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.*;
import javax.imageio.stream.FileImageOutputStream;

/**
 * 
 * @author theo
 * @version 1.10.01
 */
public class MeinCtrl extends JPanel implements ActionListener {

    static final int white = 1,  empty = 0,  black = -1;
    static final int bSize = 19,  bSquare = bSize * bSize;
    static final String tabs = "\t\t\t\t";
    static final float WON = 0.5f,  LOST = -0.5f;
    static final String zeroes = "00000000000000000000000000000000";
    static final String spaces = "                                ";
    static final int chainMask[] = {0x00, //    Indexed by number of bits on.
        0x000001, 0x000003, 0x000007, 0x00000f, 0x00001f, 0x00003f, 0x00007f, 0x0000ff,
        0x0001ff, 0x0003ff, 0x0007ff, 0x000fff, 0x001fff, 0x003fff, 0x007fff, 0x00ffff,
        0x01ffff, 0x03ffff, 0x07ffff, 0x0fffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff
    };
    /** Chain Types:
     ** definitions:
     ** A line includes a live-n threat for B if B only needs to add 4-n stones to generate two threats.
     ** A line includes a dead-n threat for B if B only needs to add 4-n stones to generate one threat.
     ** implementation:
     ** live5: can make six with one stone
     ** live4: can make six with two stones
     ** live3: playing 1 stone  can generate 2 threats.
     ** LV2D3: see live2 but also dead3 at the same time!
     ** live2: playing 2 stones can generate 2 threats.
     ** dead3: playing 1 stone  can generate 1 threat.
     ** dead2: playing 2 stone  can generate 1 threat.
     **/
    static final String[] chainType = {"-", "dead2", "dead3", "liv2a", "liv2b", "liv2c", "lv2d3", "liv3a", "liv3b", "dead4", "dead5", "live4", "lv4d5", "live5", "done6"};
    // orig posVal: {0, 100, 300, 370, 400, 430, 600, 960, 1020, 2000, 2000, 4000, 4500, 4500, 1000000};
    static int[] posVal = {0, 100, 300, 400, 400, 400, 600, 1000, 1000, 1200, 1200, 2400, 2500, 2500, 1000000};
    // Suppose we have 2 live3s. this can be converted to 2 dead4s on the next turn after the opponent blocks
    // Therefore live3s are similar in value to dead4s
    static final int[] posVal0 = {0, 100, 300, 400, 400, 400, 600, 1000, 1000, 1200, 1200, 2400, 2500, 2500, 1000000};
    static final int[] posVal1 = {0, 100, 300, 400, 400, 400, 600, 1000, 1000, 1200, 1200, 2400, 2500, 2500, 1000000};
    static final int[] thrVal = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 2, 10};
    static final int[] thrLen = {1, 2, 3, 2, 2, 2, 3, 3, 3, 4, 5, 4, 5, 5, 6};
    static final int MAX_SEG_LENGTH = 15;
    static final int DEAD2 = 1,  DEAD3 = 2,  LIV2A = 3,  LIV2B = 4,  LIV2C = 5,  LV2D3 = 6,  LIV3A = 7,  LIV3B = 8,
                     DEAD4 = 9,  DEAD5 = 10,  LIVE4 = 11,  LV4D5 = 12,  LIVE5 = 13,  DONE6 = 14;
    static final int OPT_DEFEND = 1;
    static final int DISTANCE_PRUNING_THRESH = 6;
    static final int ENGINE_DRAW_STONES = 140;
    int oScoreNumerator0 = 665; // 667;
    int oScoreNumerator1 = 665; // 667;
    int depth0 = 3;
    int depth1 = 3;
    int quiet0 = 3;
    int quiet1 = 3;
    int selectionSize0 = 20;
    int selectionSize1 = 20;
    int arbValue0 = 2000;
    int arbValue1 = 2000;
    int selectionSize = 20;
    long timeWhite = 0;
    long timeBlack = 0;
    static int[] dist = new int[bSquare];
    static int logLevel = 3;
    private static final long serialVersionUID = 3678805447708336160L;
    long cutOffTime = 50000L;
    int stdDepth = 3;
    int stdQuiet = 3;
    int strategy = 0;
    String[] moveInfoStrings = new String[100];
    int[] moveInfoScores = new int[100];

    MeinDisplay disp;
    JTextArea gameEvaluationText = new JTextArea("game eval:", 23, 55);
    JTextArea gameNotation = new JTextArea("game notation", 34, 34);
    JTextArea messText = new JTextArea();
    JScrollPane messages = new JScrollPane(messText);
    JTextField cutTime = new JTextField("" + (cutOffTime / 1000));	// cut off Time!
    JButton newB = new JButton("New");
    JButton engB = new JButton("Game/EngMatch");
    JButton backB = new JButton("Back");
    JButton forwB = new JButton("Forward");
    JButton listB = new JButton("List");
    JButton pasteB = new JButton("Paste");
    JTextField movTime = new JTextField(stdDepth + "/" + stdQuiet);
    JLabel cutOffLabel = new JLabel("Cut Off (s)");
    JLabel otherLabel = new JLabel("Depth/Quiesc.");
    
    JButton calcB = new JButton("Calc");
    JButton exitB = new JButton("Exit");
    JTextField inetIO = new JTextField("Input");
    JButton inetB = new JButton("inetPlay");
    JPanel aPan = new JPanel(new GridLayout(4, 1, 2, 2));
    JPanel bPan = new JPanel(new GridLayout(4, 3, 2, 2));
    Random rnd = new Random();

    //	X is White, Y is Black.
    // Mode variables
    boolean anaMode = false, flippedMode = true, coloMode = true;
    boolean matchMode = false, whiteSetupMode = false, annotMode = false;
    int setupStone = 0;
    int highFR = -1, highTO = -1;
    NumberFormat nf = NumberFormat.getInstance();
    Position cur = new Position();
    Game curGame = null;
    int round = 1, defendCol = empty;
    String pasteString;
    LogWriter htmlLog;
    long time0, timeE;
    int countNodeVal, countTopMoveVal;
    byte[] table[] = new byte[MAX_SEG_LENGTH - 5][];
    final boolean html = false;
    MemoryDB memDB = new MemoryDB();

    public MeinCtrl(MeinDisplay d) {
        disp = d;
        disp.refCtrl(this);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(1);
        setPreferredSize(new Dimension(600, 550));
        setLayout(new BorderLayout());

        gameEvaluationText.setFont(new Font(null, Font.PLAIN, 10));
        gameNotation.setEditable(false);
        gameNotation.setFont(new Font(null, Font.PLAIN, 10));
        gameNotation.setEditable(true);
        messText.setFont(new Font(null, Font.PLAIN, 10));
        messText.setEditable(true);

        gameEvaluationText.setPreferredSize(new Dimension(500, 150));
        gameNotation.setPreferredSize(new Dimension(200, 318));
        aPan.setPreferredSize(new Dimension(250, 318));
        bPan.setPreferredSize(new Dimension(200, 318));
        messages.setPreferredSize(new Dimension(500, 200));

        add(gameEvaluationText, BorderLayout.SOUTH);
        add(gameNotation, BorderLayout.WEST);
        add(aPan, BorderLayout.CENTER);
        add(bPan, BorderLayout.EAST);

        aPan.add(newB);
        bPan.add(pasteB);
        bPan.add(calcB);
        aPan.add(listB);
        bPan.add(backB);
        bPan.add(forwB);
        aPan.add(otherLabel);
        bPan.add(movTime);
        bPan.add(engB);
        aPan.add(cutOffLabel);
        bPan.add(cutTime);
        bPan.add(exitB);

        pasteB.addActionListener(this);
        newB.addActionListener(this);
        listB.addActionListener(this);
        exitB.addActionListener(this);
        calcB.addActionListener(this);
        backB.addActionListener(this);
        forwB.addActionListener(this);
        engB.addActionListener(this);
        cutTime.addActionListener(this);
        movTime.addActionListener(this);

        validate();
        tableGen();
        resetBoard();
    }


    /**
     * moveNum ply(internal)	num (internal and stone)    ply(movelist display)
     * 1                        0                           1
     * 2                        1                           2
     * 3                        1                           2
     * 4                        2                           3
     * 5                        2                           3
     */
    public int tryMove(int i1, int i2, int button) {
        if (setupStone > 0) {
            int stone = button - 2;
            cur.setS(i2, stone);
            disp.drawSquare(stone == white, cur.num[i2], i2 % bSize, i2 / bSize);
        } else {
            if (cur.num[i2] > 0) // occupied
            {
                return -1;
            }
            if (cur.moveNum % 2 == 0) {	// first stone of a move
                int stone = cur.moveNum / 2 % 2 == 0 ? black : white;
                cur.setS(i2, stone);
                disp.drawSquare(stone == white, cur.num[i2], i2 % bSize, i2 / bSize);
            } else {				// second stone of a move
                Move m;
                cur.setS(i1, empty);
                int stone = cur.moveNum / 2 % 2 == 0 ? black : white;
                int pScore = getMovePscore(stone, i1, i2);
                // Calculate the move value
                curGame.add(m = new Move(cur.moveNum / 2, i1, i2, 0, 0, pScore, null));
                cur.makeMove(m);
                disp.drawSquare(cur.num[m.i1] % 2 == 0, cur.num[m.i1], m.i1 % bSize, m.i1 / bSize);
                disp.drawSquare(cur.num[m.i2] % 2 == 0, cur.num[m.i2], m.i2 % bSize, m.i2 / bSize);
                showGame(curGame.getPgn());
//                System.out.println("Board eval: " + curGame.getBoardScore());
            }
        }
        return i2;
    }

    public int getMovePscore(int colToMove, int i1, int i2) {
        int[] tval = new int[3], pval = new int[4];
        int pScore = 0;
        cur.evalSq(colToMove, i1, tval, pval);
        pScore += pval[0] - pval[1];
        cur.setS(i1, colToMove);
        cur.evalSq(colToMove, i2, tval, pval);
        pScore += pval[0] - pval[1];

        cur.setS(i1, empty);
        return pScore;
    }

    public void showGame(String pgn) {
        gameNotation.setText(pgn);
    }

    public void resetEvaluationString() {
        gameEvaluationText.setText("");
        for (int i=0;i<moveInfoStrings.length;i++) {
            moveInfoStrings[i] = "";
            moveInfoScores[i] = Integer.MIN_VALUE+100;
        }
    }

    public void displayEvaluationString(int score) {
        String displayString = "";
        displayString += "GameEval " + ((cur.moveNum / 2 % 2 == 0) ? "White": "Black") + ":" + score;
        displayString +=  "\n" + "BoardScore:" + curGame.getBoardScore() + "           (>0 favors white)";

        String[] sorted = IntStream.range(0, moveInfoScores.length).boxed()
                .sorted(Comparator.comparingInt(i -> -moveInfoScores[i]))
                .map(i -> moveInfoStrings[i])
                .toArray(String[]::new);

        for (int i = 0; i < sorted.length; i++) {
            displayString = displayString + "\r\n" + sorted[i];
        }

//        System.out.println(Arrays.toString(moveInfoScores));

        gameEvaluationText.setText(displayString);
    }

    boolean make6(int size, int me, int op, int bstart, int stones) {
        //	Test if I can make six using "stones" or less stones
        //	and taking into account opponent stones at "op".
        for (int bt = chainMask[6]; bt < size; bt <<= 1) {
            if ((me & bt) == bt) {
                return true;
            }
        }
        if (stones == 0) {
            return false;
        }
        for (int b = bstart; b < size; b <<= 1) {
            if ((me & b) == 0 && (op & b) == 0) {
                me |= b;
                if (make6(size, me, op, b << 1, stones - 1)) {
                    return true;
                }
                me &= ~b;
            }
        }
        return false;
    }

    public boolean testLive(int size, int me, int num) {
        boolean found;
        switch (num) {
            case 2:
                // I put stones b0 and b1
                // After every opponent stone (b2) I can make six with 1 or 2 stones
                for (int b0 = 1; b0 < size / 2; b0 <<= 1) {
                    if ((me & b0) == 0) {
                        for (int b1 = b0 << 1; b1 < size; b1 <<= 1) {
                            if ((me & b1) == 0) {
                                me |= b0 | b1;
                                found = true;
                                for (int b2 = 1; b2 < size; b2 <<= 1) {
                                    if ((me & b2) == 0) {
                                        if (!make6(size, me, b2, 1, 2)) {
                                            // Cannot make six with 1 or two stones.
                                            found = false;
                                            break;
                                        }
                                    }
                                }
                                me &= ~(b0 | b1);
                                if (found) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                break;
            case 3:
                // There is a stone b0 I can put so that
                // after every opponent stone (b2) I can make six with 1 or 2 stones
                for (int b0 = 1; b0 < size; b0 <<= 1) {
                    if ((me & b0) == 0) {
                        me |= b0;
                        found = true;
                        for (int b2 = 1; b2 < size; b2 <<= 1) {
                            if ((me & b2) == 0) {
                                if (!make6(size, me, b2, 1, 2)) {
                                    found = false;	// disproofs only b0
                                    break;
                                }
                            }
                        }
                        me &= ~b0;
                        if (found) {
                            return true;
                        }
                    }
                }
                break;
            case 4:
                // After every opponent stone (b2) I can make six with 1 or 2 stones
                for (int b2 = 1; b2 < size; b2 <<= 1) {
                    if ((me & b2) == 0) {
                        if (!make6(size, me, b2, 1, 2)) {
                            return false;		// counter example b2 found
                        }
                    }
                }
                return true;
            case 5:
                // After every opponent stone (b2) I can make six with 1 stones
                for (int b2 = 1; b2 < size; b2 <<= 1) {
                    if ((me & b2) == 0) {
                        if (!make6(size, me, b2, 1, 1)) {
                            return false;		// counter example b2 found
                        }
                    }
                }
                return true;
        }
        return false;
    }

    public void tableGen(int len) {
        int comb = 1 << len;
        byte ct;
        table[len - 6] = new byte[comb];

        for (int p = 0; p < comb; p++) {
            if (make6(comb, p, 0, 1, 0)) {
                ct = DONE6;
            } else if (testLive(comb, p, 5)) {
                ct = LIVE5;
            } else {
                boolean l4 = testLive(comb, p, 4);
                boolean d5 = make6(comb, p, 0, 1, 1);
                if (l4 && d5) {
                    ct = LV4D5;
                } else if (l4) {
                    ct = LIVE4;
                } else if (d5) {
                    ct = DEAD5;
                } else if (make6(comb, p, 0, 1, 2)) {
                    ct = DEAD4;
                } else if (testLive(comb, p, 3)) {
                    ct = LIV3B;
                } else {
                    boolean l2 = testLive(comb, p, 2);
                    boolean d3 = make6(comb, p, 0, 1, 3);
                    if (l2 && d3) {
                        ct = LV2D3;
                    } else if (l2) {
                        ct = LIV2C;
                    } else if (d3) {
                        ct = DEAD3;
                    } else if (make6(comb, p, 0, 1, 4)) {
                        ct = DEAD2;
                    } else {
                        ct = 0;
                    }
                }
            }
            table[len - 6][p] = ct;
            if (len <= (8) && ct > 0) {
                String bin = Integer.toBinaryString(p);
//                System.out.println(len + " " + chainType[ct] + " " + zeroes.substring(bin.length(), len) + bin);
            }
        }
    }

    public void tableGen() {
        for (int len = 6; len <= MAX_SEG_LENGTH; len++) {
            tableGen(len);
        }
    }

    public String timeString() {
        Calendar now = Calendar.getInstance();
        return nf.format(now.get(Calendar.DAY_OF_MONTH)) + "-" +
            nf.format(now.get(Calendar.HOUR_OF_DAY)) + ":" +
            nf.format(now.get(Calendar.MINUTE)) + " ";
    }

    public String parseHead(String game, String field) {
        int f0, f1;
        if ((f0 = game.indexOf(field + "[")) >= 0) {
            f0 += field.length() + 1;
            if ((f1 = game.indexOf("]", f0)) >= 0) {
                return game.substring(f0, f1);
            }
        }
        return field;
    }

    public int parseMoves(String gameString) {
        char[] ch = gameString.toCharArray();
        boolean comment = false, whiteWins;
        int plies = 0, f0, f1;
        Move m;
        /*
        1.j10 2.i9l10 3.j8l8 4.k8m7 5.i11j12 6.k9j9
        7.l9l7 8.k10k7 9.k11k6 10.g9j5
        or:
        (;FF[4]EV[connect6.mc.2006.sep.1.10]PB[xooox]PW[Theo van der Storm]SO[http://www.littlegolem.com]
        ;B[j10];W[i9l10];B[j8l8];W[k8m7];B[i11j12];W[k9j9];B[l9l7];W[k10k7];B[k11k6];W[g9j5])

        curGame."Little Golem"
        String event, site, date, round;
        String white, black, result, fen, start;

         */
        whiteWins = gameString.lastIndexOf('W') > gameString.lastIndexOf('B');
        curGame.event = parseHead(gameString, "EV");
        curGame.site = parseHead(gameString, "SO");
        curGame.black = parseHead(gameString, "PB");
        curGame.white = parseHead(gameString, "PW");
        curGame.result = whiteWins ? "0-1" : "1-0";
        for (int i = 5; i < ch.length; i++) {
            int i1, i2;
            if (i < ch.length - 1 && ch[i] == '1' && ch[i + 1] >= '0' && ch[i + 1] <= '9') {
                continue;	// skip first of two digits (not row 1).
            }
            if (ch[i] >= '1' && ch[i] <= '9' &&
                ch[i - 1] >= 'a' && ch[i - 1] <= 's' &&
                ch[i - 2] >= '1' && ch[i - 2] <= '9' &&
                ch[i - 3] >= 'a' && ch[i - 3] <= 's' && !comment) {
                i2 = ch[i - 1] - 'a' + bSize * (bSize + '0' - ch[i]);
                i1 = ch[i - 3] - 'a' + bSize * (bSize + '0' - ch[i - 2]);
            } else if (ch[i] >= '1' && ch[i] <= '9' &&
                ch[i - 1] >= 'a' && ch[i - 1] <= 's' &&
                ch[i - 2] >= '0' && ch[i - 2] <= '9' && ch[i - 3] == '1' &&
                ch[i - 4] >= 'a' && ch[i - 4] <= 's' && !comment) {
                i2 = ch[i - 1] - 'a' + bSize * (bSize + '0' - ch[i]);
                i1 = ch[i - 4] - 'a' + bSize * (bSize + '0' - ch[i - 2] - 10);
            } else if (ch[i] >= '0' && ch[i] <= '9' && ch[i - 1] == '1' &&
                ch[i - 2] >= 'a' && ch[i - 2] <= 's' &&
                ch[i - 3] >= '0' && ch[i - 3] <= '9' && ch[i - 4] == '1' &&
                ch[i - 5] >= 'a' && ch[i - 5] <= 's' && !comment) {
                i2 = ch[i - 2] - 'a' + bSize * (bSize + '0' - ch[i] - 10);
                i1 = ch[i - 5] - 'a' + bSize * (bSize + '0' - ch[i - 3] - 10);
            } else if (ch[i] >= '0' && ch[i] <= '9' && ch[i - 1] == '1' &&
                ch[i - 2] >= 'a' && ch[i - 2] <= 's' &&
                ch[i - 3] >= '1' && ch[i - 3] <= '9' &&
                ch[i - 4] >= 'a' && ch[i - 4] <= 's' && !comment) {
                i2 = ch[i - 2] - 'a' + bSize * (bSize + '0' - ch[i] - 10);
                i1 = ch[i - 4] - 'a' + bSize * (bSize + '0' - ch[i - 3]);
            } else {
                i1 = i2 = -1;
            }
            if (i1 >= 0) {
                String fen = cur.toBinary();
                if (tryMove(i2, i1, 0) >= 0 && tryMove(i1, i2, 0) >= 0) {
                    plies++;
                    memDB.add(fen, i1, i2,
                        plies % 2 == 0 ? curGame.black : curGame.white,
                        gameString.length(), (plies % 2 == 1) == whiteWins,
                        curGame.event + ";" + (plies % 2 == 1 ? curGame.black : curGame.white));
                } else {
                    break;	// Invalid move
                }
            }
            if (ch[i] == '{') {
                comment = true;
            }
            if (ch[i] == '}') {
                comment = false;
            }
        }
        if (html) {
            String src = saveBoardImage(curGame.black + "-" + curGame.white + "_" + curGame.result);
            htmlLog.add("<tr><td style=\"vertical-align: bottom;\"><img alt=\"" + src + "\" src=\"" + src +
                "\" style=\"width: 589px; height: 589px;\"></td>\n");
            htmlLog.add("<td style=\"vertical-align: top;\">\n" + curGame.getPgn("<br>\n") + "\n</td></tr>\n");
        }
        return plies;
    }

    public String saveBoardImage(String name) {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
        String fname = name + ".png";
        try {
            // Get first writer
            if (iter.hasNext()) {
                System.out.println("hasNext!");
                ImageWriter writer = iter.next();
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                File outputFile = new File(fname);
                FileImageOutputStream fios = new FileImageOutputStream(outputFile);
                writer.setOutput(fios);
                IIOImage image = new IIOImage(disp.img, null, null);
                writer.write(null, image, iwp);
                fios.close();
            }
        } catch (IOException e) {
            System.err.println("Unable to save BMP: " + e.toString());
        }
        return fname;
    }

    public void takeBack(int mod) {
        Move m;
        while ((m = curGame.back()) != null) {
            cur.unMakeMove(m);
            if (m.i2 >= 0) {
                disp.drawSquare(cur.num[m.i2] % 2 == 0, cur.num[m.i2], m.i2 % bSize, m.i2 / bSize);
            }
            disp.drawSquare(cur.num[m.i1] % 2 == 0, cur.num[m.i1], m.i1 % bSize, m.i1 / bSize);
            if ((~mod & ActionEvent.SHIFT_MASK) != 0) {
                break;
            }
        }
        showGame(curGame.getPgn());
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        int mod = e.getModifiers();

        if (src == pasteB) {
            if (logLevel >= 4) {
                System.out.println("Paste game or position");
            }
            try {
                int f0, f1;
                Clipboard sysCB = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transGame = sysCB.getContents(this);
                pasteString = (String) transGame.getTransferData(DataFlavor.stringFlavor);
                if ((f0 = pasteString.indexOf("(;FF")) >= 0) {
                    if (html) {
                        htmlLog = new LogWriter("html");
                        htmlLog.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                            "<html><head>\n" +
                            "<meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">\n" +
                            "<title>Connect-6</title></head>\n" +
                            "<body style=\"background-color: white; color: black;\" alink=\"#000099\" link=\"#000099\" vlink=\"#990099\">\n" +
                            "<table style=\"text-align: left;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\"><tbody>\n");
                    }
                    do {
                        if ((f1 = pasteString.indexOf("(;FF", f0 + 1)) < 0) {
                            f1 = pasteString.length();
                        }
                        cur.newStartingPos();
                        curGame = new Game("New", "Unknown", "", "01",
                            "B", "W", "*", "", cur.toString());
                        cur.makeMove(curGame.forward());	// Obligatory first black move
                        parseMoves(pasteString.substring(f0, f1));
                        f0 = f1;
                    } while (f1 < pasteString.length());
                    if (html) {
                        htmlLog.add("</tbody></table></body></html>\n");
                        htmlLog.add(null);
                    }
                } else {	// only moves without LG header
                    cur.newStartingPos();
                    curGame = new Game("New", "Unknown", "", "01",
                        "B", "W", "*", "", cur.toString());
                    cur.makeMove(curGame.forward());	// Obligatory first black move
                    parseMoves(pasteString);
                }
                drawBoard();
                showGame(curGame.getPgn());
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        } else if (src == cutTime) {
            cutOffTime = Integer.parseInt(cutTime.getText()) * 1000L;
            if (logLevel >= 1) {
                System.out.println("cutoff(ms): " + cutOffTime);
            }
        } else if (src == movTime) {
            String s = movTime.getText();
            stdDepth = s.charAt(0) - '0';
            depth0 = depth1 = stdDepth;
            if (s.length() > 2 && s.charAt(1) == '/') {
                stdQuiet = Integer.parseInt(s.substring(2));
            } else {
                stdQuiet = 0;
            }
            quiet0 = quiet1 = stdQuiet;
            if (logLevel >= 1) {
                System.out.println("depth: " + stdDepth + " quiescence: " + stdQuiet);
            }
        } else if (src == newB) {
            resetBoard();
        } else if (src == listB) {
            cur.select((cur.moveNum / 2) % 2 == 0 ? black : white, null, 0);
        } else if (src == calcB) {
            resetEvaluationString();
            if (cur.moveNum / 2 % 2 == 0) {
                strategy = 0;
                posVal = posVal0;
            } else {
                strategy = 1;
                posVal = posVal1;
            }
            int score;
            score = cur.anaPlay(stdDepth, stdQuiet, OPT_DEFEND);
            displayEvaluationString(score);
        } else if (src == engB) {
            playEngineGame();
        } else if (src == backB) {
            if (logLevel >= 4) {
                System.out.println("Back");
            }
            takeBack(mod);
        } else if (src == forwB) {
            Move m;
            if (logLevel >= 4) {
                System.out.println("Forward");
            }
            while ((m = curGame.forward()) != null) {
                cur.makeMove(m);
                disp.drawSquare(cur.num[m.i1] % 2 == 0, cur.num[m.i1], m.i1 % bSize, m.i1 / bSize);
                if (m.i2 >= 0) {
                    disp.drawSquare(cur.num[m.i2] % 2 == 0, cur.num[m.i2], m.i2 % bSize, m.i2 / bSize);
                }
                if ((~mod & ActionEvent.SHIFT_MASK) != 0) {
                    break;
                }
            }
            showGame(curGame.getPgn() + "\n" + cur.toBinary());
        } else if (src == inetIO) {
            memDB.show(inetIO.getText());
        } else if (src == exitB) {
            System.exit(0);
        }
    }

    public void drawBoard() {
        for (int i = 0; i < bSquare; i++) {
            disp.drawSquare(cur.num[i] % 2 == 0, cur.num[i], i % bSize, i / bSize);
        }
    }

    static String coord(int m) {
        int i1 = m >> 16, i2 = m & 0xffff;
        if (i1 % bSize > i2 % bSize) {
            return (char) ('a' + i2 % bSize) + String.valueOf((bSize - i2 / bSize)) +
                    (char) ('a' + i1 % bSize) + String.valueOf((bSize - i1 / bSize));
        } else {
            return (char) ('a' + i1 % bSize) + String.valueOf((bSize - i1 / bSize)) +
                    (char) ('a' + i2 % bSize) + String.valueOf((bSize - i2 / bSize));
        }
    }

    /**
     * The Game class holds the header information and moves of a game.
     * It implements methods to move back and forward in the game,
     * to add moves to it and to create PGN (portable game notation).
     *
     * Example PGN header:
     * [Event "Champion vs Computer"]
     * [Site "Philadelphia"]
     * [Date "1996.02.10"]
     * [Round "01"]
     * [White "MeinStein"]
     * [Black "Keinstein"]
     * [Result "1-0"]
     */
    class Game {

        String event, site, date, round;
        String white, black, result, fen, start;
        int ply;
        Move move[];

        public Game(String event, String site, String date, String round,
            String black, String white, String result, String fen, String start) {
            Calendar now = Calendar.getInstance();
            if (true == event.isEmpty()) {
                this.event = event + " @ " +
                    now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            } else {
                this.event = event;
            }
            if (true == date.isEmpty()) {
                this.date = now.get(Calendar.YEAR) + "." + (1 + now.get(Calendar.MONTH)) + "." +
                    now.get(Calendar.DAY_OF_MONTH);
            } else {
                this.date = date;
            }
            this.site = site;
            this.round = round;
            this.white = white;
            this.black = black;
            this.result = result;
            this.fen = fen;
            this.start = start;
            ply = 0;
            move = new Move[1 + bSquare / 2];
            add(new Move(ply, (bSquare - 1) / 2, -1, 0, 0, 0, null));
            back();
        }

        /**
         * setResult	At the end of the game this method should be called
         * to finalise this object for logging to a file.
         * @param	result	the result to set, i.e. "1-0" or "0-1"
         */
        public void setResult(String result) {
            this.result = result;
        }

        public void add(Move playedMove) {
            move[ply++] = playedMove;

            // After replacing a move, the replacement becomes
            // the final move of the game.
            move[ply] = null;
        }

        public Move back() {
            result = "*";
            return ply > 0 ? move[--ply] : null;
        }

        public Move forward() {
            if (move[ply] != null) {
                return move[ply++];
            }
            return null;
        }

        public Move played() {
            return move[ply];
        }

        public void setPly(int ply) {
            this.ply = ply;
        }

        public int getBoardScore() {
            int boardScore = 0;
            for (int i = 0; i < ply; i++) {
                if (move[i] != null) {
                    boardScore += (i % 2 == 0 ? -1 : +1) * move[i].pScore;
                }
            }
            return boardScore;
        }

        String getPgn() {
            return getPgn("\n");
        }

        String getPgn(String eol) {
            StringBuffer strBuf = new StringBuffer();
            String c;
            // Add header
            strBuf.append("[Event \"" + event + "\"]" + eol +
                "[Site \"" + site + "\"]" + eol +
                "[Date \"" + date + "\"]" + eol +
                "[Round \"" + round + "\"]" + eol +
                "[Black \"" + black + "\"]" + eol +
                "[White \"" + white + "\"]" + eol +
                "[Result \"" + result + "\"]" + eol);

            // Add moves and comments
            for (int i = 0; i < ply; i++) {
                strBuf.append(move[i].toString());
                strBuf.append(i % 2 == 0 ? " " : eol);
            }
            strBuf.append(result + eol);
            return strBuf.toString();
        }

        /**
         * Save this game or a string to the log file.
         */
        public void save() {
            log(null);
        }

        public void log(String s) {
            LogWriter lw = new LogWriter("pgn");
            lw.add(s == null ? getPgn() + "\n" : "{ " + s + "}\n");
            lw.add(null);
        }
    }

    /**
     * 
     */
    static class Move implements Comparable, Cloneable {

        int ply, i1, i2, score, oScore, pScore, tval0, tval1;

        public Move() {
        }

        public Move(int ply, int i1, int i2, int score, int oScore, int pScore, int tval[]) {
            this.ply = ply;
            this.i1 = i1;
            this.i2 = i2;
            this.score = score;
            this.oScore = oScore;
            this.pScore = pScore;
            if (tval == null) {
                tval0 = tval1 = 0;
            } else {
                tval0 = tval[0];
                tval1 = tval[1];
            }
        }

        public void set(int ply, int i1, int i2, int score, int oScore, int pScore, int tval[]) {
            this.ply = ply;
            this.i1 = i1;
            this.i2 = i2;
            this.score = score;
            this.oScore = oScore;
            this.pScore = pScore;
            if (tval == null) {
                tval0 = tval1 = 0;
            } else {
                tval0 = tval[0];
                tval1 = tval[1];
            }
        }

        /**
         * Create algebraic notation for this move.
         *
         * @return	String with algebraic notation for this move.
         */
        @Override
        public String toString() {
            String s = (ply + 1) + "." + (ply % 2 == 0 ? 'B' : 'W') + (char) ('a' + i1 % bSize) + String.valueOf(bSize - i1 / bSize) +
                (i2 < 0 ? "" : (char) ('a' + i2 % bSize) + String.valueOf(bSize - i2 / bSize));
            return s + spaces.substring(s.length(), 11);
        }

        public int compareTo(Object p) {
            // default order: descending (highest score first)
            return ((Move) p).score - score;
        }

        public boolean equals(Move m) {
            if (m == null) {
                return false;
            }
            return this.i1 == m.i1 && this.i2 == m.i2;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        }
    }

    /**
     * Internal class for defining the position.
     * The index of the table:
     *  0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	17	18
     *  19	20	21	22	23	24	25	26	27	28	29	30	31	32	33	34	35	36	37
     *  38	39	40	41	42	43	44	45	46	47	48	49	50	51	52	53	54	55	56
     *  57	58	59	60	61	62	63	64	65	66	67	68	69	70	71	72	73	74	75
     *  76	77	78	79	80	81	82	83	84	85	86	87	88	89	90	91	92	93	94
     *  95	96	97	98	99	100	101	102	103	104	105	106	107	108	109	110	111	112	113
     *  114	115	116	117	118	119	120	121	122	123	124	125	126	127	128	129	130	131	132
     *  133	134	135	136	137	138	139	140	141	142	143	144	145	146	147	148	149	150	151
     *  152	153	154	155	156	157	158	159	160	161	162	163	164	165	166	167	168	169	170
     *  171	172	173	174	175	176	177	178	179	180	181	182	183	184	185	186	187	188	189
     *  190	191	192	193	194	195	196	197	198	199	200	201	202	203	204	205	206	207	208
     *  209	210	211	212	213	214	215	216	217	218	219	220	221	222	223	224	225	226	227
     *  228	229	230	231	232	233	234	235	236	237	238	239	240	241	242	243	244	245	246
     *  247	248	249	250	251	252	253	254	255	256	257	258	259	260	261	262	263	264	265
     *  266	267	268	269	270	271	272	273	274	275	276	277	278	279	280	281	282	283	284
     *  285	286	287	288	289	290	291	292	293	294	295	296	297	298	299	300	301	302	303
     *  304	305	306	307	308	309	310	311	312	313	314	315	316	317	318	319	320	321	322
     *  323	324	325	326	327	328	329	330	331	332	333	334	335	336	337	338	339	340	341
     *  342	343	344	345	346	347	348	349	350	351	352	353	354	355	356	357	358	359	360
     */
    class Position {
        final int PV_SIZE = 100;//,  SELECTION_SIZE = 40;
        Move[] listMoves = new Move[bSquare], seldMoves = new Move[bSquare * bSquare / 2];
        int[][] bla = new int[6][bSize], whi = new int[6][bSize];		//   | - / / \ \
        int[] lenS = new int[4], updB = new int[4], updW = new int[4], listLen = new int[3];
        int[] num = new int[bSquare];
        // pvar[s][k] contains principal variation of candidate s at the k step
        // The pvar[s][PV_SIZE] entry contains the score of the move
        int[][] pvar = new int[PV_SIZE][PV_SIZE + 1];
        int[] svar = new int[PV_SIZE];
        int qDepth = 0, moveNum, mir;
        private boolean select1[] = new boolean[bSquare];
        boolean optionDefend = false;

        public Position() {
            for (int i = 0; i < listMoves.length; i++) {
                listMoves[i] = new Move();
            }
            for (int i = 0; i < seldMoves.length; i++) {
                seldMoves[i] = new Move();
            }
            newStartingPos();
        }

        public void newStartingPos() {
            for (int sq = 0; sq < bSquare; sq++) {
                // YH Edit: Subtract 9 from this
                dist[sq] = Math.max(Math.abs(sq % bSize - 9), Math.abs(sq / bSize - 9)); // -9 +
                num[sq] = 0;
            }
            for (int i = 0; i < whi.length; i++) {
                for (int j = 0; j < whi[0].length; j++) {
                    bla[i][j] = whi[i][j] = 0;
                }
            }
            moveNum = 1;
        }

        int place(int sq) {
            if (num[sq] == 0) {
                return empty;
            }
            return num[sq] % 2 == 0 ? white : black;
        }

        public int mirror(int i, int mir) {
            int x = i % bSize;
            int y = i / bSize;
            if ((mir & 1) != 0) {
                int c = x;
                x = y;
                y = c;
            }
            if ((mir & 2) != 0) {
                x = bSize - 1 - x;
            }
            if ((mir & 4) != 0) {
                y = bSize - 1 - y;
            }
            if ((mir & 8) != 0) {
                int c = x;
                x = y;
                y = c;
            }
            return bSize * y + x;
        }

        public int[] normalise() {
            int n = 0, sx = 0, sy = 0, numNorm[] = new int[bSquare];
            for (int i = 0; i < bSquare; i++) {
                if (num[i] != 0) {
                    n++;
                    sx += i % bSize;
                    sy += i / bSize;
                }
                numNorm[i] = 0;
            }
            sx -= 9 * n;
            sy -= 9 * n;
            mir = 0;
            if (sx < 0) {
                mir |= 2;
            }
            if (sy < 0) {
                mir |= 4;
            }
            if (sx < sy) {
                mir |= 8;
            }
            for (int i = 0; i < bSquare; i++) {
                if (num[i] != 0) {
                    numNorm[mirror(i, mir)] = num[i];
                }
            }
            return numNorm;
        }

        public String toBinary() {
            StringBuffer strBuf = new StringBuffer();
            String moveString;
            int n = 0, nn[] = normalise();
            for (int i = 0; i < bSquare; i++) {
                if (nn[i] == 0) {
                    n++;
                } else if (nn[i] % 2 == 0) {
                    if (n > 0) {
                        if (n > 4) {
                            strBuf.append(String.valueOf(n));
                        } else {
                            strBuf.append("EEEE".substring(4 - n));
                        }
                        n = 0;
                    }
                    strBuf.append("W");
                } else {
                    if (n > 0) {
                        if (n > 4) {
                            strBuf.append(String.valueOf(n));
                        } else {
                            strBuf.append("EEEE".substring(4 - n));
                        }
                        n = 0;
                    }
                    strBuf.append("B");
                }
            }
            if (n > 0) {
                if (n > 4) {
                    strBuf.append(String.valueOf(n));
                } else {
                    strBuf.append("EEEE".substring(4 - n));
                }
            }
            strBuf.append((moveNum / 2 % 2 == 0 ? " b " : " w ") + String.valueOf(moveNum / 2 + 1) + (char) ('A' + mir));
            return strBuf.toString();
        }

        @Override
        public String toString() {
            StringBuffer strBuf = new StringBuffer();
            int n, curr;
            for (int r = 0; r < bSquare; r += bSize) {
                curr = place(r);
                n = 0;
                for (int c = 0; c < bSize; c++) {
                    if (curr == place(r + c)) {
                        n++;
                    } else {
                        switch (curr) {
                            case empty:
                                strBuf.append(String.valueOf(n));
                                break;
                            case white:
                                strBuf.append(String.valueOf((char) ('A' + n - 1)));
                                break;
                            case black:
                                strBuf.append(String.valueOf((char) ('a' + n - 1)));
                                break;
                        }
                        curr = place(r + c);
                        n = 1;
                    }
                }
                switch (curr) {
                    case empty:
                        strBuf.append(String.valueOf(n));
                        break;
                    case white:
                        strBuf.append(String.valueOf((char) ('A' + n - 1)));
                        break;
                    case black:
                        strBuf.append(String.valueOf((char) ('a' + n - 1)));
                        break;
                }
                if (r < bSquare - bSize) {
                    strBuf.append("/");
                }
            }
            strBuf.append((moveNum / 2 % 2 == 0 ? " b " : " w ") + (moveNum / 2 + 1));
            return strBuf.toString() + "\n" + toBinary();
        }

        public String[][] toStringArray() {
            String[][] result = new String[bSize][bSize];
            for (int i = 0; i < bSize; i++) {
                for (int j = 0; j < bSize; j++) {
                    result[i][j] = Integer.toString(num[i*bSize + j]);
                }
            }

            return result;
        }

        public int makeMove(Move m) {
            setS(m.i1, m.ply % 2 == 0 ? black : white);
            if (m.i2 >= 0) {
                setS(m.i2, m.ply % 2 == 0 ? black : white);
            }
            return 0;
        }

        public int unMakeMove(Move m) {
            if (m.i2 >= 0) {
                setS(m.i2, empty);
            }
            setS(m.i1, empty);
            return 0;
        }

        public void setS(int sq, int stone) {
            int sh, sl, c = sq % bSize, r = sq / bSize;
            if ((bla[0][c] & (1 << r)) != 0) {		// remove black stone
                bla[0][c] &= ~(1 << r);
                bla[1][r] &= ~(1 << c);
                sl = (r + c) % bSize;
                if ((sh = c - sl - 1) < 0) {
                    bla[2][sl] &= ~(1 << c);
                } else {
                    bla[3][sl] &= ~(1 << sh);
                }
                sl = (bSize + r - c) % bSize;
                if ((sh = c + sl - bSize) < 0) {
                    bla[4][sl] &= ~(1 << c);
                } else {
                    bla[5][sl] &= ~(1 << sh);
                }
                num[sq] = 0;
                if (moveNum > 1) {
                    moveNum--;
                }
            } else if ((whi[0][c] & (1 << r)) != 0) {	// remove white stone
                whi[0][c] &= ~(1 << r);
                whi[1][r] &= ~(1 << c);
                sl = (r + c) % bSize;
                if ((sh = c - sl - 1) < 0) {
                    whi[2][sl] &= ~(1 << c);
                } else {
                    whi[3][sl] &= ~(1 << sh);
                }
                sl = (bSize + r - c) % bSize;
                if ((sh = c + sl - bSize) < 0) {
                    whi[4][sl] &= ~(1 << c);
                } else {
                    whi[5][sl] &= ~(1 << sh);
                }
                num[sq] = 0;
                if (moveNum > 1) {
                    moveNum--;
                }
            } else if (stone == white) {		// play white stone
                updW[0] = (whi[0][c] |= 1 << r);
                updW[1] = (whi[1][r] |= 1 << c);
                sl = (r + c) % bSize;
                if ((sh = c - sl - 1) < 0) {
                    updW[2] = (whi[2][sl] |= 1 << c);
                } else {
                    updW[2] = (whi[3][sl] |= 1 << sh);
                }
                sl = (bSize + r - c) % bSize;
                if ((sh = c + sl - bSize) < 0) {
                    updW[3] = (whi[4][sl] |= 1 << c);
                } else {
                    updW[3] = (whi[5][sl] |= 1 << sh);
                }
                num[sq] = moveNum++ / 2 + 1;
            } else if (stone == black) {		// play black stone
                updB[0] = (bla[0][c] |= 1 << r);
                updB[1] = (bla[1][r] |= 1 << c);
                sl = (r + c) % bSize;
                if ((sh = c - sl - 1) < 0) {
                    updB[2] = (bla[2][sl] |= 1 << c);
                } else {
                    updB[2] = (bla[3][sl] |= 1 << sh);
                }
                sl = (bSize + r - c) % bSize;
                if ((sh = c + sl - bSize) < 0) {
                    updB[3] = (bla[4][sl] |= 1 << c);
                } else {
                    updB[3] = (bla[5][sl] |= 1 << sh);
                }
                num[sq] = moveNum++ / 2 + 1;
            } else {			// Retrieve the 4 slices associated with the square
                updW[0] = whi[0][c];
                updB[0] = bla[0][c];
                lenS[0] = bSize;
                updW[1] = whi[1][r];
                updB[1] = bla[1][r];
                lenS[1] = bSize;
                sl = (r + c) % bSize;
                if ((sh = c - sl - 1) < 0) {
                    updW[2] = whi[2][sl];
                    updB[2] = bla[2][sl];
                    lenS[2] = sl + 1;
                } else {
                    updW[2] = whi[3][sl];
                    updB[2] = bla[3][sl];
                    lenS[2] = bSize - sl - 1;
                }
                sl = (bSize + r - c) % bSize;
                if ((sh = c + sl - bSize) < 0) {
                    updW[3] = whi[4][sl];
                    updB[3] = bla[4][sl];
                    lenS[3] = bSize - sl;
                } else {
                    updW[3] = whi[5][sl];
                    updB[3] = bla[5][sl];
                    lenS[3] = sl;
                }
            }
        }

        public String pvString(int s) {
            String pv = "";
            for (int d = s; pvar[s][d] != 0; d++) {
                pv = pv + " " + coord(pvar[s][d]);
            }
            // Invert the score here to make more sense???
            return pv + " (" + (-pvar[s][PV_SIZE]) + ")";
        }

        public int anaPlay(int depth, int qd, int options) {
            int d, score, col = (moveNum / 2) % 2 == 0 ? black : white;

            qDepth = qd;
            optionDefend = options == OPT_DEFEND;
            defendCol = empty;
            time0 = System.currentTimeMillis();
            timeE = time0 + cutOffTime;
            countNodeVal = 0;
            countTopMoveVal = 0;
            System.out.println(this);
            pvar[0][0] = 0;
            System.out.println("Guess:" + coord(pvar[0][1]) + coord(pvar[0][2]) + coord(pvar[0][3]));
            score = cur.pvs(-posVal[DONE6] + 1000, posVal[DONE6] - 1000, col, depth, 0, eval(col));
            if (pvar[0][0] > 0) {
                tryMove(pvar[0][0] >> 16, pvar[0][0] & 0xffff, 0);
                tryMove(pvar[0][0] & 0xffff, pvar[0][0] >> 16, 0);
                System.out.println("PV: " + pvString(0));
                for (d = 1; d < depth + qDepth && pvar[0][d] != 0; d++) {
                    tryMove(pvar[0][d] >> 16, pvar[0][d] & 0xffff, 0);
                    tryMove(pvar[0][d] & 0xffff, pvar[0][d] >> 16, 0);
                }
                for (; d > 1; d--) {
                    takeBack(0);
                }
            }
            long runTime = (System.currentTimeMillis() - time0);
            System.out.println(runTime + "ms. nodes: " + countTopMoveVal + "/" + countNodeVal);
            if (col==black) {timeBlack += runTime;} else {timeWhite += runTime;};
            return score;
        }

        // Perform Principal Variation Search
        @SuppressWarnings("empty-statement")
        public int pvs(int alpha, int beta, int col, int depth, int pv, int dScore) {
            int score, max = Integer.MIN_VALUE;
            // Select some number of moves
            Move moves[] = new Move[depth > 0 ? selectionSize : selectionSize / 2];
            int nMoves = select(col, moves, pv);
            boolean go = timeE != 0L;

            for (int m = 0; m < nMoves && go; m++) {
                pvar[pv + 1][pv + 1] = 0;		// Remove old move
                if (moves[m].score > posVal[DONE6] / 2) {
                    score = posVal[DONE6] - pv;	// making 6.
                    go = false;
                } else {
                    int newDepth = depth - 1;
                    boolean deeper = newDepth > 0;
                    score = dScore + moves[m].pScore - (strategy == 0 ? arbValue0 : arbValue1);
                    if (deeper) {
                        defendCol = empty;
                    }
                    if (optionDefend && !deeper && -qDepth < newDepth) {
//                        System.out.println("Possibly go deeper:" + moves[m].tval0 + "," + moves[m].tval1);
                        // Quiescence search
                        // Color col is defending
                        if (moves[m].tval1 <= -2 && col != -defendCol) {
                            deeper = true;
                            if (defendCol == empty) {
                                defendCol = col;
                            }
                            for (int i = 1; i < nMoves && go; i++) {
                                if (moves[i].tval1 > -2) {
                                    nMoves = i;
                                }
                            }
                        // Color col is attacking
                        } else if (moves[m].tval0 >= 2 && col != defendCol) {
                            deeper = score < beta;
                            if (defendCol == empty) {
                                defendCol = -col;
                            }
                            for (int i = 1; i < nMoves && go; i++) {
                                if (moves[i].tval0 < 2) {
                                    nMoves = i;
                                }
                            }
                        }
                    }
                    if (deeper) {
                        svar[pv] = (moves[m].i1 << 16) | moves[m].i2;
                        makeMove(moves[m]);
                        score = -pvs(-beta, -Math.max(max, alpha), -col, newDepth, pv + 1, -score);
                        unMakeMove(moves[m]);
                    } else {
                        // Final ply already sorted by move quality, so we can stop after analyzing first move
                        go = false;
                    }
                }
                if (pv == 0) {
                    moveInfoStrings[m] = (m + ": " + alpha + "," + beta + " " + moves[m].score + " " + moves[m] + pvString(1));
                    moveInfoScores[m] = score;
                    System.out.println(m + ": " + alpha + "," + beta + " " + moves[m].score + " " + moves[m] + pvString(1));
                }
                if (max < score && timeE != 0L) {
                    if (pv == 0) {
                        System.out.println(m + "=========================================================");
                    }
                    pvar[pv][pv] = (moves[m].i1 << 16) | moves[m].i2;
                    pvar[pv][PV_SIZE] = max = score;
                    for (int d = pv + 1; (pvar[pv][d] = pvar[pv + 1][d]) != 0; d++);
                    pvar[pv + 1][pv + 1] = 0;		// Remove old move
                    // Beta cutoff
                    if (beta <= score) {
//                        System.out.print("[" + m + "/" + moves.length + "]");
                        return score;
                    }
                }
                countNodeVal++;
                countTopMoveVal++;
                if (System.currentTimeMillis() >= timeE) {
                    timeE = 0L;
                    return max;
                }
            }
            return max;
        }

        @SuppressWarnings("empty-statement")
        public void evalSq(int col, int sq, int tval[], int pval[]) {
            Slice slice = new Slice();
            int maxLen = 0;
            pval[0] = -dist[sq];
            // pval0 color's value if color plays sq
            // pval1 notColor's value if color plays sq
            // pval2 notColor's value if notColor plays sq
            // pval3 color's value if notColor plays sq

            pval[3] = pval[2] = pval[1] = tval[0] = tval[1] = 0;
            // Calculate values as a baseline without playing sq
            setS(sq, empty);
            for (int d = 0; d < updB.length; d++) {
                if (col == black) {
                    slice.init(lenS[d], updB[d], updW[d]);
                } else {
                    slice.init(lenS[d], updW[d], updB[d]);
                }
                while (slice.nextSegment());
                tval[0] -= slice.tval[0];
                tval[1] -= slice.tval[1];
                pval[0] -= slice.pval[0];
                pval[1] -= slice.pval[1];
            }
            pval[2] = pval[1];
            pval[3] = pval[0];
            // Calculate values if opponent would play sq
            setS(sq, -col);
            for (int d = 0; d < updB.length; d++) {
                if (-col == black) {
                    slice.init(lenS[d], updB[d], updW[d]);
                } else {
                    slice.init(lenS[d], updW[d], updB[d]);
                }
                while (slice.nextSegment());
                if (slice.pval[0] > posVal[DONE6] / 2) {
                    pval[2] += slice.pval[0] - posVal[DONE6];
                } else {
                    pval[2] += slice.pval[0];
                }
                pval[3] += slice.pval[1];
            }
            setS(sq, empty);

            // Calculate values if player would play sq
            setS(sq, col);
            for (int d = 0; d < updB.length; d++) {
                if (col == black) {
                    slice.init(lenS[d], updB[d], updW[d]);
                } else {
                    slice.init(lenS[d], updW[d], updB[d]);
                }
                while (slice.nextSegment());
                tval[0] += slice.tval[0];
                tval[1] += slice.tval[1];
                pval[0] += slice.pval[0];
                pval[1] += slice.pval[1];
                if (maxLen < slice.tval[2]) {
                    maxLen = slice.tval[2];
                }
            }
            tval[2] = maxLen;
            setS(sq, empty);
        }

        // WLog the first move is "closer" to main body than second
        public void listEval(int colToMove) {
            int score, oScore, pScore, minVal = -posVal[DONE6];
            int[] tval = new int[3], pval = new int[4];

            listLen[1] = 0;

            for (int sq = 0; sq < bSquare; sq++) {
                select1[sq] = false;
                if (num[sq] != empty) // not empty
                {
                    continue;
                }

                evalSq(colToMove, sq, tval, pval);
                pScore = pval[0] - pval[1]; // Change in color's score if color plays sq
                oScore = pval[2] - pval[3]; // Change in notColor's score if notColor plays sq
                if (tval[2] >= 5) {
                    score = minVal = posVal[DONE6];	// select only winning squares
                } else {
                    score = 10000 * tval[0] - 100000 * tval[1] + pScore + oScore * (strategy == 0? oScoreNumerator0 : oScoreNumerator1)/ 1000; // 2 / 3;
                }
                if (closestStoneDistance(sq) >= DISTANCE_PRUNING_THRESH) {
                    score -= 60000;
                }
                if (score >= minVal) {	//	 || sq == 254
                    listMoves[listLen[1]++].set(moveNum / 2, sq, -1, score, oScore, pScore, tval);
                }
            }
            Arrays.sort(listMoves, 0, listLen[1]);

            // YH: Changed this from DEAD3 to DEAD2, but is a bad idea?
            if (minVal < posVal[DEAD3] * (1000 + (strategy == 0? oScoreNumerator0 : oScoreNumerator1)) / 1660) {
                minVal = posVal[DEAD3] * (1000 + (strategy == 0? oScoreNumerator0 : oScoreNumerator1)) / 1660;
            }

            for (int s = listLen[0] = 0; s < listMoves.length; s++) {
                if (listMoves[s].score <= 0) {
                    listLen[1] = s;
                    break;
                } else if (listMoves[s].score >= minVal) {
                    listLen[0]++;
                }
            }
//            assert listLen[0] > 0: "listLen[0] should be positive";
//            assert listLen[1] > 0: "listLen[1] should be positive";
//            assert listLen[1] > listLen[0]: "listLen[1] should be more than listLen[0]";

//            listLen[0] = Math.min(listLen[0], 20);
//            if (true) {
//                for (int kk = 0; kk < listMoves.length;kk+=10) {
//                    System.out.println(listMoves[kk].toString() + listMoves[kk].score + "," + (kk < listLen[0]));
//                }
//            }
            listLen[0] = Math.min(listLen[1], listLen[0] + (minVal == posVal[DONE6] ? 0 : 4));
            for (int s = 0; s < listLen[0]; s++) {
                select1[listMoves[s].i1] = true;
            }
        }

        boolean sameSlice(int sq1, int sq2) {
            int d = Math.abs(sq1 - sq2);
            return d < 12 || d % bSize == 0 || d % (bSize - 1) == 0 || d % (bSize + 1) == 0;
        }

        int l1Distance(int sq1, int sq2) {
            int x_dist = Math.abs((sq1 % bSize) - (sq2 % bSize));
            int y_dist = Math.abs((sq1 / bSize) - (sq2 / bSize));

            return Math.max(x_dist, y_dist);
        }

        int closestStoneDistance(int sq1) {
            int closestDistance = bSize;
            for (int sq2 = 0; sq2 < bSquare; sq2++) {
                if (num[sq2] != 0) {
                    closestDistance = Math.min(l1Distance(sq1, sq2), closestDistance);
                }
            }

            return closestDistance;
        }

        public int select(int colToMove, Move moves[], int pv) {
            int[] tval = new int[3], pval = new int[4];
            int score, oScore, pScore;
            boolean won = false;

            listEval(colToMove);
//            int[] closestStoneDistanceWithSq1 = new int[listLen[0]];
//            for (int n1 = 0; n1 < listLen[0]; n1++) {
//                int sq2, sq1 = listMoves[n1].i1;
//                closestStoneDistanceWithSq1[n1] = closestStoneDistance(sq1);
//            }
            // Loop over square 1
            for (int n1 = listLen[2] = 0; n1 < listLen[0] && !won; n1++) {
                int sq2, sq1 = listMoves[n1].i1;
                assert num[sq1] == empty: "First square wasn't empty when we tried to place";
                setS(sq1, colToMove);

                // Loop over square 2
                for (int n2 = n1 + 1; n2 < listLen[1] && !won; n2++) {
                    sq2 = listMoves[n2].i1;
                    if (listMoves[n1].score < posVal[DEAD4] && n1 + n1 > listLen[0] && !sameSlice(sq1, sq2)) {
                        continue;
                    }
//                    System.out.println((sq2 % bSize) + "," + (sq2 / bSize) + "," + closestStoneDistance(sq2));
//                    // Conservative
//                    if (Math.min(closestStoneDistanceWithSq1[n1], l1Distance(sq1, sq2)) >= 4){//+0*DISTANCE_PRUNING_THRESH) {
//                        continue;
//                    }
                    evalSq(colToMove, sq2, tval, pval);
                    pScore = listMoves[n1].pScore + pval[0] - pval[1];
                    oScore = listMoves[n1].oScore + pval[2] - pval[3];
                    if (tval[2] >= 6) {
                        score = posVal[DONE6];
                        won = true;
                        listLen[2] = 0;
                    } else {
                        if (listMoves[n1].tval0 == 0 && tval[0] > 0) {
                            score = posVal[DEAD4];
                        } else {
                            score = 0;
                        }
                        tval[0] += listMoves[n1].tval0;
                        tval[1] += listMoves[n1].tval1;
                        score += 10000 * tval[0] - 100000 * tval[1] + pScore + oScore * (strategy == 0? oScoreNumerator0 : oScoreNumerator1) / 1000; // 2 / 3;
//                        if (true || strategy == 1) {
//                            if (pv == 0) {
//                                if (((pvar[0][pv + 1] >> 16) == sq1 && (pvar[0][pv + 1] & 0xffff) == sq2) ||
//                                        ((pvar[0][pv + 1] >> 16) == sq2 && (pvar[0][pv + 1] & 0xffff) == sq1)) {
//                                    score += 5000;
//                                }
//                            }
//                        }
                    }
                    seldMoves[listLen[2]++].set(cur.moveNum / 2, sq1, sq2, score, oScore, pScore, tval);
                }

                setS(sq1, empty);	// Take back sq
            }
//            System.out.println("Num moves considered:" + listLen[2] + ", ll0:" + listLen[0] + ", ll1:" + listLen[1] + ", pv:" + pv);
            Arrays.sort(seldMoves, 0, listLen[2]);
            if (moves == null) {
                int len = Math.min(2 * selectionSize, listLen[2]);

                System.out.println("stones: " + listLen[0] + " " + listLen[1]);
                for (int s = 0; s < listLen[0]; s++) {
                    System.out.println(s + " " + listMoves[s] +
                        " " + listMoves[s].oScore + " " + listMoves[s].pScore + " " + listMoves[s].score);
                }

                System.out.println("moves: " + listLen[2]);
                for (int s = 0; s < len; s++) {
                    System.out.println(s + " " + seldMoves[s] +
                        " " + seldMoves[s].oScore + " " + seldMoves[s].pScore + " " + seldMoves[s].score);
                }
                return len;
            } else {
//                int len = Math.min(moves.length, listLen[2]);
//                for (int s = 0; s < len; s++) {
//                    moves[s] = (Move) seldMoves[s].clone();
//                }
                // We reserve some candidate moves for non threat moves. This is because sometimes there are too many
                // threat moves when we don't want to make a threat move
                int len = Math.min(moves.length, listLen[2]);
                int reservedNonThreatMoves = len / 5;
                int s = 0;
                int src = 0;
                while (s < len - reservedNonThreatMoves) {
                    moves[s] = (Move) seldMoves[src].clone();
                    s++;
                    src++;
                }
                while (s < len) {
                    // If move doesn't make a threat, consider it
                    if (seldMoves[src].tval0 == 0) {
                        moves[s] = (Move) seldMoves[src].clone();
                        s++;
                    }
                    src++;
                }
//                if (src!=s) {
//                    System.out.println("Reservation in effect");
//                }
                return len;
            }
        }

        @SuppressWarnings("empty-statement")
        public int eval(int colToMove) {
            int value = 0, sliceLen;
            Slice slice = new Slice();
            for (int d = 0; d < whi.length; d++) {
                for (int sl = 0; sl < bSize; sl++) {
                    switch (d) {
                        case 0:
                            sliceLen = bSize;
                            break;
                        case 1:
                            sliceLen = bSize;
                            break;
                        case 2:
                            sliceLen = sl + 1;
                            break;
                        case 3:
                            sliceLen = bSize - sl - 1;
                            break;
                        case 4:
                            sliceLen = bSize - sl;
                            break;
                        case 5:
                            sliceLen = sl;
                            break;
                        default:
                            sliceLen = 0;
                            System.out.println("Fu bar");
                    }
                    if (sliceLen < 6) {
                        continue;
                    }
                    int b = bla[d][sl], w = whi[d][sl];
                    if (colToMove == black) {
                        slice.init(sliceLen, b, w);
                    } else {
                        slice.init(sliceLen, w, b);
                    }
                    while (slice.nextSegment());
                    value += slice.pval[0] - slice.pval[1];
                    if (slice.tval[2] >= 4) {
                        System.out.println("eval: making six");
                    } else if (slice.tval[1] > 0) {
                        System.out.println("eval: have to resolve " + slice.tval[1] + " threats.");
                    }
                }
            }
            return value;
        }
    } // Position

    /**
     * Implementation of the slice
     */
    class Slice {

        int pl1, pl2;	// Player1 is to move, Player2 is opponent.
        int len, chType;
        int segStart, colFirst, colLast, segNext, segEnd, segLen;
        int segCol, segment;
        int[] tval = new int[3], pval = new int[2];

        public void init(int len, int pl1, int pl2) {
            this.len = len;
            this.pl1 = pl1;
            this.pl2 = pl2;
            tval[0] = tval[1] = tval[2] = pval[0] = pval[1] = segNext = 0;
        }

        @Override
        public String toString() {
            String bin = Integer.toBinaryString(segment);
            return "segment(" + segLen + "): " + segCol + " " + zeroes.substring(bin.length(), segLen) + bin + " " +
                segStart + " " + colFirst + " " + segNext + " " + segEnd + " " + chainType[chType] + " v" + (pval[0] - pval[1]);
        }

        void finishSegment() {
            segment = ((segCol == 1 ? pl1 : pl2) >> segStart) & chainMask[segLen = segEnd - segStart];
            if (segLen < 6) {
                return;
            }
            if (segLen > MAX_SEG_LENGTH) {
                int d = Math.min(colFirst - segStart - 3, segLen - 10);
                if (d > 0) {
                    segStart += d;
                    segLen -= d;
                    segment >>= d;
                }
                d = Math.min(segEnd - colLast - 4, segLen - 10);
                if (d > 0) {
                    segEnd -= d;
                    segLen -= d;
                    segment &= chainMask[segLen];
                }
            }
            if (segLen > MAX_SEG_LENGTH) {
                chType = 0;
            } else {
                chType = table[segLen - 6][segment];
                if (segCol == 1) {			// Player to move
                    pval[0] += posVal[chType];	// Positional value
                    tval[0] += thrVal[chType];	// Number of threats player makes
                    if (tval[2] < thrLen[chType]) {
                        tval[2] = thrLen[chType];	// Longest threat
                    }
                } else {				// Opponent
                    pval[1] += posVal[chType];
                    tval[1] += thrVal[chType];	// Number of threats opponent makes
                }
            }
        }

        public boolean nextSegment() {
            segStart = segNext;
            segCol = chType = colFirst = 0;
            for (int ind = segStart, b = 1 << segStart; ind < len; ind++, b <<= 1) {
                if (segCol == 0) {
                    if ((pl1 & b) != 0) {
                        colFirst = colLast = ind;
                        segCol = 1;
                        segNext = ind + 1;
                    } else if ((pl2 & b) != 0) {
                        colFirst = colLast = ind;
                        segCol = 2;
                        segNext = ind + 1;
                    }
                } else {
                    if ((pl1 & b) != 0) {
                        if (segCol == 2) {
                            segEnd = ind;
                            finishSegment();
                            return true;
                        }
                        if (segCol == 1) {
                            segNext = (colLast = ind) + 1;
                        }
                    } else if ((pl2 & b) != 0) {
                        if (segCol == 1) {
                            segEnd = ind;
                            finishSegment();
                            return true;
                        }
                        if (segCol == 2) {
                            segNext = (colLast = ind) + 1;
                        }
                    } else if (ind - colLast >= 5) {
                        // End the segment with 4 empty after finding 5 empty
                        segEnd = ind;
                        finishSegment();
                        return true;
                    }
                }
            }
            segNext = segEnd = len;
            if (segCol > 0) {
                finishSegment();
                return true;
            }
            return false;
        }
    } // Slice

    /**
     * Implementation of the log writer
     */
    class LogWriter {

        StringBuffer strBuf;
        String fileExtension;

        public LogWriter(String fileExtension) {
            this.fileExtension = "." + fileExtension;
            strBuf = new StringBuffer();
        }

        public void add(String s) {
            if (s == null) // Flush
            {
                try {
                    Calendar now = Calendar.getInstance();
                    FileWriter fw = new FileWriter("MeinStein_" +
                        now.get(Calendar.YEAR) + "_" +
                        (now.get(Calendar.MONTH) + 1) + "_" +
                        now.get(Calendar.DAY_OF_MONTH) + fileExtension, true);
                    fw.write(strBuf.toString(), 0, strBuf.length());
                    fw.close();
                    strBuf = new StringBuffer();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            } else {
                strBuf.append(s);
            }
        }
    }

    void resetBoard() {
        if (logLevel >= 4) {
            System.out.println("New game");
        }
        cur.newStartingPos();
        curGame = new Game("New", "Pamplona ESP", "", String.valueOf(++round),
                "Me", "MeinStein", "*", "", cur.toString());
        cur.makeMove(curGame.forward());	// Obligatory first black move
        drawBoard();
    }

    int playEngineGame() {
        timeBlack = timeWhite = 0;
        int score;
        do {
            resetEvaluationString();
            if (cur.moveNum / 2 % 2 == 0) {
                strategy = 0;
                posVal = posVal0;
                selectionSize = selectionSize0;
                score = cur.anaPlay(depth0, quiet0, OPT_DEFEND);	// Black
            } else {
                strategy = 1;
                posVal = posVal1;
                selectionSize = selectionSize1;
                score = cur.anaPlay(depth1, quiet1, OPT_DEFEND);	// White
            }
            displayEvaluationString(score);
            disp.now();
            paintImmediately(0, 0, getWidth(), getHeight());
        } while (-posVal[DONE6] < score && score < posVal[DONE6] && cur.moveNum < ENGINE_DRAW_STONES);

        int result = 0;
        System.out.println(posVal[DONE6] + "," + score);
        if (-posVal[DONE6] == score || score == posVal[DONE6]) {
            result = (cur.moveNum / 2 % 2 == 0) ? -1 : 1; // + 1 for black
        }
        return result;
    }

} // MeinCtrl

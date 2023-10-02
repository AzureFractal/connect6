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

import java.util.*;

/**
 *
 * @author theo
 * @version 1.10.01
 */
class MemoryDB {

    final int bSize = 19;
    SortedSet Book = new TreeSet();
    int count = 0;

    /**
     * Internal class player
     */
    class Player {

        int rank;
        String name, country;
        int rating;

        public Player(int rank, String name, String country, int rating) {
            this.rank = rank;
            this.name = name;
            this.country = country;
            this.rating = rating;
        }
    }

    /**
     * Internal class MoveHistory
     */
    class MoveHistory implements Comparable {

        String fen, info;
        int m1, m2, rank, gameL, key;
        boolean winning;

        public MoveHistory(String fen, int m1, int m2, int rank, int gameL, boolean winning, String info) {
            this.fen = fen;
            this.info = info;
            this.m1 = m1;
            this.m2 = m2;
            this.rank = rank;
            this.gameL = gameL;
            this.winning = winning;
            key = ++count;
        }

        public int compareTo(Object p) {
            MoveHistory mh = (MoveHistory) p;
            int c;
            if ((c = fen.compareTo(mh.fen)) != 0) {
                return c;
            }
            if ((c = playerList[mh.rank].rating - playerList[rank].rating) != 0) {
                return c;
            }
            return key - mh.key;
        }
    }

    public void add(String fen, int i1, int i2, String name, int gameL, boolean winning, String info) {
        int rank = 0, mir = fen.charAt(fen.length() - 1) - 'A';
        for (int i = 1; i < playerList.length; i++) {
            if (playerList[i].name.equals(name)) {
                rank = i;
                break;
            }
        }
        Book.add(new MoveHistory(fen.substring(0, fen.length() - 1),
            mirror(i1, mir), mirror(i2, mir), rank, gameL, winning, info));
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

    public void show(String fenm) {
        int rank = 0, mir = fenm.charAt(fenm.length() - 1) - 'A';
        String fen = fenm.substring(0, fenm.length() - 1);
        SortedSet tail = Book.tailSet(new MoveHistory(fen, 0, 0, 0, 0, true, ""));
        Iterator it = tail.iterator();
        System.out.println(fen);
        while (it.hasNext()) {
            MoveHistory mh = (MoveHistory) it.next();
            Player p = playerList[mh.rank];
            if (fen != null && !mh.fen.equals(fen)) {
                break;
            }
            if ((mir & 8) != 0) {
                mir = (mir | 1) & ~8;
            }
            int m1 = mirror(mh.m1, mir);
            int m2 = mirror(mh.m2, mir);
            String moveString = (char) ('a' + m1 % bSize) + String.valueOf(19 - m1 / bSize) +
                (char) ('a' + m2 % bSize) + String.valueOf(19 - m2 / bSize);

            System.out.println(moveString + " " + p.rating + " " + mh.gameL + " " + mh.rank + " " +
                mh.winning + " " + p.name + " (" + p.country + ") " + mh.info);
        }
    }
    Player playerList[] = {
        new Player(0, "unknown", "", 9999),
        new Player(1, "iec", "Slovakia", 2051),
        new Player(2, "ondik", "Czech Republic", 2048),
        new Player(3, "Patriot", "Russian Federation", 2047),
        new Player(4, "Andrey", "Russian Federation", 2019),
        new Player(5, "Russian boy", "Russian Federation", 2017),
        new Player(6, "Infinity", "Russian Federation", 1983),
        new Player(7, "euhuang", "Taiwan", 1975),
        new Player(8, "richu333", "China", 1965),
        new Player(9, "Marsh Song", "Nauru", 1962),
        new Player(10, "xooox", "Russian Federation", 1960),
        new Player(11, "Chen Chen-Kuo", "Taiwan", 1958),
        new Player(12, "someone", "China", 1950),
        new Player(13, "hichess", "", 1929),
        new Player(14, "celie", "China", 1915),
        new Player(15, "drozdov", "Russian Federation", 1899),
        new Player(16, "alonso", "Spain", 1899),
        new Player(17, "thomas", "", 1884),
        new Player(18, "Kai-Ze, Wang", "", 1867),
        new Player(19, "Rebecca", "Australia", 1865),
        new Player(20, "Delanote Dag", "Belgium", 1864),
        new Player(21, "lev1", "Russian Federation", 1860),
        new Player(22, "player2", "", 1860),
        new Player(23, "neko", "Taiwan", 1841),
        new Player(24, "gadabout", "Taiwan", 1840),
        new Player(25, "fun-fun", "Macau", 1837),
        new Player(26, "Arrow", "Russian Federation", 1835),
        new Player(27, "zhi zhe Huang", "Taiwan", 1832),
        new Player(28, "zevs1", "", 1823),
        new Player(29, "enya", "Iceland", 1812),
        new Player(30, "Fourseason's song", "", 1810),
        new Player(31, "CHY", "Taiwan", 1809),
        new Player(32, "changkon", "", 1799),
        new Player(33, "Rustam", "Russian Federation", 1791),
        new Player(34, "koyaan", "", 1769),
        new Player(35, "renbo", "Taiwan", 1767),
        new Player(36, "sunny", "Taiwan", 1766),
        new Player(37, "ntcbman", "Taiwan", 1761),
        new Player(38, "MING", "Taiwan", 1760),
        new Player(39, "sunny", "", 1737),
        new Player(40, "dullfish", "", 1735),
        new Player(41, "editor", "", 1735),
        new Player(42, "Vladimir Sinitsyn", "Russian Federation", 1735),
        new Player(43, "shape", "Russian Federation", 1728),
        new Player(44, "zhangying", "", 1724),
        new Player(45, "Ray Garrison", "USA", 1721),
        new Player(46, "euhuanq", "", 1719),
        new Player(47, "game", "", 1708),
        new Player(48, "ypercube", "Greece", 1706),
        new Player(49, "bloke", "", 1697),
        new Player(50, "Nathaniel Watson", "USA", 1695),
        new Player(51, "Nevermind", "Vanuatu", 1689),
        new Player(52, "iol", "", 1686),
        new Player(53, "null", "Poland", 1686),
        new Player(54, "bonhart", "Poland", 1683),
        new Player(55, "Chaosu", "Poland", 1681),
        new Player(56, "sanlly", "", 1680),
        new Player(57, "loi", "", 1678),
        new Player(58, "overflow", "", 1675),
        new Player(59, "Max", "Russian Federation", 1670),
        new Player(60, "tou", "", 1668),
        new Player(61, "Luca Bruzzi", "Italy", 1666),
        new Player(62, "3dsmax", "", 1665),
        new Player(63, "Marius Halsor", "Norway", 1662),
        new Player(64, "K", "", 1656),
        new Player(65, "Moot Point", "", 1651),
        new Player(66, "Theo van der Storm", "Netherlands", 1648),
        new Player(67, "dein", "Poland", 1646),
        new Player(68, "sai", "", 1645),
        new Player(69, "Jose (piau)", "Spain", 1645),
        new Player(70, "Hisoka", "Taiwan", 1643),
        new Player(71, "Malaj (visiting)", "Hungary", 1643),
        new Player(72, "vector", "Hungary", 1642),
        new Player(73, "Black", "Belarus", 1639),
        new Player(74, "dxsdk", "", 1639),
        new Player(75, "magician", "", 1637),
        new Player(76, "deep6plus", "Taiwan", 1635),
        new Player(77, "ak57", "Russian Federation", 1632),
        new Player(78, "vladelena", "Belarus", 1628),
        new Player(79, "vacsi", "Hungary", 1623),
        new Player(80, "masikfules", "Hungary", 1622),};
}

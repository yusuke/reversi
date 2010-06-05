/*
Copyright (c) 2000-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package jp.samuraism.reversi;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class ReversiApplet extends Applet {
    private Image white, black, blank;
    private int cellWidth, cellHeight, width, height;

    private Board board;

    public void init() {
        board = new Board(8, 8);

        white = getImage(getDocumentBase(), getParameter("white"));
        black = getImage(getDocumentBase(), getParameter("black"));
        blank = getImage(getDocumentBase(), getParameter("blank"));
        Dimension d = getSize();
        cellWidth = (d.width - 9) / 8;
        cellHeight = (d.height - 9) / 8;
        width = d.width;
        height = d.height;
        addMouseListener(ma);
    }

    MouseAdapter ma = new MouseAdapter() {
        public void mousePressed(MouseEvent me) {
            Point p = me.getPoint();
            board.tryPlace(p.x / (cellWidth + 1), p.y / (cellHeight + 1));
            repaint();
        }
    };

    public void paint(Graphics g) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                g.drawImage(getImage(i, j), 1 + (cellWidth + 1) * i + i,
                        1 + (cellHeight + 1) * j + j, cellWidth, cellHeight, this);
            }
        }

        g.setColor(Color.black);
        for (int i = 0; i <= 8; i++) {
            g.drawLine(0, i * (cellHeight + 1) + i, width, i * (cellHeight + 1) + i);
            g.drawLine(i * (cellWidth + 1) + i, 0, i * (cellWidth + 1) + i, height);
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    private Image getImage(int x, int y){
        Board.State state = board.getGrid()[x][y];
        switch(state){
            case WHITE:
                return white;
            case BLACK:
                return black;
            case BLANK:
                return blank;
        }
        throw new AssertionError();
    }

}

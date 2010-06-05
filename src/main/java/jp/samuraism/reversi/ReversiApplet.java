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
import static jp.samuraism.reversi.Board.State.*;


/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class ReversiApplet extends Applet {
    private final static boolean DEBUG = true;
    private Image white, black, blank;
    private int cellWidth, cellHeight, width, height;
    private Image turn;

    private Board board;

    public void init() {
        board = new Board(8, 8);

        white = getImage(getDocumentBase(), getParameter("white"));
        black = getImage(getDocumentBase(), getParameter("black"));
        blank = getImage(getDocumentBase(), getParameter("blank"));
        // always starts with black's turn
        turn = black;
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
            if (place(p.x / (cellWidth + 1), p.y / (cellHeight + 1), turn)) {
                repaint();
                turn = getReversedColor(turn);
                if (!isPlaceable(turn)) {
                    if (!isPlaceable(getReversedColor(turn))) {
                        // both players are not placeable
                        judge();
                    } else {
                        // pass
                        turn = getReversedColor(turn);
                    }
                }
            } else {
            }
        }
    };

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
    private Board.State toState(Image stateImage){
        if(stateImage == white){
            return WHITE;
        }else if(stateImage == black){
            return BLACK;
        }else if(stateImage == blank){
            return BLANK;
        }
        throw new AssertionError();
    }


    public void paint(Graphics g) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                g.drawImage(getImage(i, j), 1 + (cellWidth + 1) * i + i,
                        1 + (cellHeight + 1) * j + j, cellWidth, cellHeight, this);
            }
        }

        g.setColor(Color.black);
        for (int i = 0; i < 9; i++) {
            g.drawLine(0, i * (cellHeight + 1) + i, width, i * (cellHeight + 1) + i);
            g.drawLine(i * (cellWidth + 1) + i, 0, i * (cellWidth + 1) + i, height);
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    /**
     * 置ける場所が一カ所でもあるか判定する
     * @param state 色
     * @return 置けるかどうか
     */
    public boolean isPlaceable(Image state) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.searchPlaceableDirection(i, j, toState(state)) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void judge() {
        int whiteCount = 0, blackCount = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.getGrid()[i][j] == WHITE) {
                    whiteCount++;
                } else if (board.getGrid()[i][j] == BLACK) {
                    blackCount++;
                }
            }
        }
        log("Black:" + blackCount + " " + "White:" + whiteCount);
    }

    /**
     * 色を反転させる
     *
     * @param state 反転させる色
     * @return 反転した色
     */
    private Image getReversedColor(Image state) {
        if (state == white) {
            return black;
        } else if (state == black) {
            return white;
        } else {
            return blank;
        }
    }

    /**
     * 駒を置く
     *
     * @param x     x-axis
     * @param y     y-axis
     * @param state state
     * @return if the placement succeeded
     */
    private boolean place(int x, int y, Image state) {
        byte search = 1;
        byte direction;
        //駒を置けるかどうかの判定
        if ((direction = board.searchPlaceableDirection(x, y, toState(state))) != 0) {
            //i,j = 探索するベクトル
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (((i == 0) && (j == 0))) continue;
                    if ((direction & search) == search) {
                        for (int k = 1; getImage(x + i * k, y + j * k) != state; k++) {
                            board.getGrid()[x + i * k][y + j * k] = toState(state);
                        }
                    }
                    search <<= 1;
                }
            }
            board.getGrid()[x][y] = toState(state);
            return true;
        } else {
            //置けない!
            return false;
        }
    }



    private static void log(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}

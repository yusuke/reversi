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


import java.awt.*;

import static jp.samuraism.reversi.Board.State.*;
/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Board {
    private final static boolean DEBUG = true;
    private State[][] grid;
    private Board.State turn;

    public static enum State{
        WHITE,
        BLACK,
        BLANK
    }

    public Board(int width, int height){
        log("initializing the board");
        // always starts with black's turn
        turn = BLACK;

        grid = new State[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if ((i == 3 && j == 3) || (i == 4 && j == 4)) {
                    grid[i][j] = BLACK;
                } else if ((i == 3 && j == 4) || (i == 4 && j == 3)) {
                    grid[i][j] = WHITE;
                } else {
                    grid[i][j] = BLANK;
                }
            }
        }

    }

    public State[][] getGrid() {
        return grid;
    }

    private static void log(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
    /**
     * 駒を置けるかどうかの判定
     *
     * @param x x座標
     * @param y y座標
     * @param state 置く色
     * @return byteで駒を置けるかどうか ビット毎に置ける方向を示す 0:左上 1:左  2:左下  3:上  4:下  5:右上  6:右  7:右下
     */
    public byte searchPlaceableDirection(int x, int y, State state) {
        byte direction = 0;
        byte search = 1;
        if (grid[x][y] == BLANK) {
            for (int xx = -1; xx <= 1; xx++) {
                for (int yy = -1; yy <= 1; yy++) {
                    // xx, yy :探索するベクトル
                    if (isInsideBoard(x + xx, y + yy)) {
                        ///一つとなりに置く駒と違う色があるか
                        if ((grid[x + xx][y + yy] != BLANK) && grid[x + xx][y + yy] != state) {
                            //さらにその先に置く駒の色があるか盤面上を探索
                            for (int k = 2; isInsideBoard(x + xx * k, y + yy * k); k++) {
                                if (grid[x + xx * k][y + yy * k] == state) {
                                    //自分の色があった、石を置ける！
                                    direction |= search;
                                } else if (grid[x + xx * k][y + yy * k] == BLANK) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!((xx == 0) && (yy == 0))) {
                        search <<= 1;
                    }
                }
            }
            //どの方向にも置けない
            return direction;
        } else {
            //既に駒があって置けない！
            return 0;
        }
    }

    public void tryPlace(int x, int y){
        if (place(x, y)) {
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

    /**
     * 駒を置く
     *
     * @param x     x-axis
     * @param y     y-axis
     * @return if the placement succeeded
     */
    public boolean place(int x, int y) {
        byte search = 1;
        byte direction;
        //駒を置けるかどうかの判定
        if ((direction = searchPlaceableDirection(x, y, turn)) != 0) {
            //i,j = 探索するベクトル
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (((i == 0) && (j == 0))) continue;
                    if ((direction & search) == search) {
                        for (int k = 1; grid[x + i * k][y + j * k] != turn; k++) {
                            grid[x + i * k][y + j * k] = turn;
                        }
                    }
                    search <<= 1;
                }
            }
            grid[x][y] = turn;
            return true;
        } else {
            //置けない!
            return false;
        }
    }
    /**
     * 座標が盤の中に収まっているかどうか
     * @param x x座標
     * @param y y座標
     * @return 収まっているかどうか
     */
    private boolean isInsideBoard(int x, int y) {
        return (x >= 0) && (x < getGrid().length) & (y >= 0) && (y < getGrid()[0].length);
    }

    public void judge() {
        int whiteCount = 0, blackCount = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (grid[i][j] == WHITE) {
                    whiteCount++;
                } else if (grid[i][j] == BLACK) {
                    blackCount++;
                }
            }
        }
        log("Black:" + blackCount + " " + "White:" + whiteCount);
    }

    /**
     * 置ける場所が一カ所でもあるか判定する
     * @param state 色
     * @return 置けるかどうか
     */
    public boolean isPlaceable(State state) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (searchPlaceableDirection(i, j, state) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 色を反転させる
     *
     * @param state 反転させる色
     * @return 反転した色
     */
    private Board.State getReversedColor(Board.State state) {
        if (state == WHITE) {
            return BLACK;
        } else if (state == BLACK) {
            return WHITE;
        } else {
            return BLANK;
        }
    }
}

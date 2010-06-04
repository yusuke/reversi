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
package jp.samuraism;

import java.awt.event.*;
import java.applet.*;
import java.awt.*;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Reversi extends Applet implements MouseListener {
    Koma ishi[][] = new Koma[8][8];
    Image white, black, blank;
    int cellWidth, cellHeight, width, height;
    Image turn;
    Image buffer;
    Graphics bufferg;

    public void init() {
        white = getImage(getDocumentBase(), getParameter("white"));
        black = getImage(getDocumentBase(), getParameter("black"));
        blank = getImage(getDocumentBase(), getParameter("blank"));
        turn = black;//最初は黒から
        Dimension d = getSize();
        cellWidth = (d.width-9)/8;
        cellHeight = (d.height-9)/8;
        width = d.width;
        height = d.height;
        buffer = createImage(width, height);
        addMouseListener(this);
        // 盤面の初期化
        System.out.println("initializing ban");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i == 3 & j == 3) || (i == 4 & j == 4)) {
                    ishi[i][j] = new Koma(i, j, black);
                } else if ((i == 3 & j == 4) || (i == 4 & j == 3)) {
                    ishi[i][j] = new Koma(i, j, white);
                } else {
                    ishi[i][j] = new Koma(i, j, blank);
                }
            }
        }
    }

    public void mouseClicked(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void mousePressed(MouseEvent me) {
        Point p = me.getPoint();
        if(PutKoma(p.x/(cellWidth+1),p.y/(cellHeight+1),turn)){
            System.out.println("ok");
            repaint();
            turn = ReverseColor(turn);
            if(!CanPut(turn)){
                System.out.println("1");
                //2人とも置ける駒がないか？
                if(!CanPut(ReverseColor(turn))){
                    System.out.println("2");
                    Finish();//終了
                    System.exit(0);
                }else{
                    System.out.println("3");
                    //パス
                    turn=ReverseColor(turn);
                }
            }
            if(turn == black){
                System.out.println("black");
            }else if(turn == white){
                System.out.println("white");
            }else if(turn == blank){
                System.out.println("blank");
            }
        }else{
            System.out.println("no");
        }
    }

    public void paint(Graphics g) {
        if(bufferg == null){
            bufferg = buffer.getGraphics();
        }
        for (int i = 0; i < 8; i++) {
            for(int j=0;j<8;j++){
                bufferg.drawImage(ishi[i][j].State(), 1 + (cellWidth + 1) * i,
                        1 + (cellHeight + 1) * j, cellWidth, cellHeight, this);
            }
        }

        bufferg.setColor(Color.black);
        for (int i = 0; i <= 8; i++) {
            bufferg.drawLine(0, i * (cellHeight + 1), width - 1, i * (cellHeight + 1));
            bufferg.drawLine(i * (cellWidth + 1), 0, i * (cellWidth + 1), height - 1);
        }

        g.drawImage(buffer, 0, 0, this);
    }
    public void update(Graphics g){
        paint(g);
    }
    //置ける場所が一カ所でもあるか判定するメソッド
    public boolean CanPut(Image iro){
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(CanPutKoma(i,j,iro)!=0){
                    return true;
                }
            }
        }
        return false;
    }

    public void Finish(){
        int whiteCount = 0, blackCount=0;
        for(int  i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(ishi[i][j].State()==white){
                    whiteCount++;
                }else if(ishi[i][j].State() == black){
                    blackCount++;
                }
            }
        }
        System.out.println("Black:"+blackCount+" "+"White:"+whiteCount);
    }
    //反対の色を返す
    Image ReverseColor(Image iro){
        if(iro == white){
            return black;
        }else if(iro == black){
            return white;
        }else{
            return blank;
        }
    }
    //駒を置く
    //戻り値:boolean 駒を置けたかどうか
    boolean PutKoma(int x, int y, Image iro){
        byte search = 1;
        byte direction;
        //駒を置けるかどうかの判定
        if((direction=CanPutKoma(x,y,iro))!=0){
            //i,j = 探索するベクトル
            for(int i=-1;i<=1;i++){
                for(int j=-1;j<=1;j++){
                    if(((i==0)&(j==0))) continue;
                    if((direction & search) == search){
                        for(int k=1;ishi[x+i*k][y+j*k].State()!=iro;k++){
                            ishi[x+i*k][y+j*k].Turn(iro);
                        }
                    }
                    search <<= 1;
                }
            }
            if(direction != 0){
                ishi[x][y].Put(iro);
                return true;
            }
            return false;
        }else{
            //置けない!
            return false;
        }
    }
    //CanPutKoma : 駒を置く
    //戻り値 : byte 駒を置けるかどうか ビット毎に置ける方向を示す
    //0:左上 1:左  2:左下  3:上  4:下  5:右上  6:右  7:右下
    public byte CanPutKoma(int x, int y, Image iro){
        byte direction = 0;
        byte search = 1;
        //駒を置けるかどうかの判定
        if(ishi[x][y].State() == blank){
            //i,j:探索するベクトル
            for(int i=-1;i<=1;i++){
                for (int j = -1; j <= 1; j++) {
                    if (IsIn(x + i, y + j)) {
                        ///一つとなりに置く駒と違う色があるか
                        if ((ishi[x + i][y + j].State() != blank) & (ishi[x + i][y + j].State() != iro)) {
                            //さらにその先に置く駒の色があるか
                            for (int k = 2; IsIn(x + i * k, y + j * k); k++) {
                                if (ishi[x + i * k][y + j * k].State() == iro) {
                                    //石を置ける！
                                    direction |= search;
                                } else if (ishi[x + i * k][y + j * k].State() == blank) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!((i == 0) & (j == 0))) {
                        search <<= 1;
                    }
                }
            }
            //どの方向にも置けない
            return direction;
        }else{
            //既に駒があって置けない！
            return 0;
        }
    }
    //座標が盤の中に収まっているかどうか
    boolean IsIn(int x, int y){
        return (x>=0)&(x<ishi.length)&(y>=0)&(y<ishi[0].length);
    }
    //State:駒の状態を返す
    Image State(int x, int y){
        return ishi[x][y].State();
    }

}

class Koma {
    //インスタンス変数の宣言
    //state 駒の状態
    Image state;
    int x, y;

    Graphics base;

    //コンストラクタ
    Koma(int xx, int yy, Image jyotai) {
        state = jyotai;
        x = xx;
        y = yy;
    }

    Image State() {
        return state;
    }

    void Put(Image jyotai) {
        state = jyotai;
    }

    void Turn(Image jyotai) {
        state = jyotai;
    }
}

package com.shpp.p2p.cs.yyefimov.assignment4;

import acm.graphics.GObject;
import acm.graphics.GRect;

//  Class will wrap each GObject in GameObject.
public class GameObjects {
    //  Mark for paddle.
    private boolean isPaddle = false;
    //  Mark for ball.
    private boolean isBall = false;
    //  Mark for brick.
    private boolean isBrick = false;
    //  Mark for bonus.
    private boolean isBonusBrick = false;
    //  Gobject.
    public GObject obj;
    //  Brick lives.
    private int brickLife = 0;
    //  Speed.
    private double vx, vy;
    //  Brick Y coordinate.
    private double brickLongitude;

    //  Constructor.
    public GameObjects(GObject newObject){
        obj = newObject;
    }

    /*
    *   Next Methods are getters and setters for private parameters.
    * */

    public void makeItPaddle(){
        isPaddle = true;
    }

    public void makeItBall(){
        isBall = true;
    }

    public void makeItBrick(){
        isBrick = true;
    }

    public boolean ifPaddle(){
        return isPaddle;
    }

    public boolean ifBall(){
        return isBall;
    }

    public boolean ifBrick(){
        return isBrick;
    }

    public GRect getPaddle(){
        if (ifPaddle())
            return (GRect) obj;
        return null;
    }

    public void setBrickLife(int number){
        brickLife = number;
    }

    public void reduceBrickLife(int i) {
        brickLife= brickLife - i;
    }

    public int getBrickLife() {
        return brickLife;
    }

    public double getVx(){return vx;}

    public double getVy(){return vy;}

    public void setVx(double vxNew){
        vx = vxNew;
    }

    public void setVy(double vyNew){
        vy = vyNew;
    }

    public void setBrickLongitude(double longitude){
        brickLongitude = longitude;
    }

    public double getBrickLongitude(){
        return brickLongitude;
    }

    public void makeItBonusBrick(){
        isBonusBrick = true;
    }
    public boolean ifBonusBrick(){
        return isBonusBrick;
    }

}

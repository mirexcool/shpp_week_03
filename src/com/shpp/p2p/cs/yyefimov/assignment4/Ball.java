package com.shpp.p2p.cs.yyefimov.assignment4;

import acm.graphics.GOval;

import java.awt.*;

public class Ball {
    private final GOval ball;

    //  Ball speeds on each way.
    private double vx, vy;

    //  If we loose ball we will mark this flag true.
    private boolean loosedBall = false;

    //  We will count how many bricks we need to destroy to win.
    private int bricksLeftToDestroy;

    //  Construction of Ball object.
    public Ball(int x, int y, int ballWidth, int ballHeight, boolean filled, Color color, int numbersOfBricks) {
        ball = new GOval(x, y, ballWidth, ballHeight);
        ball.setFilled(filled);
        ball.setColor(color);
        bricksLeftToDestroy = numbersOfBricks;
    }

    /*  We make all class fields private, so next methods will be getters and setters for each field.
    * */
    // ball getter.
    public GOval getBall() {
        return ball;
    }

    // Ball vx setter.
    public void setVx(double x) {
        vx = x;
    }

    // Ball vy setter.
    public void setVy(double y) {
        vy = y;
    }

    // Ball vx getter.
    public double getVx() {
        return vx;
    }

    // Ball vy getter.
    public double getVy() {
        return vy;
    }

    //  Method will change ball status (loosed ball).
    public void weLoosedBall() {
        loosedBall = true;
    }

    //  Method will change ball status (new ball).
    public void resetBall() {
        loosedBall = false;
    }

    //  loosedBall getter.
    public boolean isBallLoosed() {
        return loosedBall;
    }

    //  Method will decrease brick counter.
    public void minusBrick(){
        bricksLeftToDestroy--;
    }

    //  bricksLeftToDestroy getter.
    public int howManyBricksLeftToWin()
    {
        return bricksLeftToDestroy;
    }
}

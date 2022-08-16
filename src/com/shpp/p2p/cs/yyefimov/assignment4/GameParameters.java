package com.shpp.p2p.cs.yyefimov.assignment4;

import acm.graphics.GLabel;

//  Class GameParameters will show the game state for our main Class Breakout
public class GameParameters {
    /*
        @pause will switch game state, ball is moving or not.

        @gameOver show us if we need to stop game and show "Game over" massage.

        @exit if true, we should close the game.

        @score will hold the amount of points what was earned by player.

        @lives shows how many lives left to win the round.

        @menuBarReset will show if we need to re-draw menuBar.

        @menubar array which consist of two GLable (lives and score Strings).

        @paddleContactAvailable flag to avoid ball stacking on paddle.

        @gameWon shows if we won the game.
        */
    public boolean pause = true;
    public boolean gameOver = false;
    public boolean gameWon = false;

    public boolean exit = false;

    public double score = 0;

    public boolean paddleContactAvailable = true;

    public int lives;
    public boolean menuBarReset = true;
    public GLabel[] menuBar = new GLabel[2];

    //  Constructor.
    GameParameters(int attemps){
        lives = attemps;
    }
}

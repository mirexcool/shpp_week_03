package com.shpp.p2p.cs.yyefimov.assignment4;

import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

//  Breakout class is implementation of classic game "Breakout".
public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Dimensions of game board (usually the same)
     */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    //  Starting speed of our ball.
    private static final int BALL_SPEED_Y = 5;

    //  We can set an amount of checking points for our ball. Default 4 points. If you want to get better
    //  collision operating, you should set more points (36, 180, 360 etc).
    private static final int BALL_CONTOUR_POINTS = 4;

    //  Is a measurement of a plane angle in which one full rotation is 360 degrees.
    private static final int DEGREE = 360;

    /*  The amount of time between frames (60 FPS).  */
    private static final double TIME_FOR_FRAME = 1000.0 / 60;


    //  Array with colors for painting bricks.
    private static final Color[] BRICK_COLORS = {Color.RED, Color.ORANGE, Color.YELLOW, Color.green, Color.CYAN};

    //  Paddle object will store here.
    private GRect paddle;

    //  Ball object will store here.
    private Ball ball;

    //  We will store bricks in ArrayList.
    private final ArrayList<GRect> bricks = new ArrayList<>();


    //  Method run() will prepare round for game and play each round game.
    public void run() {
        roundPrepare();
        //  We have NTURNS tries to destroy all bricks, so each try will be separated in one cycle.
        for (int i = 0; i < NTURNS; i++) {
            playGame();
            resetBall();
        }
        waitForClick();
        exit();
    }

    //  Method will move ball until game not finished or ball hits bottom/
    private void playGame() {
        if (gameNotFinished())
            waitForClick();

        while (gameNotFinished()) {
            moveBall();
            pause(TIME_FOR_FRAME);
        }
    }

    //  Method will move ball and check collision.
    private void moveBall() {
        int moveX = (int) (Math.abs(ball.getVx()));
        int moveY = (int) (Math.abs(ball.getVy()));

        shortBallMoves(moveX, moveY);
    }

    /*  Method receive two parameters and moving ball on one pixel each time to avoid "teleportation"
        effect.
    * */
    private void shortBallMoves(int moveX, int moveY) {
        //  We're moving ball on N times via X, and M times via Y.
        for (int i = 0; i < moveY; i++) {
            separatedMoveY(ball.getVy() > 0);
            if (moveX != 0) {
                separatedMoveX(ball.getVx() > 0);
                moveX--;
            }
            //  We will stop moving if we get collision.
            if (ballReflecting() || checkContact(getBallContour()))
                break;
        }
    }

    //  Method checking if we hit the wall, and turn ball to right way. If ball hits wall returns true.
    private boolean ballReflecting() {
        if (ball.getBall().getY() >= getHeight() - ball.getBall().getHeight()) {
            ball.weLoosedBall();
            return true;
        }
        if (ball.getBall().getX() <= 0) {
            ball.setVx(-ball.getVx());
            return true;
        }
        if (ball.getBall().getX() >= getWidth() - ball.getBall().getWidth()) {
            ball.setVx(-ball.getVx());
            return true;
        }
        if (ball.getBall().getY() <= 0) {
            ball.setVy(-ball.getVy());
            return true;
        }
        return false;
    }

    //  Method will check if ball get collision and separate behaviour of each collision type.
    private boolean checkContact(double[][] ballContour) {
        GObject collider;

        for (double[] coors : ballContour) {
            collider = getElementAt(coors[0], coors[1]);
            if (collider != null) {
                //  We exclude collision with itself and check only collision with GRect(paddle, or brick).
                if (!(collider instanceof GOval)) {
                    if (collider instanceof GRect)
                        if (collider == paddle)
                            //  Operate collision with paddle.
                            ballPaddleContact();
                        else {
                            //  Operate collision with brick.
                            ballBrickContact(collider, coors[1]);
                            return true;
                        }
                }
            }
        }
        return false;
    }

    //  Method will reflect ball out from paddle.
    private void ballPaddleContact() {
        ball.setVy(-Math.abs(ball.getVy()));
    }

    //  Method operate collision between ball and brick. After collision, we delete brick
    //  and reflects ball on right way. Using ballY point of contact, we can identify when we hit
    //  brick to side or up/down.
    private void ballBrickContact(GObject collider, double ballY) {
        for (GRect brick : bricks) {
            if (collider == brick) {

                //  If we hit brick from side we need to reflect vx speed for ball.
                if ((ballY < (collider.getY() + BRICK_HEIGHT - 1)) && (ballY > collider.getY() + 1)) {
                    ball.setVx(-ball.getVx());
                    //  We make one move to avoid multiples collision.
                    separatedMoveX(ball.getVx() > 0);
                } else {
                    ball.setVy(-ball.getVy());
                    //  We make one move to avoid multiples collision.
                    separatedMoveY(ball.getVy() > 0);
                }
                //  We reduce brick counter to know if we won the game, and clear brick from canvas.
                remove(brick);
                ball.minusBrick();
                return;
            }
        }
    }

    //  Method returns a set of point what will use to identify ball contour.
    private double[][] getBallContour() {
        if (BALL_CONTOUR_POINTS > 4)
            return getBallContourExt();
        else return defaultContour();

        }
    //  Contour consist of 4 points (ball in square).
    private double[][] defaultContour() {
        double xPos = ball.getBall().getX();
        double yPos = ball.getBall().getY();
        double ballDiameter = BALL_RADIUS * 2;

        return new double[][]{
                {xPos, yPos},
                {xPos + ballDiameter, yPos},
                {xPos, yPos + ballDiameter},
                {xPos + ballDiameter, yPos + ballDiameter}};
    }

    //  Method will draw a lot of points to identify our ball contour.
    private double[][] getBallContourExt() {
        double xPos = ball.getBall().getX() + BALL_RADIUS + 1;
        double yPos = ball.getBall().getY() + BALL_RADIUS + 1;
        double[][] contour = new double[BALL_CONTOUR_POINTS][2];
        //  How to use Sin, Cos to draw circle:
        //  https://uk.wikipedia.org/wiki/%D0%A1%D0%B8%D0%BD%D1%83%D1%81
        for (int i = 0; i < DEGREE; i += DEGREE / BALL_CONTOUR_POINTS) {
            double[] coors = new double[2];
            coors[0] = xPos + BALL_RADIUS * Math.cos(Math.toRadians(i));
            coors[1] = yPos + BALL_RADIUS * Math.sin(Math.toRadians(i));
            contour[i / (DEGREE / BALL_CONTOUR_POINTS)] = coors;
        }

        return contour;
    }

    //  Short move via X coordinate.
    private void separatedMoveX(boolean wayRight) {
        if (wayRight)
            ball.getBall().move(1, 0);
        else ball.getBall().move(-1, 0);
    }

    //  Short move via Y coordinate.
    private void separatedMoveY(boolean wayDown) {
        if (wayDown)
            ball.getBall().move(0, 1);
        else ball.getBall().move(0, -1);
    }

    //  If we loose ball, we delete it and create new one.
    private void resetBall() {
        ball.getBall().setLocation((int)(getWidth()/2.0), (int)(getHeight() / 2.0));
        ball.resetBall();
    }

    //  Method is preparing canvas for game.
    private void roundPrepare() {
        addMouseListeners();
        createPaddle();
        createBall();
        createBricks();
    }

    //  Method will draw brick set on canvas.
    private void createBricks() {
        for (int i = 0; i < NBRICK_ROWS; i++) {
            for (int j = NBRICKS_PER_ROW - 1; j >= 0; j--) {
                double xPos = i * BRICK_WIDTH + i * BRICK_SEP + BRICK_SEP / 2.0;
                double yPos = BRICK_Y_OFFSET + j * BRICK_HEIGHT + j * BRICK_SEP;
                bricks.add(createBrick(xPos, yPos, j / 2));
            }
        }
    }

    //  Creating each brick.
    private GRect createBrick(double xPos, double yPos, int color) {
        GRect brick = new GRect(xPos, yPos, BRICK_WIDTH, BRICK_HEIGHT);
        // Set color and fill each brick.
        brick.setFilled(true);
        //  We have 5 colors, so this is protection from exceptions on complicated cases of input data.
        if (color > BRICK_COLORS.length - 1)
            color = BRICK_COLORS.length - 1;
        brick.setColor(BRICK_COLORS[color]);
        add(brick);
        return brick;
    }

    //  Paddle creating.
    private void createPaddle() {
        paddle = new GRect((getWidth() - PADDLE_WIDTH) / 2.0,
                getHeight() - PADDLE_HEIGHT - PADDLE_Y_OFFSET,
                PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        paddle.setColor(Color.BLACK);
        add(paddle);
    }

    //  Ball creating.
    private void createBall() {
        double vx;
        //  Casting to int will round the value for further colliding calculations.
        ball = new Ball((int) (getWidth() / 2.0),(int) (getHeight() / 2.0),
                2 * BALL_RADIUS, 2 * BALL_RADIUS,
                true, Color.BLACK,
                NBRICK_ROWS * NBRICKS_PER_ROW);
        add(ball.getBall());

        vx = RandomGenerator.getInstance().nextDouble(1.0, BALL_SPEED_Y);
        if (RandomGenerator.getInstance().nextBoolean(0.5))
            vx = -vx;

        ball.setVx(vx);
        ball.setVy(BALL_SPEED_Y);
    }

    //  Paddle moving with mouse pointer.
    public void mouseMoved(MouseEvent mouseEvent) {
        paddle.setLocation(mouseEvent.getX() - paddle.getWidth() / 2.0, paddle.getY());
        if (paddle.getX() <= 0)
            paddle.setLocation(0, paddle.getY());
        if (mouseEvent.getX() >= getWidth() - paddle.getWidth() / 2.0)
            paddle.setLocation(getWidth() - paddle.getWidth() - 1, paddle.getY());
    }

    //  Check if we won the game or loose ball.
    private boolean gameNotFinished() {
        if (ball.isBallLoosed())
            return false;
        return ball.howManyBricksLeftToWin() > 0;
    }

}
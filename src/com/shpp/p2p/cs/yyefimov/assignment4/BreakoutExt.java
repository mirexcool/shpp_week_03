package com.shpp.p2p.cs.yyefimov.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class BreakoutExt extends WindowProgram {
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

    /*  The amount of time between frames (60 FPS).  */
    private static final double TIME_FOR_FRAME = 1000.0 / 60;
    //  Starting speed of our ball.
    private static final int BALL_SPEED_Y = 5;
    //  Array with colors for painting bricks.
    private static final Color[] BRICK_COLORS = {Color.RED, Color.ORANGE, Color.YELLOW, Color.green, Color.CYAN};
    //  BRICK_STRENGTH shows how many times you need to hit brick to destroy it.
    private static final int BRICK_STRENGTH = 1;
    //  After brick destroying, we speed up ball.
    private static final double SPEED_UP_PER_BRICK = 1.0075;

    /*  PADDLE_SENSITIVE will define how ball will reflect from paddle. For example, if
        PADDLE_SENSITIVE = 1, after reflecting ball vx = 0.
        PADDLE_SENSITIVE = 10, we will get 10 segments on paddle, and vx will depend on which part of
        paddle's is hitting the ball. If ball hit straight to center of paddle, vx = 0. Left corner, vx = -abs(vy).
        Right corner, vx = abs(vy). So the vx can change from -(abs(vy)) to abs(vy).
    * */
    private static final double PADDLE_SENSITIVE = 10;
    /*  Game can produce some kind of bonuses. BONUS_CHANCE = 0.01 = 1% of bricks will have bonus.  */
    private final static double BONUS_CHANCE = 0.075;

    /*  @gameObjects will store every single object in the game. Using the class GameObject we will
        separate and store all information what we need about each object.
    * */
    private static ArrayList<GameObjects> gameObjects;
    /*  @gameParameters is wrapper class for a lot of game parameters. (score, lives etc.)*/
    private final static GameParameters gameParameters = new GameParameters(NTURNS);
    //  @RG random generator for Breakout Class.
    private final static RandomGenerator RG = RandomGenerator.getInstance();

    //  Method will create the game and will do all game logic.
    @Override
    public void run() {
        //  Mouse listeners for our game.
        addMouseListeners();
        roundPrepare();
        //  We have many tries to destroy all bricks, so each try will be separated in ony cycle.
        for (int i = 0; i < NTURNS; i++) {
            playGame();
            newRoundGame();
            gameParameters.menuBarReset = true;

        }
        //  If you lose all lives but bricks still on canvas, it should be GAME OVER massage.
        gameOver();
    }

    //  Method will move ball and do every logic in the game.
    private void playGame() {
        while (gameNotFinished() && !gameParameters.gameOver) {
            checkForPause();
            try {
                moveBall();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            bonusHighLight();
            showLiveAndScore();
            pause(TIME_FOR_FRAME);
        }
    }

    //  Method will draw lives and score on the canvas.
    private void showLiveAndScore() {
        if (gameParameters.menuBarReset) {
            if (gameParameters.menuBar[0] != null && gameParameters.menuBar[1] != null) {
                remove(gameParameters.menuBar[0]);
                remove(gameParameters.menuBar[1]);
            }
            gameParameters.menuBar[0] = new GLabel("Lives: " + gameParameters.lives);
            gameParameters.menuBar[1] = new GLabel("Score: " + (int) gameParameters.score);

            gameParameters.menuBar[0].setFont(GLabel.DEFAULT_FONT + "-" + 20);
            gameParameters.menuBar[1].setFont(GLabel.DEFAULT_FONT + "-" + 20);

            gameParameters.menuBar[0].setLocation(0, getHeight() - gameParameters.menuBar[0].getDescent());
            gameParameters.menuBar[1].setLocation(getWidth() - gameParameters.menuBar[1].getWidth(), getHeight() - gameParameters.menuBar[0].getDescent());

            add(gameParameters.menuBar[0]);
            add(gameParameters.menuBar[1]);
            gameParameters.menuBarReset = false;
        }
    }

    //  Method is changing colors for bonus bricks.
    private void bonusHighLight() {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifBonusBrick())
                eachObject.obj.setColor(RG.nextColor());
        }
    }

    //  Method will create ball, bricks, paddle for game.
    private void roundPrepare() {
        gameObjects = new ArrayList<>();
        createPaddle();
        createBall();
        createBricks();
    }

    // Method will create paddle and add it to canvas and to the @gameObjects.
    private void createPaddle() {
        GRect paddle = new GRect((getWidth() - PADDLE_WIDTH) / 2.0,
                getHeight() - PADDLE_HEIGHT - PADDLE_Y_OFFSET,
                PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        paddle.setColor(Color.BLACK);

        add(paddle);
        GameObjects newObj = new GameObjects(paddle);
        newObj.makeItPaddle();
        gameObjects.add(newObj);
    }

    // Method will create ball and add it to canvas and to the @gameObjects.
    private void createBall() {
        GOval ball = new GOval(getWidth() / 2.0 - 1, getHeight() / 2.0 - 1, 2 * BALL_RADIUS, 2 * BALL_RADIUS);

        ball.setFilled(true);
        ball.setColor(Color.BLACK);
        add(ball);

        GameObjects obj = new GameObjects(ball);
        double vx = RG.nextDouble(1.0, BALL_SPEED_Y);
        if (RG.nextBoolean(0.5))
            vx = -vx;
        obj.setVx(vx);
        obj.setVy(BALL_SPEED_Y);
        obj.makeItBall();
        gameObjects.add(obj);
    }

    // Method will create bricks and add it to canvas and to the @gameObjects.
    private void createBricks() {
        for (int i = 0; i < NBRICK_ROWS; i++) {
            for (int j = NBRICKS_PER_ROW - 1; j >= 0; j--) {
                double xPos = i * BRICK_WIDTH + i * BRICK_SEP + BRICK_SEP / 2.0;
                double yPos = BRICK_Y_OFFSET + j * BRICK_HEIGHT + j * BRICK_SEP;
                createBrickGameObject(xPos, yPos, j / 2);
            }
        }
    }

    //  Method gets all parameters to create each brick and adds it to @gameObjects.
    private void createBrickGameObject(double xPos, double yPos, int color) {
        GameObjects newObject = new GameObjects(createBrick(xPos, yPos, color));

        newObject.makeItBrick();
        newObject.setBrickLongitude(yPos);
        newObject.setBrickLife(BRICK_STRENGTH);
        if ((int) RG.nextDouble(0, 1 / BONUS_CHANCE) == 0) {
            newObject.obj.setColor(Color.BLACK);
            newObject.makeItBonusBrick();
        }
        add(newObject.obj);
        gameObjects.add(newObject);
    }

    //  Method creates brick (GRect) and return it.
    private GRect createBrick(double brickX, double brickY, int color) {
        GRect brick = new GRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
        // Set color and fill each brick.
        brick.setFilled(true);
        if (color > BRICK_COLORS.length - 1)
            color = BRICK_COLORS.length - 1;
        brick.setColor(BRICK_COLORS[color]);
        return brick;
    }

    //  If you lose the ball you will pay 1 live, ball goes to start point and pause is turn on.
    private void newRoundGame() {
        resetBall();
        resetPaddle();
        gameParameters.gameOver = false;
        gameParameters.pause = true;
        gameParameters.lives--;
    }

    //  Method will reset paddle.
    private void resetPaddle() {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifPaddle()) {
                ((GRect) eachObject.obj).setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
            }
        }
    }

    //  Method will delete ball was what lost and creates new one on the start position.
    private void resetBall() {
        GameObjects toDelete = null;
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifBall()) {
                toDelete = eachObject;
            }
        }
        if (toDelete != null) {
            remove(toDelete.obj);
            gameObjects.remove(toDelete);
        }
        createBall();
    }

    //  Method will show GAME OVER massage ent quit the game after mouse CLICK.
    private void gameOver() {
        GLabel massage;
        gameParameters.gameOver = true;
        if (!gameParameters.gameWon) {
            massage = new GLabel("GAME OVER. Your score: " + (int) gameParameters.score);
            massage.setColor(Color.RED);
        } else {
            massage = new GLabel("YOU WIN. Your score:" + (int) gameParameters.score);
            massage.setColor(Color.GREEN);
        }

        massage.setFont(GLabel.DEFAULT_FONT + "-" + 30);
        massage.setLocation((getWidth() - massage.getWidth()) / 2.0, (getHeight() - massage.getHeight()) / 2.0);
        add(massage);
        while (!gameParameters.exit) {
            pause(TIME_FOR_FRAME);
        }
        exit();
    }

    //  Method will do pause in game for not defined time frame, until not mouse click.
    private void checkForPause() {
        GLabel massage = new GLabel("CLICK for start!");
        massage.setFont(GLabel.DEFAULT_FONT + "-" + 40);

        massage.setLocation((getWidth() - massage.getWidth()) / 2.0, (getHeight() - massage.getHeight()) / 2.0);
        massage.setColor(Color.ORANGE);
        add(massage);
        while (gameParameters.pause) {
            pause(TIME_FOR_FRAME * 10);
            remove(massage);
            pause(TIME_FOR_FRAME * 10);
            add(massage);
        }
        remove(massage);
    }

    //  Method is moving the ball and operate each contact to another objects in the game.
    private void moveBall() throws Exception {
        GameObjects ballObject = getBallObject();
        if (ballObject == null)
            return;
        GOval ball = (GOval) ballObject.obj;

        int moveY = (int) (Math.abs(ballObject.getVx()));
        int moveX = (int) (Math.abs(ballObject.getVy()));
        shortBallMove(moveX, moveY, ballObject, ball);
    }

    /*  Method is splitting long move for many short moves. For example:
     *   if we need to move(10,10) we will do ten short move(1,1).    */
    private void shortBallMove(int moveX, int moveY, GameObjects ballObject, GOval ball) throws Exception {
        GameObjects toDelete = null;
        boolean bonus = false;

        for (int i = 0; i < moveX; i++) {
            separatedMoveY(ballObject.getVy() > 0, ball);
            if (moveY != 0) {
                separatedMoveX(ballObject.getVx() > 0, ball);
                moveY--;
            }
            toDelete = checkBrickContact(ballObject, getBallContour(ball));
            if (toDelete != null) {
                if (toDelete.ifBonusBrick())
                    bonus = true;
                break;
            }
            if (ballReflecting(ballObject) || checkPaddleContact(ballObject, getBallContour(ball)))
                break;
        }
        if (toDelete != null)
            checkBrickDeleteAndBonus(bonus, toDelete);
    }

    //  Method will remove GameObject from arraylist and produce bonus effect.
    private void checkBrickDeleteAndBonus(boolean bonus, GameObjects toDelete) throws Exception {
        if (bonus)
            makeBonus();
        gameObjects.remove(toDelete);
        playClip("src\\com\\shpp\\p2p\\cs\\yyefimov\\assignment4\\soundEffects\\brickDestroy.wav");
    }

    //  Method will return some kind of "frame" for our ball. "Frame" is array which consist of
    //  four 2-Dimensional points
    private double[][] getBallContour(GOval ball) {
        double xPos = ball.getX() - 1;
        double yPos = ball.getY() - 1;
        double ballDiameter = ball.getWidth();
        return new double[][]{
                {xPos + ballDiameter / 2.0, yPos + ballDiameter / 2.0},
                {xPos, yPos},
                {xPos, yPos + ballDiameter},
                {xPos + ballDiameter, yPos},
                {xPos + ballDiameter, yPos + ballDiameter}
        };
    }

    //  Short move via X coordinate.
    private void separatedMoveX(boolean wayRight, GOval ball) {
        if (wayRight)
            ball.move(1, 0);
        else ball.move(-1, 0);
    }

    //  Short move via Y coordinate.
    private void separatedMoveY(boolean wayDown, GOval ball) {
        if (wayDown)
            ball.move(0, 1);
        else ball.move(0, -1);
    }

    //  Method will find the ball in gameObjects and return it.
    private GameObjects getBallObject() {
        for (GameObjects eachBall : gameObjects) {
            if (eachBall.ifBall()) {
                return eachBall;
            }
        }
        return null;
    }

    /*  Method will produce bonus effect after you hit in bonus brick. We have three kind of bonuses:
     *   1:  increase ball speed, BONUS_SPEED is multiplier.
     *   2:  making paddle longer, BONUS_PADDLE is multiplier.
     *   3:  making paddle shorter, ANTI_BONUS_PADDLE is divider.    */
    private static final double BONUS_SPEED = 1.25;
    private static final double BONUS_PADDLE = 1.25;
    private static final double ANTI_BONUS_PADDLE = 1.35;

    private void makeBonus() {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifPaddle()) {
                double size = eachObject.obj.getWidth();
                if (RG.nextBoolean()) {
                    if (RG.nextBoolean()) {
                        ((GRect) eachObject.obj).setSize(size / ANTI_BONUS_PADDLE, (eachObject.obj).getHeight());
                    } else
                        ((GRect) eachObject.obj).setSize(size * BONUS_PADDLE, (eachObject.obj).getHeight());
                } else {
                    GameObjects ball = getBallObject();
                    if (ball != null)
                        ball.setVy(ball.getVy() * BONUS_SPEED);
                }
            }
        }
    }

    /*  Method will check every point of ball "frame". If we find contact, we need to operate this.
     *   We're skipping ball contact to @menubar. */
    private GameObjects checkBrickContact(GameObjects ball, double[][] ballContour) {
        GObject collider;
        for (double[] doubles : ballContour) {
            collider = getElementAt(doubles[0], doubles[1]);
            if (collider != null) {
                if (!(collider instanceof GLabel) && !(collider instanceof GOval))
                    return ballBrickContact(ball, collider);
            }
        }
        return null;
    }

    /*  @collider if GObject, but our brick is GameObject, so first we need to find our GameObject
        using GObject. When we get GameObject we can modify it(destroy or other).
        Score what you will earn when brick is destroyed depending on ball speed and paddle width.
    * */
    private GameObjects ballBrickContact(GameObjects ball, GObject collider) {
        GameObjects deleteBrick = null;
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifBrick())
                if (eachObject.obj == collider) {
                    gameParameters.paddleContactAvailable = true;
                    //  If we hit brick from side we need to reflect vx speed for ball.
                    if (ball.obj.getY() > eachObject.getBrickLongitude()
                            && ball.obj.getY() < eachObject.getBrickLongitude() + BRICK_HEIGHT) {
                        ball.setVx(-ball.getVx());
                        if (ball.getVx() == 0) ball.setVy(-ball.getVy());
                    } else ball.setVy(-ball.getVy());
                    if (eachObject.ifBrick()) {
                        eachObject.reduceBrickLife(1);
                        //  If brick live == 0 we will destroy it and ear points.
                        if (eachObject.getBrickLife() == 0) {
                            deleteBrickAndEarnPoints(eachObject, ball);
                            deleteBrick = eachObject;
                            break;
                        }
                    }
                }
        }
        return deleteBrick;
    }

    //  Method will calculate how many points we earn and remove brick from canvas.
    private void deleteBrickAndEarnPoints(GameObjects eachObject, GameObjects ball) {
        remove(eachObject.obj);
        ball.setVy(ball.getVy() * SPEED_UP_PER_BRICK);
        gameParameters.score = gameParameters.score + PADDLE_WIDTH / getPaddleWidth() + Math.abs((ball.getVy() / BALL_SPEED_Y - 1));
        gameParameters.menuBarReset = true;
    }

    //  Method returns paddle width.
    private double getPaddleWidth() {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifPaddle())
                return (eachObject.obj.getWidth());
        }
        return 0;
    }

    /*  Method will check every point of ball "frame". If we find contact, we need to operate this.
     *   We're skipping ball contact to @menubar. */
    private boolean checkPaddleContact(GameObjects ball, double[][] ballContour) throws Exception {
        GObject collider;
        for (double[] doubles : ballContour) {
            collider = getElementAt(doubles[0], doubles[1]);
            if (collider != null) {
                if (!(collider instanceof GLabel) && !(collider instanceof GOval) && gameParameters.paddleContactAvailable) {
                    checkCollisionBallPaddle(ball, collider, doubles[0]);
                    return true;
                }
            }
        }
        return false;
    }

    /*  If we hit to paddle, we need to reflect the ball on the right way.
     *   There is some rules of reflecting from paddle. Reflecting depends on only from what part of paddle hit the ball.*/
    private void checkCollisionBallPaddle(GameObjects ball, GObject collider, double xPos) throws Exception {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifPaddle())
                if (eachObject.getPaddle() == collider) {
                    calculateReflecting(eachObject, ball, xPos);

                    gameParameters.paddleContactAvailable = false;
                    if (ball.obj.getY() + ball.obj.getHeight() > getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT)
                        ball.obj.setLocation(ball.obj.getX(), getHeight() - PADDLE_HEIGHT - PADDLE_Y_OFFSET - ball.obj.getHeight() - 1);
                    playClip("src\\com\\shpp\\p2p\\cs\\yyefimov\\assignment4\\soundEffects\\paddleBallContact.wav");
                }
        }
    }

    //  Method will reflect ball to right way.
    private void calculateReflecting(GameObjects eachObject, GameObjects ball, double xPos) {
        double delta = xPos - eachObject.obj.getX();
        /*  We split our paddle on segments, after that we can know in what part
         *  of paddle ball hits and that how we will control ball by paddle.
         * */
        double k = delta / (getPaddleWidth() / PADDLE_SENSITIVE);
        k = k - PADDLE_SENSITIVE / 2.0;
        ball.setVy(-ball.getVy());
        println(k);
        double xSpeed = k;
        if (Math.abs(xSpeed) > Math.abs(ball.getVy())) {
            if (xSpeed > 0)
                xSpeed = Math.abs(ball.getVy());
            else xSpeed = -Math.abs(ball.getVy());
        }
        ball.setVx(xSpeed);
    }

    //  Method will play sound effect when called.
    public void playClip(String path) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));

        AudioFormat format = stream.getFormat();
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2,
                    format.getFrameRate(), true);
            stream = AudioSystem.getAudioInputStream(format, stream);
        }

        DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(),
                ((int) stream.getFrameLength() * format.getFrameSize()));
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);
        clip.start();
    }

    //  Method will reflect ball from canvas borders. If we hit the lowest border - we loose the live.
    private boolean ballReflecting(GameObjects eachBall) {
        GOval ball = (GOval) eachBall.obj;
        if (ball.getY() >= getHeight() - ball.getHeight()) {
            gameParameters.gameOver = true;
            gameParameters.paddleContactAvailable = true;
            return true;
        }
        if (ball.getX() <= 0) {
            eachBall.setVx(-eachBall.getVx());
            ball.setLocation(1, ball.getY());
            gameParameters.paddleContactAvailable = true;
            return true;
        }
        if (ball.getX() >= getWidth() - ball.getWidth()) {
            eachBall.setVx(-eachBall.getVx());
            ball.setLocation(getWidth() - ball.getWidth() - 1, ball.getY());
            gameParameters.paddleContactAvailable = true;
            return true;
        }
        if (ball.getY() <= 0) {
            eachBall.setVy(-eachBall.getVy());
            ball.setLocation(ball.getX(), 2);
            gameParameters.paddleContactAvailable = true;
            return true;
        }
        return false;
    }

    //  Method will check if there are at least one brick left in the game.
    private boolean gameNotFinished() {
        for (GameObjects eachObject : gameObjects) {
            if (eachObject.ifBrick()) {
                if (eachObject.getBrickLife() > 0)
                    return true;
            }
        }
        gameParameters.gameWon = true;
        return false;
    }

    //  Method will control paddle by mouse moving.
    public void mouseMoved(MouseEvent mouseEvent) {
        GRect paddle = (GRect) gameObjects.get(0).obj;
        paddle.setLocation(mouseEvent.getX() - paddle.getWidth() / 2.0, paddle.getY());
        if (paddle.getX() <= 0)
            paddle.setLocation(0, paddle.getY());
        if (mouseEvent.getX() >= getWidth() - paddle.getWidth() / 2.0)
            paddle.setLocation(getWidth() - paddle.getWidth() - 1, paddle.getY());
    }

    //  Method will turn off the pause and quit the game if the game over.
    public void mouseClicked(MouseEvent mouseEvent) {
        if (gameParameters.pause) {
            try {
                playClip("src\\com\\shpp\\p2p\\cs\\yyefimov\\assignment4\\soundEffects\\gameStart.wav");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        gameParameters.pause = false;
        if (gameParameters.gameOver || gameParameters.gameWon)
            gameParameters.exit = true;
    }
}
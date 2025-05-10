import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Game extends JPanel implements KeyListener {


    private final Object conditionLock = new Object();

    private boolean directionChanged = false;

    private static final int REFRESH_RATE = 16;

    private volatile boolean running = true;

    private ArrayList<Point2D> snake2; // Second snake
    private int direction2; // Direction for second snake
    private ArrayList<Integer> keys2; // Keys for second snake


    private static final int SPEED = 100;

    /** The width and height of the boxes that the screen is split up into. */
    private static final int BOXWIDTH = 20;

    /**
     * The number of pixels of padding that adds distinction between the various
     * segments of the snake.
     */
    private static final int PADDING = 1;

    private int direction;
    // Add a score variable
    private int score1;

    private int score2;

    private int count;

    private boolean gameOver;

    private final Object directionLock = new Object();


    private ArrayList<Point2D> snake;


    private ArrayList<Integer> keys;

    private Point2D food;

    public Game(JFrame frame) {
        /* calls the superconstructor for the JPanel component */
        super();

        /* initializes the keys ArrayList */
        this.setKeys(new ArrayList<Integer>());

        /*
         * sets the size of the component with insets in mind, sets it to double
         * buffered, sets it to focusable, and adds the keylistener that this
         * object implements
         */
        this.setSize(frame.getWidth() - frame.getInsets().left - frame.getInsets().right,
                frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.snake2 = new ArrayList<Point2D>();
        this.direction2 = -1;
        this.keys2 = new ArrayList<Integer>();
        this.score2 = 0; // Initialize Player 2's score


        /* calls the resetGame() method */
        this.resetGame();


        /* sets the game to 'run' mode and starts the subthreads */
        new Thread(new Movement()).start();
        new Thread(new Input()).start();
        new Thread(new Input2()).start();
        new Thread(new CollisionDetection()).start();
        new Thread(new Repaint()).start();
        new Thread(new ScoreUpdate()).start();


        // Initialize second snake
        resetGame();
        snake2.add(new Point((int) (Math.random() * getWidth() / BOXWIDTH),
                (int) (Math.random() * getHeight() / BOXWIDTH)));

        // Start Player 2 input thread
        new Thread(new Input2()).start();
    }



    private class ScoreUpdate extends Thread {
        public void run() {
            while (running) {
                try {
                    // Sleep for 1 second before updating the score display
                    Thread.sleep(1000);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Update the display (this could be console output or UI updates)
                System.out.println("Score1: " + score1 + " | Score2: " + score2+" "+"Second:"+" "+count);
                System.out.println("___________________________________________");
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        /* creates a Graphics2D object from the passed Graphics object */
        Graphics2D g2d = (Graphics2D) g;

        /* draws the background of the game */
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        /* draws the snake */
        g2d.setColor(Color.GREEN);
        for (Point2D point : snake) {
            g2d.fillRect((int) point.getX() * BOXWIDTH + PADDING, (int) point.getY() * BOXWIDTH + PADDING,
                    BOXWIDTH - PADDING, BOXWIDTH - PADDING);
        }

        /* draws the food */
        g2d.setColor(Color.RED);
        g2d.fillRect((int) food.getX() * BOXWIDTH + PADDING, (int) food.getY() * BOXWIDTH + PADDING, BOXWIDTH - PADDING,
                BOXWIDTH - PADDING);
        // Display the score
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score1: " + score1, 10, 20);
        g2d.drawString("Score2: " + score2, 10, 40);
        // Display "Game Over" and score if the game is over
        if (gameOver) {
            g2d.setColor(Color.white);
            g2d.drawString("Game Over", getWidth() / 2 - 40, getHeight() / 2);
            g2d.drawString("    Score1: " + score1, getWidth() / 2 - 50, getHeight() / 2 + 15);
            g2d.drawString("       Score2: " + score2, getWidth() / 2 - 60, getHeight() / 2 + 30);
            g2d.drawString("         Press '0' to restart", getWidth() / 2 - 80, getHeight() / 2 + 45);


        }
        // Draw the second snake
        g2d.setColor(Color.BLUE); // Different color for Player 2
        for (Point2D point : snake2) {
            g2d.fillRect((int) point.getX() * BOXWIDTH + PADDING,
                    (int) point.getY() * BOXWIDTH + PADDING,
                    BOXWIDTH - PADDING, BOXWIDTH - PADDING);
        }

    }

    /**
     * Resets the parameters for the game. It does not create a new Game object
     * but rather, it resets the direction, snake, and food.
     */
    private void resetGame() {
        /* sets the snake to be at rest */
        setDirection(-1);

        /* resets the snake to a random point on the grid */
        setSnake(new ArrayList<Point2D>());
        getSnake().add(new Point((int) (Math.random() * getWidth() / BOXWIDTH),
                (int) (Math.random() * getHeight() / BOXWIDTH)));

        /* resets the food and calls the createFood() method */
        food = new Point();

        setFood(new Point());
        createFood();


        // Reset the score
        score1 = 0;
        score2 = 0;
        gameOver = false; // Reset game over state

        // Ensure the repaint method is called to update the UI
        repaint();

    }


    /** Essentially resets the food position. */
    private void createFood() {
        /* declares temporary variables */
        int x;
        int y;
        boolean flag;

        do {
            /* resets the flag */
            flag = false;

            /* sets a random coordinate for the temporary variables */
            x = (int) (Math.random() * getWidth() / BOXWIDTH);
            y = (int) (Math.random() * getHeight() / BOXWIDTH);

            /*
             * iterates through the snake's segments and checks to see if the
             * temporary coordinate intersects with any of the segments
             */
            for (Point2D point : snake)
                if (point.distance(x, y) == 0)
                    flag = true;

            /* continue looping if the temporary coordinate intersects */
        } while (flag);

        /* sets the food to the temporary coordinate */
        this.getFood().setLocation(x, y);


    }


    // Update keyPressed to handle Player 2 keys
    @Override
    public void keyPressed(KeyEvent e) {
        if (!keys.contains(e.getKeyCode())) {
            keys.add(e.getKeyCode());
        }
        if (!keys2.contains(e.getKeyCode())) {
            keys2.add(e.getKeyCode());
        }
    }



    // Update keyReleased to handle Player 2 keys
    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove((Integer) e.getKeyCode());
        keys2.remove((Integer) e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        /* if the ArrayList contains the escape key, stop the game */
        if (keys.contains(KeyEvent.VK_ESCAPE)){
            System.exit(0);}
        // Check for "0" key to reset the game if it's over
        if (gameOver && e.getKeyChar() == '0') {
            System.out.println("Restarting game...");
            System.out.println("Score1:"+" "+score1);
            System.out.println();
            System.out.println("Score2:"+" "+score2);
            System.out.println("-------------------");
            new Frame(1280, 720);

        }



    }



    public int getDirection() {
        synchronized (conditionLock) {
            return direction;
        }
    }

    public void setDirection(int direction) {
        synchronized (conditionLock) {
            this.direction = direction;
            directionChanged = true;
            conditionLock.notify(); // Notify movement thread
        }
    }

    /* standard get/set methods */
    public ArrayList<Point2D> getSnake() {
        return snake;
    }

    public void setSnake(ArrayList<Point2D> snake) {
        this.snake = snake;
    }

    public Point2D getFood() {
        return food;
    }

    public void setFood(Point2D food) {
        this.food = food;
    }


    public void setKeys(ArrayList<Integer> keys) {
        this.keys = keys;
    }



    public void updateDirection(int newDirection) {
        synchronized (directionLock) {
            direction = newDirection;
        }
    }

    private class Movement extends Thread {
        public void run() {
            while (running) {
                if (gameOver) {
                    // If game is over, do not move snakes
                    continue;
                }
                moveSnake(snake, direction);
                moveSnake(snake2, direction2);
                try {
                    Thread.sleep(SPEED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void moveSnake(ArrayList<Point2D> snake, int direction) {
            if (snake.isEmpty()) return; // Check if the snake is empty

            int oldX = (int) snake.get(0).getX();
            int oldY = (int) snake.get(0).getY();

            // Update the head position based on direction
            switch (direction) {
                case 0: snake.get(0).setLocation(oldX, oldY - 1); break; // Up
                case 1: snake.get(0).setLocation(oldX, oldY + 1); break; // Down
                case 2: snake.get(0).setLocation(oldX - 1, oldY); break; // Left
                case 3: snake.get(0).setLocation(oldX + 1, oldY); break; // Right
            }

            // Move the body segments
            for (int i = 1; i < snake.size(); i++) {
                Point2D temp = new Point2D.Double(snake.get(i).getX(), snake.get(i).getY());
                snake.get(i).setLocation(oldX, oldY);
                oldX = (int) temp.getX();
                oldY = (int) temp.getY();
            }
        }
    }



    private class CollisionDetection extends Thread {
        public void run() {
            while (running) {
                if (!gameOver) {
                    checkCollision(snake);
                    checkCollision(snake2);
                    checkFoodCollision(snake);
                    checkFoodCollision(snake2);
                    checkSnakeCollision();
                }
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void checkCollision(ArrayList<Point2D> snake) {
            if (snake.isEmpty()) return; // Check if the snake is empty

            Point2D head = snake.get(0);
            // Wall collision
            if (head.getX() < 0 || head.getY() < 0 || head.getX() >= getWidth() / BOXWIDTH || head.getY() >= getHeight() / BOXWIDTH) {
                gameOver = true; // Set game over if the snake hits the wall
                return;
            }

            // Self collision detection removed, handle collisions between snakes separately
        }
        // Check for collisions between the two snakes
        private void checkSnakeCollision() {
            if (snake.isEmpty() || snake2.isEmpty()) return; // Check if either snake is empty

            Point2D head1 = snake.get(0);

            // Check if head of snake1 collides with any part of snake2
            for (int i = 0; i < snake2.size(); i++) {
                if (head1.distance(snake2.get(i)) < 1) { // Collision detected
                    resetGame(); // Reset the game
                    return;
                }
            }

            Point2D head2 = snake2.get(0);

            // Check if head of snake2 collides with any part of snake1
            for (int i = 0; i < snake.size(); i++) {
                if (head2.distance(snake.get(i)) < 1) { // Collision detected
                    resetGame(); // Reset the game
                    break;

                }
            }
        }
    }

        private void checkFoodCollision(ArrayList<Point2D> snake) {
            if (snake.isEmpty()) return; // Check if the snake is empty

            Point2D head = snake.get(0);
            // Check if the head of the snake is at the same position as the food
            if (head.distance(food) < 1) { // Use a small threshold for collision detection
                // Grow the snake
                snake.add(new Point((int) snake.get(snake.size() - 1).getX(), (int) snake.get(snake.size() - 1).getY()));
                // Update the score
                if (snake == Game.this.snake) {
                    score1 += 10;
                } else {
                    score2 += 10;
                }
                createFood(); // Create new food
            }
        }




    private class Input extends Thread {

        public void detectInput(int newDirection) {
            // When input is detected
            synchronized (conditionLock) {
                updateDirection(newDirection); // Update the direction
                directionChanged = true;
                conditionLock.notify(); // Notify the movement thread
            }
        }
        public void moveSnake() {
            synchronized (conditionLock) {
                while (!directionChanged) {
                    try {
                        conditionLock.wait(); // Wait until direction is updated
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Move the snake based on updated direction
                directionChanged = false; // Reset the flag
            }
        }
        @Override
        public void run() {
            /* creates a loop for as long as the game is running */
            while (running) {
                /*
                 * if the ArrayList is not empty, checks to see what keys are
                 * being pressed and sets the direction based on the checks
                 */

                synchronized (conditionLock) {

                    if (!keys.isEmpty())
                        if (keys.get(keys.size() - 1) == KeyEvent.VK_W && (direction != 1 || snake.size() == 1))
                            direction = 0;
                        else if (keys.get(keys.size() - 1) == KeyEvent.VK_S && (direction != 0 || snake.size() == 1))
                            direction = 1;
                        else if (keys.get(keys.size() - 1) == KeyEvent.VK_A && (direction != 3 || snake.size() == 1))
                            direction = 2;
                        else if (keys.get(keys.size() - 1) == KeyEvent.VK_D && (direction != 2 || snake.size() == 1))
                            direction = 3;
                }
                /*
                 * sleep for REFRESH_RATE milliseconds; makes the loop iterate
                 * every speed milliseconds
                 */
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // New Input thread for Player 2
    private class Input2 extends Thread {
        @Override
        public void run() {
            while (running) {
                synchronized (conditionLock){
                    if (!keys2.isEmpty())
                    if (keys2.get(keys2.size() - 1) == KeyEvent.VK_UP && (direction2 != 1 || snake2.size() == 1))
                        direction2 = 0;
                    else if (keys2.get(keys2.size() - 1) == KeyEvent.VK_DOWN && (direction2 != 0 || snake2.size() == 1))
                        direction2 = 1;
                    else if (keys2.get(keys2.size() - 1) == KeyEvent.VK_LEFT && (direction2 != 3 || snake2.size() == 1))
                        direction2 = 2;
                    else if (keys2.get(keys2.size() - 1) == KeyEvent.VK_RIGHT && (direction2 != 2 || snake2.size() == 1))
                        direction2 = 3;
                }

                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class Repaint extends Thread {


        @Override
        public void run() {
            /* creates a loop for as long as the game is running */
            while (running) {
                /* calls the repaint() method to keep the component updated */
                repaint();

                /*
                 * sleep for REFRESH_RATE milliseconds; makes the loop iterate
                 * every speed milliseconds
                 */
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

        }



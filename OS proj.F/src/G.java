import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;

public class G extends JPanel implements KeyListener {

    private static final int REFRESH_RATE = 16;
    private static final int SPEED = 100;
    private static final int BOXWIDTH = 20;
    private static final int PADDING = 1;
    private boolean running;
    private int direction1, direction2; // Directions for both players
    private int score1, score2; // Scores for both players
    private boolean gameOver;
    private ArrayList<Point> snake1, snake2;
    private ArrayList<Integer> keys;
    private Point food;

    public G(JFrame frame) {
        super();
        this.setKeys(new ArrayList<>());
        this.setSize(frame.getWidth() - frame.getInsets().left - frame.getInsets().right,
                frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(this);
        resetGame();
        setRunning(true);
        new Thread(new Update()).start();
        new Thread(new Input()).start();
        new Thread(new Repaint()).start();
    }

    private void setRunning(boolean b) {
        this.running = b;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw player 1 snake
        g2d.setColor(Color.GREEN);
        for (Point point : snake1) {
            g2d.fillRect(point.x * BOXWIDTH + PADDING, point.y * BOXWIDTH + PADDING,
                    BOXWIDTH - PADDING, BOXWIDTH - PADDING);
        }

        // Draw player 2 snake
        g2d.setColor(Color.BLUE);
        for (Point point : snake2) {
            g2d.fillRect(point.x * BOXWIDTH + PADDING, point.y * BOXWIDTH + PADDING,
                    BOXWIDTH - PADDING, BOXWIDTH - PADDING);
        }

        // Draw food
        g2d.setColor(Color.RED);
        g2d.fillRect(food.x * BOXWIDTH + PADDING, food.y * BOXWIDTH + PADDING,
                BOXWIDTH - PADDING, BOXWIDTH - PADDING);

        // Display the score
        g2d.setColor(Color.WHITE);
        g2d.drawString("Player 1 Score: " + score1, 10, 20);
        g2d.drawString("Player 2 Score: " + score2, 10, 40);

        // Game Over state
        if (gameOver) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("Game Over", getWidth() / 2 - 40, getHeight() / 2);
            g2d.drawString("Player 1 Final Score: " + score1, getWidth() / 2 - 70, getHeight() / 2 + 15);
            g2d.drawString("Player 2 Final Score: " + score2, getWidth() / 2 - 70, getHeight() / 2 + 30);
            g2d.drawString("Press '0' to restart", getWidth() / 2 - 70, getHeight() / 2 + 45);
        }
    }

    private void resetGame() {
        setDirection1(-1);
        setDirection2(-1);
        snake1 = new ArrayList<>();
        snake2 = new ArrayList<>();
        snake1.add(new Point((int) (Math.random() * getWidth() / BOXWIDTH),
                (int) (Math.random() * getHeight() / BOXWIDTH)));
        snake2.add(new Point((int) (Math.random() * getWidth() / BOXWIDTH),
                (int) (Math.random() * getHeight() / BOXWIDTH)));

        food = new Point();
        createFood();

        score1 = 0;
        score2 = 0;
        gameOver = false;
        repaint();
    }

    private void createFood() {
        int x, y;
        boolean flag;
        do {
            flag = false;
            x = (int) (Math.random() * getWidth() / BOXWIDTH);
            y = (int) (Math.random() * getHeight() / BOXWIDTH);

            for (Point point : snake1) if (point.equals(new Point(x, y))) flag = true;
            for (Point point : snake2) if (point.equals(new Point(x, y))) flag = true;
        } while (flag);
        food.setLocation(x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!keys.contains(e.getKeyCode())) keys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove((Integer) e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (keys.contains(KeyEvent.VK_ESCAPE)) System.exit(0);
        if (gameOver && e.getKeyChar() == '0') {
            System.out.println("Restarting game...");
            new Frame(1280, 720);
        }
    }

    public ArrayList<Integer> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Integer> keys) {
        this.keys = keys;
    }

    public int getDirection1() {
        return direction1;
    }

    public void setDirection1(int direction1) {
        this.direction1 = direction1;
    }

    public int getDirection2() {
        return direction2;
    }

    public void setDirection2(int direction2) {
        this.direction2 = direction2;
    }

    private class Update extends Thread {

        public void run() {
            while (running) {
                updateSnake(snake1, direction1);
                updateSnake(snake2, direction2);

                checkCollisions();

                try {
                    Thread.sleep(SPEED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateSnake(ArrayList<Point> snake, int direction) {
            int oldX = snake.get(0).x;
            int oldY = snake.get(0).y;

            // Move the head of the snake
            if (direction == 0) snake.get(0).setLocation(oldX, oldY - 1);
            else if (direction == 1) snake.get(0).setLocation(oldX, oldY + 1);
            else if (direction == 2) snake.get(0).setLocation(oldX - 1, oldY);
            else if (direction == 3) snake.get(0).setLocation(oldX + 1, oldY);

            // Update the body of the snake
            for (int i = 1; i < snake.size(); i++) {
                Point temp = snake.get(i);
                snake.get(i).setLocation(oldX, oldY);
                oldX = temp.x;
                oldY = temp.y;
            }
        }

        private void checkCollisions() {
            if (snake1.get(0).x < 0 || snake1.get(0).y < 0 || snake1.get(0).x >= getWidth() / BOXWIDTH || snake1.get(0).y >= getHeight() / BOXWIDTH ||
                    snake2.get(0).x < 0 || snake2.get(0).y < 0 || snake2.get(0).x >= getWidth() / BOXWIDTH || snake2.get(0).y >= getHeight() / BOXWIDTH) {
                gameOver = true;
            }

            for (int i = 1; i < snake1.size(); i++) {
                if (snake1.get(i).equals(snake1.get(0)) || snake2.get(0).equals(snake1.get(i))) {
                    gameOver = true;
                }
            }

            for (int i = 1; i < snake2.size(); i++) {
                if (snake2.get(i).equals(snake2.get(0)) || snake1.get(0).equals(snake2.get(i))) {
                    gameOver = true;
                }
            }

            // Check for food consumption
            if (snake1.get(0).equals(food)) {
                snake1.add(new Point(snake1.get(0).x, snake1.get(0).y));
                createFood();
                score1 += 10;
            }

            if (snake2.get(0).equals(food)) {
                snake2.add(new Point(snake2.get(0).x, snake2.get(0).y));
                createFood();
                score2 += 10;
            }
        }
    }

    private class Input extends Thread {

        public void run() {
            while (running) {
                if (!keys.isEmpty()) {
                    int key = keys.get(keys.size() - 1);
                    if (key == KeyEvent.VK_W && direction1 != 1) direction1 = 0;
                    if (key == KeyEvent.VK_S && direction1 != 0) direction1 = 1;
                    if (key == KeyEvent.VK_A && direction1 != 3) direction1 = 2;
                    if (key == KeyEvent.VK_D && direction1 != 2) direction1 = 3;

                    if (key == KeyEvent.VK_UP && direction2 != 1) direction2 = 0;
                    if (key == KeyEvent.VK_DOWN && direction2 != 0) direction2 = 1;
                    if (key == KeyEvent.VK_LEFT && direction2 != 3) direction2 = 2;
                    if (key == KeyEvent.VK_RIGHT && direction2 != 2) direction2 = 3;
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

        public void run() {
            while (running) {
                repaint();
                try {
                    Thread.sleep(REFRESH_RATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
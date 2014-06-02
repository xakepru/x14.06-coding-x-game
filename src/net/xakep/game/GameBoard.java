package net.xakep.game;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameBoard extends JPanel {
    private BufferedImage hackerImage;
    private BufferedImage adminImage;
    private Font levelFont;
    static final int CELL_WIDTH = 64;
    static final int CELL_COUNT = 8;
    private Font tableFont;
    private Boolean[] table = new Boolean[CELL_COUNT * CELL_COUNT];
    private final Color oddColor;
    private final Color evenColor;

    enum GameState {START, HACKER_TURN, ADMIN_TURN,  END}
    private GameState state;
    private GameProcessor gameProcessor;

    private Timer timer;

    public static void main(String... args) {
        JFrame frame = new JFrame("Админы против хакеров");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.getContentPane().add(new GameBoard());

        int size = CELL_WIDTH * (CELL_COUNT + 2);
        frame.setSize(size, size);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public GameBoard() {
        hackerImage = null;
        adminImage = null;
        try {
            tableFont = Font.createFont(Font.TRUETYPE_FONT, new File("images/VIDEOPHREAK.ttf")).deriveFont(Font.BOLD, 20);
            levelFont = Font.createFont(Font.TRUETYPE_FONT, new File("images/PROPAGAN.ttf")).deriveFont(Font.BOLD, 60f);
            hackerImage = ImageIO.read(new File("images/hacker.png"));
            adminImage = ImageIO.read(new File("images/admin.png"));
        } catch (IOException e) {
        } catch (FontFormatException e) {
        }
        oddColor = new Color(0x76, 0x3d, 0x3d);
        evenColor = new Color(0xd6, 0xbf, 0xbf);

        timer = new Timer();

        gameProcessor = new GameProcessor2();
        state = GameState.START;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GameBoard.this.mouseClicked(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        //рисуем клетки поля
        for (int i = 0; i < CELL_COUNT * CELL_COUNT; i++) {
            if (i % 2 == 0 && i / CELL_COUNT % 2 == 0 || i % 2 != 0 && i / CELL_COUNT % 2 != 0) {
                g.setColor(oddColor);
            } else {
                g.setColor(evenColor);
            }
            g.fillRect(CELL_WIDTH + i % CELL_COUNT * CELL_WIDTH, CELL_WIDTH + i / CELL_COUNT  * CELL_WIDTH, CELL_WIDTH, CELL_WIDTH);
        }

        //рисуем границу поля
        g.setColor(Color.BLACK);
        g.drawRect(CELL_WIDTH, CELL_WIDTH, CELL_WIDTH * CELL_COUNT, CELL_WIDTH * CELL_COUNT);

        //рисуем буквы
        g.setFont(tableFont);
        for (int i = 0; i < CELL_COUNT; i++) {
            g.drawChars(new char[]{(char) ('A' + i)}, 0, 1, CELL_WIDTH / 2 - 10, (i + 1) * CELL_WIDTH + CELL_WIDTH / 2 + 8);
        }

        //рисуем цифры
        for (int i = 0; i < CELL_COUNT; i++) {
            g.drawChars(new char[]{(char) ('1' + i)}, 0, 1, (i + 1) * CELL_WIDTH + CELL_WIDTH / 2 - 10, CELL_WIDTH / 2 + 8);
        }

        //рисуем фишки
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                BufferedImage image = table[i] ? hackerImage : adminImage;
                g.drawImage(image, CELL_WIDTH + i % CELL_COUNT * CELL_WIDTH, CELL_WIDTH + i / CELL_COUNT * CELL_WIDTH, null);
            }
        }

        switch (state) {
            case START:
                drawCenterText(g, "НОВАЯ ИГРА");
                break;
            case END:
                drawCenterText(g, "КОНЕЦ ИГРЫ");
                break;
        }
    }

    private void drawCenterText(Graphics g, String str) {
        g.setColor(Color.GREEN.darker());
        g.setFont(levelFont);
        int size = CELL_WIDTH * (CELL_COUNT + 2);
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(str, g);
        g.drawString(str, (int) (size / 2 - stringBounds.getWidth() / 2), (int) (size / 2 + stringBounds.getHeight() / 4));
    }

    private void mouseClicked(int x, int y) {
        switch (state) {
            case START:
                state = GameState.HACKER_TURN;
                repaint();
                break;
            case HACKER_TURN:
                // находим ячейку поля, на которую пользователь кликнул мышкой
                if (x > CELL_WIDTH && x < CELL_WIDTH * (CELL_COUNT + 2)
                        && y > CELL_WIDTH && y < CELL_WIDTH * (CELL_COUNT + 2)) {
                    int xPos = (x - CELL_WIDTH) / CELL_WIDTH;
                    int yPos = (y - CELL_WIDTH) / CELL_WIDTH;
                    int index = yPos * CELL_COUNT + xPos;
                    if (table[index] == null) {
                        table[index] = true;
                        repaint();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (gameProcessor.isGameOver(table, true)) {
                                    state = GameState.END;
                                } else {
                                    state = GameState.ADMIN_TURN;
                                }
                                repaint();
                            }
                        }, 500);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                think();
                            }
                        }, 1000);
                    }
                }
                break;
            case END:
                state = GameState.START;
                for (int i = 0; i < table.length; i++) {
                    table[i] = null;
                }
                repaint();
                break;
        }
    }

    private void think() {
        int nextMove = gameProcessor.nextMove(table, false);
        if (nextMove < 0) {
            state = GameState.END;
        } else {
            table[nextMove] = false;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (gameProcessor.isGameOver(table, false)) {
                        state = GameState.END;
                    } else {
                        state = GameState.HACKER_TURN;
                    }
                    repaint();
                }
            }, 500);
        }
        repaint();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Game implements Runnable {
    private JFrame frame;
    private boolean running = false;
    private Thread thread;
    private mario player;
    private Gameinti gameScreen;

    public Game() {
        init();
    }

    private void init() {
        frame = new JFrame("Mario Bros");
        frame.setSize(1080, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(new StartScreen());
        frame.setVisible(true);
    }

    private synchronized void start() {
        if (running) return; // Cegah thread ganda
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        final double FPS = 60.0;
        final double NANO_SECONDS_PER_FRAME = 1000000000.0 / FPS;

        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        long timer = 0;
        int frames = 0;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / NANO_SECONDS_PER_FRAME;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                if (gameScreen != null) {
                    gameScreen.updateMarioVelocity();
                    if (gameScreen.getPlayer() != null) {
                        gameScreen.getPlayer().updatelokasi();
                    }
                }
                frame.repaint();
                frames++;
                delta--;
            }

            if (timer >= 1000000000) {
                frames = 0;
                timer = 0;
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class StartScreen extends JPanel {
        private Font titleFont, menuFont;
        private BufferedImage selectIcon;
        private int selectedOption = 0;

        public StartScreen() {
            setBackground(Color.BLACK);

            titleFont = useFont("assets/font/mario-font.ttf", 72);
            menuFont = useFont("assets/font/mario-font.ttf", 36);

            try {
                selectIcon = ImageIO.read(new File("assets/select-icon.png"));
            } catch (IOException e) {
                System.err.println("Gagal memuat ikon select!");
                e.printStackTrace();
            }

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectedOption = (selectedOption + 1) % 2;
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectedOption = (selectedOption - 1 + 2) % 2;
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (selectedOption == 0) {
                            startGame();
                        } else if (selectedOption == 1) {
                            System.exit(0);
                        }
                    }
                }
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            setFocusable(true);
            requestFocusInWindow();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage gambarbck = null;
            try {
                gambarbck = ImageIO.read(getClass().getResource("/assets/bckmenu.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            g.drawImage(gambarbck, 0, 0, getWidth(), getHeight(), null);
            g.setColor(Color.WHITE);
            g.setFont(titleFont != null ? titleFont : new Font("Arial", Font.BOLD, 72));
            String title = "Mario Bros";
            FontMetrics fm = g.getFontMetrics();
            int titleX = (getWidth() - fm.stringWidth(title)) / 2;
            int titleY = 200;
            g.drawString(title, titleX, titleY);

            g.setFont(menuFont != null ? menuFont : new Font("Arial", Font.PLAIN, 36));

            String startText = "Start";
            String quitText = "Quit";

            int menuX = getWidth() / 2 - 50;
            int startY = 350;
            int quitY = 420;

            g.drawString(startText, menuX, startY);
            g.drawString(quitText, menuX, quitY);

            if (selectIcon != null) {
                int iconX = menuX - 60;
                int iconY = selectedOption == 0 ? startY - 30 : quitY - 30;

                g.drawImage(selectIcon, iconX, iconY, 32, 32, null);
            }
        }
    }

    private Font useFont(String path, float size) {
        try {
            File fontFile = new File(path);
            if (!fontFile.exists()) {
                System.err.println("Font tidak ditemukan: " + path);
                return new Font("Arial", Font.PLAIN, (int) size);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            return font.deriveFont(size);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    private void startGame() {
        start();
        frame.getContentPane().removeAll();
        gameScreen = new Gameinti();
        frame.add(gameScreen);
        frame.revalidate();
        frame.repaint();
        gameScreen.requestFocusInWindow();
    }

    private class Gameinti extends JPanel {
        private BufferedImage superMushroom, oneUpMushroom, fireFlower, coin;
        private BufferedImage ordinaryBrick, surpriseBrick, groundBrick, pipe;
        private BufferedImage goombaLeft, goombaRight, koopaLeft, koopaRight, endFlag;
        private boolean sudahgambar = false;
        Map mapgame = new Map();
        ArrayList<Bricks> listbricks = new ArrayList<Bricks>();
        private boolean[] keysPressed = new boolean[256];

        public Gameinti() {
            System.out.println("Game Dimulai!");
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    if (keyCode >= 0 && keyCode < keysPressed.length) {
                        keysPressed[keyCode] = true;
                    }
                    if (player == null) return;
                    switch (keyCode) {
                        case KeyEvent.VK_SPACE:
                            player.melompat();
                            break;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    if (keyCode >= 0 && keyCode < keysPressed.length) {
                        keysPressed[keyCode] = false;
                    }
                }
            });
        }

        public void updateMarioVelocity() {
            if (player == null) return;

            boolean leftPressed = keysPressed[KeyEvent.VK_LEFT];
            boolean rightPressed = keysPressed[KeyEvent.VK_RIGHT];

            if (leftPressed) {
                player.gerakmario(false, listbricks);
            } else if (rightPressed) {
                player.gerakmario(true, listbricks);
            } else {
                player.setVelX(0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            Toolkit.getDefaultToolkit().sync();
            BufferedImage gambarbck = null;
            try {
                gambarbck = ImageIO.read(getClass().getResource("/assets/background.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            g2.drawImage(gambarbck, 0, 0, null);
            if (!sudahgambar) {
                BufferedImage sprite = null;
                try {
                    sprite = ImageIO.read(getClass().getResource("/assets/sprite.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.superMushroom = getSubImage(sprite, 2, 5, 48, 48);
                this.oneUpMushroom = getSubImage(sprite, 3, 5, 48, 48);
                this.fireFlower = getSubImage(sprite, 4, 5, 48, 48);
                this.coin = getSubImage(sprite, 1, 5, 48, 48);
                this.ordinaryBrick = getSubImage(sprite, 1, 1, 48, 48);
                this.surpriseBrick = getSubImage(sprite, 2, 1, 48, 48);
                this.groundBrick = getSubImage(sprite, 2, 2, 48, 48);
                this.pipe = getSubImage(sprite, 3, 1, 96, 96);
                this.goombaLeft = getSubImage(sprite, 2, 4, 48, 48);
                this.goombaRight = getSubImage(sprite, 5, 4, 48, 48);
                this.koopaLeft = getSubImage(sprite, 1, 3, 48, 64);
                this.koopaRight = getSubImage(sprite, 4, 3, 48, 64);
                this.endFlag = getSubImage(sprite, 5, 1, 48, 48);

                int ordinaryBrickColor = new Color(0, 0, 255).getRGB();
                int surpriseBrickColor = new Color(255, 255, 0).getRGB();
                int groundBrickColor = new Color(255, 0, 0).getRGB();
                int marioColor = new Color(160, 160, 160).getRGB();
                BufferedImage gambarmap = null;
                try {
                    gambarmap = ImageIO.read(getClass().getResource("/assets/maps/Map 1.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int pixelMultiplier = 48;
                for (int x = 0; x < gambarmap.getWidth(); x++) {
                    for (int y = 0; y < gambarmap.getHeight(); y++) {

                        int currentPixel = gambarmap.getRGB(x, y);
                        int xLocation = x * pixelMultiplier;
                        int yLocation = y * pixelMultiplier;

                        if (currentPixel == ordinaryBrickColor) {
                            Bricks brick = new OrdinaryBrick(xLocation, yLocation, this.ordinaryBrick);
                            listbricks.add(brick);
                            mapgame.addBrick(brick);
                        } else if (currentPixel == surpriseBrickColor) {
                            Bricks brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick);
                            listbricks.add(brick);
                            mapgame.addBrick(brick);
                        } else if (currentPixel == groundBrickColor) {
                            Bricks brick = new GroundBrick(xLocation, yLocation, this.groundBrick);
                            listbricks.add(brick);
                            mapgame.addGroundBrick(brick);
                        } else if (currentPixel == marioColor) {
                            player = new mario(xLocation, yLocation);
                            player.draw(g);
                        }
                    }
                }
                mapgame.drawBricks(g2);
                sudahgambar = true;
            } else {
                mapgame.drawBricks(g2);
                player.draw(g);
            }
            if (player.jumping) {
                player.falling = true;
            }
            for (Bricks brick : listbricks) {
                Rectangle brickTopBounds = brick.dapatkanbatas(1);
                if (player.dapatkanbatas(2).intersects(brickTopBounds)) {
                    player.y = brick.y - player.img.getHeight() + 1;
                    player.falling = false;
                    player.velY = 0;
                }
            }
            if (player.y + player.img.getHeight() >= 624) {
                player.y = 624 - player.img.getHeight();
                player.falling = false;
                player.velY = 0;
            }
            g2.dispose();
        }

        public BufferedImage getSubImage(BufferedImage image, int col, int row, int w, int h) {
            return image.getSubimage((col - 1) * 48, (row - 1) * 48, w, h);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            setFocusable(true);
            requestFocusInWindow();
        }

        public mario getPlayer() {
            return player;
        }
    }

    public static void main(String[] args) {
        new Game();
    }
}
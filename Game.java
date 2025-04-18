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
    private boolean isRunning;
    private Thread thread;
    private mario player;
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

        // For FPS counter
        long timer = 0;
        int frames = 0;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / NANO_SECONDS_PER_FRAME;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                if (player != null) {
                    player.updatelokasi();
                }
                frame.repaint();
                frames++;
                delta--;
            }

            // Display FPS every second
            if (timer >= 1000000000) {
//                System.out.println("FPS: " + frames);
                frames = 0;
                timer = 0;
            }

            // Sleep to reduce CPU usage
            try {
                Thread.sleep(1);
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
                gambarbck = ImageIO.read(getClass().getResource("/assets/bckmenu.jpg" ));
            } catch (IOException e) {
                e.printStackTrace();
            }
            g.drawImage(gambarbck, 0, 0,getWidth(), getHeight(), null);
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
        Gameinti gameScreen = new Gameinti();
        frame.add(gameScreen);
        frame.revalidate();
        frame.repaint();
        gameScreen.requestFocusInWindow();
    }
    private class Gameinti extends JPanel{
        private BufferedImage superMushroom, oneUpMushroom, fireFlower, coin;
        private BufferedImage ordinaryBrick, surpriseBrick, groundBrick, pipe;
        private BufferedImage goombaLeft, goombaRight, koopaLeft, koopaRight, endFlag;
        private boolean sudahgambar = false;
        Map mapgame = new Map();
        ArrayList<Bricks> listbricks = new ArrayList<Bricks>();
        public Gameinti() {
            System.out.println("Game Dimulai!");
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (player == null) return;

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            player.gerakmario(false,listbricks);
                            break;
                        case KeyEvent.VK_RIGHT:
                            player.gerakmario(true,listbricks);
                            break;
                        case KeyEvent.VK_SPACE:
                            player.melompat();
                            break;
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {
                    if (player == null) return;

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            break;
                        case KeyEvent.VK_RIGHT:
                            break;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            BufferedImage gambarbck = null;
            try {
                gambarbck = ImageIO.read(getClass().getResource("/assets/background.png" ));
            } catch (IOException e) {
                e.printStackTrace();
            }
            g2.drawImage(gambarbck, 0, 0, null);
            if(sudahgambar == false){
                //membuat sprite
                BufferedImage sprite = null;
                try {
                    sprite = ImageIO.read(getClass().getResource("/assets/sprite.png" ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.superMushroom = this.getSubImage(sprite, 2, 5, 48, 48);
                this.oneUpMushroom= this.getSubImage(sprite, 3, 5, 48, 48);
                this.fireFlower= this.getSubImage(sprite, 4, 5, 48, 48);
                this.coin = this.getSubImage(sprite, 1, 5, 48, 48);
                this.ordinaryBrick = this.getSubImage(sprite, 1, 1, 48, 48);
                this.surpriseBrick = this.getSubImage(sprite, 2, 1, 48, 48);
                this.groundBrick = this.getSubImage(sprite, 2, 2, 48, 48);
                this.pipe = this.getSubImage(sprite, 3, 1, 96, 96);
                this.goombaLeft = this.getSubImage(sprite, 2, 4, 48, 48);
                this.goombaRight = this.getSubImage(sprite, 5, 4, 48, 48);
                this.koopaLeft = this.getSubImage(sprite, 1, 3, 48, 64);
                this.koopaRight = this.getSubImage(sprite, 4, 3, 48, 64);
                this.endFlag = this.getSubImage(sprite, 5, 1, 48, 48);
                //generate player musuh dan map

                int ordinaryBrick = new Color(0, 0, 255).getRGB();
                int surpriseBrick = new Color(255, 255, 0).getRGB();
                int groundBrick = new Color(255, 0, 0).getRGB();
                int mario = new Color(160, 160, 160).getRGB();
                BufferedImage gambarmap = null;
                try {
                    gambarmap = ImageIO.read(getClass().getResource("/assets/maps/Map 1.png" ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int pixelMultiplier = 48;
                for (int x = 0; x < gambarmap.getWidth(); x++) {
                    for (int y = 0; y < gambarmap.getHeight(); y++) {

                        int currentPixel = gambarmap.getRGB(x, y);
                        int xLocation = x*pixelMultiplier;
                        int yLocation = y*pixelMultiplier;

                        if (currentPixel == ordinaryBrick) {
                            Bricks brick = new OrdinaryBrick(xLocation, yLocation, this.ordinaryBrick);
                            listbricks.add(brick);
                            mapgame.addBrick(brick);
                        }
                        else if (currentPixel == surpriseBrick) {
                            Bricks brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick);
                            listbricks.add(brick);
                            mapgame.addBrick(brick);
                        }
                        else if (currentPixel == groundBrick) {
                            Bricks brick = new GroundBrick(xLocation, yLocation, this.groundBrick);
                            listbricks.add(brick);
                            mapgame.addGroundBrick(brick);
                        }
                        else if (currentPixel == mario) {
                            player = new mario(xLocation, yLocation);
                            player.draw(g);
                        }
                    }
                }
                mapgame.drawBricks(g2);
                sudahgambar = true;
            }else{
                mapgame.drawBricks(g2);
                player.draw(g);
            }
            if(player.jumping){
                player.falling = true;
            }
            for (Bricks brick : listbricks) {
                Rectangle brickTopBounds = brick.dapatkanbatas(1);
                if (player.dapatkanbatas(2).intersects(brickTopBounds)) {
                    player.y = brick.y - player.img.getHeight()+1;
                    player.falling = false;
                    player.velY = 0;
                }
            }
            if(player.y + player.img.getHeight() >= 624){
                player.y = 624-player.img.getHeight();
                player.falling = false;
                player.velY = 0;
            }
            Toolkit.getDefaultToolkit().sync();
            g2.dispose();
        }
        public BufferedImage getSubImage(BufferedImage image, int col, int row, int w, int h){
            if((col == 1 || col == 4) && row == 3){ //koopa
                return image.getSubimage((col-1)*48, 128, w, h);
            }
            return image.getSubimage((col-1)*48, (row-1)*48, w, h);
        }
        @Override
        public void addNotify() {
            super.addNotify();
            setFocusable(true);
            requestFocusInWindow(); // supaya bisa menerima input kalau dibutuhkan
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}

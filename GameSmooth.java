import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class GameSmooth extends Canvas implements Runnable {
    public static final int WIDTH = 1080, HEIGHT = 720;
    private Thread thread;
    private boolean running = false;

    private enum GameState {
        MENU, PLAYING
    }

    private GameState currentState = GameState.MENU;
    private int menuSelection = 0;

    private mario player;

    private Map mapgame = new Map();
    private Camera camera;
    private ArrayList<Bricks> listBricks = new ArrayList<>();
    private boolean[] keys = new boolean[256];

    private BufferedImage background, gameBackground, spriteSheet, selectIcon;
    private BufferedImage ordinaryBrickTex, surpriseBrickTex, groundBrickTex;
    private int groundY;
    private boolean spaceReleased = true;
    private Clip clip;
    private Font marioFont;

    public GameSmooth() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        loadResources();
        camera = new Camera();
        initInput();
        initWindow();
        start();
    }

    private void loadResources() {
        try {
            background = ImageIO.read(getClass().getResource("/assets/bckmenu.jpg"));
            gameBackground = ImageIO.read(getClass().getResource("/assets/background.png"));
            spriteSheet = ImageIO.read(getClass().getResource("/assets/sprite.png"));
            selectIcon = ImageIO.read(getClass().getResource("/assets/select-icon.png"));
            marioFont = useFont("/assets/font/mario-font.ttf", 64);

            ordinaryBrickTex = getSub(spriteSheet, 1, 1);
            surpriseBrickTex = getSub(spriteSheet, 2, 1);
            groundBrickTex = getSub(spriteSheet, 2, 2);

            BufferedImage mapImg = ImageIO.read(getClass().getResource("/assets/maps/Map 1.png"));
            int pixelSize = 48;

            for (int mx = 0; mx < mapImg.getWidth(); mx++) {
                for (int my = 0; my < mapImg.getHeight(); my++) {
                    int px = mapImg.getRGB(mx, my);
                    int wx = mx * pixelSize, wy = my * pixelSize;

                    if (px == new Color(160, 160, 160).getRGB()) {
                        player = new mario(wx, wy);
                    } else if (px == new Color(0, 0, 255).getRGB()) {
                        OrdinaryBrick ob = new OrdinaryBrick(wx, wy, ordinaryBrickTex);
                        listBricks.add(ob);
                        mapgame.addBrick(ob);
                    } else if (px == new Color(255, 255, 0).getRGB()) {
                        SurpriseBrick sb = new SurpriseBrick(wx, wy, surpriseBrickTex);
                        listBricks.add(sb);
                        mapgame.addBrick(sb);
                    } else if (px == new Color(255, 0, 0).getRGB()) {
                        GroundBrick gb = new GroundBrick(wx, wy, groundBrickTex);
                        listBricks.add(gb);
                        mapgame.addGroundBrick(gb);
                    }
                }
            }

            // Hitung groundY
            int maxY = 0;
            for (Bricks brick : listBricks) {
                if (brick instanceof GroundBrick) {
                    maxY = Math.max(maxY, (int) brick.y);
                }
            }
            groundY = maxY + groundBrickTex.getHeight();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BufferedImage getSub(BufferedImage sheet, int col, int row) {
        return sheet.getSubimage((col - 1) * 48, (row - 1) * 48, 48, 48);
    }

    private void initInput() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = true;
                if (currentState == GameState.MENU) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                        menuSelection = 1 - menuSelection;
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (menuSelection == 0)
                            currentState = GameState.PLAYING;
                        else
                            System.exit(0);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                    spaceReleased = false;
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = false;
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                    spaceReleased = true;
            }
        });
        setFocusable(true);
    }

    private void initWindow() {
        JFrame frame = new JFrame("Mario Bros");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public synchronized void start() {
        if (running)
            return;
        running = true;
        thread = new Thread(this);
        thread.start();
        playBackgroundMusic("/assets/audio/background.wav");
    }

    public void run() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        final double FPS = 60.0;
        final double NS_PER_FRAME = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / NS_PER_FRAME;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
            }
            render(bs);
        }
    }

    private void tick() {
        if (currentState != GameState.PLAYING || player == null)
            return;
        if (keys[KeyEvent.VK_LEFT]) {
            player.gerakmario(false, listBricks);
        } else if (keys[KeyEvent.VK_RIGHT]) {
            player.gerakmario(true, listBricks);
        } else {
            player.setVelX(0);
        }
        if (keys[KeyEvent.VK_SPACE]) {
            player.melompat();
            playJumpMusic("/assets/audio/jump.wav");
            spaceReleased = false;
        }

        player.updatelokasi();
        player.falling = true;
        for (Bricks brick : listBricks) {
            Rectangle top = brick.dapatkanbatas(1);
            if (player.dapatkanbatas(2).intersects(top)) {
                player.y = brick.y - player.img.getHeight();
                player.falling = false;
                player.velY = 0;
                break;
            }
        }
        if (player.y + player.img.getHeight() >= groundY) {
            player.y = groundY - player.img.getHeight();
            player.falling = false;
            player.velY = 0;
        }

        camera.update(player);
    }

    private void render(BufferStrategy bs) {
        do {
            do {
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                g.clearRect(0, 0, WIDTH, HEIGHT);

                if (currentState == GameState.MENU) {
                    g.drawImage(background, 0, 0, WIDTH, HEIGHT, null);
                    renderMenu(g);
                } else if (currentState == GameState.PLAYING) {
                    g.translate(-camera.getX(), 0);

                    g.drawImage(gameBackground, 0, 0, null);
                    mapgame.drawBricks(g);
                    if (player != null)
                        player.draw(g);

                    g.translate(camera.getX(), camera.getY());
                }

                g.dispose();
            } while (bs.contentsRestored());
            bs.show();
        } while (bs.contentsLost());
        Toolkit.getDefaultToolkit().sync();
    }

    private void renderMenu(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(marioFont);
        g.drawString("MARIO BROS", WIDTH / 2 - 250, HEIGHT / 2 - 250);
        g.setFont(marioFont.deriveFont(56f));
        g.setColor(menuSelection == 0 ? Color.BLACK : Color.WHITE);
        g.drawString("Start", WIDTH / 2 - 100, HEIGHT / 2);
        g.setColor(menuSelection == 1 ? Color.BLACK : Color.WHITE);
        g.drawString("Quit", WIDTH / 2 - 100, HEIGHT / 2 + 80);
        int iconY = (menuSelection == 0) ? HEIGHT / 2 - 40 : HEIGHT / 2 + 20;
        g.drawImage(selectIcon, WIDTH / 2 - 150, iconY, null);
    }

    private void playBackgroundMusic(String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(path));
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playJumpMusic(String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(path));
            Clip jump = AudioSystem.getClip();
            jump.open(audioIn);
            jump.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Font useFont(String path, float size) {
        try {
            InputStream fontStream = getClass().getResourceAsStream(path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            return font.deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    public static void main(String[] args) {
        new GameSmooth();
    }
}

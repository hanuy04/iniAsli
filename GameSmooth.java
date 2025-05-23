import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

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
    private int mapEndX;
    private Map mapgame = new Map();
    private Camera camera;
    private ArrayList<Bricks> listBricks = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    private boolean[] keys = new boolean[256];

    private BufferedImage background, gameBackground, spriteSheet, selectIcon;
    private BufferedImage ordinaryBrickTex, surpriseBrickTex, groundBrickTex,goombaLeft,goombaRight,koopaLeft,koopaRight;
    private int groundY;
    private boolean spaceReleased = true;
    private Clip clip;
    private Font marioFont;
    private int score = 0;

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

            ordinaryBrickTex = getSub(spriteSheet, 1, 1,48,48);
            surpriseBrickTex = getSub(spriteSheet, 2, 1,48,48);
            groundBrickTex = getSub(spriteSheet, 2, 2,48,48);
            goombaLeft = getSub(spriteSheet, 2, 4, 48, 48);
            goombaRight = getSub(spriteSheet, 5, 4, 48, 48);
            koopaLeft = getSub(spriteSheet, 1, 3, 48, 64);
            koopaRight = getSub(spriteSheet, 4, 3, 48, 64);
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
                    }else if (px == new Color(0, 255, 255).getRGB()) {
                        Enemy enemy = new Enemy(wx, wy, this.goombaLeft, this.goombaRight);
                        enemies.add(enemy);
                    }
                    else if (px == new Color(255, 0, 255).getRGB()) {
                        Enemy enemy = new Enemy(wx, wy, this.koopaLeft, this.koopaRight);
                        enemies.add(enemy);
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
            mapEndX = mapImg.getWidth() * 48;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private BufferedImage getSub(BufferedImage image, int col, int row, int w, int h ) {
        if((col == 1 || col == 4) && row == 3){ //koopa
            return image.getSubimage((col-1)*48, 128, w, h);
        }
        return image.getSubimage((col-1)*48, (row-1)*48, w, h);
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
        // playBackgroundMusic("/assets/audio/background.wav");
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
        } else if (keys[KeyEvent.VK_ESCAPE]) {
            restartGame();
            keys[KeyEvent.VK_ESCAPE] = false;
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

        // Update player bounds untuk collision detection
        player.bounds = new Rectangle((int)player.x, (int)player.y, player.img.getWidth(), player.img.getHeight());
        
        // Cek collision dengan enemy
        checkEnemyCollisions();

        camera.update(player);
        if (player.x >= mapEndX - player.img.getWidth()) {
            System.out.println("Selamat! Mario mencapai ujung peta.");
            System.exit(0);
        }
    }

    private void checkEnemyCollisions() {
        // Buat rectangle untuk bagian bawah player (kaki Mario)
        Rectangle playerFeet = new Rectangle(
            (int)player.x + 10,
            (int)player.y + player.img.getHeight() - 10,
            player.img.getWidth() - 20,
            10
        );
        
        // Buat rectangle untuk seluruh tubuh player kecuali kaki
        Rectangle playerBody = new Rectangle(
            (int)player.x,
            (int)player.y,
            player.img.getWidth(),
            player.img.getHeight() - 10
        );
        
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            Rectangle enemyHead = new Rectangle(
                (int)enemy.x + 5,
                (int)enemy.y,
                enemy.spriteLeft.getWidth() - 10,
                10
            );
            
            Rectangle enemyBody = new Rectangle(
                (int)enemy.x,
                (int)enemy.y + 10,
                enemy.spriteLeft.getWidth(),
                enemy.spriteLeft.getHeight() - 10
            );
            
            // Jika kaki player mengenai kepala enemy dan player sedang jatuh
            if (playerFeet.intersects(enemyHead) && player.velY > 0) {
                // Hapus enemy
                it.remove();
                
                // Berikan efek bounce pada player
                player.velY = -8;
                
                // Tambah score
                score += 100;
                
                // Play stomping sound
                playStompSound("/assets/audio/stomp.wav");
            }
            // Jika tubuh player bersentuhan dengan tubuh enemy, game over
            else if (playerBody.intersects(enemyBody)) {
                System.out.println("Game Over! Mario menabrak musuh.");
                System.exit(0);
            }
        }
        camera.update(player);
    }

    private void restartGame() {
        currentState = GameState.MENU;
        listBricks.clear();
        mapgame = new Map();
        enemies.clear();
        player = null;
        loadResources();
        camera = new Camera();
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
                    
                    // Update semua enemy
                    for (Enemy e : enemies) {
                        e.update(listBricks);
                        e.draw(g);
                    }

                    // Tampilkan score
                    g.translate(camera.getX(), 0);
                    g.setColor(Color.WHITE);
                    g.setFont(marioFont.deriveFont(24f));
                    g.drawString("SCORE: " + score, 20, 30);
                    g.translate(-camera.getX(), 0);
                    
                    g.translate(camera.getX(), camera.getY());
                }
                for (Enemy e : enemies) {
                    e.update(listBricks);
                    player.bounds = new Rectangle((int)player.x, (int)player.y, player.img.getWidth(), player.img.getHeight());
                    if (player.bounds.intersects(e.getBounds())) {
                        // System.exit(0);

                        // ulang game dari awal
                        restartGame();
                        break;
                    }
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
    
    private void playStompSound(String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(path));
            Clip stomp = AudioSystem.getClip();
            stomp.open(audioIn);
            stomp.start();
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
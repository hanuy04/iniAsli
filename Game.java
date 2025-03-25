import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Game implements Runnable {
    private JFrame frame;
    private boolean running = false;
    private BufferedImage superMushroom, oneUpMushroom, fireFlower, coin;
    private BufferedImage ordinaryBrick, surpriseBrick, groundBrick, pipe;
    private BufferedImage goombaLeft, goombaRight, koopaLeft, koopaRight, endFlag;
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

    @Override
    public void run() {
        running = true;
        while (running) {
            // Game loop (akan diimplementasikan nanti)
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
        frame.getContentPane().removeAll();
        frame.repaint();
        System.out.println("Game Started!");
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
    }
    public BufferedImage getSubImage(BufferedImage image, int col, int row, int w, int h){
        if((col == 1 || col == 4) && row == 3){ //koopa
            return image.getSubimage((col-1)*48, 128, w, h);
        }
        return image.getSubimage((col-1)*48, (row-1)*48, w, h);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}

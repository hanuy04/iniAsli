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
        // Tambahkan logika untuk masuk ke game loop nanti
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}

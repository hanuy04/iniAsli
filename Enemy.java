import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Enemy {
    private int x, y;
    private int width = 16, height = 16;
    private int speed = 1;
    private boolean movingRight = false;
    private int tickCounter = 0;
    private int tickRate = 10; // semakin besar nilainya, semakin lambat    
    private BufferedImage spriteLeft;
    private BufferedImage spriteRight;
    private Rectangle bounds;

    public Enemy(int x, int y, BufferedImage spriteLeft, BufferedImage spriteRight) {
        this.x = x;
        this.y = y;
        this.spriteLeft = spriteLeft;
        this.spriteRight = spriteRight;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void update(ArrayList<Bricks> bricks) {
        tickCounter++;
        if (tickCounter < tickRate) return;
        tickCounter = 0;
    
        // Gerakan
        if (movingRight) {
            x += speed;
        } else {
            x -= speed;
        }
        x += movingRight ? speed : -speed;
    
        // Update bounding box
        bounds.setLocation(x, y);
    
        for (Bricks brick : bricks) {
            Rectangle brickBounds = brick.bounds;
            if (bounds.intersects(brickBounds)) {
                // Ganti arah dan perbaiki posisi
                movingRight = !movingRight;
                x += movingRight ? speed : -speed;
                bounds.setLocation(x, y);
                break;
            }
        }
    }

    public void draw(Graphics g) {
        BufferedImage currentSprite = movingRight ? spriteRight : spriteLeft;
        g.drawImage(currentSprite, x, y, null);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}

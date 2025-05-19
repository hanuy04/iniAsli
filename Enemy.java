import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Enemy {
    public int x, y;
    public int width = 16, height = 16;
    public int speed = 1;
    public boolean movingRight = false;
    public int tickCounter = 0;
    public int tickRate = 20; // semakin besar nilainya, semakin lambat    
    public BufferedImage spriteLeft;
    public BufferedImage spriteRight;
    public Rectangle bounds;

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

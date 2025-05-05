import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Map {
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 20;
    private ArrayList<Bricks> bricks = new ArrayList<>();
    private ArrayList<Bricks> groundBricks = new ArrayList<>();
    public void addBrick(Bricks brick) {
        this.bricks.add(brick);
    }

    public void addGroundBrick(Bricks brick) {
        this.groundBricks.add(brick);
    }
    public void drawBricks(Graphics2D g2) {
        for(Bricks brick : bricks){
            if(brick != null){
                BufferedImage style = brick.image;

                if(style != null){
                    g2.drawImage(style, (int)brick.x, (int)brick.y, null);
                }
            }
        }
        for(Bricks brick : groundBricks){
            if(brick != null){
                BufferedImage style = brick.image;

                if(style != null){
                    g2.drawImage(style, (int)brick.x, (int)brick.y, null);
                }
            }
        }
    }
}

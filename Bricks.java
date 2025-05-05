import java.awt.*;
import java.awt.image.BufferedImage;

class Bricks {
    BufferedImage image;
    double x;
    double y;
    private boolean breakable;
    private boolean empty;
    public Bricks(BufferedImage image, double x, double y, boolean breakable, boolean empty) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.breakable = breakable;
        this.empty = empty;
    }
    public Rectangle dapatkanbatas(int status){
        if(status == 1){
            return new Rectangle((int)x+this.image.getWidth()/6, (int)y, 2*this.image.getWidth()/3, this.image.getHeight()/2);
        }else if (status == 2){
            return new Rectangle((int)x+this.image.getWidth()/6, (int)y + this.image.getHeight()/2, 2*this.image.getWidth()/3, this.image.getHeight()/2);
        }else if (status == 3){
            return new Rectangle((int)x + 3*this.image.getWidth()/4, (int)y + this.image.getHeight()/4, this.image.getWidth()/4, this.image.getHeight()/2);
        }else if (status == 4){
            return new Rectangle((int)x, (int)y + this.image.getHeight()/4, this.image.getWidth()/4, this.image.getHeight()/2);
        }
        return null;
    }
}
class GroundBrick extends Bricks{
    public GroundBrick(double x, double y, BufferedImage image){
        super(image, x, y, false, true);
    }
}
class OrdinaryBrick extends Bricks{
    public OrdinaryBrick(double x, double y, BufferedImage image){
        super(image, x, y, false, false);
    }
}
class SurpriseBrick extends Bricks{
    public SurpriseBrick(double x, double y, BufferedImage image){
        super(image, x, y, false, false);
    }
}
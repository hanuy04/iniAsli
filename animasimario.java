import java.awt.image.BufferedImage;

public class animasimario {
    private int index = 0, count = 0;
    private BufferedImage[] leftFrames, rightFrames;
    private BufferedImage currentFrame;

    public animasimario(BufferedImage[] leftFrames, BufferedImage[] rightFrames){
        this.leftFrames = leftFrames;
        this.rightFrames = rightFrames;

        currentFrame = rightFrames[1];
    }

    public BufferedImage animate(int speed, boolean toRight){
        count++;
        BufferedImage[] frames = toRight ? rightFrames : leftFrames;

        if(count > speed){
            nextFrame(frames);
            count = 0;
        }

        return currentFrame;
    }

    private void nextFrame(BufferedImage[] frames) {
        if(index + 3 > frames.length)
            index = 0;

        currentFrame = frames[index+2];
        index++;
    }

    public BufferedImage[] getLeftFrames() {
        return leftFrames;
    }

    public BufferedImage[] getRightFrames() {
        return rightFrames;
    }
}

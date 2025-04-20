import java.awt.image.BufferedImage;

public class animasimario {
    private BufferedImage[] leftFrames;
    private BufferedImage[] rightFrames;
    private int frameIndex = 0;
    private int frameDelay = 5;
    private int frameCounter = 0;

    public animasimario(BufferedImage[] left, BufferedImage[] right) {
        this.leftFrames = left;
        this.rightFrames = right;
    }

    public void update() {
        frameCounter++;
        if (frameCounter >= frameDelay) {
            frameIndex = (frameIndex + 1) % rightFrames.length;
            frameCounter = 0;
        }
    }

    public BufferedImage[] getLeftFrames() {
        return leftFrames;
    }

    public BufferedImage[] getRightFrames() {
        return rightFrames;
    }

    public BufferedImage getCurrentFrame(boolean toRight) {
        return toRight ? rightFrames[frameIndex] : leftFrames[frameIndex];
    }
}

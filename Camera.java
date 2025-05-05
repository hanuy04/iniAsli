public class Camera {
    private float x, y;

    public void update(mario player) {
        x = (float) (player.x - GameSmooth.WIDTH / 2f + player.img.getWidth() / 2f);
        y = (float) (player.y - GameSmooth.HEIGHT / 2f + player.img.getHeight() / 2f);

        float maxX = Map.MAP_WIDTH * 48 - GameSmooth.WIDTH;
        float maxY = Map.MAP_HEIGHT * 48 - GameSmooth.HEIGHT;
        x = clamp(x, 0, maxX);
        y = clamp(y, 0, maxY);
    }

    private float clamp(float val, float min, float max) {
        if (val < min)
            return min;
        if (val > max)
            return max;
        return val;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}

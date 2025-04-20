import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class mario {
    public double x;
    public double y;
    public BufferedImage img;
    public Dimension ukuran;
    public BufferedImage marioForms;
    public boolean isSuper, isFire;
    public animasimario animasimario;
    public boolean hadapkanan= true;
    public double velX, velY;
    public boolean jumping;
    public boolean falling;
    public double gravityAcc = 0.38;
    public mario(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            marioForms = ImageIO.read(getClass().getResource("/assets/mario-forms.png" ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage[] leftFrames = this.getLeftFrames(0);
        BufferedImage[] rightFrames = this.getRightFrames(0);
        this.animasimario = new animasimario(leftFrames, rightFrames);
        this.isSuper = false;
        this.isFire = false;
        this.img = this.ambilspritemariosekarang(hadapkanan,false,false);
        this.falling = true;
        this.velY = 0;
        this.velX = 0;
        this.jumping = false;
    }
    public BufferedImage ambilspritemariosekarang(boolean toRight, boolean movingInX, boolean movingInY){
        if (movingInY) {
            return toRight ? animasimario.getRightFrames()[0] : animasimario.getLeftFrames()[0];
        } else if (movingInX) {
            return animasimario.getCurrentFrame(toRight);
        } else {
            return toRight ? animasimario.getRightFrames()[0] : animasimario.getLeftFrames()[0];
        }
    }
    public BufferedImage[] getLeftFrames(int status) {
        BufferedImage[] leftFrames = new BufferedImage[5];
        int col = 1;
        int width = 52, height = 48;

        if(status == 1) { //super mario
            col = 4;
            width = 48;
            height = 96;
        }
        else if(status == 2){ //fire mario
            col = 7;
            width = 48;
            height = 96;
        }

        for(int i = 0; i < 5; i++){
            leftFrames[i] = marioForms.getSubimage((col-1)*width, (i)*height, width, height);
        }
        return leftFrames;
    }
    public BufferedImage[] getRightFrames(int status) {
        BufferedImage[] rightFrames = new BufferedImage[5];
        int col = 2;
        int width = 52, height = 48;

        if(status == 1) { //super mario
            col = 5;
            width = 48;
            height = 96;
        }
        else if(status == 2){ //fire mario
            col = 8;
            width = 48;
            height = 96;
        }

        for(int i = 0; i < 5; i++){
            rightFrames[i] = marioForms.getSubimage((col-1)*width, (i)*height, width, height);
        }
        return rightFrames;
    }
    public void draw(Graphics g){
        boolean movingInX = (this.velX != 0);
        boolean movingInY = (this.velY != 0);
        this.img = this.ambilspritemariosekarang(hadapkanan, movingInX, movingInY);
        g.drawImage(this.img, (int)x, (int)y, null);
    }
    public void updatelokasi(){
        if(jumping && velY <= 0){
            jumping = false;
            falling = true;
        }
        else if(jumping){
            velY = velY - gravityAcc;
            y = y - velY;
        }

        if(falling){
            System.out.println("yow");
            y = y + velY;
            velY = velY + gravityAcc;
        }
        x = x + velX;
        velX = 0;
    }
    public Rectangle dapatkanbatas(int status){
        if(status == 1){
            //atas
            return new Rectangle((int)x+this.img.getWidth()/6, (int)y, 2*this.img.getWidth()/3, this.img.getHeight()/2);
        }else if (status == 2){
            //bawah
            return new Rectangle((int)x+this.img.getWidth()/6, (int)y + this.img.getHeight()/2, 2*this.img.getWidth()/3, this.img.getHeight()/2);
        }else if (status == 3){
            //kanan
            return new Rectangle((int)x + 3*this.img.getWidth()/4, (int)y + this.img.getHeight()/4, this.img.getWidth()/4, this.img.getHeight()/2);
        }else if (status == 4){
            //kiri
            return new Rectangle((int)x, (int)y + this.img.getHeight()/4, this.img.getWidth()/4, this.img.getHeight()/2);
        }
        return null;
    }
//    public void gerakmario(boolean toRight, ArrayList<Bricks> listbricks){
//        if(toRight){
//            boolean collision = false;
//            for (Bricks brick : listbricks) {
//                Rectangle batasplayer = this.hadapkanan ? this.dapatkanbatas(3) : this.dapatkanbatas(4);
//                Rectangle batasbricks = !this.hadapkanan ? brick.dapatkanbatas(3) : brick.dapatkanbatas(4);
//                if (batasplayer.intersects(batasbricks)){
//                    collision = true;
//                    this.velX = 0;
//                    if(this.hadapkanan){
//                        this.x = brick.x-this.img.getWidth();
//                    }else{
//                        this.x = brick.x+this.img.getWidth();
//                    }
//                    break;
//                }
//            }
//            if(!collision){
//                this.velX = 5;
//            }
//        }else{
//            this.velX = -5;
//        }
////        else if(camera.getX() < getX()){
////            setVelX(-5);
////        }
//
//        this.hadapkanan = toRight;
//    }
    public void gerakmario(boolean keKanan, ArrayList<Bricks> bricks) {
        this.hadapkanan = keKanan;
        double speed = 5;

        if (keKanan) {
            this.x += speed;
        } else {
            this.x -= speed;
        }

        this.animate(true, false); // true = bergerak horizontal, false = tidak vertical
    }
    public void melompat(){
        if(!this.jumping && !this.falling){
            this.jumping = true;
            this.velY = 10;
        }
    }
    public void animate(boolean movingInX, boolean movingInY) {
        // Perbarui frame animasi setiap kali Mario bergerak
        if (movingInX || movingInY) {
            animasimario.update(); // update frame currentIndex
        }

        // Ambil frame berdasarkan arah dan status gerakan
        this.img = ambilspritemariosekarang(hadapkanan, movingInX, movingInY);
    }
}

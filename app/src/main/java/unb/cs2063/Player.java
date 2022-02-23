package unb.cs2063;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

//this class represents a bitmap image used for the player of the game
public class Player {
    // Reduce the number of times using getters and setters, just make class variables public and
    // directly access them to improve performance
    private Bitmap image;
    private int x, y;
    private int angle;

    private int defaultVelocity = 5;
    private double acceleration = 0;
    private int xVelocity = defaultVelocity;
    private int yVelocity = defaultVelocity;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    private final static String TAG = "Player";

    public Player(Bitmap bmp) {
        image = bmp;

        x = (screenWidth/2) - (image.getWidth()/2);
        y = (screenHeight/2) - (image.getHeight()/2);
        angle = 0;
    }

    public void draw(Canvas canvas) {
        //rotating the bitmap by rotating the canvas
        canvas.save();
        //Rotate around the center of the bitmap
        canvas.rotate(angle, x + (image.getWidth()/2), y + (image.getHeight()/2));
        canvas.drawBitmap(image, x, y, null);
        canvas.restore();
    }

    public void update() {
        if((x > screenWidth - image.getWidth()) || (x < 0)){
            //if the player has reached a X bound can still move on y axis
            y += yVelocity;
            //if player is at the right of the screen (highest x value)
            if((xVelocity < -1) && (x > screenWidth - image.getWidth())){
                y += yVelocity;
                x += xVelocity;
            }
            //if player is at the left of screen (lowest x value)
            else if((xVelocity > 1) && (x < 0)){
                y += yVelocity;
                x += xVelocity;
            }
        }
        else if((y > screenHeight - image.getHeight()) || (y < 0)){
            //if the player has reached a Y bound can still move on the x axis
            x += xVelocity;
            //if player is at the bottom of the screen (highest Y value)
            if((yVelocity < -1) && y > (screenHeight - image.getHeight())){
                y += yVelocity;
                x += xVelocity;
            }
            //if player is at the top of the screen (lowest Y value)
            else if((yVelocity > 1) && (y < 0)){
                y += yVelocity;
                x += xVelocity;
            }
        }
        else if(((x > screenWidth - image.getWidth()) || (x < 0)) && ((y > screenHeight - image.getHeight()) || (y < 0))){
            return;
        }
        else{
            x += xVelocity;
            y += yVelocity;
        }

    }

    public void move(double accelX){
        updateVelocity();
        //chose 7 as that is when the phone is at ~70 degrees
        acceleration = -(accelX - 7);
        if(acceleration > 0){
            xVelocity *= acceleration;
            yVelocity += acceleration;
        }
        if(acceleration <= 0){
            xVelocity = 0;
            yVelocity = 0;
        }
        update();
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getBitmapX(){
        return image.getWidth();
    }

    public int getBitmapY(){
        return image.getHeight();
    }

    public void flipxVelocity(){
        xVelocity = xVelocity*-1;
    }

    public void flipyVelocity(){
        yVelocity = yVelocity*-1;
    }

    public int getCenterX(){
        return x + (image.getWidth()/2);
    }

    public int getCenterY(){
        return y + (image.getHeight()/2);
    }

    public boolean collision(int objX, int objY){
        if(objX < x + image.getWidth() && objX > x) {
            if (objY < y + image.getHeight() && objY > y) {
                return true;
            }
        }
        return false;
    }

    public void setRotation(int angle){
        this.angle = angle;
    }

    public void updateVelocity(){
        xVelocity = defaultVelocity;
        yVelocity = defaultVelocity;
        double radians = (Math.PI/180)*(angle - 90);
        xVelocity = (int)(xVelocity * Math.cos(radians));
        yVelocity = (int)(yVelocity * Math.sin(radians));
        Log.d("Player move()", "\n\n\txVelocity: " + xVelocity + "\n\tyVelocity: " + yVelocity);
    }
}

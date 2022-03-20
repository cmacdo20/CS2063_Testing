package unb.cs2063;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Rock {
    // Reduce the number of times using getters and setters, just make class variables public and
    // directly access them to improve performance
    private Bitmap image;
    private int x, y;
    private int rotation;

    public boolean destroyed = false;

    private int xVelocity = 7;
    private int yVelocity = 3;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int MAXSPEED;

    private final static String TAG = "Player";

    public Rock(Bitmap bmp, int x, int y) {
        image = bmp;
        this.x = x;
        this.y = y;

        rotation = 0;
    }

    public void draw(Canvas canvas) {
        //rotating the bitmap by rotating the canvas
        canvas.save();
        //Rotate around the center of the bitmap
        canvas.rotate(rotation, x + (image.getWidth()/2), y + (image.getHeight()/2));
        //should be using translate
        //canvas.translate();
        canvas.drawBitmap(image, x, y, null);
        canvas.restore();
    }

    public void update() {
        //Log.i(TAG, "In update()");
    }

    public void Move(){
        x += xVelocity;
        y += yVelocity;
        //Moves within 3 screen widths (one extra on each side)
        if((x > 2*screenWidth - image.getWidth()) || (x < -screenWidth)){
            xVelocity = xVelocity*-1;
        }
        //Moves within 1.5 screen heights
        if((y > screenHeight - image.getHeight()) || (y < 0)){
            yVelocity = yVelocity*-1;
        }
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

    public void setxVelocity(int x){
        xVelocity = x;
    }

    public void setyVelocity(int y){
        yVelocity = y;
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

    public void setCoords(int x, int y){
        this.x = x;
        this.y = y;
    }

    public boolean collision(int objX, int objY){
        if(objX < x + image.getWidth() && objX > x) {
            if (objY < y + image.getHeight() && objY > y) {
                return true;
            }
        }
        return false;
    }

    public void setRotation(int rotation){
        this.rotation = rotation;
    }

    public Bitmap getImage(){
        return image;
    }

}

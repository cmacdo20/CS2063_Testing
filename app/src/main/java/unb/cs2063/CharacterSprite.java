package unb.cs2063;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

//this class represents a bitmap image on the screen.
public class CharacterSprite {
    // Reduce the number of times using getters and setters, just make class variables public and
    // directly access them to improve performance
    private Bitmap image;
    private int x, y;
    private int rotation;

    private int xVelocity = 10;
    private int yVelocity = 5;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    private final static String TAG = "CharacterSprite";

    public CharacterSprite(Bitmap bmp) {
        image = bmp;
        //x = 100;
        //y = 100;

        x = (screenWidth/2) - (image.getWidth()/2);
        y = (screenHeight/2) - (image.getHeight()/2);
        rotation = 0;
    }

    public void draw(Canvas canvas) {
        //rotating the bitmap by rotating the canvas
        canvas.save();
        //Rotate around the center of the bitmap
        canvas.rotate(rotation, x + (image.getWidth()/2), y + (image.getHeight()/2));
        canvas.drawBitmap(image, x, y, null);
        canvas.restore();
    }

    public void update() {
        //Log.i(TAG, "In update()");
    }

    public void rotateTranslate(){
        if(x < 0 && y < 0){
            x = screenWidth / 2;
            y = screenHeight / 2;
        }
        else{
            x += xVelocity;
            y += yVelocity;
            if((x > screenWidth - image.getWidth()) || (x < 0)){
                xVelocity = xVelocity*-1;
            }
            if((y > screenHeight - image.getHeight()) || (y < 0)){
                yVelocity = yVelocity*-1;
            }
        }

        // logic to adjust the rotation
        if(rotation < 360){
            rotation++;
        }
        else{
            rotation = 0;
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

    public void setRotation(int rotation){
        this.rotation = rotation;
    }
}

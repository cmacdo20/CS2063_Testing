package unb.cs2063;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

public class Shot {

    private Bitmap image;
    private int x, y;
    private int angle;

    public boolean fired = false;
    public boolean impact = false;

    private int xVelocity, yVelocity;

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    public Shot(Bitmap bmp, int x, int y){
        image = bmp;
        this.x = x;
        this.y = y;
        xVelocity = 20;
        yVelocity = 20;
    }

    public void setParameters(int x, int y, int angle){
        this.x = x - image.getWidth()/2;
        this.y = y - image.getHeight()/2;
        this.angle = angle;
    }

    public void update(){
        if(fired){
            x += xVelocity;
            y += yVelocity;

            if(x < -image.getWidth() || x > screenWidth || y < -image.getHeight() || y > screenHeight){
                fired = false;
                x = -100;
                y = -100;
                xVelocity = 20;
                yVelocity = 20;
            }
            else if(impact){
                fired = false;
                x = -100;
                y = -100;
                xVelocity = 20;
                yVelocity = 20;
                impact = false;
            }
        }
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);
    }

    public void shotFired(){
        fired = true;
    }

    public void updateVelocity(){
        double radians = (Math.PI/180)*(angle - 90);
        xVelocity = (int)(xVelocity * Math.cos(radians));
        yVelocity = (int)(yVelocity * Math.sin(radians));
    }

    public void collision(Bitmap object, int x, int y){
        if(this.x < x + object.getWidth() && this.x > x) {
            if (this.y < y + object.getHeight() && this.y > y) {
                impact = true;
            }
        }
        else{
            impact = false;
        }
    }
}

package unb.cs2063;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Sprite {
    public Vector position;
    public Vector velocity;
    public double rotation;
    public Collision imageBounds;
    public Bitmap image;

    public boolean wrapOn = false;
    public boolean edgeOn = false;


    public boolean offScreen = false;
    public int points = 0;

    private double screenWidth;
    private double screenHeight;

    public Sprite(){
        position = new Vector();
        velocity = new Vector();
        rotation = 0;
        imageBounds = new Collision();
    }

    public Sprite(Bitmap bmp){
        this();
        this.setImage(bmp);
    }

    public void setImage(Bitmap bmp){
        image = bmp;
        imageBounds.setSize(image.getWidth(), image.getHeight());
    }

    public Collision getImageBounds(){
        imageBounds.setPosition(position.x, position.y);
        return imageBounds;
    }

    public boolean collision(Sprite obj){
        return this.getImageBounds().collision(obj.getImageBounds());
    }

    public void update(){
        spriteOffScreen();
        if(wrapOn)
            this.wrap();
        if(edgeOn)
            this.screenEdge();
        if(!wrapOn && !edgeOn)
            position.add(velocity.x, velocity.y);
        spriteOffScreen();
    }

    public void draw(Canvas canvas){
        canvas.save();
        canvas.rotate((float)rotation,
                (float)(position.x + (image.getWidth()/2)),
                (float)(position.y + (image.getWidth()/2)));
        canvas.drawBitmap(image, (int)position.x, (int)position.y, null);
        canvas.restore();
    }

    public void setScreenSize(double screenWidth, double screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    // FIXME: Corners break
    public void screenEdge(){
        // if the player has reached a X bound can still move on y axis
        if((position.x > screenWidth - image.getWidth()) || (position.x < 0)){
            position.add(0, velocity.y);

            // if player is at the right of the screen (highest x value)
            if((velocity.x < -1) && (position.x > screenWidth - image.getWidth()))
                position.add(velocity.x, velocity.y);

            // if player is at the left of screen (lowest x value)
            else if((velocity.x > 1) && (position.x < 0))
                position.add(velocity.x, velocity.y);
        }
        // if the player has reached a Y bound can still move on the x axis
        else if((position.y > screenHeight - image.getHeight()) || (position.y < 0)){
            position.add(velocity.x, 0);

            // if player is at the bottom of the screen (highest Y value)
            if((velocity.y < -1) && position.y > (screenHeight - image.getHeight()))
                position.add(velocity.x, velocity.y);

            // if player is at the top of the screen (lowest Y value)
            else if((velocity.y > 1) && (position.y < 0))
                position.add(velocity.x, velocity.y);
        }
        else
            position.add(velocity.x, velocity.y);
    }

    public void wrap(){
        // Move Object
        position.add(velocity.x, velocity.y);

        // Check bounds
        if(position.x + image.getWidth() < 0)
            position.x = screenWidth;

        if(position.x > screenWidth)
            position.x = -image.getWidth();

        if(position.y + image.getHeight() < 0)
            position.y =screenHeight;

        if(position.y > screenHeight)
            position.y = -image.getHeight();
    }

    public void spriteOffScreen(){
        if((position.x + image.getWidth() < 0) ||
                (position.x > screenWidth) ||
                (position.y + image.getHeight() < 0) ||
                (position.y > screenHeight))
            offScreen = true;
        else
            offScreen = false;
    }

    public void addPoints(int points){
        this.points += points;
    }
}

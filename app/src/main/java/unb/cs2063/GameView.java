package unb.cs2063;

import static java.lang.Math.abs;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//this class is where the canvas(background which everything is drawn on) is updated and things are
// added to it
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Player player;
    private Rock apple;
    private Shot shot;
    private boolean appleDead = false;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.arrow));
        apple = new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.myapple),100,100);
        shot = new Shot(BitmapFactory.decodeResource(getResources(),R.drawable.myapple),100,100);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    //Use to update events on the screen
    public void update() {
        //player.update();
        //apple.update();
        shot.update();
        apple.Move();

        int appleX = apple.getCenterX();
        int appleY = apple.getCenterY();

        int arrowX = player.getCenterX();
        int arrowY = player.getCenterY();

        if(player.collision(appleX, appleY)){
            if(apple.collision(arrowX, arrowY)) {
                apple.flipxVelocity();
                apple.flipyVelocity();
            }
        }

        //Sets the arrow to follow the apple
        int rotation = calcAngle(arrowX, arrowY, appleX, appleY);
        //player.setRotation(rotation);
    }

    //This method is called when the screen is touched
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //x and y values for touch input
            int clickX = (int)event.getX();
            int clickY = (int)event.getY();
            //deleteApple();

            //angle to place pressed on screen
            int angle = calcAngle(player.getCenterX(), player.getCenterY(), clickX, clickY);
            shot.setParameters(player.getCenterX(), player.getCenterY(), angle);
            shot.shotFired();
            shot.updateVelocity();
            player.setRotation(angle);
            Log.d("Touch Event", angle + " Degrees");
            /*
            //x and y for bitmap (from top corner)
            int arrowX = player.getCenterX();
            int arrowY = player.getCenterY();

            int rotation = calcAngle(arrowX, arrowY, clickX, clickY);
            player.setRotation(rotation);
            */

            //apple.setX(clickX);
            //apple.setY(clickY);

            return true;
        }
        return false;
    }

    //The canvas is redrawn each time. To delete things just dont draw them and move them off screen
    //Attempt to reuse old deleted objects
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        player.draw(canvas);
        if(shot.fired){
            shot.draw(canvas);
        }
        if (appleDead){
            return;
        }
        apple.draw(canvas);
    }

    public void deleteApple(){
        appleDead = true;
        //Move the bitmap off screen
        apple.setX(-100);
        apple.setY(-100);
        apple.setxVelocity(0);
        apple.setyVelocity(0);
    }

    //returns the angle in degrees (0-359) assuming a 90 degree offset
    //x1 and y1 is the origin point
    public static int calcAngle(int x1, int y1, int x2, int y2){
        double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        angle = angle + 90;
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (int)angle;
    }
}

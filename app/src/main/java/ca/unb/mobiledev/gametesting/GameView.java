package ca.unb.mobiledev.gametesting;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//this class is where the canvas(background which everything is drawn on) is updated and things are
// added to it
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private CharacterSprite characterSprite;
    private CharacterSprite apple;

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
        characterSprite = new CharacterSprite(BitmapFactory.decodeResource(getResources(),R.drawable.arrow));
        apple = new CharacterSprite(BitmapFactory.decodeResource(getResources(),R.drawable.myapple));
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
        //characterSprite.update();
        //apple.update();
        apple.rotateTranslate();
        //characterSprite.rotateTranslate();

        int appleX = apple.getCenterX();
        int appleY = apple.getCenterY();

        int arrowX = characterSprite.getCenterX();
        int arrowY = characterSprite.getCenterY();

        int rotation = calcAngle(arrowX, arrowY, appleX, appleY);

        if(characterSprite.collision(appleX, appleY)){
            if(apple.collision(arrowX, arrowY)) {
                apple.flipxVelocity();
                apple.flipyVelocity();
            }
        }

        Log.d("Touch Event", "Rotation: " + rotation);

        characterSprite.setRotation(rotation);
    }

    //This method is called when the screen is touched
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //x and y values for touch input
            int clickX = (int)event.getX();
            int clickY = (int)event.getY();

            /*
            //x and y for bitmap (from top corner)
            int arrowX = characterSprite.getCenterX();
            int arrowY = characterSprite.getCenterY();

            int rotation = calcAngle(arrowX, arrowY, clickX, clickY);
            characterSprite.setRotation(rotation);
            */

            apple.setX(clickX);
            apple.setY(clickY);

            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        characterSprite.draw(canvas);
        apple.draw(canvas);
    }

    //returns the angle in degrees (0-359) assuming a 90 degree offset
    public static int calcAngle(int x1, int y1, int x2, int y2){
        double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        angle = angle + 90;
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (int)angle;
    }
}

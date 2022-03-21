package unb.cs2063;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

// FIXME: Collision is only detected on the first rock even if its not on screen.

// this class is where the canvas(background which everything is drawn on) is updated and things are
// added to it
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Player player;
    private Rock rock1;
    private Rock rock2;
    private ArrayList<Shot> shotList;

    private Paint textPaint;

    //Accelerometer related variables
    private SensorManager sensorManager;
    private Sensor sensor;

    //Sensor event listener will be called when sensor event is noticed
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //Y for spinning (rotating device on side)
            //X for forward or backwards
            //Will need to make a set default position thing eventually
            //Filter out gravity and see what happens
            accelX = sensorEvent.values[0];
            accelY = sensorEvent.values[1];
            //Log.d("Sensor Changed", "Vals:\tX: " + sensorEvent.values[0] +
            //        "\n\tY: " + sensorEvent.values[1] + "\n\tZ: " + sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private float accelX = 0;
    private float accelY = 0;

    public GameView(Context context) {
        super(context);

        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);
        setFocusable(true);

        //Setting up the sensor and sensor manager and opening a listener (will need to close at some point)
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        //Setting up the text paint
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        shotList = new ArrayList<Shot>();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.arrow));
        rock1 = new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.rock),100,100);
        rock2 = new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.myapple),100,100);
        rock2.destroyed = true;
        //shot = new Shot(BitmapFactory.decodeResource(getResources(),R.drawable.shot),100,100);
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
        if(!rock1.destroyed){
            rock1.Move();
        }
        else if(!rock2.destroyed){
            rock2.Move();
        }

        //Collision between object and player (move into player class)
        int rock1X = rock1.getCenterX();
        int rock1Y = rock1.getCenterY();

        int rock2X = rock1.getCenterX();
        int rock2Y = rock1.getCenterY();

        int arrowX = player.getCenterX();
        int arrowY = player.getCenterY();

        //Player collision with rocks, add for rock 2
        if(player.collision(rock1X, rock1Y)){
            if(rock1.collision(arrowX, arrowY)) {
                rock1.flipxVelocity();
                rock1.flipyVelocity();
            }
        }



        //Collision between shot and object (checks from center of shot to anywhere on object)
        for(int i = 0; i < shotList.size(); i++) {
            Shot shot = shotList.get(i);
            shot.update();
            if (shot.fired) {
                if (!rock1.destroyed) {
                    shot.collision(rock1.getImage(), rock1X, rock1Y);
                } else if (!rock2.destroyed) {
                    shot.collision(rock2.getImage(), rock2X, rock2Y);
                }
                if (shot.impact) {
                    shotList.remove(i);
                    player.addPoints(10);
                    if (!rock1.destroyed) {
                        rock1.destroyed = true;
                        rock2.destroyed = false;
                        //will need to make this random
                        rock2.setCoords(0, 0);
                    } else if (!rock2.destroyed) {
                        rock2.destroyed = true;
                        rock1.destroyed = false;
                        rock1.setCoords(0, 0);
                    }
                    Log.d("GameView", "Player score: " + player.getPoints());
                }
            }
            if(!shot.fired)
                shotList.remove(i);
        }

        //Player movement calls
        player.setRotation((int)accelY*25);
        player.move((accelX));
    }

    //This method is called when the screen is touched
    //TODO: create more shots when screen is spammed (make a limit)
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //x and y values for touch input
            int clickX = (int)event.getX();
            int clickY = (int)event.getY();
            //deleteApple();

            Shot shot = new Shot(BitmapFactory.decodeResource(getResources(),R.drawable.shot),100,100);
            //angle to place pressed on screen
            int angle = calcAngle(player.getCenterX(), player.getCenterY(), clickX, clickY);
            //shot.setParameters(player.getCenterX(), player.getCenterY(), angle);
            shot.setParameters(player.getCenterX(), player.getCenterY(), (int)accelY*25);
            shot.shotFired();
            shot.updateVelocity();
            shotList.add(shot);
            Log.d("Touch Event", angle + " Degrees");

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
        for(Shot shot : shotList)
            shot.draw(canvas);
        if(!rock1.destroyed && rock2.destroyed){
            rock1.draw(canvas);
        }
        else if(!rock2.destroyed && rock1.destroyed){
            rock2.draw(canvas);
        }
        canvas.drawText("Score: " + player.points, 20, 40, textPaint);
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

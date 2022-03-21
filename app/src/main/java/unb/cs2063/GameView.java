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
    private ArrayList<Shot> shotList;
    private ArrayList<Rock> rockList;
    private int MAXROCKS = 5;

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

    // PLayer movement from accelerometer
    private float accelX = 0;
    private float accelY = 0;

    public GameView(Context context) {
        super(context);
        // Setting up thread and surface
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);

        // Setting up the sensor and sensor manager and opening a listener (will need to close at some point)
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Setting up the text paint
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        // Create arraylists for shots and rocks
        shotList = new ArrayList<Shot>();
        rockList = new ArrayList<Rock>();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //Called when creating the surface
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
        // Create player
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.arrow));
        // Create initial rock
        Rock rock = new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.rock),100,100);
        rockList.add(rock);
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

        // Updates rocks
        for(Rock rock : rockList) {
            rock.Move();

            // Check for collision between player and rock, if collision flip rock velocity
            if (player.collision(rock.getCenterX(), rock.getCenterY())) {
                if (rock.collision(player.getCenterX(), player.getCenterY())) {
                    rock.flipxVelocity();
                    rock.flipyVelocity();
                }
            }
        }

        // Collision between shot and rock (checks from center of shot to anywhere on object)
        // if rock is hit spawns two new ones as long as max count is not reached
        for(int i = 0; i < shotList.size(); i++) {
            Shot shot = shotList.get(i);
            shot.update();
            for(int j = 0; j < rockList.size(); j++) {
                Rock rock = rockList.get(j);
                if (shot.fired) {
                    if (!rock.destroyed)
                        shot.collision(rock.getImage(), rock.getCenterX(), rock.getCenterY());
                    if (shot.impact) {
                        shotList.remove(i);
                        rockList.remove(j);
                        player.addPoints((int)(Math.random()*25));

                        rockList.add(new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.rock),
                                (int)(Math.random()*300),(int)(Math.random()*300)));
                        if(rockList.size() <= MAXROCKS)
                            rockList.add(new Rock(BitmapFactory.decodeResource(getResources(),R.drawable.rock),
                                    (int)Math.random()*300, (int)Math.random()*300));
                    }
                }
                if (!shot.fired)
                    shotList.remove(i);
            }
        }

        //Player movement calls
        player.setRotation((int)accelY*25);
        player.move((accelX));
    }

    //This method is called when the screen is touched
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
        // Draw background
        canvas.drawColor(Color.WHITE);
        // Draw player
        player.draw(canvas);
        // Draw all shots
        for(Shot shot : shotList)
            shot.draw(canvas);
        // Drawn all rocks
        for(Rock rock : rockList)
            rock.draw(canvas);
        // Draw score
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

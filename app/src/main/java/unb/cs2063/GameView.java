package unb.cs2063;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
        shot = new Shot(BitmapFactory.decodeResource(getResources(),R.drawable.shot),100,100);
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
        apple.Move();

        //Collision between object and player (move into player class)
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

        //Collision between shot and object (checks from center of shot to anywhere on object)
        shot.update();
        if(shot.fired){
            shot.collision(apple.getImage(), appleX, appleY);
            if(shot.impact){
                player.addPoints(10);
                Log.d("GameView", "Player score: " + player.getPoints());
            }
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

            //angle to place pressed on screen
            int angle = calcAngle(player.getCenterX(), player.getCenterY(), clickX, clickY);
            //shot.setParameters(player.getCenterX(), player.getCenterY(), angle);
            shot.setParameters(player.getCenterX(), player.getCenterY(), (int)accelY*25);
            shot.shotFired();
            shot.updateVelocity();
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

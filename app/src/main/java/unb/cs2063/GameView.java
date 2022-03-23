package unb.cs2063;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
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

// this class is where the canvas(background which everything is drawn on) is updated and things are
// added to it
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;

    // Sprites
    private Sprite player;
    private ArrayList<Sprite> shotList;
    private ArrayList<Sprite> rockList;
    private int MAXROCKS = 5;

    // Screen related variables
    private Paint textPaint;
    private double screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private double screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    // Accelerometer related variables
    private SensorManager sensorManager;
    private Sensor sensor;

    // PLayer movement from accelerometer
    private float accelX = 0;
    private float accelY = 0;

    // Sensor event listener will be called when sensor event is noticed
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
            //        "\tY: " + sensorEvent.values[1] + "\n\tZ: " + sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

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
        shotList = new ArrayList<Sprite>();
        rockList = new ArrayList<Sprite>();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    //Called when creating the surface
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

        // Create player and set to center of screen
        player = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.rocket));
        player.setScreenSize(screenWidth, screenHeight);
        player.position.set((screenWidth/2) - (player.image.getWidth()/2),
                (screenHeight/2) - (player.image.getHeight()/2));
        player.wrapOn = true;

        // Create initial rock
        Sprite rock = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.rock));
        rock.setScreenSize(screenWidth, screenHeight);
        rock.position.set(100,100);
        rock.wrapOn = true;
        rock.velocity.setLength(Math.random()*5);
        rock.velocity.setAngle(Math.random()*360);
        rock.setImage(Bitmap.createScaledBitmap(rock.image, 250, 250, false));
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

        // Player movement calls
        player.rotation = (int)accelY*25;
        // Adjust movement based on tilt
        if(accelX < 8) {
            player.velocity.setLength(3);
            player.velocity.setAngle(player.rotation);
        }
        else{
            player.velocity.setLength(0);
        }
        player.update();

        // Shots
        for(int i = 0; i < shotList.size(); i++) {
            Sprite shot = shotList.get(i);
            shot.update();

            if(shot.offScreen){
                shotList.remove(i);
            }
        }

        // Checking for collision between rock and shot
        for(int i = 0; i < shotList.size(); i++){
            Sprite shot = shotList.get(i);
            for(int j = 0; j < rockList.size(); j++){
                Sprite rock = rockList.get(j);
                if(shot.collision(rock)){
                    shotList.remove(i);
                    player.addPoints((int)(Math.random()*25));
                    rockList.remove(j);

                    // Add two new rocks
                    rock = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.rock));
                    rock.setScreenSize(screenWidth, screenHeight);
                    rock.position.set(100,100);
                    rock.wrapOn = true;
                    rock.velocity.setLength(Math.random()*5);
                    rock.velocity.setAngle(Math.random()*360);
                    rockList.add(rock);

                    rock = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.rock));
                    rock.setScreenSize(screenWidth, screenHeight);
                    rock.position.set(100,100);
                    rock.wrapOn = true;
                    rock.velocity.setLength(Math.random()*5);
                    rock.velocity.setAngle(Math.random()*360);
                    rockList.add(rock);
                }
            }
        }

        // Checking for collision between player and rock
        for(int j = 0; j < rockList.size(); j++){
            Sprite rock = rockList.get(j);
            if(player.collision(rock)){
                rockList.remove(j);
                player.addPoints(-100);

                // if all rocks get removed add a new one
                if(rockList.size() == 0){
                    rock = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.rock));
                    rock.setScreenSize(screenWidth, screenHeight);
                    rock.position.set(100,100);
                    rock.wrapOn = true;
                    rock.velocity.setLength(Math.random()*5);
                    rock.velocity.setAngle(Math.random()*360);
                    //Creates scalled bitmap
                    rock.setImage(Bitmap.createScaledBitmap(rock.image, 250, 250, false));
                    rockList.add(rock);
                }
            }

        }

        // Rocks
        for(Sprite rock : rockList)
            rock.update();
    }

    //This method is called when the screen is touched
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){

            Sprite shot = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.shot));
            shot.setScreenSize(screenWidth, screenHeight);
            shot.position.set(player.position.x + (player.image.getWidth()/2) - (shot.image.getWidth()/2),
                    player.position.y + (player.image.getHeight()/2) - (shot.image.getHeight()/2));
            shot.velocity.setLength(10);
            shot.velocity.setAngle(player.rotation);
            shotList.add(shot);

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
        // Draw all shots
        for(Sprite shot : shotList)
            shot.draw(canvas);
        // Draw player
        player.draw(canvas);
        // Draw all rocks
        for(Sprite rock : rockList)
            rock.draw(canvas);
        // Draw score
        canvas.drawText("Score: " + player.points, 20, 40, textPaint);
    }
}

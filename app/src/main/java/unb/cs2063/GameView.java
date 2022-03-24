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
    private int maxRocks = 1;

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
        player.edgeOn = true;
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

        // Player rotation based on phone Y tilt
        player.rotation = (int)accelY*25;
        player.velocity.setAngle(player.rotation);

        // Adjust movement based on tilt
        if(accelX < 8) {
            if(accelX < 8 && accelX > 6)
                player.velocity.setLength(3);
            else if(accelX < 6 && accelX > 4)
                player.velocity.setLength(4);
            else if(accelX < 4)
                player.velocity.setLength(5);
        }
        else{
            player.velocity.setLength(0);
        }
        player.update();

        // update shots
        for(int i = 0; i < shotList.size(); i++) {
            Sprite shot = shotList.get(i);
            shot.update();

            if(shot.offScreen){
                shotList.remove(i);
            }
        }

        double xSpawn, ySpawn;

        // Checking for collision between rock and shot
        for(int i = 0; i < shotList.size(); i++){
            Sprite shot = shotList.get(i);
            for(int j = 0; j < rockList.size(); j++){
                Sprite rock = rockList.get(j);
                if(shot.collision(rock)){
                    // Get position of collided rock to spawn children
                    xSpawn = rock.position.x;
                    ySpawn = rock.position.x;
                    shotList.remove(i);
                    // add points to player based on rock level destroyed
                    player.addPoints((int)(10*rock.level));

                    // check the rock level and spawn the appropriate child rock(s)
                    if(rock.level == 1) {
                        while(Math.random() > 0.25) {
                            this.createRock(xSpawn, ySpawn, 5,
                                    150, 150, 2);
                        }
                    }
                    else if(rock.level == 2){
                        while(Math.random() > 0.3) {
                            this.createRock(xSpawn, ySpawn, 6,
                                    100, 100, 3);
                        }
                    }
                    else if(rock.level == 3){
                        while (Math.random() > 0.35) {
                            this.createRock(xSpawn, ySpawn, 7,
                                    75, 75, 4);
                        }
                    }
                    rockList.remove(j);
                }
            }
        }

        // if all rocks get removed add a new one
        if(rockList.isEmpty()){
            for(int i = 0; i < maxRocks; i++)
                this.createRock(this.generateSpawnX(), this.generateSpawnY(), 5, 200, 200,1);
        }

        // Levels, maximum of 5. Each level increases the maximum amount of level 1 rocks
        if(player.points > 500)
            maxRocks = 2;
        if(player.points > 1000)
            maxRocks = 3;
        if(player.points > 2500)
            maxRocks = 4;
        if(player.points > 5000)
            maxRocks = 5;

        // Checking for collision between player and rock
        for(int j = 0; j < rockList.size(); j++){
            Sprite rock = rockList.get(j);
            if(player.collision(rock)){
                rockList.remove(j);
                //player.addPoints(-100);
                //this.playerKilled();

                // if all rocks get removed add a new one
                if(rockList.size() == 0){
                    this.createRock(this.generateSpawnX(), this.generateSpawnY(), 5, 200, 200,1);
                }
            }
        }

        // Upadte all rocks
        for(Sprite rock : rockList)
            rock.update();
    }

    //This method is called when the screen is touched
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // Everytime the screen is touched a shot is created
            // Shots exist until they are completely off the screen.
            Sprite shot = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.shot));
            shot.setScreenSize(screenWidth, screenHeight);
            shot.position.set(player.position.x + (player.image.getWidth()/2) - (shot.image.getWidth()/2),
                    player.position.y + (player.image.getHeight()/2) - (shot.image.getHeight()/2));
            shot.velocity.setLength(20);
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
        // Draw score and level
        canvas.drawText("Score: " + player.points, 20, 40, textPaint);
        canvas.drawText("Level: " + maxRocks, 20, 80, textPaint);
    }

    // Creates a rock and adds it to the rockList
    private void createRock(double posX, double posY, double velocity, int scaleWidth, int scaleHeight, int level){
        Sprite rock = new Sprite(BitmapFactory.decodeResource(getResources(),R.drawable.rock));
        rock.setScreenSize(screenWidth, screenHeight);
        rock.position.set(posX, posY);
        rock.wrapOn = true;
        rock.velocity.setLength(Math.random()*velocity);
        rock.velocity.setAngle(Math.random()*360);
        rock.setImage(Bitmap.createScaledBitmap(rock.image, scaleWidth, scaleHeight, false));
        rock.setLevel(level);
        rockList.add(rock);
    }

    // Randomly selects the right or left hand side of the screen
    private double generateSpawnX(){
        boolean side = Math.random() < 0.5;
        if(side)
            return 100;
        else
            return screenWidth-100;
    }

    // Randomly selects the top or bottom of the screen
    private double generateSpawnY(){
        boolean side = Math.random() < 0.5;
        if(side)
            return 100;
        else
            return screenHeight-100;
    }

    // Used to launch the game over activity and end the thread
    private void playerKilled(){

    }
}

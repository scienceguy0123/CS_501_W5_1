package com.example.w5_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    private ArrayList<Drawable> drawables;  //keeping track of our drawables
    private int currDrawableIndex;  //keeping track of which drawable is currently displayed.
    private static final String DRAWABLE_PREFIX = "num_";
    //Boiler Plate Stuff.
    private ImageView imgView;

    GestureDetector GD;         //consumes gesture events.

    Animation rotateanim;

    private float lastX, lastY, lastZ;  //old coordinate positions from accelerometer, needed to calculate delta.
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.imageView);
        GD = new GestureDetector(this, this);
        GD.setOnDoubleTapListener(this);
        currDrawableIndex = 0;
        getDrawables();
        imgView.setImageDrawable(null);
        changePicture();

        rotateanim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        rotateanim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                NavRight();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        acceleration = 0.00f;                                         //Initializing Acceleration data.
        currentAcceleration = SensorManager.GRAVITY_EARTH;            //We live on Earth.
        lastAcceleration = SensorManager.GRAVITY_EARTH;               //Ctrl-Click to see where else we could use our phone.


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        enableAccelerometerListening();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStop() {
        disableAccelerometerListening();
        super.onStop();
    }

    // enable listening for accelerometer events
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableAccelerometerListening() {
        // The Activity has a SensorManager Reference.
        // This is how we get the reference to the device's SensorManager.
        SensorManager sensorManager =
                (SensorManager) this.getSystemService(
                        Context.SENSOR_SERVICE);    //The last parm specifies the type of Sensor we want to monitor


        //Now that we have a Sensor Handle, let's start "listening" for movement (accelerometer).
        //3 parms, The Listener, Sensor Type (accelerometer), and Sampling Frequency.
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);   //don't set this too high, otw you will kill user's battery.
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disableAccelerometerListening() {

//Disabling Sensor Event Listener is two step process.
        //1. Retrieve SensorManager Reference from the activity.
        //2. call unregisterListener to stop listening for sensor events
        //THis will prevent interruptions of other Apps and save battery.

        // get the SensorManager
        SensorManager sensorManager =
                (SensorManager) this.getSystemService(
                        Context.SENSOR_SERVICE);

        // stop listening for accelerometer events
        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    float significant_shake = 900;
    long lastUpdate = 0;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // get x, y, and z values for the SensorEvent
            //each time the event fires, we have access to three dimensions.
            //compares these values to previous values to determine how "fast"
            // the device was shaken.
            //Ref: http://developer.android.com/reference/android/hardware/SensorEvent.html
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 150) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];   //obtaining the latest sensor data.
                float y = event.values[1];   //sort of ugly, but this is how data is captured.
                float z = event.values[2];

                // save previous acceleration value
                lastAcceleration = currentAcceleration;

                // calculate the current acceleration
                currentAcceleration = x*x + y*y + z*z;
                ;   //This is a simplified calculation, to be real we would need time and a square root.

                // calculate the change in acceleration        //Also simplified, but good enough to determine random shaking.
                acceleration = currentAcceleration *  (currentAcceleration - lastAcceleration);

                // if the acceleration is above a certain threshold
                if (currentAcceleration > significant_shake) {

                    NavRight();


                }
                else {
                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.GD.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        imgView.startAnimation(rotateanim);


        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }

    double scroll_sum = 0;

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        scroll_sum = 0 ;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        scroll_sum += distanceX;

        if(scroll_sum > 500){
            NavRight();
            scroll_sum = 0;
        }
        else if(scroll_sum < -500){
            NavLeft();
            scroll_sum = 0;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    float onFling_Xpara = 2700;
    float onFling_Ypara = 2400;
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
        if ((velocityY >= -1 * onFling_Ypara && velocityY < onFling_Ypara) && velocityX < -1 * onFling_Xpara){

            NavRight();
            NavRight();
            NavRight();
            return true;
        }

        if((velocityY >= -1 * onFling_Ypara && velocityY < onFling_Ypara) && velocityX > onFling_Xpara){
            NavLeft();
            NavLeft();
            NavLeft();
            return true;
        }

        return true;

    }

    public void NavRight() {
        if (currDrawableIndex == drawables.size() - 1)
            currDrawableIndex = 0;
        else
            currDrawableIndex++;
        changePicture();
    }

    public void NavLeft() {
        if (currDrawableIndex == 0)
            currDrawableIndex = drawables.size() - 1;
        else
            currDrawableIndex--;
        changePicture();
    }

    public void getDrawables() {
        Field[] drawablesFields = com.example.w5_1.R.drawable.class.getFields();
        drawables = new ArrayList<>();

        String fieldName;
        for (Field field : drawablesFields) {
            try {
                fieldName = field.getName();
                Log.i("LOG_TAG", "com.your.project.R.drawable." + fieldName);
                if (fieldName.startsWith(DRAWABLE_PREFIX))
                    drawables.add(getResources().getDrawable(field.getInt(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Routine to change the picture in the image view dynamically.
    public void changePicture() {
        imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgView.setImageDrawable(drawables.get(currDrawableIndex));  //note, this is the preferred way of changing images, don't worry about parent viewgroup size changes.
    }
}
package com.example.gkielian.android_02_sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class HelloSensor extends Activity implements SensorEventListener{

    //make member objects
    private SensorManager mySensorManager;
    private Sensor myAccelerometer;

    //arrays to store data (plan to store x, y, and z values)
    private float[] myGravityData = new float[3];
    private float[] myAccelerationData = new float[3];

    //log the last time we sensed
    private long lastTime;

    //floor of the update rate -- in milliseconds
    private final long MIN_UPDATE_DELAY = 100;

    //convergence speed (float),
    // faster values closer to 1.0f
    // slower values yield more stable "g"
    private final float CONVERGENCE_SPEED = 0.3f;

    //establish output TextViews
    private TextView myGravityMagnitude;
    private TextView myAccelerationMagnitude;

    //LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //select layout file
        setContentView(R.layout.activity_hello_sensor);

        //bind views to local variables
        myGravityMagnitude = (TextView) findViewById(R.id.gravity_magnitude);
        myAccelerationMagnitude = (TextView) findViewById(R.id.acceleration_magnitude);

        //initialize the sensor manager
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //check for accelerometer: send message if not then stop app
        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if ( myAccelerometer == null )
        {
            Context context = getApplicationContext();
            Toast.makeText(context, "...you don't have an accelerometer", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //registering accelerometer sensor manager when resuming
    @Override
    protected void onResume(){
        super.onResume();

        //SENSOR_DELAY_GAME refreshes at a rate sensible for GAME (which normally means > 60fps)
        mySensorManager.registerListener(this,myAccelerometer,SensorManager.SENSOR_DELAY_GAME);

        //all options for that last argument from slowest to fastest:
        //SENSOR_DELAY_NORMAL,
        //SENSOR_DELAY_UI,
        //SENSOR_DELAY_GAME,
        //SENSOR_DELAY_FASTEST
    }

    //unregistering accelerometer sensor manager if we ever pause
    @Override
    protected void onPause() {
        super.onPause();

        mySensorManager.unregisterListener(this);
    }


    //HELPER METHODS

    //used to give the gravity reading some "inertia" (pun)
    private float weightPreviousValue(float new_data, float previous_value) {
        return previous_value * (1 - CONVERGENCE_SPEED) + new_data * CONVERGENCE_SPEED;
    }


    //used to determine the change in the sensor reading relative to stable value
    //(basically we subtract gravity from our reading for acceleration)
    private float differenceFromG(float sensor_reading, float gravity_component) {
        //delta below
        return  sensor_reading - gravity_component;
    }

    //METHODS REQUIRED TO IMPLEMENT SENSOREVENTLISTENER

    //empty, but this class is required when implementing SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    //Main Calculations done here to analyze incoming sensor readings
    @Override
    public void onSensorChanged(SensorEvent sensor_event) {
        if (sensor_event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime > MIN_UPDATE_DELAY) {
                lastTime = currentTime;

                float xData, yData, zData;

                xData = sensor_event.values[0];
                yData = sensor_event.values[1];
                zData = sensor_event.values[2];

                //FIND GRAVITY MAGNITUDE

                //get the stable portion of the reading (gravity)
                myGravityData[0] = weightPreviousValue(xData, myGravityData[0]);
                myGravityData[1] = weightPreviousValue(yData, myGravityData[1]);
                myGravityData[2] = weightPreviousValue(zData, myGravityData[2]);

                double gravityMagnitude = Math.sqrt(
                        myGravityData[0]*myGravityData[0]
                                + myGravityData[1]*myGravityData[1]
                                + myGravityData[2]*myGravityData[2] );


                //FIND ACCELERATION MAGNITUDE

                //that which isn't gravity, is the acceleration
                myAccelerationData[0] = differenceFromG(xData,myGravityData[0]);
                myAccelerationData[1] = differenceFromG(yData,myGravityData[1]);
                myAccelerationData[2] = differenceFromG(zData,myGravityData[2]);

                double accelerationMagnitude = Math.sqrt(
                        myAccelerationData[0]*myAccelerationData[0]
                                + myAccelerationData[1]*myAccelerationData[1]
                                + myAccelerationData[2]*myAccelerationData[2] );

                //PRINT RESULTS
                myGravityMagnitude.setText(Double.toString(gravityMagnitude));
                myAccelerationMagnitude.setText(Double.toString(accelerationMagnitude));
            }


        }
    }

    //REMAINING ARE PROVIDED AUTOMATICALLY BY ANDROID STUDIO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hello_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

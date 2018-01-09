package com.example.kongwenyao.educationalgame;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Objects;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {

    private GamePanel gamePanel;
    private MainPlayer mainPlayer;
    private ImageView playerImageView;
    private PlayerState facingDirection;

    public static PointF playerPos;
    public static PointF playerSize;

    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        //Custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;

        //Variable Assignment
        playerImageView = findViewById(R.id.player_view);
        gamePanel = findViewById(R.id.game_view);
        gamePanel.setOnTouchListener(this);

        //Player Default
        mainPlayer = new MainPlayer(this, PlayerState.REST_RIGHT);
        playerImageView.setImageDrawable(mainPlayer.getDrawable());
        facingDirection = PlayerState.REST_RIGHT;

    }

    @Override
    protected void onResume() {
        super.onResume();

        //if (sensorManager != null && sensor != null) {
        //   sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        //}
        //TODO: gameThread.setRunning(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this);
        //TODO: terminate gameThread
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Set Initial Values
        playerPos = new PointF(playerImageView.getX(), playerImageView.getY());
        playerSize = new PointF(playerImageView.getWidth(), playerImageView.getHeight());   //place after setImageDrawable()
    }

    public float filterPosition(float x) {  //Used when using sensor
        if (x > (gamePanel.getWidth() - playerImageView.getWidth()) + 50) {
            x = (gamePanel.getWidth() - playerImageView.getWidth()) + 50;
        } else if (x < -50) {
            x = -50;
        }
        return x;
    }

    @Override
    public void onSensorChanged(SensorEvent event) { //test needed
        float targetPosition;
        float x = event.values[0];

        //float z = event.values[2]

        if (x < -2 && x > 2) {
            PlayerState playerState = getRestDirection(facingDirection);
            animatePlayer(playerState);
        }

        if (x > 0) { //tilt device right
            animatePlayer(PlayerState.WALK_RIGHT);
            targetPosition = filterPosition(playerImageView.getX() + 5);
            playerImageView.setX(targetPosition);
            facingDirection = PlayerState.WALK_RIGHT;

        } else if (x < 0){ //tilt device left
            animatePlayer(PlayerState.WALK_LEFT);
            targetPosition = filterPosition(playerImageView.getX() - 5);
            playerImageView.setX(targetPosition);
            facingDirection = PlayerState.WALK_LEFT;
        }

        playerPos = new PointF(playerImageView.getX(), playerImageView.getY());   //Update changed x coordinate for collision detection
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.game_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.game_leaderboard) {
            intent = new Intent(this, LeaderboardActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        PlayerState playerState;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                //Player WALK Movement
                playerState = getWalkDirection(event.getX());
                animatePlayer(playerState);
                updatePlayerViewCoordinate(event.getX());
                facingDirection = playerState;
                break;
            case MotionEvent.ACTION_UP:
                //Player REST Movement
                playerState = getRestDirection(facingDirection);
                animatePlayer(playerState);
                updatePlayerViewCoordinate(event.getX());
                break;
        }

        playerPos = new PointF(playerImageView.getX(), playerImageView.getY());   //Player ImageView top left coordinate
        //System.out.println(String.format("%f %f", event.getX(), event.getY()));

        return true;
    }

    public void updatePlayerViewCoordinate(float pointX) {

        //Check if point.x out of restricted width
        if (pointX > (gamePanel.getWidth() + 50) - playerImageView.getWidth() / 2) {
            pointX = (gamePanel.getWidth() + 50) - playerImageView.getWidth() / 2;
        } else if (pointX < playerImageView.getWidth() / 2) {
            pointX = (playerImageView.getWidth() / 2) - 50;  //Image side padding equals 50
        }

        playerImageView.setX(pointX - playerImageView.getWidth() / 2);
        playerImageView.setY(gamePanel.getHeight() - playerImageView.getHeight());
    }

    public void animatePlayer(PlayerState playerState) {
        if (!Objects.equals(mainPlayer.getPlayerState(), playerState)) {    //Check previous player state
            mainPlayer.stopAnimation(); //Stop previous running animation
            mainPlayer.updatePlayer(playerState);
            playerImageView.setImageDrawable(mainPlayer.getDrawable());
            mainPlayer.startAnimation();
        }
    }

    public PlayerState getWalkDirection(float x) {
        PlayerState playerState;

        //Determine which direction based on x value
        if (x < playerImageView.getX() + playerImageView.getX() / 2) {
            playerState = PlayerState.WALK_RIGHT;
        } else {
            playerState = PlayerState.WALK_LEFT;
        }

        return playerState;
    }

    public PlayerState getRestDirection(PlayerState facingDirection) {
        PlayerState playerState;
        if (facingDirection == PlayerState.REST_RIGHT || facingDirection == PlayerState.WALK_RIGHT) {   //Determine player facing direction
            playerState = PlayerState.REST_RIGHT;
        } else {
            playerState = PlayerState.REST_LEFT;
        }

        return playerState;
    }

}

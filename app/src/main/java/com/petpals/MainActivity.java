package com.petpals;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    AnimationDrawable palAnimation;
    AnimationDrawable foodAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView palImage = (ImageView) findViewById(R.id.pal_view);
        palImage.setBackgroundResource(R.drawable.pal_animation);
        palAnimation = (AnimationDrawable) palImage.getBackground();
        palAnimation.start();

        ImageView foodImage = (ImageView) findViewById(R.id.food_view);
        foodImage.setBackgroundResource(R.drawable.food_animation);
        foodAnimation = (AnimationDrawable) foodImage.getBackground();
    }

    public void onSend(View v)
    {
        Toast.makeText(this, "Clicked on Send button", Toast.LENGTH_LONG).show();
    }

    public void onReceive(View v)
    {
        Toast.makeText(this, "Clicked on Receive button", Toast.LENGTH_LONG).show();
    }

    public void onFeed(View v)
    {
        Toast.makeText(this, "Clicked on Feed button", Toast.LENGTH_LONG).show();
        if (foodAnimation.isRunning()) {
            foodAnimation.stop();
        }

        foodAnimation.start();
    }
}

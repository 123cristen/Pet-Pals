package com.petpals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class CreatePetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pet);
    }

    public void submitName(View button) {
        Log.d("CreatePetActivity", "submitted name");

        // TODO: show error if name includes commas

        EditText petNameText = (EditText) findViewById(R.id.EditPetName);
        String petName = petNameText.getText().toString();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PET_NAME", petName);
        startActivity(intent);
        finish();
    }
}

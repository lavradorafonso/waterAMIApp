package com.example.waterami;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView send_email = findViewById(R.id.send_email);
        send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("support@cwj.pt")); // enter an email id here

                startActivity(Intent.createChooser(sendEmail, "Choose an email client from..."));
            }
        });

    }
    //
    //public void send_email(View view){
        //Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("support@cwj.pt"));
       // startActivity(Intent.createChooser(sendEmail, "Choose an email client from..."));

    //}
}
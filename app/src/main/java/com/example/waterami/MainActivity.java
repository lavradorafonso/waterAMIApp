package com.example.waterami;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {
    MqttHelper mqttHelper;
    Mqttservice mqtt;
    private Context context;
    private IMqttActionListener listener;
    boolean verificacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();

        Button login = findViewById(R.id.login);
        Button about = findViewById(R.id.about);

        final EditText username = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        username.getText().clear();
        password.getText().clear();

        //Intent mymqttservice_intent = new Intent(this, Mqttservice.class);
        //startService(mymqttservice_intent);


        mqttHelper = new MqttHelper(getApplicationContext());
       // mqtt= new Mqttservice();
        mqttHelper.connect();
       

        base base = new base(MainActivity.this);
        base.clearDatabase("tca");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


               mqttHelper.publish("home/water/login_in", "select * from users where username='"+username.getText().toString()+"' and password='"+password.getText().toString()+"'");
               mqttHelper.subscribeToTopic("home/water/login_out",2);
                mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean b, String s) {
                        Log.w("connectado!!!!  ", s);
                    }
                    @Override
                    public void connectionLost(Throwable throwable) {
                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        Log.w("login login", mqttMessage.toString());
                        if(mqttMessage.toString().equals("certo")){
                            verificacao=true;
                            Intent intent = new Intent(getApplicationContext(), menu.class);
                            startActivity(intent);
                           Toast.makeText(context, "Login correto", Toast.LENGTH_LONG).show();
                            //toast.show();
                        }
                        if(mqttMessage.toString().equals("errado")){
                            verificacao=false;
                            Toast toast = Toast.makeText(context, "Login incorreto", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                    }
                });

            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                     Intent intent = new Intent(getApplicationContext(), about.class);
                    startActivity(intent);
            }
        });


    }
}
            /*    final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 2000);
*/
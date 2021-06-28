package com.example.waterami;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class menu extends AppCompatActivity {
    private static final String TAG = "menu";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String[] areas= new String[]{"Lisboa","Porto","Braga","Setúbal","Aveiro","Leiria","Santarém","Faro","Coimbra","Viseu","Madeira","Açores","Viana do Castelo","Vila Real","Castelo Branco","Guarda","Évora","Beja","Bragança","Portoalegre"};
    MqttHelper mqttHelper;
    private Context context;

    base base;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();
        Button update = (Button) findViewById(R.id.update);
          mqttHelper = new MqttHelper(getApplicationContext());
          mqttHelper.connect();

        base =new base(menu.this);
        base.clearDatabase("medidas");
        Button data=(Button)findViewById(R.id.data) ;
        Button map = (Button) findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHelper.subscribeToTopic("home/water/out",2);
                mqttHelper.publish("home/water/in","select * from tca");
                mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean b, String s) {
                    }
                    @Override
                    public void connectionLost(Throwable throwable) {
                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                        base.d_tca(mqttMessage.toString());
                        Log.w("login login", mqttMessage.toString());
                       /* Intent intent = new Intent(menu.this, mapa.class);
                        startActivity(intent);*/
                    }
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }
                });


                LayoutInflater layoutInflater = LayoutInflater.from(menu.this);
                View promptView = layoutInflater.inflate(R.layout.selecionar_zona, null);
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(menu.this);
                alertDialogBuilder.setView(promptView);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(menu.this,
                        android.R.layout.simple_dropdown_item_1line, areas);
                MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView)
                        promptView.findViewById(R.id.id_text_select);
                textView.setAdapter(adapter);
                textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                alertDialogBuilder.setCancelable(true)
                        .setPositiveButton("GO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            String x=textView.getText().toString();
                            String[] zonas_selecionas=x.split("\\s*,\\s*");
                               mqttHelper.publish("teste",zonas_selecionas[zonas_selecionas.length-1]);
                                 if(isServicesOK()){

                                     Intent intent = new Intent(menu.this, mapa.class);
                                     startActivity(intent);
                                      }

                            }

                        });

                AlertDialog b = alertDialogBuilder.create();

                b.show();
                b.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));

            }
        });
         data.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(menu.this);

                final View customLayout=getLayoutInflater().inflate(R.layout.grafico_context,null);
                builder.setView(customLayout);
                builder.setTitle("Insert tca id");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText id_tca=customLayout.findViewById(R.id.grafico_context);
                        String x=id_tca.getText().toString();
                        if(!x.equals("")) {
                            mqttHelper.subscribeToTopic("home/water/out", 2);
                            mqttHelper.publish("home/water/in", "select * from tca"+x);
                            mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                                @Override
                                public void connectComplete(boolean b, String s) {
                                }

                                @Override
                                public void connectionLost(Throwable throwable) {
                                }

                                @Override
                                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                                    base.clearDatabase("medidas");
                                    base.d_valores(mqttMessage.toString());

                                    Intent intent = new Intent(getApplicationContext(), grafico.class);
                                    intent.putExtra("id_tca",x);
                                    startActivity(intent);

                                }

                                @Override
                                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                                }
                            });
                        }
                        else{
                            Toast.makeText(menu.this, "Insira o TCA", Toast.LENGTH_LONG).show();
                        }

                    }

                });
                 AlertDialog dialog=builder.create();
                 dialog.show();
                 dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));

             }
         });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(menu.this, tecnico.class);
                startActivity(intent);
            }
        });
    }
    
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(menu.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(menu.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
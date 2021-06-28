package com.example.waterami;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Format;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class grafico extends AppCompatActivity {
    private static final String TAG = grafico.class.getSimpleName();
    GraphView graphView;
    long dia = 86400000;
    long semana = 604800000;
    long mes = 2628000000L;
    MqttHelper mqttHelper;
    base base;
    Date date = new Date();
    long timeMilli = date.getTime();
    long[] x;
    float[] y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);
        Button select = findViewById(R.id.select);
        EditText tca=findViewById(R.id.id_tca);
        Button alterar=findViewById(R.id.alterar);
        Objects.requireNonNull(getSupportActionBar()).hide();
        graphView = findViewById(R.id.graph);
        base = new base(grafico.this);
        Log.d(TAG, "mes:"+mes);
        Log.d(TAG, "milisegundos:"+timeMilli);
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.connect();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select.performLongClick();
            }
        });

       String id_tca =  getIntent().getStringExtra("id_tca");


        tca.setText(id_tca);

        registerForContextMenu(select);

        alterar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                 String id_tcax = tca.getText().toString();

                base.clearDatabase("medidas");
                mqttHelper.publish("home/water/in","select*from tca"+id_tcax);
                mqttHelper.subscribeToTopic("home/water/in",2);
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

                        base.d_valores(mqttMessage.toString());
                        make_grafico(dia,"Últimas 24 horas");
                        tca.setText(id_tcax);
                    }
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }});


            }
            });

        make_grafico(dia,"Últimas 24 horas");

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Selecionar");
        menu.add(0, v.getId(), 0, "Últimas 24 horas");
        menu.add(0, v.getId(), 0, "Últimos 7 dias");
        menu.add(0, v.getId(), 0, "Últimos 30 dias");


    }


    public boolean onContextItemSelected(MenuItem item) {
        long x[] = base.get_timestamp();
        float y[] = base.get_agua();

        if (item.getTitle() == "Últimas 24 horas") {
           make_grafico(dia,"Últimas 24 horas");


            return true;
        }
        if (item.getTitle() == "Últimos 7 dias") {
            make_grafico(semana,"Últimos 7 dias");
            return true;
        }
        if (item.getTitle() == "Últimos 30 dias") {
            make_grafico(mes,"Últimos 30 dias");
            return true;
        }


        return false;
    }

    private void make_grafico(long tempo,String titulo){
        x=base.get_timestamp();
        y=base.get_agua();
       for (int i = 0; i < x.length; i++) {
            Log.d(TAG, "timestamp n" + i + "  :" + y[i] + "o timestamp é: " + x[i]);
        }
        float max=0;
        float min=0;
        int indice = get_indice(tempo, x);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (int i = indice; i < x.length; i++) {
            series.appendData(new DataPoint(x[i], y[i]), true, 500000);
            Log.d(TAG, "grafico::" + x[i] + "y:" + y[i]+" e o caralho do i:"+i);
            if(y[i]>=max) max=y[i];
            if(y[i]<=min) min=y[i];
        }

        graphView.removeAllSeries();
        graphView.getGridLabelRenderer().setHumanRounding(true);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(x[indice]);
        graphView.getViewport().setMaxX(x[x.length - 1]);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(min*0.9);
        graphView.getViewport().setMaxY(max*1.1);
        graphView.getViewport().setScalable(true);
        graphView.setTitle(titulo);
        graphView.addSeries(series);
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Format formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
                    return formatter.format(value);
                }
                return super.formatLabel(value, isValueX);
            }
        });
    }
    private int get_indice(long x, long[] arr) {
        int size = arr.length - 1;
        Log.d(TAG, "x:::: " + x);
        while (arr[size] > (timeMilli - x)) {
            Log.d(TAG, "size!!!!: " + size);
            if(size==0) break;

            size--;

        }
        Log.d(TAG, "size!: " + size);

      return size;
    }
}
package com.example.waterami;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;

public class tecnico extends AppCompatActivity {
    private static final String TAG =tecnico.class.getSimpleName(); ;
    EditText data;
    EditText hora;
    private Context context;
    int day=0;
    int mon=0;
    int year=0;
    int hour=0;
    int min=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tecnico);
        getSupportActionBar().hide();
        MqttHelper mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.connect();

        data = findViewById(R.id.id_data);
        hora=findViewById(R.id.id_hora);
        Button submeter=findViewById(R.id.submeter);
        TextView multiplicador = findViewById(R.id.multiplicador_textbox);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        multiplicador.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                closekeyboard();
                PopupMenu menu = new PopupMenu(tecnico.this, multiplicador);
                menu.getMenu().add("1 L");
                menu.getMenu().add("10 L");
                menu.getMenu().add("100 L");
                menu.getMenu().add("1000 L");
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //item.getTitle(),
                       multiplicador.setText(item.getTitle());
                        return true;
                    }
                });
                menu.show();
            }
        });


        data.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        day  = Integer.parseInt(clean.substring(0,2));
                        mon  = Integer.parseInt(clean.substring(2,4));
                        year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    data.setText(current);
                    data.setSelection(sel < current.length() ? sel : current.length());

                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });


        submeter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, String.valueOf(year));
            }
        });

    }
    private  void closekeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}


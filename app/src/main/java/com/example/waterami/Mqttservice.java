package com.example.waterami;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Mqttservice extends Service {
    private String ip = "waterami.duckdns.org", port = "1883";
    final String username = "waterami";
    final String password = "waterami";
    private final IBinder mBinder = new LocalBinder();
    private Handler mHandler;

    private class ToastRunnable implements Runnable {//to toast to your main activity for some time
        String mText;
        int mtime;

        public ToastRunnable(String text, int time) {
            mText = text;
            mtime = time;
        }

        @Override
        public void run() {

            final Toast mytoast = Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_LONG);
            mytoast.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mytoast.cancel();
                }
            }, mtime);
        }
    }

    private static final String TAG = "mqttservice";
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private ConnectivityManager mConnMan;
    private volatile IMqttAsyncClient mqttClient;
    private String uniqueID;


    class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            IMqttToken token;
            boolean hasConnectivity = false;
            boolean hasChanged = false;
            NetworkInfo infos[] = mConnMan.getAllNetworkInfo();
            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")) {
                    if ((infos[i].isConnected() != hasMmobile)) {
                        hasChanged = true;
                        hasMmobile = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if (infos[i].getTypeName().equalsIgnoreCase("WIFI")) {
                    if ((infos[i].isConnected() != hasWifi)) {
                        hasChanged = true;
                        hasWifi = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }
            hasConnectivity = hasMmobile || hasWifi;
            Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " + (mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect();

            }


        }
    }


    public class LocalBinder extends Binder {
        public Mqttservice getService() {
            // Return this instance of LocalService so clients can call public methods
            return Mqttservice.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void publish(String topic, MqttMessage message) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);// we create a 'shared" memory where we will share our preferences for the limits and the values that we get from onsensorchanged
        try {

            mqttClient.publish(topic, message);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    public void publish1(String topic, String info) {

        byte[] encodedInfo;
        try {
            encodedInfo = info.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedInfo);
            mqttClient.publish(topic, message);
            Log.e("Mqtt", "publish done");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("Mqtt", Objects.requireNonNull(e.getMessage()));
        } catch (Exception e) {
            Log.e("Mqtt", "general exception " + e.getMessage());
        }

    }


    @Override
    public void onCreate() {

        mHandler = new Handler();//for toasts
        IntentFilter intentf = new IntentFilter();
        setClientID();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service", "onDestroy");

    }


    private void setClientID() {
        uniqueID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        Log.d(TAG, "uniqueID=" + uniqueID);

    }


    private void doConnect() {
        String broker = "tcp://" + ip + ":" + port;
        Log.d(TAG, "mqtt_doConnect()");
        IMqttToken token;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setMaxInflight(100);//handle more messages!!so as not to disconnect
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(1000);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        try {

            mqttClient = new MqttAsyncClient(broker, uniqueID, new MemoryPersistence());
            token = mqttClient.connect(options);
            token.waitForCompletion(3500);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    try {
                        mqttClient.disconnectForcibly();
                        mqttClient.connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    Log.i(TAG, "Message arrived from topic " + topic);


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("published");
                }
            });

//            mqttClient.subscribe("Sensors/" + uniqueID, 0);
//            mqttClient.subscribe("Sensors/message", 0);

        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                    mHandler.post(new ToastRunnable("WE ARE OFFLINE BROKER_UNAVAILABLE!", 1500));
                    break;
                case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    mHandler.post(new ToastRunnable("WE ARE OFFLINE CLIENT_TIMEOUT!", 1500));
                    break;
                case MqttException.REASON_CODE_CONNECTION_LOST:
                    mHandler.post(new ToastRunnable("WE ARE OFFLINE CONNECTION_LOST!", 1500));
                    break;
                case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                    Log.v(TAG, "c" + e.getMessage());
                    e.printStackTrace();
                    break;
                case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                    Intent i = new Intent("RAISEALLARM");
                    i.putExtra("ALLARM", e);
                    Log.e(TAG, "b" + e.getMessage());
                    break;
                default:
                    Log.e(TAG, "a" + e.getMessage());
            }
        }
        mHandler.post(new ToastRunnable("WE ARE ONLINE!", 500));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");
        return START_STICKY;
    }
}

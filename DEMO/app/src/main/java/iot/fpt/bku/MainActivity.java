package iot.fpt.bku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txthumid;
    ToggleButton btnLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bach bao how to get full screen have to be before setContentView()
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // phai declare pointers
        btnLED = findViewById(R.id.btnLED);
        txtTemp = findViewById(R.id.txtTemperature);
        txthumid = findViewById(R.id.txtHumidity);

        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bCheck) {
                if(bCheck == true){
                    btnLED.setVisibility(View.INVISIBLE);
                    Log.d("mqtt", "Button is On");
                    sendDataMQTT("lequocanh545/f/bbc-led", "1");
                }else{
                    btnLED.setVisibility(View.INVISIBLE);
                    Log.d("mqtt", "Button is OFF");
                    sendDataMQTT("lequocanh545/f/bbc-led", "0");
                }
            }
        });

        startMQTT();
        setupScheduler();
    }
/////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        aTimer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    Timer aTimer = new Timer();
    int waiting_period = 0;
    int sendAgainNum = 2;
    boolean send_message_again = false;
    List<MQTTMessage> list = new ArrayList<>();

    private void setupScheduler(){
        TimerTask scheduler = new TimerTask() {
            @Override
            public void run() {
                Log.d("mqtt", "Timer is executed " + waiting_period +"s" +" "+send_message_again);
                if(list.isEmpty()){
                    Log.d("waiting list","empty");
                }else{
                    Log.d("waiting list","not empty " + list.size());
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(waiting_period > 0){
                            waiting_period--;
                            if(waiting_period == 0){
                                send_message_again = true;
                            }
                        }
                        if(sendAgainNum == 0){
                            btnLED.setVisibility(View.VISIBLE);
                            sendAgainNum = 2;
                            send_message_again = false;
                            waiting_period = 0;
                            list.clear();
                        }
                        if(send_message_again == true){
                            sendAgainNum--;
                            Log.d("mqtt", "Send message again");
                            sendDataMQTT(list.get(0).topic , list.get(0).mess);
                        }
                    }
                });
            }
        };
        aTimer.schedule(scheduler, 0, 2000);
    }

    private void sendDataMQTT(String topic, String value){
        if(!topic.equals("lequocanh545/feeds/bbc-error")){
            waiting_period = 3;
            send_message_again = false;
            MQTTMessage aMessage = new MQTTMessage();
            aMessage.topic = topic; aMessage.mess = value;
            list.add(aMessage);
        }

        MqttMessage msg = new MqttMessage();
        msg.setId(1234); // tren adafruit set hay ko cung dc
        msg.setQos(0); // may 0-4. cang cao thi` lop du lieu cang tin cay
        msg.setRetained(true);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);

        }catch (MqttException e){
        }
    }

    private void startMQTT(){
        mqttHelper = new MQTTHelper(getApplicationContext(), "662000");

        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("mqtt", "connection is successfully");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals("lequocanh545/feeds/bbc-led")){
                    Log.d("mqtt", "LED-in Received : " + message.toString());
                }
                if(topic.equals("lequocanh545/feeds/bbc-led")){
                    sendDataMQTT("lequocanh545/feeds/bbc-error", "[ACK-LED:"+message.toString()+"]");
                    if(message.toString().equals("1")){
                        btnLED.setChecked(true);
                    }else{
                        btnLED.setChecked(false);
                    }
                }
                if(topic.equals("lequocanh545/feeds/bbc-error")){
                    if(message.toString().equals("[ACK-LED:"+list.get(0).mess.toString()+"]")){
                        waiting_period = 0;
                        send_message_again = false;
                        list.clear();
                        btnLED.setVisibility(View.VISIBLE);
                    }
                }

                if(topic.equals("lequocanh545/feeds/bbc-temp")){
                    sendDataMQTT("lequocanh545/feeds/bbc-error", "[ACK-TEMP:"+message.toString()+"]");
                    Log.d("mqtt", "TEMP-in Changed: "+message.toString());
                    txtTemp.setText(message.toString()+"°C");
                }

                if(topic.equals("lequocanh545/feeds/bbc-humidity")){
                    sendDataMQTT("lequocanh545/feeds/bbc-error", "[ACK-HUMID:"+message.toString()+"]");
                    Log.d("mqtt", "HUMID-in Changed: "+message.toString());
                    txthumid.setText(message.toString()+"%");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public class MQTTMessage{
        public String topic;
        public String mess;
    }
}

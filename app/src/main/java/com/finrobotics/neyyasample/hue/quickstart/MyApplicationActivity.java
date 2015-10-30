package com.finrobotics.neyyasample.hue.quickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.finrobotics.neyyasample.MyService;
import com.finrobotics.neyyasample.R;
import com.finrobotics.neyyasdk.core.Gesture;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MyApplicationActivity extends AppCompatActivity {
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "QuickStart";
    private int[] colors = {12750, 25500, 46920, 56100, 65280};
    private int count = 0;
    private int currentBrightness = 254;
    private boolean isLightOn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        phHueSDK = PHHueSDK.create();
        Button randomButton;
        randomButton = (Button) findViewById(R.id.buttonRand);
        registerReceiver(mNeyyaUpdateReceiver, makeNeyyaUpdateIntentFilter());
        randomButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                randomLights();
            }

        });

    }

    private final BroadcastReceiver mNeyyaUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MyService.BROADCAST_GESTURE.equals(action)) {
                int gesture = intent.getIntExtra(MyService.DATA_GESTURE, 0);
                performGesture(gesture);
            }
        }
    };

    private void performGesture(int gesture) {
        Log.w(TAG, "Gesture - " + Gesture.parseGesture(gesture));
        switch (gesture) {
            case Gesture.SWIPE_DOWN:
                adjustBrightness(false);
                break;
            case Gesture.SWIPE_UP:
                adjustBrightness(true);
                break;
            case Gesture.SWIPE_LEFT:
                if (count == 0)
                    count = 4;
                else
                    count--;
                changeColor(colors[count]);
                break;
            case Gesture.SWIPE_RIGHT:
                if (count == 4)
                    count = 0;
                else
                    count++;
                changeColor(colors[count]);

                break;
            case Gesture.DOUBLE_TAP:
                if (isLightOn) {
                    lightsOn(isLightOn);
                    isLightOn = false;
                } else {
                    lightsOn(isLightOn);
                    isLightOn = true;
                }
                break;
        }
    }

    private void changeColor(int number) {

        PHBridge bridge = phHueSDK.getSelectedBridge();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(number);
            bridge.updateLightState(light, lightState, listener);
        }
    }

    private void adjustBrightness(boolean increase) {
        if (increase) {
            currentBrightness += 50;
            if (currentBrightness > 254)
                currentBrightness = 254;

            changeBrightness();
        } else {
            currentBrightness -= 50;
            if (currentBrightness < 0)
                currentBrightness = 0;
            changeBrightness();
        }

    }

    private void changeBrightness() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setBrightness(currentBrightness);
            bridge.updateLightState(light, lightState, listener);
        }
    }

    private void lightsOn(boolean status){
        PHBridge bridge = phHueSDK.getSelectedBridge();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setOn(status);
            bridge.updateLightState(light, lightState, listener);
        }
    }


    public void randomLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(MAX_HUE));

            // To validate your lightstate is valid (before sending to the bridge) you can use:  
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, listener);
            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
    }

    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener listener = new PHLightListener() {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            Log.w(TAG, "Light has updated");
        }

        @Override
        public void onError(int arg0, String arg1) {
        }

        @Override
        public void onReceivingLightDetails(PHLight arg0) {
        }

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {
        }

        @Override
        public void onSearchComplete() {
        }
    };

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }

    private IntentFilter makeNeyyaUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.BROADCAST_GESTURE);
        return intentFilter;
    }
}

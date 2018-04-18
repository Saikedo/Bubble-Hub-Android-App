package net.california_design.bubble_hub;


import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This activity handles all the Bubble pillar related controls.
 */
public class RemoteControlBubblePillar extends RemoteControl {

    ////////////////////////Class global variables////////////////////////
    private Button btnOnOff, btnSleep, btnLightsFadeSplit, btnLightsFade, btnLightsMix, btnLightsCustomColor, btnBubbleHubConnection;
    private boolean mIsBubbleHubConnectedStatus = true;

    private static class BtMessageOut {
        static final String BT_MESSAGE_OUT_FADE_LIGHTS = "6", BT_MESSAGE_OUT_FADE_SPLIT_LIGHTS = "7", BT_MESSAGE_OUT_MIXED_LIGHTS = "8",
                BT_MESSAGE_OUT_BUBBLE_HUB_CONNECT = "9", BT_MESSAGE_OUT_BUBBLE_HUB_DISCONNECT = "10";
    }

    private static class BtMessageIn {
        static final int BT_MESSAGE_IN_SYSTEM_OFF = 0, BT_MESSAGE_IN_SYSTEM_ON = 1, BT_MESSAGE_IN_SLEEP_MODE_STARTED = 2,
                Bt_MESSAGE_IN_SLEEP_ACHIEVED = 3, BT_MESSAGE_IN_SLEEP_CANCELLED = 4, BT_MESSAGE_IN_MANUAL_MODE = 5, BT_MESSAGE_IN_FADE_LIGHTS = 6, BT_MESSAGE_IN_FADE_SPLIT_LIGHTS = 7,
                BT_MESSAGE_IN_MIXED_LIGHTS = 8, BT_MESSAGE_IN_CUSTOM_LIGHTS = 9, BT_MESSAGE_IN_BUBBLE_HUB_CONNECT = 10, BT_MESSAGE_IN_BUBBLE_HUB_DISCONNECT = 11;
    }
    ////////////////////////Variable declaration ends here////////////////////////


    int getLayoutID() {
        return R.layout.activity_remote_control_bubble_pillar;
    }


    void initializeViewsAndFragments() {
        btnOnOff = findViewById(R.id.button_on_off);
        btnOnOff.setOnClickListener(this);
        btnSleep = findViewById(R.id.button_sleep);
        btnSleep.setOnClickListener(this);
        btnLightsFade = findViewById(R.id.button_lights_fade);
        btnLightsFade.setOnClickListener(this);
        btnLightsFadeSplit = findViewById(R.id.button_lights_fade_split);
        btnLightsFadeSplit.setOnClickListener(this);
        btnLightsCustomColor = findViewById(R.id.button_lights_custom_color);
        btnLightsCustomColor.setOnClickListener(this);
        btnLightsMix = findViewById(R.id.button_lights_mix);
        btnLightsMix.setOnClickListener(this);
        btnBubbleHubConnection = findViewById(R.id.button_bubble_hub_connection);
        btnBubbleHubConnection.setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mCountDownTimerFragment = (CountDownTimerFragment) fragmentManager.findFragmentById(R.id.countDownTimerFragment);

        mCountDownTimer = findViewById(R.id.countDownTimerFragment);
        mCountDownTimer.setVisibility(View.INVISIBLE);
    }



    public void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.button_on_off: {
                if(!mArduinoPowerStatus) {
                    sendBtMessage(BT_MESSAGE_OUT_SYSTEM_ON, DIRECT_MESSAGE);
                } else {
                    sendBtMessage(BT_MESSAGE_OUT_SYSTEM_OFF, DIRECT_MESSAGE);
                }
                break;
            }
            case R.id.button_sleep: {
                if(btnSleep.getText().toString().equals(getString(R.string.sleep))){
                    sendBtMessage(BT_MESSAGE_OUT_SYSTEM_SLEEP, BUBBLES_OR_LIGHTS_MESSAGE);
                } else if(btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))) {
                    sendBtMessage(BT_MESSAGE_OUT_CANCEL_SLEEP, DIRECT_MESSAGE);
                }

                break;
            }
            case R.id.button_lights_fade: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_FADE_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_lights_fade_split: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_FADE_SPLIT_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_lights_custom_color: {
                showColorPickerDialog();
                break;
            }
            case R.id.button_lights_mix: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_MIXED_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }

            case R.id.button_bubble_hub_connection: {
                if(mIsBubbleHubConnectedStatus) {
                    sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_BUBBLE_HUB_DISCONNECT,DIRECT_MESSAGE);
                } else {
                    sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_BUBBLE_HUB_CONNECT,DIRECT_MESSAGE);
                }
                break;
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////


    void processBtMessage(Object messageObject, int messageType) {
        if (messageType == PROCESS_MESSAGE_TYPE_STATUS_REQUEST) {
            if (!(messageObject instanceof String)) {
                return;
            }
            String message = messageObject.toString();

            //1st character: 0 = off, 1 = on
            //2nd character: 0 = fade lights, 1 = fade split lights, 2 = custom lights, 3 = mixed lights
            //3rd character: 0 = bubble hub disconnected, 1 = bubble hub connected

            int powerStatus = Character.getNumericValue(message.charAt(2));
            int lightsMode = Character.getNumericValue(message.charAt(3));
            int bubbleHubConnectStatus = Character.getNumericValue(message.charAt(4));

            ArrayList<Integer> messageArray = new ArrayList<>();

            if(powerStatus == 0) {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_OFF);
            } else {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_ON);

                switch(lightsMode) {
                    case 0: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS);
                        break;
                    }
                    case 1: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_FADE_SPLIT_LIGHTS);
                        break;
                    }
                    case 2: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS);
                        break;
                    }
                    case 3: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_MIXED_LIGHTS);
                        break;
                    }
                }
            }

            if(bubbleHubConnectStatus == 0) {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_BUBBLE_HUB_DISCONNECT);
            } else {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_BUBBLE_HUB_CONNECT);
            }

            processMessageArray(messageArray);

        } else if (messageType == PROCESS_MESSAGE_TYPE_MESSAGE_ARRAY) {
            if (messageObject instanceof ArrayList<?>) {

                ArrayList<Integer> arrayListInteger = new ArrayList<>();

                ArrayList<?> arrayList = (ArrayList<?>) messageObject;
                for (int i = 0; i < arrayList.size(); i++) {
                    Object object = arrayList.get(i);
                    if (object instanceof Integer) {
                        arrayListInteger.add((Integer) object);
                    }
                }

                processMessageArray(arrayListInteger);
            }
        }
    }

    void processMessageArray(ArrayList<Integer> messageArray) {
        for(int msg : messageArray) {

            if(msg == BtMessageIn.BT_MESSAGE_IN_SYSTEM_OFF) {
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting Button to On");

                if(btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))){
                    sendBtMessage(BT_MESSAGE_OUT_CANCEL_SLEEP, DIRECT_MESSAGE);
                } else {
                    mCountDownTimer.setVisibility(View.INVISIBLE);
                    mCountDownTimerFragment.stopCountDownTimer();
                    btnSleep.setText(R.string.sleep);
                }

                mArduinoPowerStatus = false;
                LightsButtonsBackgroundUnpressed();
                btnOnOff.setBackgroundResource(R.drawable.button_background_gradient_green_black_border);
                btnOnOff.setText(R.string.On);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SYSTEM_ON) {

                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting Button to Off");
                mArduinoPowerStatus = true;
                btnOnOff.setBackgroundResource(R.drawable.button_background_gradient_red_black_border);
                btnOnOff.setText(R.string.Off);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MANUAL_MODE) {
                Toast.makeText(this, "Remote operation of bubble pillar is restricted when manual mode is turned on.", Toast.LENGTH_LONG).show();
                finishActivity();
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_MODE_STARTED) {
                btnSleep.setText(R.string.cancel_sleep);
                mCountDownTimer.setVisibility(View.VISIBLE);

                mCountDownTimerFragment.StartTimer();

                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Sleep mode started");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_CANCELLED){
                if(btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))){
                    mCountDownTimer.setVisibility(View.INVISIBLE);
                    mCountDownTimerFragment.stopCountDownTimer();
                    btnSleep.setText(R.string.sleep);
                    CustomLogger.log("RemoteControlBubblePillar: bReceiver: Sleep cancelled");
                }
            } else if(msg == BtMessageIn.Bt_MESSAGE_IN_SLEEP_ACHIEVED) {
                mCountDownTimer.setVisibility(View.INVISIBLE);
                mCountDownTimerFragment.stopCountDownTimer();
                btnSleep.setText(R.string.sleep);
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Sleep mode achieved");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting fade lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_FADE_SPLIT_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsFadeSplit.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting fade split lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MIXED_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_red_border);
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting mixed lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_BUBBLE_HUB_CONNECT) {
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting bubble hub button to disconnect");
                mIsBubbleHubConnectedStatus = true;
                btnBubbleHubConnection.setBackgroundResource(R.drawable.button_background_gradient_red_black_border);
                btnBubbleHubConnection.setText(R.string.disconnect_from_bubble_hub);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_BUBBLE_HUB_DISCONNECT) {
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting bubble hub button to connect");
                mIsBubbleHubConnectedStatus = false;
                btnBubbleHubConnection.setBackgroundResource(R.drawable.button_background_gradient_green_black_border);
                btnBubbleHubConnection.setText(R.string.ConnectToBubbleHub);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS) {
                CustomLogger.log("RemoteControlBubblePillar: bReceiver: Setting custom lights button to pressed");
                LightsButtonsBackgroundUnpressed();
                btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
            }
        }
    }

    /** resets the background images of all light related buttons */
    private void LightsButtonsBackgroundUnpressed() {
        btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsFadeSplit.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_black_border);
        btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
    }


}

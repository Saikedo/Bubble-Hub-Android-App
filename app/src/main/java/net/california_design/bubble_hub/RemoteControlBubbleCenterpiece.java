package net.california_design.bubble_hub;

import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This activity handles all the centerpiece related controls.
 */
public class RemoteControlBubbleCenterpiece extends RemoteControl {
    ////////////////////////Class global variables////////////////////////
    private Button btnOnOff, btnSleep, btnLightsFade, btnLightsCustomColor;
    private Handler mBrightnessSenderHandler = new Handler();
    private SeekBar mBrightnessSlider;
    private TextView mBrightnessPercentageTextView;
    private int mBrightness = 255;
    private Boolean mSeekBarManualChange = false;

    private static class BtMessageOut {
        static final String BT_MESSAGE_OUT_FADE_LIGHTS = "6";
    }

    private static class BtMessageIn {
        static final int BT_MESSAGE_IN_SYSTEM_OFF = 0, BT_MESSAGE_IN_SYSTEM_ON = 1, BT_MESSAGE_IN_SLEEP_MODE_STARTED = 2, Bt_MESSAGE_IN_SLEEP_ACHIEVED = 3,
                BT_MESSAGE_IN_SLEEP_CANCELLED = 4, BT_MESSAGE_IN_MANUAL_MODE = 5, BT_MESSAGE_IN_FADE_LIGHTS = 6, BT_MESSAGE_IN_CUSTOM_LIGHTS = 7;
    }
    ////////////////////////Variable declaration ends here////////////////////////


    int getLayoutID() {
        return R.layout.activity_remote_control_bubble_centerpiece;
    }


    void initializeViewsAndFragments() {
        mBrightnessPercentageTextView = findViewById(R.id.activity_remote_control_text_brightness_percentage_id);
        mBrightnessPercentageTextView.setText(getString(R.string.brightnessPercentage,  100));


        btnOnOff = findViewById(R.id.button_on_off);
        btnOnOff.setOnClickListener(this);
        btnSleep = findViewById(R.id.button_sleep);
        btnSleep.setOnClickListener(this);
        btnLightsFade = findViewById(R.id.button_lights_fade);
        btnLightsFade.setOnClickListener(this);
        btnLightsCustomColor = findViewById(R.id.button_lights_custom_color);
        btnLightsCustomColor.setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mCountDownTimerFragment = (CountDownTimerFragment) fragmentManager.findFragmentById(R.id.countDownTimerFragment);

        mCountDownTimer = findViewById(R.id.countDownTimerFragment);
        mCountDownTimer.setVisibility(View.INVISIBLE);


        mBrightnessSlider = findViewById(R.id.remote_control_centerpiece_brightness_slider);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBrightnessSlider.setSplitTrack(false);
        }
        mBrightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
            mBrightness = progressValue;
            mBrightnessPercentageTextView.setText(getString(R.string.brightnessPercentage,  (mBrightness * 100)/255));

            mBrightnessSenderHandler.removeCallbacksAndMessages(null);

            // We only want to send a brightness message if the user interacts with the seekBar and not when the seekBar
            // Value is set by a program.
            if(mSeekBarManualChange) {
                //Since we don't want to send too much bluetooth information too fast, I implemented this handler
                //that only sends the data to Arduino if the particular brightness has been chosen for more than 100ms.
                mBrightnessSenderHandler.postDelayed( () -> {
                    //Centerpiece controller checks the 25th bit and if it is 1, it processes the message as custom color
                    //But first we clear the 8 most significant bits since they were used for transparency and we do not use them
                    int brightnessWithSetFlag = (mBrightness & ~((~0 << 24))) | (1 << 25);
                    sendBtMessage(Integer.toString(brightnessWithSetFlag),DIRECT_MESSAGE);
                }, 100);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mSeekBarManualChange = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mSeekBarManualChange = false;
            //Centerpiece controller checks the 26th bit and if it is 1, it processes the message as custom color
            //But first we clear the 8 most significant bits since they were used for transparency and we do not use them
            int brightnessWithSetFlag = (mBrightness & ~((~0 << 24))) | (1 << 25);
                sendBtMessage(Integer.toString(brightnessWithSetFlag),DIRECT_MESSAGE);
            }
        });
    }



    void onClickListener(View view)
    {
        switch (view.getId()) {
            case R.id.button_on_off: {
                if(!mArduinoPowerStatus) {
                    sendBtMessage(BT_MESSAGE_OUT_SYSTEM_ON,DIRECT_MESSAGE);
                } else {
                    sendBtMessage(BT_MESSAGE_OUT_SYSTEM_OFF,DIRECT_MESSAGE);
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
            case R.id.button_lights_custom_color: {
                showColorPickerDialog();
                break;
            }
        }
    }


    void processBtMessage(Object messageObject, int messageType) {
        if (messageType == PROCESS_MESSAGE_TYPE_STATUS_REQUEST) {
            if (!(messageObject instanceof String)) {
                return;
            }
            String message = messageObject.toString();

            //1st character: 0 = off, 1 = on
            //2nd character: 0 = fade lights, 1 = custom lights
            //3rd-5th character: brightness
            int powerStatus = Character.getNumericValue(message.charAt(2));
            int lightsMode = Character.getNumericValue(message.charAt(3));
            int brightness = Integer.parseInt(message.substring(4, message.length()));

            ArrayList<Integer> messageArray = new ArrayList<>();

            if (powerStatus == 0) {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_OFF);
            } else {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_ON);

                if (lightsMode == 0) {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS);
                } else {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS);
                }
            }

            setBrightness(brightness);
            processMessageArray(messageArray);

        } else if (messageType == PROCESS_MESSAGE_TYPE_CLASS_SPECIFIC_MESSAGE) {
            if (!(messageObject instanceof String)) {
                return;
            }
            String message = messageObject.toString();

            final String BRIGHTNESS_MESSAGE = "brightness";
            setBrightness(Integer.parseInt(message.substring(BRIGHTNESS_MESSAGE.length(), message.length())));

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
                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Setting Button to On");

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

                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Setting Button to Off");
                mArduinoPowerStatus = true;
                btnOnOff.setBackgroundResource(R.drawable.button_background_gradient_red_black_border);
                btnOnOff.setText(R.string.Off);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MANUAL_MODE) {
                Toast.makeText(this, "Remote operation of Bubble Centerpiece is restricted when manual mode is turned on.", Toast.LENGTH_LONG).show();
                finishActivity();
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_MODE_STARTED) {
                btnSleep.setText(R.string.cancel_sleep);
                mCountDownTimer.setVisibility(View.VISIBLE);
                mCountDownTimerFragment.StartTimer();

                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Sleep mode started");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_CANCELLED){
                if(btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))){
                    mCountDownTimer.setVisibility(View.INVISIBLE);
                    mCountDownTimerFragment.stopCountDownTimer();
                    btnSleep.setText(R.string.sleep);
                    CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Sleep cancelled");
                }
            } else if(msg == BtMessageIn.Bt_MESSAGE_IN_SLEEP_ACHIEVED) {
                mCountDownTimer.setVisibility(View.INVISIBLE);
                mCountDownTimerFragment.stopCountDownTimer();
                btnSleep.setText(R.string.sleep);
                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Sleep mode achieved");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Setting fade lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS) {
                CustomLogger.log("RemoteControlBubbleCenterpiece: bReceiver: Setting custom lights button to pressed");
                LightsButtonsBackgroundUnpressed();
                btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
            }
        }
    }

    /** resets the background images of all light related buttons */
    private void LightsButtonsBackgroundUnpressed() {
        btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
    }




    private void setBrightness(int brightnessLevel) {
        mBrightness = brightnessLevel;
        mBrightnessPercentageTextView.setText(getString(R.string.brightnessPercentage,  (mBrightness * 100)/255));
        mBrightnessSlider.setProgress(mBrightness);
        CustomLogger.log("RemoteControlBubbleCenterpiece: processIncomingBtMessage: Received brightness " + mBrightness);
    }
}

package net.california_design.bubble_hub;


import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This activity handles all the Bubble wall related controls.
 */
public class RemoteControlBubbleWall extends RemoteControl {

    ////////////////////////Class global variables////////////////////////
    private Button btnOnOff, btnSleep, btnBubblesSmall, btnBubblesBig, btnBubblesSmallPlusBig, btnBubblesPyramid, btnBubblesPillar, btnBubblesMix,
            btnLightsRainbow1, btnLightsRainbow2, btnLightsFadeSplit, btnLightsFade, btnLightsMix, btnLightsCustomColor;
    private ImageButton btnSoundOnOff, btnSoundVolumeMute, btnSoundPlayStop, btnSoundUsb, btnSoundSd, btnSoundBt, btnSoundAux;
    static public int mSoundVolume;
    private boolean mSoundMuteStatus = false;


    static class BtMessageOut {
        static final String BT_MESSAGE_OUT_SMALL_BUBBLES = "6", BT_MESSAGE_OUT_BIG_BUBBLES = "7", BT_MESSAGE_OUT_SMALL_AND_BIG_BUBBLES = "8",
                BT_MESSAGE_OUT_PYRAMID_BUBBLES = "9", BT_MESSAGE_OUT_PILLAR_BUBBLES = "10", BT_MESSAGE_OUT_MIXED_BUBBLES = "11", BT_MESSAGE_OUT_FADE_LIGHTS = "12",
                BT_MESSAGE_OUT_FADE_SPLIT_LIGHTS = "13", BT_MESSAGE_OUT_RAINBOW_LIGHTS = "14", BT_MESSAGE_OUT_RAINBOW_2_LIGHTS = "15", BT_MESSAGE_OUT_MIXED_LIGHTS = "16",
                BT_MESSAGE_OUT_SOUND_ON_OFF = "17", BT_MESSAGE_OUT_SOUND_VOLUME_DOWN = "18", BT_MESSAGE_OUT_SOUND_VOLUME_UP = "19", BT_MESSAGE_OUT_SOUND_MUTE = "20",
                BT_MESSAGE_OUT_SOUND_PREVIOUS = "21", BT_MESSAGE_OUT_SOUND_PLAY_PAUSE = "22", BT_MESSAGE_OUT_SOUND_NEXT = "23", BT_MESSAGE_OUT_SOUND_USB_MODE = "24",
                BT_MESSAGE_OUT_SOUND_AUX_MODE = "25", BT_MESSAGE_OUT_SOUND_BT_MODE = "26", BT_MESSAGE_OUT_SOUND_SD_MODE = "27";
    }

    private  static class BtMessageIn {
        private static int BT_MESSAGE_IN_SYSTEM_OFF = 0, BT_MESSAGE_IN_SYSTEM_ON = 1, BT_MESSAGE_IN_SLEEP_MODE_STARTED = 2,
        Bt_MESSAGE_IN_SLEEP_ACHIEVED = 3, BT_MESSAGE_IN_SLEEP_CANCELLED = 4, BT_MESSAGE_IN_MANUAL_MODE = 5,
        BT_MESSAGE_IN_SMALL_BUBBLES = 6, BT_MESSAGE_IN_BIG_BUBBLES = 7, BT_MESSAGE_IN_SMALL_AND_BIG_BUBBLES = 8,
        BT_MESSAGE_IN_PYRAMID_BUBBLES = 9, BT_MESSAGE_IN_PILLAR_BUBBLES = 10, BT_MESSAGE_IN_MIXED_BUBBLES = 11,
        BT_MESSAGE_IN_FADE_LIGHTS = 12, BT_MESSAGE_IN_FADE_SPLIT_LIGHTS = 13, BT_MESSAGE_IN_RAINBOW_LIGHTS_1 = 14,
        BT_MESSAGE_IN_RAINBOW_LIGHTS_2 = 15, BT_MESSAGE_IN_MIXED_LIGHTS = 16, BT_MESSAGE_IN_CUSTOM_LIGHTS = 17,
        BT_MESSAGE_IN_SOUND_ON = 18, BT_MESSAGE_IN_SOUND_OFF = 19, BT_MESSAGE_IN_SOUND_VOLUME_CHANGE = 20,
        BT_MESSAGE_IN_SOUND_MUTE = 21, BT_MESSAGE_IN_SOUND_UNMUTE = 22, BT_MESSAGE_IN_SOUND_PLAY = 23, BT_MESSAGE_IN_SOUND_PAUSE  = 24,
        BT_MESSAGE_IN_SOUND_USB = 25, BT_MESSAGE_IN_SOUND_AUX = 26, BT_MESSAGE_IN_SOUND_BT = 27, BT_MESSAGE_IN_SOUND_SD = 28;
    }
    ////////////////////////Variable declaration ends here////////////////////////


    int getLayoutID() {
        return R.layout.activity_remote_control_bubble_wall;
    }


    void initializeViewsAndFragments() {
        //find Widgets and initialize
        btnOnOff = findViewById(R.id.button_on_off);
        btnOnOff.setOnClickListener(this);
        btnSleep = findViewById(R.id.button_sleep);
        btnSleep.setOnClickListener(this);
        btnBubblesSmall = findViewById(R.id.button_bubbles_small);
        btnBubblesSmall.setOnClickListener(this);
        btnBubblesBig = findViewById(R.id.button_bubbles_big);
        btnBubblesBig.setOnClickListener(this);
        btnBubblesSmallPlusBig = findViewById(R.id.button_bubbles_small_plus_big);
        btnBubblesSmallPlusBig.setOnClickListener(this);
        btnBubblesPyramid = findViewById(R.id.button_bubbles_pyramid);
        btnBubblesPyramid.setOnClickListener(this);
        btnBubblesPillar = findViewById(R.id.button_bubbles_pillar);
        btnBubblesPillar.setOnClickListener(this);
        btnBubblesMix = findViewById(R.id.button_bubbles_mix);
        btnBubblesMix.setOnClickListener(this);
        btnLightsRainbow1 = findViewById(R.id.button_lights_rainbow);
        btnLightsRainbow1.setOnClickListener(this);
        btnLightsRainbow2 = findViewById(R.id.button_lights_rainbow_2);
        btnLightsRainbow2.setOnClickListener(this);
        btnLightsFadeSplit = findViewById(R.id.button_lights_fade_split);
        btnLightsFadeSplit.setOnClickListener(this);
        btnLightsFade = findViewById(R.id.button_lights_fade);
        btnLightsFade.setOnClickListener(this);
        btnLightsMix = findViewById(R.id.button_lights_mix);
        btnLightsMix.setOnClickListener(this);
        btnLightsCustomColor = findViewById(R.id.button_lights_custom);
        btnLightsCustomColor.setOnClickListener(this);
        btnSoundOnOff = findViewById(R.id.button_sound_on_off);
        btnSoundOnOff.setOnClickListener(this);
        ImageButton btnSoundVolumeDown = findViewById(R.id.button_sound_volume_down);
        btnSoundVolumeDown.setOnClickListener(this);
        ImageButton btnSoundVolumeUp = findViewById(R.id.button_sound_volume_up);
        btnSoundVolumeUp.setOnClickListener(this);
        btnSoundVolumeMute = findViewById(R.id.button_sound_mute);
        btnSoundVolumeMute.setOnClickListener(this);
        btnSoundPlayStop = findViewById(R.id.button_sound_stop_play);
        btnSoundPlayStop.setOnClickListener(this);
        ImageButton btnSoundPrevious = findViewById(R.id.button_sound_previous);
        btnSoundPrevious.setOnClickListener(this);
        ImageButton btnSoundNext = findViewById(R.id.button_sound_next);
        btnSoundNext.setOnClickListener(this);
        btnSoundUsb = findViewById(R.id.button_sound_mode_usb);
        btnSoundUsb.setOnClickListener(this);
        btnSoundSd = findViewById(R.id.button_sound_mode_sd);
        btnSoundSd.setOnClickListener(this);
        btnSoundBt = findViewById(R.id.button_sound_mode_bt);
        btnSoundBt.setOnClickListener(this);
        btnSoundAux = findViewById(R.id.button_sound_mode_aux);
        btnSoundAux.setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mCountDownTimerFragment = (CountDownTimerFragment) fragmentManager.findFragmentById(R.id.countDownTimerFragment);

        mCountDownTimer = findViewById(R.id.countDownTimerFragment);
        mCountDownTimer.setVisibility(View.INVISIBLE);
    }


    void onClickListener(View view) {
        switch (view.getId()) {

            case R.id.button_on_off: {
                if (!mArduinoPowerStatus) {
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
                    sendBtMessage(BT_MESSAGE_OUT_CANCEL_SLEEP,DIRECT_MESSAGE);
                }
                break;
            }
            case R.id.button_bubbles_small: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SMALL_BUBBLES, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_bubbles_big: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_BIG_BUBBLES ,BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_bubbles_small_plus_big: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SMALL_AND_BIG_BUBBLES, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_bubbles_pyramid: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_PYRAMID_BUBBLES, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_bubbles_pillar: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_PILLAR_BUBBLES, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_bubbles_mix: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_MIXED_BUBBLES, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_lights_rainbow: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_RAINBOW_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_lights_rainbow_2: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_RAINBOW_2_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
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
            case R.id.button_lights_mix: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_MIXED_LIGHTS, BUBBLES_OR_LIGHTS_MESSAGE);
                break;
            }
            case R.id.button_lights_custom: {
                showColorPickerDialog();
                break;
            }
            case R.id.button_sound_on_off: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_ON_OFF, DIRECT_MESSAGE);
                break;
            }
            case R.id.button_sound_volume_down: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_VOLUME_DOWN,SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_volume_up: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_VOLUME_UP, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_mute: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_MUTE, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_stop_play: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_PLAY_PAUSE, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_previous: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_PREVIOUS, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_next: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_NEXT, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_mode_usb: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_USB_MODE, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_mode_sd: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_SD_MODE, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_mode_bt: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_BT_MODE, SOUND_MESSAGE);
                break;
            }
            case R.id.button_sound_mode_aux: {
                sendBtMessage(BtMessageOut.BT_MESSAGE_OUT_SOUND_AUX_MODE, SOUND_MESSAGE);
                break;
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////




    void processBtMessage(Object messageObject, int messageType) {
        if (messageType == PROCESS_MESSAGE_TYPE_STATUS_REQUEST) {
            if (!(messageObject instanceof String)) {
                return;
            }
            String message = messageObject.toString();

            //1st character: System power: 0 = off, 1 = on
            //2nd character: Bubbles: 0 = mixed, 1 = small, 2 = big, 3 = small+big, 4 = pyramid, 5 = pillar
            //3rd character: Lights: 0 = mixed, 1 = Rainbow, 2 = Rainbow2, 3 = fade, 4 = fade split, 5 = custom
            //4th character: Sound power: 0 = off, 1 = on
            //5th character: Sound mute: 0 = not mute, 1 = mute
            //6th character: Sound play: 0 = pause, 1 = play
            //7th character: Sound mode: 0 = USB, 1 = AUX, 2 = BT, 3 = SD
            //8th-9th characters: Sound volume

            int powerStatus = Character.getNumericValue(message.charAt(2));
            int bubblesMode = Character.getNumericValue(message.charAt(3));
            int lightsMode = Character.getNumericValue(message.charAt(4));
            int soundPowerStatus = Character.getNumericValue(message.charAt(5));
            int soundMute = Character.getNumericValue(message.charAt(6));
            int soundPlay = Character.getNumericValue(message.charAt(7));
            int soundMode = Character.getNumericValue(message.charAt(8));
            int soundVolume = Integer.parseInt(message.substring(9, message.length()));

            ArrayList<Integer> messageArray = new ArrayList<>();


            if(powerStatus == 0) {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_OFF);
            } else {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SYSTEM_ON);

                switch(bubblesMode) {
                    case 0: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_MIXED_BUBBLES);
                        break;
                    }
                    case 1: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SMALL_BUBBLES);
                        break;
                    }
                    case 2: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_BIG_BUBBLES);
                        break;
                    }
                    case 3: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SMALL_AND_BIG_BUBBLES);
                        break;
                    }
                    case 4: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_PYRAMID_BUBBLES);
                        break;
                    }
                    case 5: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_PILLAR_BUBBLES);
                        break;
                    }
                }

                switch(lightsMode) {
                    case 0: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_MIXED_LIGHTS);
                        break;
                    }
                    case 1: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_RAINBOW_LIGHTS_1);
                        break;
                    }
                    case 2: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_RAINBOW_LIGHTS_2);
                        break;
                    }
                    case 3: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS);
                        break;
                    }
                    case 4: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_FADE_SPLIT_LIGHTS);
                        break;
                    }
                    case 5: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS);
                        break;
                    }
                }
            }

            if(soundPowerStatus == 0) {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_OFF);
            } else {
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_ON);

                if(soundMute == 0) {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_UNMUTE);
                } else {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_MUTE);
                }

                if(soundPlay == 0) {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_PAUSE);
                } else {
                    messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_PLAY);
                }

                switch(soundMode) {
                    case 0: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_USB);
                        break;
                    }
                    case 1: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_AUX);
                        break;
                    }
                    case 2: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_BT);
                        break;
                    }
                    case 3: {
                        messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_SD);
                        break;
                    }
                }

                mSoundVolume = soundVolume;
                messageArray.add(BtMessageIn.BT_MESSAGE_IN_SOUND_VOLUME_CHANGE);
            }

            processMessageArray(messageArray);

        } else if (messageType == PROCESS_MESSAGE_TYPE_CLASS_SPECIFIC_MESSAGE) {
            if (!(messageObject instanceof String)) {
                return;
            }
            String message = messageObject.toString();

            final String SOUND_LEVEL = "soundLevel";

            if (message.contains(SOUND_LEVEL)) {
                mSoundVolume = Integer.parseInt(message.substring(10, message.length()));
                setSoundVolume(mSoundVolume);
            }

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

                if(btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))){
                    sendBtMessage(BT_MESSAGE_OUT_CANCEL_SLEEP, DIRECT_MESSAGE);
                } else {
                    mCountDownTimer.setVisibility(View.INVISIBLE);
                    mCountDownTimerFragment.stopCountDownTimer();
                    btnSleep.setText(R.string.sleep);
                }

                mArduinoPowerStatus = false;
                BubbleButtonsBackgroundUnpressed();
                LightsButtonsBackgroundUnpressed();
                btnOnOff.setBackgroundResource(R.drawable.button_background_gradient_green_black_border);
                btnOnOff.setText(R.string.On);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting Button to On");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SYSTEM_ON) {
                mArduinoPowerStatus = true;

                btnOnOff.setBackgroundResource(R.drawable.button_background_gradient_red_black_border);
                btnOnOff.setText(R.string.Off);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting Button to Off");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_MODE_STARTED) {
                btnSleep.setText(R.string.cancel_sleep);
                mCountDownTimer.setVisibility(View.VISIBLE);
                mCountDownTimerFragment.StartTimer();
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting mCountDownTimer instead of sleep button");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SLEEP_CANCELLED) {
                if (btnSleep.getText().toString().equals(getString(R.string.cancel_sleep))) {
                    mCountDownTimer.setVisibility(View.INVISIBLE);
                    mCountDownTimerFragment.stopCountDownTimer();
                    btnSleep.setText(R.string.sleep);
                    CustomLogger.log("RemoteControlBubbleWall: bReceiver: Sleep cancelled");
                }
            } else if(msg == BtMessageIn.Bt_MESSAGE_IN_SLEEP_ACHIEVED) {
                mCountDownTimer.setVisibility(View.INVISIBLE);
                mCountDownTimerFragment.stopCountDownTimer();
                btnSleep.setText(R.string.sleep);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting Sleep button instead of mCountDownTimer");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MANUAL_MODE) {
                Toast.makeText(this, "Remote operation of Bubble Wall is restricted when manual mode is turned on.", Toast.LENGTH_LONG).show();
                finishActivity();
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SMALL_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesSmall.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting small bubbles button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_BIG_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesBig.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting big bubbles button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SMALL_AND_BIG_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesSmallPlusBig.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting small and big bubbles to button pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_PYRAMID_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesPyramid.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting pyramid bubbles button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_PILLAR_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesPillar.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting pillar bubbles button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MIXED_BUBBLES) {
                BubbleButtonsBackgroundUnpressed();
                btnBubblesMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting mixed bubbles button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_RAINBOW_LIGHTS_1) {
                LightsButtonsBackgroundUnpressed();
                btnLightsRainbow1.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting rainbow lights 1 button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_RAINBOW_LIGHTS_2) {
                LightsButtonsBackgroundUnpressed();
                btnLightsRainbow2.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting rainbow lights 2 button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_FADE_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting fade lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_FADE_SPLIT_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsFadeSplit.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting fade split lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_MIXED_LIGHTS) {
                LightsButtonsBackgroundUnpressed();
                btnLightsMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_red_border);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting mixed lights button to pressed");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_CUSTOM_LIGHTS) {
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting custom lights button to pressed");
                LightsButtonsBackgroundUnpressed();
                btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_red_border);
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_ON) {
                mSoundSystemPowerStatus = true;

                btnSoundOnOff.setImageResource(R.drawable.icon_sound_off);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound power button to Off");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_OFF) {
                mSoundSystemPowerStatus = false;
                SoundModeButtonsBackgroundUnpressed();
                btnSoundOnOff.setImageResource(R.drawable.icon_sound_on);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound power button to On");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_VOLUME_CHANGE || msg == BtMessageIn.BT_MESSAGE_IN_SOUND_MUTE || msg == BtMessageIn.BT_MESSAGE_IN_SOUND_UNMUTE) {
                if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_MUTE) {
                    mSoundMuteStatus = true;
                } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_UNMUTE) {
                    mSoundMuteStatus = false;
                }

                if(mSoundMuteStatus) {
                    btnSoundVolumeMute.setImageResource(R.drawable.icon_sound_mute);
                    CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to mute icon");
                } else {
                    setSoundVolume(mSoundVolume);
                }
            }
            else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_PLAY) {
                btnSoundPlayStop.setImageResource(R.drawable.icon_sound_pause);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound play pause/button to pause");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_PAUSE) {
                btnSoundPlayStop.setImageResource(R.drawable.icon_sound_play);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound play/pause button to play");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_USB) {
                SoundModeButtonsBackgroundUnpressed();
                btnSoundUsb.setBackgroundResource(R.drawable.background_border_red);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound mode to USB");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_AUX) {
                SoundModeButtonsBackgroundUnpressed();
                btnSoundAux.setBackgroundResource(R.drawable.background_border_red);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound mode to Aux");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_BT) {
                SoundModeButtonsBackgroundUnpressed();
                btnSoundBt.setBackgroundResource(R.drawable.background_border_red);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound mode to BT");
            } else if(msg == BtMessageIn.BT_MESSAGE_IN_SOUND_SD) {
                SoundModeButtonsBackgroundUnpressed();
                btnSoundSd.setBackgroundResource(R.drawable.background_border_red);
                CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound mode to SD");
            }
        }
    }


    void setSoundVolume(int soundVolume) {
        if(soundVolume == 0) {
            btnSoundVolumeMute.setImageResource(R.drawable.icon_volume_level_0);
            CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to sound level 0");
        } else if(soundVolume >0 && soundVolume < 13) {
            btnSoundVolumeMute.setImageResource(R.drawable.icon_volume_level_1);
            CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to sound level 1");
        } else if(soundVolume >= 13 && soundVolume < 23) {
            btnSoundVolumeMute.setImageResource(R.drawable.icon_volume_level_2);
            CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to sound level 2");
        } else if(soundVolume >= 23 && soundVolume < 31) {
            btnSoundVolumeMute.setImageResource(R.drawable.icon_volume_level_3);
            CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to sound level 3");
        } else if(soundVolume == 31) {
            btnSoundVolumeMute.setImageResource(R.drawable.icon_volume_level_max);
            CustomLogger.log("RemoteControlBubbleWall: bReceiver: Setting sound level to sound level max");
        }
    }


    /** resets the background images of all bubble related buttons */
    private void BubbleButtonsBackgroundUnpressed() {
        btnBubblesSmall.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnBubblesBig.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnBubblesSmallPlusBig.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnBubblesPyramid.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnBubblesPillar.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnBubblesMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_black_border);
    }


    /** resets the background images of all light related buttons */
    private void LightsButtonsBackgroundUnpressed() {
        btnLightsRainbow1.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsRainbow2.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsFade.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsFadeSplit.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
        btnLightsMix.setBackgroundResource(R.drawable.button_background_gradient_color_violet_black_border);
        btnLightsCustomColor.setBackgroundResource(R.drawable.button_background_gradient_light_blue_black_border);
    }


    /** resets the background images of sound mode related buttons */
    private void SoundModeButtonsBackgroundUnpressed() {
        btnSoundUsb.setBackgroundResource(android.R.color.transparent);
        btnSoundAux.setBackgroundResource(android.R.color.transparent);
        btnSoundBt.setBackgroundResource(android.R.color.transparent);
        btnSoundSd.setBackgroundResource(android.R.color.transparent);
    }
}
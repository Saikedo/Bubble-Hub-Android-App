package net.california_design.bubble_hub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Saik on 12/20/2017.
 * Hello
 */

public abstract class RemoteControl extends AppCompatActivity implements View.OnClickListener, BluetoothConnection.BluetoothConnectionInterface {

    ////////////////////////Class global variables////////////////////////
    int mCustomColor = 0xFFFF0000; //red
    ProgressDialog mProgressDialog;
    View mCountDownTimer;
    CountDownTimerFragment mCountDownTimerFragment;
    Handler mCustomColorSenderHandler = new Handler();
    boolean mArduinoPowerStatus = false;
    int hardwareVersionMajor;
    int hardwareVersionMinor;
    static final int DIRECT_MESSAGE = 0;
    static final int BUBBLES_OR_LIGHTS_MESSAGE = 1;
    static final int SOUND_MESSAGE = 2;
    BluetoothConnection bluetoothConnection;
    Boolean mFinishActivityCalled = false;
    boolean mSoundSystemPowerStatus = false;
    final String BT_MESSAGE_OUT_STATUS_REQUEST  = "0";
    final String BT_MESSAGE_OUT_VERSION_REQUEST = "1";
    final String BT_MESSAGE_OUT_SYSTEM_OFF = "2";
    final String BT_MESSAGE_OUT_SYSTEM_ON = "3";
    final String BT_MESSAGE_OUT_SYSTEM_SLEEP = "4";
    final String BT_MESSAGE_OUT_CANCEL_SLEEP = "5";


    final int PROCESS_MESSAGE_TYPE_STATUS_REQUEST = 0;
    final int PROCESS_MESSAGE_TYPE_CLASS_SPECIFIC_MESSAGE = 1;
    final int PROCESS_MESSAGE_TYPE_MESSAGE_ARRAY = 2;

    /////////////////////////////onCreateMethod////////////////////////
    abstract int getLayoutID();


    abstract void initializeViewsAndFragments();



    private String getBubbleDeviceType() {
        String BubbleDeviceType;
        switch (this.getLayoutID()) {
            case R.layout.activity_remote_control_bubble_wall:
                BubbleDeviceType = "Bubble Wall";
                break;
            case R.layout.activity_remote_control_bubble_pillar:
                BubbleDeviceType = "Bubble Pillar";
                break;
            case R.layout.activity_remote_control_bubble_centerpiece:
                BubbleDeviceType = "Bubble Centerpiece";
                break;
            default:
                BubbleDeviceType = "Unknown Device";
                break;
        }
        return BubbleDeviceType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Should never happen
        if (getIntent().getStringExtra(MainActivity.EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS) == null) {
            Toast.makeText(this, "Unable to access the Bluetooth device.", Toast.LENGTH_LONG).show();
            finishActivity();
        } else {
            String macAddress = getIntent().getStringExtra(MainActivity.EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS);

            // Establish connection with Bluetooth device
            bluetoothConnection = new BluetoothConnection(this);
            bluetoothConnection.connect(macAddress);

            initializeViewsAndFragments();

            mProgressDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
            mProgressDialog.setTitle("Connecting to " + getBubbleDeviceType());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }
    /////////////////////////////onCreateMethod////////////////////////


    /////////////////////OnClick///////////////////////////////////////////////


    public void onClick(View v) {
        onClickListener(v);
    }
    abstract void onClickListener(View v);
    /////////////////////OnClick End///////////////////////////////////////////////


    ///////////////////Options Menu//////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bubble_activity_toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            CustomLogger.log("RemoteControl: onOptionsItemSelected: we are going back to main menu because action bar back button was pressed");
            finishActivity();
        } else if (item.getItemId() == R.id.bubble_activity_action_bar_menu_help_id) {
            Intent intent = new Intent(this, HelpBubbleControlActivity.class);

            switch (this.getLayoutID()) {
                case R.layout.activity_remote_control_bubble_wall:
                    intent.putExtra(HelpBubbleControlActivity.HELP_PAGE_CONTENTS, HelpBubbleControlActivity.HELP_PAGE_FOR_BUBBLE_WALL);
                    break;
                case R.layout.activity_remote_control_bubble_pillar:
                    intent.putExtra(HelpBubbleControlActivity.HELP_PAGE_CONTENTS, HelpBubbleControlActivity.HELP_PAGE_FOR_BUBBLE_PILLAR);
                    break;
                case R.layout.activity_remote_control_bubble_centerpiece:
                    intent.putExtra(HelpBubbleControlActivity.HELP_PAGE_CONTENTS, HelpBubbleControlActivity.HELP_PAGE_FOR_BUBBLE_CENTERPIECE);
                    break;
                default:
                    intent.putExtra(HelpBubbleControlActivity.HELP_PAGE_CONTENTS, "Unknown Device");
                    break;
            }
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        CustomLogger.log("RemoteControl: onBackPressed: Go back to previous activity since back button is pressed.");
        finishActivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //TODO maybe put this back ?
        //Note that disconnectFromBt is called before every finish() call and once again in onDestroy.
        //The reason behind calling it again in here is to 100% make sure that we disconnected from bt.
        //The reason I call disconnectFromBt before finish is because it makes the bt disconnect faster
        //than just waiting for onDestroy to be called.
        // finishActivity();
    }
    /////////////////////Options Menu End//////////////////////////////////////////


    //////////////////////ReceiveMessageFromBluetoothConnection//////////////////////

    /**
     * Receives Bluetooth state messages from BluetoothConnection class.
     */
    public void receiveMessageFromBluetoothConnection(String message) {
        switch (message) {
            case BluetoothConnection.CONNECTING: {
                CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: CONNECTING");
                mProgressDialog.setMessage("Establishing connection. Please Wait...");
                break;
            }
            case BluetoothConnection.CONNECTED: {
                CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: CONNECTED");
                mProgressDialog.setMessage("Connection Established, subscribing to characteristics. Please Wait...");
                break;
            }
            case BluetoothConnection.DISCONNECTING: {
                if (!mFinishActivityCalled) {
                    CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: DISCONNECTING", 'e');
                }
                break;
            }
            case BluetoothConnection.DISCONNECTED: {
                if (!mFinishActivityCalled) {
                    mFinishActivityCalled = true;
                    CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: DISCONNECTED", 'e');
                    Toast.makeText(this, "Lost connection with " + getBubbleDeviceType() + ". Please try again", Toast.LENGTH_LONG).show();
                    finishActivity();
                }
                break;
            }
            case BluetoothConnection.CONNECTION_FAILED: {
                if (!mFinishActivityCalled) {
                    mFinishActivityCalled = true;
                    CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: CONNECTION_FAILED", 'e');
                    Toast.makeText(this, "Failed to connect with " + getBubbleDeviceType() + ". Please try again", Toast.LENGTH_LONG).show();
                    finishActivity();
                }
                break;
            }
            case BluetoothConnection.CONNECTION_LOST: {
                if (!mFinishActivityCalled) {
                    mFinishActivityCalled = true;
                    CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: CONNECTION_LOST", 'e');
                    Toast.makeText(this, "Lost connection with " + getBubbleDeviceType() + ". Please try again", Toast.LENGTH_LONG).show();
                    finishActivity();
                }
                break;
            }
            case BluetoothConnection.SUBSCRIBED_TO_CHARACTERISTICS: {
                CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: SUBSCRIBED_TO_CHARACTERISTICS");
                mProgressDialog.setMessage("Subscribed to characteristics, setting up notifications. Please Wait...");
                break;
            }
            case BluetoothConnection.NOTIFICATION_SETUP_SUCCESS: {
                CustomLogger.log("RemoteControl: receiveMessageFromBluetoothConnection: NOTIFICATION_SETUP_SUCCESS");

                bluetoothConnection.write(BT_MESSAGE_OUT_VERSION_REQUEST);
                bluetoothConnection.write(BT_MESSAGE_OUT_STATUS_REQUEST);

                mProgressDialog.dismiss();
                break;
            }
            case BluetoothConnection.NOTIFICATION_SETUP_FAILED: {
                if (!mFinishActivityCalled) {
                    mFinishActivityCalled = true;
                    CustomLogger.log("RemoteControl: broadcastReceiver: NOTIFICATION_SETUP_FAILED", 'e');
                    Toast.makeText(this, "Failed to setup notifications. Please try again", Toast.LENGTH_LONG).show();
                    finishActivity();
                }
                break;
            }
            case BluetoothConnection.WRITE_FAILED: {
                CustomLogger.log("RemoteControl: broadcastReceiver: WRITE FAILED", 'e');
                Toast.makeText(this, "Failed to send message. Please try again", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    /**
     * Receives data from BluetoothConnection class and forwards it to processIncomingBtMessage method for processing
     */
    public void receiveDataFromBluetoothConnection(String data) {
        String garbageValue = "\uFFFD";

        // Sometimes I receive garbage value that shows up as unicode replacement character. If we receive garbage
        // We just try to get the correct version number again as well as requesting status update.
        if (data.contains(garbageValue)) {
            sendBtMessage(BT_MESSAGE_OUT_VERSION_REQUEST, DIRECT_MESSAGE);
            sendBtMessage(BT_MESSAGE_OUT_STATUS_REQUEST, DIRECT_MESSAGE);
            Toast.makeText(this, "Received Garbage message", Toast.LENGTH_LONG).show();
        } else {
            processIncomingBtMessage(data);
        }
    }


    /**
     * handles all the incoming bluetooth data.
     * Data processing have several different outcomes. Almost in all cases the data
     * represents the state that the arduino is in. Since we do not change the UI at the moment
     * when the user interacts with it, we need the arduino to indicate that the state change occurred
     * so that we can update the UI. This is done to make sure that all of the bluetooth messages
     * sent by Android are received and correctly processed by the Arduino device.
     */
    public void processIncomingBtMessage(String incomingMessage) {
        final String START_SYMBOL = "*";
        final String END_SYMBOL = "|";
        final String BRIGHTNESS_MESSAGE = "brightness";
        final String HARDWARE_VERSION_NUMBER = "VerNum";
        final String STATUS_REQUEST = "SR";
        final String SOUND_LEVEL = "soundLevel";

        CustomLogger.log("RemoteControl: bReceiver: Received message " + incomingMessage);

        int start = incomingMessage.indexOf(START_SYMBOL);
        int end = incomingMessage.indexOf(END_SYMBOL);
        int extraStartSymbol = incomingMessage.substring(start + 1, incomingMessage.length()).indexOf(START_SYMBOL);

        if (start == -1 || end == -1 || start > end || (extraStartSymbol != -1 && extraStartSymbol < end)) {
            return;
        } else {
            incomingMessage = incomingMessage.substring(start + 1, end);
        }


        ArrayList<Integer> messageArray = new ArrayList<>();

        if(incomingMessage.length() > 2 && incomingMessage.substring(0, 2).equals(STATUS_REQUEST)) {
            processBtMessage(incomingMessage, PROCESS_MESSAGE_TYPE_STATUS_REQUEST);
        } else if(incomingMessage.contains(HARDWARE_VERSION_NUMBER)) {
            hardwareVersionMajor = Integer.parseInt(incomingMessage.substring(HARDWARE_VERSION_NUMBER.length(),incomingMessage.indexOf('.')));
            hardwareVersionMinor = Integer.parseInt(incomingMessage.substring(incomingMessage.indexOf('.') + 1,incomingMessage.length()));

            CustomLogger.log("RemoteControl: processIncomingBtMessage: Current Hardware version is : Ver " + hardwareVersionMajor + "." + hardwareVersionMinor);
            return;
        } else if(incomingMessage.contains(BRIGHTNESS_MESSAGE) || incomingMessage.contains(SOUND_LEVEL)) {
            processBtMessage(incomingMessage, PROCESS_MESSAGE_TYPE_CLASS_SPECIFIC_MESSAGE);
            return;
        } else {
            try{
                messageArray.add(Integer.parseInt(incomingMessage));
            } catch(java.lang.NumberFormatException e) {
                CustomLogger.log("RemoteControl: processIncomingBtMessage : Error occurred when trying to parse incoming message to int. Incoming message was " + incomingMessage + " and we got error message " + e);
                return;
            }
        }

        processBtMessage(messageArray, PROCESS_MESSAGE_TYPE_MESSAGE_ARRAY);
    }
    abstract void processBtMessage(Object messageObject, int messageType);



    void sendBtMessage(String message, int messageType) {
        if (messageType == DIRECT_MESSAGE) {
            bluetoothConnection.write(message);
        } else if (messageType == BUBBLES_OR_LIGHTS_MESSAGE) {
            bubblesOrLightsMessageSender(message);
        } else if(messageType == SOUND_MESSAGE) {
            soundMessageSender(message);
        }
    }


    /** handles the sending of light related messages
     * Since the system have to be turned on before sending a light related message can be send
     * this method checks if the system is on before sending the message and if the system is not on,
     * we first ask the user if they want to turn on the system and if they agree, we send a message
     * to turn on the system followed by previously requested light message.
     */
    private void bubblesOrLightsMessageSender(final String message) {
        if(!mArduinoPowerStatus) {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            a_builder.setMessage("Do you want to turn on the " + getBubbleDeviceType() + " ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",(dialogInterface, which) -> {

                        bluetoothConnection.write(BT_MESSAGE_OUT_SYSTEM_ON);

                        if(!message.equals(BT_MESSAGE_OUT_SYSTEM_SLEEP)) {
                            bluetoothConnection.write(message);
                        }

                        dialogInterface.cancel();
                    })
                    .setNegativeButton("No",(dialogInterface, which) ->
                            dialogInterface.cancel()
                    );
            AlertDialog alert = a_builder.create();
            alert.setTitle(getBubbleDeviceType() + " Is Turned Off");
            alert.show();
        } else {
            bluetoothConnection.write(message);
        }
    }




    /** Handles the sending of sound related messages. Please see comment on bubbleOrLightsMessageSender for more info */
    private void soundMessageSender(final String message)
    {
        if(!mSoundSystemPowerStatus) {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            a_builder.setMessage("Do you want to turn on the speaker ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",  (dialogInterface, which) -> {

                        bluetoothConnection.write(RemoteControlBubbleWall.BtMessageOut.BT_MESSAGE_OUT_SOUND_ON_OFF);
                        dialogInterface.cancel();
                    })
                    .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.cancel() );
            AlertDialog alert = a_builder.create();
            alert.setTitle("Speaker is not turned on!");
            alert.show();
        } else {
            bluetoothConnection.write(message);
        }
    }


    /**Opens the dialog activity for choosing the custom colors and handles all the callbacks for that.*/
    void showColorPickerDialog() {

        View v = View.inflate(this, R.layout.color_wheel_custom_layout, null);

        ColorPicker cp = v.findViewById(R.id.colorPicker);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, mCustomColor, cp, v, color -> {{
            mCustomColor = color;

            mCustomColorSenderHandler.removeCallbacksAndMessages(null);

            //Since we don't want to send too much bluetooth information too fast, I implemented this handler
            //that only sends the data to Arduino if the particular color has been chosen for more than 100ms.
            mCustomColorSenderHandler.postDelayed( () -> {{
                //Arduino checks the 25th bit and if it is 1, it processes the message as custom color
                //But first we clear the 8 most significant bits since they were used for transparency and we do not use them
                int customColorWithSetFlag = (mCustomColor & ~((~0 << 24))) | (1 << 24);
                sendBtMessage(Integer.toString(customColorWithSetFlag),DIRECT_MESSAGE);
            }
            }, 100);
        }

        });
        colorPickerDialog.setCanceledOnTouchOutside(false);
        colorPickerDialog.setView(v);
        colorPickerDialog.show();
    }



    void finishActivity() {
        mFinishActivityCalled = true;
        mProgressDialog.dismiss();
        bluetoothConnection.triggerDisconnect();
        finish();
    }
}

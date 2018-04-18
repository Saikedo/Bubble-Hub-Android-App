package net.california_design.bubble_hub;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import rx.Subscription;


// TODO: (Secondary Goal) Try to shorten some of the methods
// TODO: (Secondary Goal) Inherit from RemoteControl class for all 3 remote controls that we have right now

/**
 * Handles the displaying of discovered devices and the navigation to other activities.
 *
 * Navigation to RemoteControlBubblePillar, RemoteControlBubbleWall RemoteControlBubbleCenterpiece
 * is done when the user clicks on appropriate bluetooth device from the list.
 *
 * Navigation to FeedbackPopUpActivity is done when the user clicks on feedback menu button
 * on the toolbar.
 */
public class MainActivity extends AppCompatActivity {
    ////////////////////////Class global variables////////////////////////
    static final String EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS = "com.california_design.bubble_hub.california_design.EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS";
    private ListView mDeviceList;
    DeviceListAdapter mDeviceListAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Handler mCheckIfBtDeviceTimerExpiredHandler = new Handler();
    private LinearLayout mLocationPermissionWarningLinearLayout, mTopTextLinearLayout;
    private final UUID BLUETOOTH_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private class BtDeviceObject {
        RxBleDevice bluetoothDevice;
        long timeSinceLastUpdate;

        BtDeviceObject(RxBleDevice btDevice, long time) {
            bluetoothDevice = btDevice;
            timeSinceLastUpdate = time;
        }
    }
    private List<BtDeviceObject> mBtDeviceObjectList = new ArrayList<>();

    //I use mBtDeviceAddress list to be able to quickly find if a given bluetooth
    //device is contained in mBtDeviceObjectList. Instead of storing the whole bt
    //device, I just store the unique addresses and then use .contains and
    // .indexOf methods on mBtDeviceAddresses to get the desired info about
    //mBtDeviceObjectList members.
    private List<String> mBtDeviceAddress = new ArrayList<>();
    private Subscription mScanSubscription;
    ////////////////////////Variable declaration ends here////////////////////////



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceList = findViewById(R.id.listView);

        Button locationPermissionButton = findViewById(R.id.main_location_permission_button);
        locationPermissionButton.setOnClickListener( view -> {
                if(!haveLocationPermissions()){
                    requestLocationPermission(MainActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        );

        mLocationPermissionWarningLinearLayout = findViewById(R.id.main_linear_layout_location_permission_information_id);
        mTopTextLinearLayout = findViewById(R.id.main_linear_layout_top_text);

        InitializeDeviceListAdapter();

        //if device does not have BT capabilities, abort the app
        if(BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(getApplicationContext(), "Failed to access the phone's Bluetooth device.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkLocationAndBluetoothPermissions();
    }



    @Override
    protected void onPause() {
        super.onPause();
        stopScanningForDevices();
        mCheckIfBtDeviceTimerExpiredHandler.removeCallbacksAndMessages(null);
    }



    @Override
    public void onResume() {
        super.onResume();
        startScanningForDevices();
        mCheckIfBtDeviceTimerExpiredHandler.postDelayed(checkIfBtDeviceTimerExpired, 500);
    }



    @Override
    protected void onStart() {
        super.onStart();
        mBtDeviceObjectList.clear();
        mBtDeviceAddress.clear();
        mDeviceListAdapter.notifyDataSetChanged();
        CustomLogger.log("MainActivity: onStart: Clearing listOfBtDevices and updating the list");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanningForDevices();
        mCheckIfBtDeviceTimerExpiredHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_toolbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.main_activity_action_bar_menu_feedback_id){
            Intent intent = new Intent(MainActivity.this, FeedbackPopUpActivity.class);
            startActivity(intent);
        } else if(item.getItemId() == R.id.main_activity_action_bar_menu_help_id) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Receives btDevice that was discovered by the DiscoverBtDevices class.
     * If the btDevice is absent from mBtDeviceObjectList(Either first time discovery or timed out),
     * btDevice is added to the list. If the btDevice already exists in the list, the timer of
     * that btDevice is refreshed to the current time.
     */
    private void subscribeToScanSubscription() {
        mScanSubscription = BluetoothConnection.getBleClient(this).scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),

                new ScanFilter.Builder()
                        .setServiceUuid(new ParcelUuid(BLUETOOTH_SERVICE_UUID))
                        .build()
        )
                .subscribe(
                        scanResult -> {
                            // Process scan result here.
                            RxBleDevice rxBleDevice = scanResult.getBleDevice();

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> {
                                    if(!mBtDeviceAddress.contains(rxBleDevice.getMacAddress())) {
                                        CustomLogger.log("MainActivity: mLeScanCallback: Discovered device" + rxBleDevice.getName());
                                        mBtDeviceObjectList.add(new BtDeviceObject(rxBleDevice, new Date().getTime()));
                                        mBtDeviceAddress.add(rxBleDevice.getMacAddress());
                                        mDeviceListAdapter.notifyDataSetChanged();
                                    } else {
                                        int indexOfBtDevice = mBtDeviceAddress.indexOf(rxBleDevice.getMacAddress());
                                        if(indexOfBtDevice != -1) {
                                            mBtDeviceObjectList.get(indexOfBtDevice).timeSinceLastUpdate = new Date().getTime();
                                        }
                                    }
                                }
                            );


                        },
                        throwable -> {
                            // Handle an error here.
                            CustomLogger.log("MainActivity: subscribeToScanSubscription: Failed to subscribe. throwable " + throwable.getMessage(), 'e');

                        }
                );
    }

    /** Checks the timestamps of all btDevices in btDeviceList once in 500ms.
     * If the timestamp is 2s or older, the btDevice is removed from the list.
     */
    private Runnable checkIfBtDeviceTimerExpired = new Runnable() {
        @Override
        public void run() {
            //Note: Not using iterators here gave me a  java.util.ConcurrentModificationException from time to time and crashed the application
            for (ListIterator<BtDeviceObject> btDeviceListIterator = mBtDeviceObjectList.listIterator(); btDeviceListIterator.hasNext();) {
                BtDeviceObject btDeviceObject = btDeviceListIterator.next();
                if(new Date().getTime() - btDeviceObject.timeSinceLastUpdate  > 2000){
                    btDeviceListIterator.remove();
                    mBtDeviceAddress.remove(btDeviceObject.bluetoothDevice.getMacAddress());
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }

        mCheckIfBtDeviceTimerExpiredHandler.postDelayed(this, 500);
        }
    };


    /** Checks if all the appropriate permissions are granted for the app to operate correctly.
     *  1. Checks to see if the device have BLE support. If the device does not have BLE support,
     *  app is terminated since we cannot operate without BLE support.
     *
     *  2. Checks if bluetooth is enabled and if not, asks the user to turn it on.
     *
     *  3. For devices with API >= 23, BLE requires location permissions to operate. So for all
     *  devices with API >= 23, we check if the appropriate permissions are granted and if not,
     *  we ask the user to grant the permissions.
     */
    private void checkLocationAndBluetoothPermissions() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This phone does not support Bluetooth 4.0. Bubble Hub Application requires Bluetooth 4.0 in order to operate.", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) { //if BT is present but is turned off
            CustomLogger.log("MainActivity: onCreate: Ask user to turn on BT.");

            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (!haveLocationPermissions()) {
                mDeviceList.setVisibility(View.GONE);
                mTopTextLinearLayout.setVisibility(View.GONE);
                mLocationPermissionWarningLinearLayout.setVisibility(View.VISIBLE);
                CustomLogger.log("MainActivity: onCreate: missing location permissions");
                requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                CustomLogger.log("MainActivity: onCreate: got location permissions");
                mLocationPermissionWarningLinearLayout.setVisibility(View.GONE);
                mDeviceList.setVisibility(View.VISIBLE);
                mTopTextLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    /** Returns true if we have both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION. Returns false otherwise. */
    public boolean haveLocationPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }


    /** Asks the user to grant ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions */
    public void requestLocationPermission(final Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDeviceList.setVisibility(View.VISIBLE);
                    mTopTextLinearLayout.setVisibility(View.VISIBLE);
                    mLocationPermissionWarningLinearLayout.setVisibility(View.GONE);
                } else {
                    mDeviceList.setVisibility(View.GONE);
                    mTopTextLinearLayout.setVisibility(View.GONE);
                    mLocationPermissionWarningLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void startScanningForDevices() {
        mBtDeviceObjectList.clear();
        mBtDeviceAddress.clear();
        mDeviceListAdapter.notifyDataSetChanged();

        subscribeToScanSubscription();

        CustomLogger.log("MainActivity startScanningForDevices: Starting to scan for devices");
    }



    private void stopScanningForDevices() {
        if(BluetoothAdapter.getDefaultAdapter() != null){
            mScanSubscription.unsubscribe();
        }


        mBtDeviceObjectList.clear();
        mBtDeviceAddress.clear();
        mDeviceListAdapter.notifyDataSetChanged();

        CustomLogger.log("MainActivity: startScanningForDevices: Stop scanning");
    }




    private void InitializeDeviceListAdapter() {
        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.main_screen_listview_layout, mBtDeviceObjectList);
        mDeviceList.setAdapter(mDeviceListAdapter);
        mDeviceList.setOnItemClickListener(myListClickListener);
    }


    /**
     * receives message that the item on the list was pressed.
     * If the item corresponds to BubbleWall bluetooth device, RemoteControlBubbleWall activity is started.
     * If the item corresponds to BubblePillar bluetooth device, RemoteControlBubblePillar activity is started.
     */
    private AdapterView.OnItemClickListener myListClickListener = (adapterView, view, position, argument) ->
    {

        RxBleDevice rxBleDevice = mBtDeviceObjectList.get(position).bluetoothDevice;
         if(rxBleDevice.getName() != null) {

             if(rxBleDevice.getName().contains("BubbleWall")) {
                 CustomLogger.log("MainActivity: bReceiver: Going to wall menu");
                 Intent startActivityIntent = new Intent(MainActivity.this, RemoteControlBubbleWall.class);
                 startActivityIntent.putExtra(EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS , rxBleDevice.getMacAddress());
                 startActivity(startActivityIntent);
             } else if(rxBleDevice.getName().contains("BubblePillar")) {
                 CustomLogger.log("MainActivity: bReceiver: Going to pillar menu");
                 Intent startActivityIntent = new Intent(MainActivity.this, RemoteControlBubblePillar.class);
                 startActivityIntent.putExtra(EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS , rxBleDevice.getMacAddress());
                 startActivity(startActivityIntent);
             } else if(rxBleDevice.getName().contains("BubbleCenCon")) {
                 CustomLogger.log("MainActivity: bReceiver: Going to centerpiece control menu");
                 Intent startActivityIntent = new Intent(MainActivity.this, RemoteControlBubbleCenterpiece.class);
                 startActivityIntent.putExtra(EXTRA_BLUETOOTH_DEVICE_MAC_ADDRESS , rxBleDevice.getMacAddress());
                 startActivity(startActivityIntent);
             }
         } else {
             CustomLogger.log("MainActivity: AdapterView.OnItemCLickListener: rxBleDevice name was null", 'e');
         }
    };


    /** Handles the bluetooth device list aspects. */
    private class DeviceListAdapter extends ArrayAdapter<BtDeviceObject> {
        private List<BtDeviceObject> deviceList;
        private int resource;
        private LayoutInflater inflater;

        DeviceListAdapter(@NonNull Context context, @LayoutRes int resource, List<BtDeviceObject> btDeviceObjectList) {
            super(context, resource, btDeviceObjectList);
            deviceList = btDeviceObjectList;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null || deviceList.get(position).bluetoothDevice.getName() == null) {
                convertView = inflater.inflate(resource, null);
            }


            if(deviceList.get(position).bluetoothDevice.getName() != null) {
                ImageView listImage = convertView.findViewById(R.id.main_screen_list_view_layout_list_image);
                TextView listText =  convertView.findViewById(R.id.main_screen_list_view_layout_list_text);

                if(deviceList.get(position).bluetoothDevice.getName().contains("Wall")) {
                    if(deviceList.get(position).bluetoothDevice.getName().contains("1")) {
                        listImage.setImageResource(R.drawable.icon_bubble_wall_1);
                        listText.setText(getString(R.string.Bubble_Wall,  1));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("2")) {
                        listImage.setImageResource(R.drawable.icon_bubble_wall_2);
                        listText.setText(getString(R.string.Bubble_Wall,  2));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("3")) {
                        listImage.setImageResource(R.drawable.icon_bubble_wall_3);
                        listText.setText(getString(R.string.Bubble_Wall,  3));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("4")) {
                        listImage.setImageResource(R.drawable.icon_bubble_wall_4);
                        listText.setText(getString(R.string.Bubble_Wall,  4));
                    }

                }
                else if(deviceList.get(position).bluetoothDevice.getName().contains("Pillar")) {
                    if(deviceList.get(position).bluetoothDevice.getName().contains("1")) {
                        listImage.setImageResource(R.drawable.icon_bubble_pillar_1);
                        listText.setText(getString(R.string.Bubble_Pillar,  1));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("2")) {
                        listImage.setImageResource(R.drawable.icon_bubble_pillar_2);
                        listText.setText(getString(R.string.Bubble_Pillar,  2));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("3")) {
                        listImage.setImageResource(R.drawable.icon_bubble_pillar_3);
                        listText.setText(getString(R.string.Bubble_Pillar,  3));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("4")) {
                        listImage.setImageResource(R.drawable.icon_bubble_pillar_4);
                        listText.setText(getString(R.string.Bubble_Pillar,  4));
                    }
                }
                else if(deviceList.get(position).bluetoothDevice.getName().contains("CenCon")) {
                    if(deviceList.get(position).bluetoothDevice.getName().contains("1")) {
                        listImage.setImageResource(R.drawable.icon_centerpiece_1);
                        listText.setText(getString(R.string.centerpiece,  1));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("2")) {
                        listImage.setImageResource(R.drawable.icon_centerpiece_2);
                        listText.setText(getString(R.string.centerpiece,  2));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("3")) {
                        listImage.setImageResource(R.drawable.icon_centerpiece_3);
                        listText.setText(getString(R.string.centerpiece,  3));
                    }
                    else if(deviceList.get(position).bluetoothDevice.getName().contains("4")) {
                        listImage.setImageResource(R.drawable.icon_centerpiece_4);
                        listText.setText(getString(R.string.centerpiece,  4));
                    }
                }

            }


            return convertView;
        }
    }
}

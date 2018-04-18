package net.california_design.bubble_hub;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;


/**
 * Handles Bluetooth Connection as well as bluetooth information sending and receiving.
 * This class uses RxAndroidBle library to handle all Bluetooth related tasks.
 */
class BluetoothConnection
{
    ////////////////////////Class global variables////////////////////////
    interface BluetoothConnectionInterface {
        void receiveMessageFromBluetoothConnection(String message);
        void receiveDataFromBluetoothConnection(String data);
    }

    private static RxBleClient mRxBleClient;
    private RxBleDevice rxBleDevice;
    private Observable<RxBleConnection> mConnectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private CompositeSubscription  compositeSubscription = new CompositeSubscription();
    private Handler mMainLooperHandler = new Handler(Looper.getMainLooper());
    private BluetoothConnectionInterface mBluetoothConnectionInterface = null;
    static final String CONNECTING = "net.california_design.bubble_hub.bluetooth_connection.CONNECTING";
    static final String CONNECTED = "net.california_design.bubble_hub.bluetooth_connection.DISCONNECTED";
    static final String CONNECTION_FAILED = "net.california_design.bubble_hub_bluetooth_connection.CONNECTION_FAILED";
    static final String CONNECTION_LOST = "net.california_design.bubble_hub_bluetooth_connection.CONNECTION_LOST";
    static final String DISCONNECTING = "net.california_design.bubble_hub.bluetooth_connection.DISCONNECTING";
    static final String DISCONNECTED = "net.california_design.bubble_hub.bluetooth_connection.CONNECTED";
    static final String SUBSCRIBED_TO_CHARACTERISTICS = "net.california_design.bubble_hub.bluetooth_connection.SUBSCRIBED_TO_CHARACTERISTICS";
    static final String NOTIFICATION_SETUP_FAILED = "net.california_design.bubble_hub.bluetooth_connection.NOTIFICATION_SETUP_FAILED";
    static final String NOTIFICATION_SETUP_SUCCESS = "net.california_design.bubble_hub.bluetooth_connection.NOTIFICATION_SETUP_SUCCESS";
    static final String WRITE_FAILED = "net.california_design.bubble_hub.bluetooth_connection.WRITE_FAILED";
    private static final UUID BLUETOOTH_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    ////////////////////////Variable declaration ends here////////////////////////



    BluetoothConnection(BluetoothConnectionInterface context) {
        mBluetoothConnectionInterface = context;
    }

    static RxBleClient getBleClient(Context context) {
        if(mRxBleClient == null) {
            mRxBleClient = RxBleClient.create(context);
        }

        return mRxBleClient;
    }



    /** Establishes the bluetooth connection with the bluetooth device. */
    void connect(String macAddress) {
        rxBleDevice = mRxBleClient.getBleDevice(macAddress);
        mConnectionObservable = prepareConnectionObservable();
        Subscription connectionStateChangesSubscription = rxBleDevice.observeConnectionStateChanges()
                .subscribe(this::connectionStatus);
        compositeSubscription.add(connectionStateChangesSubscription);

        if (isConnected()) {
            triggerDisconnect();
        } else {
            mConnectionObservable
                    .flatMap(RxBleConnection::discoverServices)
                    .flatMap(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(BLUETOOTH_CHARACTERISTIC_UUID))
                    .subscribe(
                            characteristic -> {

                                CustomLogger.log("BluetoothConnection: connect: Subscribed to characteristics");
                                sendMessageThroughInterface(SUBSCRIBED_TO_CHARACTERISTICS);

                                setupNotificationListener();
                            },
                            this::onConnectionFailure,
                            this::onConnectionFinished
                    );
        }
    }


    private void onConnectionFinished() {
        CustomLogger.log("Connection finished: ");
        sendMessageThroughInterface(CONNECTION_LOST);
    }



    private void onConnectionFailure(Throwable throwable) {
        CustomLogger.log("BluetoothConnection: onConnectionFailure: failed to connect. " + throwable);

        if(throwable.getMessage().contains("Disconnected")) {
            sendMessageThroughInterface(CONNECTION_LOST);
            return;
        }
        sendMessageThroughInterface(CONNECTION_FAILED);
    }



    private boolean isConnected() {
        return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }



    private void setupNotificationListener() {
        if (isConnected()) {
            Subscription notificationSubscription = mConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(BLUETOOTH_CHARACTERISTIC_UUID))
                    .doOnNext(notificationObservable -> notificationHasBeenSetUp())
                    .flatMap(notificationObservable -> notificationObservable)
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
            compositeSubscription.add(notificationSubscription);
        }
    }



    private void notificationHasBeenSetUp() {
        CustomLogger.log("BluetoothConnection: notificationHasBeenSetUp: notification setup success");
        sendMessageThroughInterface(NOTIFICATION_SETUP_SUCCESS);
    }




    private void onNotificationReceived(byte[] bytes) {
        String data = new String(bytes);
        CustomLogger.log("BluetoothConnection: onNotificationReceived: received " + data);
        mMainLooperHandler.post( () ->  mBluetoothConnectionInterface.receiveDataFromBluetoothConnection(data));
    }




    private void onNotificationSetupFailure(Throwable throwable) {
        CustomLogger.log("BluetoothConnection: onNotificationSetupFailure: Failed to setupNotification. " + throwable, 'e');

        if(throwable.getMessage().contains("Disconnected")) {
            sendMessageThroughInterface(CONNECTION_LOST);
            return;
        }
        sendMessageThroughInterface(NOTIFICATION_SETUP_FAILED);
    }



    private void connectionStatus(RxBleConnection.RxBleConnectionState rxBleConnectionState) {
        switch(rxBleConnectionState) {
            case CONNECTING:
                CustomLogger.log("BluetoothConnection: connectionStatus: CONNECTING");
                sendMessageThroughInterface(CONNECTING);
                break;
            case CONNECTED:
                CustomLogger.log("BluetoothConnection: connectionStatus: CONNECTED");
                sendMessageThroughInterface(CONNECTED);
                break;
            case DISCONNECTING:
                CustomLogger.log("BluetoothConnection: connectionStatus: DISCONNECTING");
                sendMessageThroughInterface(DISCONNECTING);
                break;
            case DISCONNECTED:
                CustomLogger.log("BluetoothConnection: connectionStatus: DISCONNECTED");
                sendMessageThroughInterface(DISCONNECTED);
                break;
        }
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return rxBleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(new ConnectionSharingAdapter());
    }





    void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
        compositeSubscription.unsubscribe();
    }



    void write(String msg) {
        if (isConnected()) {
            String finalMessage = msg + "\n";
            final byte[] tx = finalMessage.getBytes();

            mConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(BLUETOOTH_CHARACTERISTIC_UUID, tx))
                    .subscribe(
                            bytes -> CustomLogger.log("BluetoothConnection: onWriteSuccess: Write was successful. We written data: " + new String(bytes)),
                            this::onWriteFailure
                    );
        }
    }

    private void onWriteFailure(Throwable throwable) {
        CustomLogger.log("BluetoothConnection: onWriteFailure: Write failed. Throwable " + throwable.getMessage());

        if(throwable.getMessage().contains("Disconnected")) {
            sendMessageThroughInterface(CONNECTION_LOST);
            return;
        }

        sendMessageThroughInterface(WRITE_FAILED);
    }


    private void sendMessageThroughInterface(String message) {
        mMainLooperHandler.post( () ->  mBluetoothConnectionInterface.receiveMessageFromBluetoothConnection(message));
    }

}
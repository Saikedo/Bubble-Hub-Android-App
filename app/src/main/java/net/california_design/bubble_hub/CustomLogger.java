package net.california_design.bubble_hub;


import android.util.Log;

/**
 * Handles logging. The purpose of this helper class class is to avoid using tags and importing Log in every class.
 */
class CustomLogger {
    ////////////////////////Class global variables////////////////////////
    private static final boolean sIsDebugModeOn = true;
    private static final String MAIN_TAG = "MAIN_TAG";
    ////////////////////////Variable declaration ends here////////////////////////

    //The following arguments are allowed for log type
    //e: Error
    //w: Warning
    //i: Information
    //d: Debug
    //v: Verbose

    static <T>  void log(T message, char logType){
        if(message != null && sIsDebugModeOn){
            switch (logType) {
                case 'e':  Log.e(MAIN_TAG, message.toString());
                    break;
                case 'w':  Log.w(MAIN_TAG, message.toString());
                    break;
                case 'i':  Log.i(MAIN_TAG, message.toString());
                    break;
                case 'd':  Log.d(MAIN_TAG, message.toString());
                    break;
                case 'v':  Log.v(MAIN_TAG, message.toString());
                    break;
                default:   Log.e(MAIN_TAG, "CustomLogger: log: ERROR: logType was not one of the accepted chars");
                    break;
            }
        }
    }



    static <T>  void log(T message){
        if(message != null && sIsDebugModeOn){
            Log.d(MAIN_TAG, message.toString());
        }
    }
}

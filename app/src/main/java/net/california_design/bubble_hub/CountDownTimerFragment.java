package net.california_design.bubble_hub;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This fragment class is responsible for handling a countdown that is shown when we
 * enter a sleep mode.
 */
public class CountDownTimerFragment extends Fragment {
    ////////////////////////Class global variables////////////////////////
    private long mTimeCountInMilliSeconds = 30000;
    private ProgressBar mProgressBarCircle;
    private TextView mTextViewTime;
    CountDownTimer mCountDownTimer;
    ////////////////////////Variable declaration ends here////////////////////////



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.count_down_timer_fragment, container, false);

        //I was having trouble making the countdown timer scale correctly in the XML file
        //so I finally gave up and handled the scaling in code.
        root.post( () -> {
            //Adjusts the fragment padding to fit inside the view
            int fragmentHeight = root.getMeasuredHeight();
            int fragmentWidth = root.getMeasuredWidth();
            if(fragmentWidth > fragmentHeight){
                root.setPadding((fragmentWidth - fragmentHeight) / 2, 0, (fragmentWidth - fragmentHeight) / 2, 0);
            } else {
                root.setPadding(0, (fragmentHeight - fragmentWidth) / 2, 0, (fragmentHeight - fragmentWidth) / 2);
            }

            //adjusts the text size to fit inside the progress bar
            float height_sp = fragmentHeight / getResources().getDisplayMetrics().scaledDensity;
            float width_sp = fragmentWidth / getResources().getDisplayMetrics().scaledDensity;
            if(height_sp > width_sp){
                mTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, width_sp / 2 - 2);
            } else{
                mTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, height_sp / 2 - 2);
            }
        });

        return root;
    }



    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        try {
            if(getView() != null){
                mProgressBarCircle = getView().findViewById(R.id.progressBarCircle);
            }
        }
        catch(java.lang.NullPointerException e) {
            CustomLogger.log("CountDownTimerFragment: onViewCreated: Got Error with code " + e, 'e');
        }
        mTextViewTime = getView().findViewById(R.id.textViewTime);


    }



    public void StartTimer() {
        mTimeCountInMilliSeconds = 30000;
        setProgressBarValues();
        startCountDownTimer();
    }



    private void startCountDownTimer() {
        mCountDownTimer = new CountDownTimer(mTimeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTextViewTime.setText(hmsTimeFormatter(millisUntilFinished));

                mProgressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                mTextViewTime.setText(hmsTimeFormatter(mTimeCountInMilliSeconds));
                // call to initialize the progress bar values
                setProgressBarValues();
                // changing the timer status to stopped
            }

        }.start();
        mCountDownTimer.start();
    }



    private void setProgressBarValues() {
        mProgressBarCircle.setMax((int) mTimeCountInMilliSeconds / 1000);
        mProgressBarCircle.setProgress((int) mTimeCountInMilliSeconds / 1000);
    }



    void stopCountDownTimer() {
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
            setProgressBarValues();
        }
    }



    private String hmsTimeFormatter(long milliSeconds) {
        return String.format(Locale.US, "%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

    }
}


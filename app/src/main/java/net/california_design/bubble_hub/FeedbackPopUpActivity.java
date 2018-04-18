package net.california_design.bubble_hub;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Handles user feedback/bug report submission.
 */
public class FeedbackPopUpActivity extends AppCompatActivity implements View.OnClickListener {

    ////////////////////////Class global variables////////////////////////
    private EditText mFeedbackText;
    private String mSystemInfo;
    private TextView mNumberOfAttachmentsTextView;
    private static final int PICK_MEDIA = 100;
    private ArrayList<Uri> mMediaUriList = new ArrayList<>();
    ////////////////////////Variable declaration ends here////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_feedback_pop_up);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.6) );

        Button notNowButton = findViewById(R.id.feedback_activity_not_now_button_id);
        notNowButton.setOnClickListener(this);
        Button submitButton = findViewById(R.id.feedback_activity_submit_button_id);
        submitButton.setOnClickListener(this);
        Button attachImageOrVideoButton = findViewById(R.id.feedback_activity_attach_image_video_button_id);
        attachImageOrVideoButton.setOnClickListener(this);
        mNumberOfAttachmentsTextView = findViewById(R.id.feedback_activity_attach_number_of_attachments_text_n_id);
        mFeedbackText = findViewById(R.id.feedback_activity_editText);

        mSystemInfo = "------------------------------------------\n";

        //Get application version
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            mSystemInfo += "Application Version: " + pInfo.versionName + "\n";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Get phone model and os version
        mSystemInfo += "Phone Information: " + Build.MANUFACTURER  + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " +
                 Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.feedback_activity_not_now_button_id: {
                finish();
                break;
            }
            case R.id.feedback_activity_submit_button_id: {
                String feedback = mFeedbackText.getText().toString();

                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"BubbleHubFeedback@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bubble Hub bug report/feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, feedback + "\n\n" + mSystemInfo);
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mMediaUriList);
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent, "Choose app to send e-mail"));

                finish();
                break;
            }
            case R.id.feedback_activity_attach_image_video_button_id: {
                Intent openGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                openGallery.setType("image/*");
                startActivityForResult(openGallery, PICK_MEDIA);

                break;
            }
        }
    }

    /**
     * Receives the uri of the media file that the user chose to attach to the email.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_MEDIA){
            mMediaUriList.add(data.getData());
            mNumberOfAttachmentsTextView.setVisibility(View.VISIBLE);
            mNumberOfAttachmentsTextView.setText(getString(R.string.number_of_attachments,  mMediaUriList.size()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

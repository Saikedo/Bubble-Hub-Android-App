package net.california_design.bubble_hub;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * This activity displays the help page for all RemoteControl activities
 */
public class HelpBubbleControlActivity extends AppCompatActivity {
    ////////////////////////Class global variables////////////////////////
    static final String HELP_PAGE_CONTENTS = "com.california_design.bubble_hub.california_design.HELP_PAGE_CONTENTS";
    static final String HELP_PAGE_FOR_BUBBLE_PILLAR = "com.california_design.bubble_hub.california_design.HELP_PAGE_FOR_BUBBLE_PILLAR";
    static final String HELP_PAGE_FOR_BUBBLE_WALL = "com.california_design.bubble_hub.california_design.HELP_PAGE_FOR_BUBBLE_WALL";
    static final String HELP_PAGE_FOR_BUBBLE_CENTERPIECE = "com.california_design.bubble_hub.california_design.HELP_PAGE_FOR_BUBBLE_CENTERPIECE";
    ////////////////////////Variable declaration ends here////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_bubble_control);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        TextView textView = findViewById (R.id.bubble_pillar_control_help_page_body);
        textView.setMovementMethod (LinkMovementMethod.getInstance());

        final String helpPageType = getIntent().getStringExtra(HELP_PAGE_CONTENTS);
        if(helpPageType != null) {
            switch(helpPageType){
                case HELP_PAGE_FOR_BUBBLE_PILLAR: {
                    textView.setText(Html.fromHtml(getString(R.string.bubble_pillar_help_page_info)));
                    setTitle("Bubble Pillar Help Page");
                    break;
                }
                case HELP_PAGE_FOR_BUBBLE_WALL: {
                    textView.setText(Html.fromHtml(getString(R.string.bubble_wall_help_page_info)));
                    setTitle("Bubble Wall Help Page");
                    break;
                }
                case HELP_PAGE_FOR_BUBBLE_CENTERPIECE: {
                    textView.setText(Html.fromHtml(getString(R.string.bubble_centerpiece_help_page_info)));
                    setTitle("Centerpiece Help Page");
                    break;
                }
                default: {
                    textView.setText (Html.fromHtml (getString (R.string.help_page_not_found)));
                    break;
                }
            }
        } else {
            textView.setText (Html.fromHtml (getString (R.string.help_page_not_found)));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

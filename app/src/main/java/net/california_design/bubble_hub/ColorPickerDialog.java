package net.california_design.bubble_hub;


import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Handles the Custom color picker related tasks. This is basically an AlertDialog wrapper for ColorPicker view.
 */
class ColorPickerDialog extends AlertDialog implements ColorPicker.OnColorChangeListener, View.OnClickListener{
    ////////////////////////Class global variables////////////////////////
    interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
    private final OnColorSelectedListener mOnColorSelectedListener;

    private int mColor = -1;
    ////////////////////////Variable declaration ends here////////////////////////



    ColorPickerDialog(Context context, int initialColor, ColorPicker cp, View view, OnColorSelectedListener onColorSelectedListener) {
        super(context, R.style.MyDialogTheme);

        this.mOnColorSelectedListener = onColorSelectedListener;
        cp.setColor(initialColor);
        cp.setOnColorChangeListener(this);

        Button closeButton = view.findViewById(R.id.color_wheel_close_button);
        closeButton.setOnClickListener(this);
    }


    /** Receives color change messages from ColorPicker and forwards them to classes that
     * implemented OnColorSelectedListener interface
     */
    public void onColorChange(int newColor) {
        if(mColor != newColor){
            mColor = newColor;
            mOnColorSelectedListener.onColorSelected(mColor);
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.color_wheel_close_button: {
                dismiss();
                break;
            }
        }
    }
}
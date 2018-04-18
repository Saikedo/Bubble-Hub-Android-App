package net.california_design.bubble_hub;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Responsible for creating the color wheel and sending color data to ColorPickerDialog via OnColorChangeListener interface.
 *
 * This class is was modified to suit our needs and the base project can be found here
 * https://github.com/chiralcode/Android-Color-Picker/
 */
public class ColorPicker extends View {
    ////////////////////////Class global variables////////////////////////
    interface OnColorChangeListener {
        void onColorChange(int color);
    }

    public static int DEFAULT_COLOR = 0xFFFF0000;
    private OnColorChangeListener mOnColorChangeListener;
    private Rainbow mRainbow;
    private Circle mCircle;
    private final RectF mCircleBox = new RectF();
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private MyColor mMyColor = new MyColor(DEFAULT_COLOR);
    private enum Keep {WIDTH, HEIGHT}
    private enum Touch {NONE, HUE}
    private Touch mTouch = Touch.NONE;
    ////////////////////////Variable declaration ends here////////////////////////



    public ColorPicker(Context context) {
        this(context, null);
    }



    public ColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }



    public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRainbow = new Rainbow();
        mCircle = new Circle();
    }



    public void setColor(int color){
        setColorInternal(new MyColor(color));
    }


    public int getColor(){
        return mMyColor.rgb();
    }



    public void setOnColorChangeListener(OnColorChangeListener listener) {
        mOnColorChangeListener = listener;
    }



    private class MyColor {
        private float mHue;

        MyColor(float h) {
            hue(h);
        }


        MyColor(int rgb) {
            float[] hsv = new float[3];
            Color.colorToHSV(rgb, hsv);

            hue(hsv[0]);
        }


        MyColor(MyColor myColor){
            this(myColor.hue());
        }


        boolean equalHue(MyColor other){
            return other.mHue == mHue;
        }


        @Deprecated
        @Override
        public boolean equals(Object o) {
            return super.equals(o) || o instanceof MyColor && ((MyColor) o).mHue == mHue;
        }


        int rgb(){ return Color.HSVToColor(new float[]{mHue, 1, 1}); }
        int r(){ return (rgb() >> 16) & 0xFF; }
        int g(){ return (rgb() >> 8) & 0xFF; }
        int b(){ return rgb() & 0xFF; }
        float hue(){
            return mHue;
        }
        MyColor hue(float h) {
            mHue = mod(h);
            return this;
        }

        MyColor inverted(){
            return new MyColor(Color.rgb(255-r(), 255-g(), 255-b()));
        }
    }



    private void setColorInternal(MyColor color) {
        boolean hsvChanged = !mMyColor.equalHue(color);
        mMyColor = color;

        mRainbow.setColor(mMyColor);
        mCircle.setColor(getColor());

        mPaint.setColor(mMyColor.rgb());

        if (hsvChanged) {
            invalidate();
        }

        if (hsvChanged && mOnColorChangeListener != null) {
            mOnColorChangeListener.onColorChange(getColor());
        }
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Keep keep = Keep.WIDTH;

        int desiredHeight = (int) dp(50);
        if (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT) {
            desiredHeight = MeasureSpec.getSize(heightMeasureSpec); // parent height
            keep = Keep.HEIGHT;
        }

        int desiredWidth = (int) dp(50);
        if (getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
            desiredWidth = MeasureSpec.getSize(widthMeasureSpec); // parent width
            keep = Keep.WIDTH;
        }

        int size = keep == Keep.HEIGHT ? desiredHeight : desiredWidth;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            int widthLimited = MeasureSpec.getSize(widthMeasureSpec);
            size = Math.min(size, widthLimited);
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            int heightLimited = MeasureSpec.getSize(heightMeasureSpec);
            size = Math.min(size, heightLimited);
        }

        setMeasuredDimension(size, size);
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {

        float size = Math.max(dp(20), Math.min(dp(35), dp(30)*Math.min(w, h)/1000));

        float padding = Math.max(dp(5), Math.min(dp(10), dp(7)*Math.min(w, h)/1000));

        PointF center = new PointF(w/2, h/2);
        float radius = (Math.min(w, h) - padding - size)/2;

        mRainbow.setGeometry(center, radius, size);
        mCircle.setGeometry(center, (int) (radius * 0.8));

        mCircleBox.set(center.x - radius, center.y - radius, center.x + radius, center.y + radius);

        mPaint.setStyle(Paint.Style.FILL);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = false;

        PointF pointer = new PointF(event.getX(), event.getY());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mRainbow.encloses(pointer)) {
                // mTouch on hue mCircle
                mTouch = Touch.HUE;

                mCircle.setColor(getColor());
                mCircle.redrawCircle();

                handled = true;
            }
        }

        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mTouch == Touch.HUE) {
                setColorInternal(new MyColor(mMyColor).hue(mRainbow.hueAt(pointer)));

                mCircle.setColor(getColor());
                mCircle.redrawCircle();

                handled = true;
            }
        }

        if (handled) {
            if (getParent() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                // prevent parent (e.g. scroll view) from reaction on further events
                // during this mTouch
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true; // event was handled
        }

        return super.dispatchTouchEvent(event);
    }



    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.saveColor = new float[]{mMyColor.mHue, 1, 1};

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        mMyColor = new MyColor((int) ss.saveColor[0]);
    }



    private static class SavedState extends BaseSavedState {
        float[] saveColor;
        int saveAlpha;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readFloatArray(saveColor);
            saveAlpha = in.readInt();
        }


        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloatArray(saveColor);
            out.writeInt(saveAlpha);
        }


        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }



    private float dp(int value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value /*dp*/, getResources().getDisplayMetrics());
    }



    private float mod(float i){
        return (((i % 360) + 360) % 360);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        mRainbow.draw(canvas);
        mCircle.draw(canvas);
    }



    private class Circle {
        private RectF boundingBox = new RectF();
        private int color;
        private final Paint paint;


        Circle() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
        }


        void setGeometry(PointF center, float radius) {

            boundingBox = new RectF(center.x - radius, center.y - radius,
                    center.x + radius, center.y + radius);
            redrawCircle();
        }


        void setColor(int clr){
            color = clr;
        }


        private void redrawCircle(){
            paint.setColor(color);
        }


        void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(-90, canvas.getWidth() / 2, canvas.getHeight() / 2);
            canvas.drawArc(boundingBox, 0, 360, false, paint);
            canvas.restore();
        }
    }



    private class Rainbow {
        private RectF boundingBox = new RectF();
        private PointF center = new PointF();
        private float radius = 0;
        private float with = 0;
        private MyColor mColor = new MyColor(Color.BLACK);
        private final Paint paint;
        private final Paint markerPaint;
        private float[] marker = new float[]{0, 0, 0, 0};


        Rainbow(){
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);

            markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            markerPaint.setStyle(Paint.Style.STROKE);
            markerPaint.setStrokeWidth(dp(1));

        }


        void setGeometry(PointF center, float radius, float with){
            this.center = center;
            this.radius = radius;
            this.with = with;

            paint.setStrokeWidth(with);

            boundingBox = new RectF(center.x - radius, center.y - radius,
                    center.x + radius, center.y + radius);
            updateShader();
            updateMarker();
        }


        void setColor(MyColor color){
            if (mColor.hue() != color.hue()){
                mColor = color;
                updateMarker();
            }
            mColor = color;
        }


        private void updateShader(){
            int[] gradient = new int[]{Color.RED, Color.YELLOW, Color.GREEN,
                    Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
            SweepGradient rainbow = new SweepGradient(center.x, center.y, gradient, null);

            paint.setShader(rainbow);
        }


        private void updateMarker(){

            markerPaint.setColor(new MyColor(mColor.hue()).inverted().rgb());

            float sx = (float) Math.cos(Math.toRadians(mColor.hue()));
            float sy = (float) Math.sin(Math.toRadians(mColor.hue()));

            marker = new float[]{
                    center.x + (radius -with/3)*sx, center.y + (radius -with/3)*sy,
                    center.x + (radius +with/3)*sx, center.y + (radius +with/3)*sy};
        }


        void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(-90, canvas.getWidth() / 2, canvas.getHeight() / 2);
            canvas.drawArc(boundingBox, 0, 360, false, paint);
            canvas.drawLines(marker, markerPaint);
            canvas.restore();
        }


        boolean encloses(PointF pointer) {
            double r = Math.sqrt(Math.pow(pointer.x-center.x, 2) + Math.pow(pointer.y-center.y, 2));
            return radius - with <= r && r <= radius + with;
        }


        float hueAt(PointF pointer) {
            return mod((float) (Math.toDegrees(
                    Math.atan2(pointer.y-center.y, pointer.x-center.x)) + 90));
        }
    }
}
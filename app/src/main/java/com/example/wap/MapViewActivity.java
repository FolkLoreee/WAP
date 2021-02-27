package com.example.wap;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MapViewActivity extends View implements OnTouchListener {
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private Bitmap bitmap;

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    boolean drag = false;

    private Bitmap im;
    public MapViewActivity(Context context)
    {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStrokeWidth(6);
        mCanvas = new Canvas();
        mPath = new Path();

    }

    public void setmCanvasBitmap(int i){
        if (i == 1){
            Drawable drawable = getResources().getDrawable(R.drawable.floor_wap_1);

            //original height and width of the bitmap
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int intrinsicWidth = drawable.getIntrinsicWidth();
            bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bitmap);
            mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.floor_wap_1),0,0,null);
        }
        else{
            Drawable drawable = getResources().getDrawable(R.drawable.floor_wap_2);

            //original height and width of the bitmap
            int intrinsicHeight = drawable.getIntrinsicHeight();
            int intrinsicWidth = drawable.getIntrinsicWidth();
            bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(bitmap);
            mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.floor_wap_2),0,0,null);
        }
    }

    public void onClickUndo () {
        if (paths.size()>0)
        {
            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();
        }
        else
        {

        }
        //toast the user
    }

    public void onClickRedo (){
        if (undonePaths.size()>0)
        {
            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }
        else
        {

        }
        //toast the user
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);

        float relativeX;
        float relativeY;
        float[] values = new float[9];



        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
                matrix.getValues(values);
                relativeX = (event.getX() - values[2]) / values[0];
                relativeY = (event.getY() - values[5]) / values[4];
                mPath.addCircle(relativeX, relativeY, 10, Path.Direction.CW);

                if (drag){
                    Log.i("path added", "path added");
                    mPath = new Path();
                    drag = false;
                }
                else{
                    mCanvas.drawPath(mPath, mPaint);
                    mPath = new Path();
                    paths.add(mPath);

                }
//                mPath = new Path();
//                paths.add(mPath);



            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;


            case MotionEvent.ACTION_MOVE:

                if(mode == DRAG)
                {
                    Log.i("become drag", "became drag");
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    drag = true;


                }

                else if(mode == ZOOM)
                {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        drag = true;
                    }
                }
                break;
        }

        StringBuilder sb = new StringBuilder();

        matrix.getValues(values);

        // values[2] and values[5] are the x,y coordinates of the top left corner of the drawable image, regardless of the zoom factor.
        // values[0] and values[4] are the zoom factors for the image's width and height respectively.
        // If you zoom at the same factor, these should both be the same value.
        // event is the touch event for MotionEvent.ACTION_UP
        relativeX = (event.getX() - values[2]) / values[0];
        relativeY = (event.getY() - values[5]) / values[4];
        sb.append("x: " + relativeX + ", y: " + relativeY);

        //canvas.drawCircle(relativeX, relativeY, 10, paint);

        view.setImageMatrix(matrix);

        return true;
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
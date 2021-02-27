package com.example.wap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MappingActivity extends AppCompatActivity implements View.OnTouchListener {

    // XML Elements
    ImageView mapImage;
    Button level1Btn;
    Button level2Btn;
    Button undo;
    Button submit;
    TextView coordinatesText;

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

    // Bitmap
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    ArrayList<Path> paths = new ArrayList<Path>();
    ArrayList<Path> undonePaths = new ArrayList<Path>();
    Path mPath;
    boolean drag = false;
    boolean hasPath = false;
    int intrinsicHeight;
    int intrinsicWidth;
    int floor = R.drawable.floor_wap_1;
    Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
        level1Btn = (Button) findViewById(R.id.level1Btn);
        level2Btn = (Button) findViewById(R.id.level2Btn);

        undo = (Button) findViewById(R.id.undo);
        submit = (Button) findViewById(R.id.submit);

        // Set up the map of level 1 by default
        mapImage = (ImageView) findViewById(R.id.mapImage);
        mapImage.setBackground(getResources().getDrawable(R.drawable.black));

        drawable = getResources().getDrawable(floor);

        //original height and width of the bitmap
        intrinsicHeight = drawable.getIntrinsicHeight();
        intrinsicWidth = drawable.getIntrinsicWidth();

        // Set up bitmap for ImageView
        bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        mPath = new Path();
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.floor_wap_1),0,0,null);
        paint = new Paint();
        paint.setColor(Color.RED);
        mapImage.setImageBitmap(bitmap);

        // Set up buttons
        level1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change after upload img funciton done
                floor = R.drawable.floor_wap_1;
                drawable = getResources().getDrawable(floor);

                //original height and width of the bitmap
                intrinsicHeight = drawable.getIntrinsicHeight();
                intrinsicWidth = drawable.getIntrinsicWidth();

                bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                mPath = new Path();
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),floor),0,0,null);
                paint = new Paint();
                paint.setColor(Color.RED);
                mapImage.setImageBitmap(bitmap);

                level1Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));

            }
        });

        level2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change after upload img funciton done
                floor = R.drawable.floor_wap_2;
                drawable = getResources().getDrawable(floor);

                //original height and width of the bitmap
                intrinsicHeight = drawable.getIntrinsicHeight();
                intrinsicWidth = drawable.getIntrinsicWidth();

                bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                mPath = new Path();
                paths.add(mPath);
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),floor),0,0,null);
                paint = new Paint();
                paint.setColor(Color.RED);
                mapImage.setImageBitmap(bitmap);

                //mapImage.setBackground(getResources().getDrawable(R.drawable.floor_wap_2));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level1Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));

            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("undo","undo");
                if(paths.size() > 0){
                    undonePaths.add(paths.remove(paths.size()-1));
                }
                else {
                    Toast.makeText(MappingActivity.this, "nothing to undo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MappingActivity.this, "not done yet", Toast.LENGTH_SHORT).show();
            }
        });

        mapImage.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);

        float relativeX;
        float relativeY;
        float[] values = new float[9];

        // Handle touch events here...
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
                    if(hasPath){
                        bitmap = Bitmap.createBitmap((int) intrinsicWidth, (int) intrinsicHeight, Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(bitmap);
                        mPath = new Path();
                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),floor),0,0,null);
                        paint = new Paint();
                        paint.setColor(Color.RED);
                        mapImage.setImageBitmap(bitmap);
                        paths.clear();
                        mPath.addCircle(relativeX, relativeY, 10, Path.Direction.CW);

                        canvas.drawPath(mPath, paint);
                        mPath = new Path();
                        hasPath = true;
                    }

                    canvas.drawPath(mPath, paint);
                    mPath = new Path();
//                    paths.add(mPath);
                    hasPath = true;

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

        coordinatesText.setText(sb);

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
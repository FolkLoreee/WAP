package com.example.wap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MappingActivity extends AppCompatActivity implements View.OnTouchListener {

    // XML Elements
    ImageView mapImage;
    Button level1Btn;
    Button level2Btn;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
        level1Btn = (Button) findViewById(R.id.level1Btn);
        level2Btn = (Button) findViewById(R.id.level2Btn);

        // Set up the map of level 1 by default
        mapImage = (ImageView) findViewById(R.id.mapImage);
        mapImage.setImageDrawable(getResources().getDrawable(R.drawable.floor_wap_1));

        // Set up buttons
        level1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapImage.setImageDrawable(getResources().getDrawable(R.drawable.floor_wap_1));
                level1Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));
            }
        });

        level2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapImage.setImageDrawable(getResources().getDrawable(R.drawable.floor_wap_2));
                level2Btn.setBackgroundColor(getResources().getColor(R.color.grey));
                level1Btn.setBackgroundColor(getResources().getColor(R.color.purple_500));
            }
        });

        mapImage.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);

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

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;


            case MotionEvent.ACTION_MOVE:

                if(mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }

                else if(mode == ZOOM)
                {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        StringBuilder sb = new StringBuilder();

        float[] values = new float[9];
        matrix.getValues(values);

        // values[2] and values[5] are the x,y coordinates of the top left corner of the drawable image, regardless of the zoom factor.
        // values[0] and values[4] are the zoom factors for the image's width and height respectively.
        // If you zoom at the same factor, these should both be the same value.
        // event is the touch event for MotionEvent.ACTION_UP
        float relativeX = (event.getX() - values[2]) / values[0];
        float relativeY = (event.getY() - values[5]) / values[4];
        sb.append("x: " + relativeX + ", y: " + relativeY);

        coordinatesText.setText(sb);

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
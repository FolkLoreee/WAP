//package com.example.wap;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.annotation.SuppressLint;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.PointF;
//import android.graphics.Rect;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.util.FloatMath;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewTreeObserver;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.w3c.dom.Text;
//
//import java.util.ArrayList;
//
//public class MapActivity extends AppCompatActivity{
//
//    // XML Elements
//    ImageView mapImage;
//    Button level1Btn;
//    Button level2Btn;
//    Button undo;
//    Button submit;
//    TextView coordinatesText;
//
//    // These matrices will be used to move and zoom image
//    Matrix matrix = new Matrix();
//    Matrix savedMatrix = new Matrix();
//
//    // We can be in one of these 3 states
//    static final int NONE = 0;
//    static final int DRAG = 1;
//    static final int ZOOM = 2;
//    int mode = NONE;
//
//    // Remember some things for zooming
//    PointF start = new PointF();
//    PointF mid = new PointF();
//    float oldDist = 1f;
//
//    // Bitmap
//    Bitmap bitmap;
//    Canvas canvas;
//    Paint paint;
//    ArrayList<Path> paths = new ArrayList<Path>();
//    ArrayList<Path> undonePaths = new ArrayList<Path>();
//    Path mPath;
//    boolean drag = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        final MapViewActivity mapView = new MapViewActivity(this);
//        setContentView(R.layout.activity_mapping);
//
//        coordinatesText = (TextView) findViewById(R.id.coordinatesText);
//        level1Btn = (Button) findViewById(R.id.level1Btn);
//        level2Btn = (Button) findViewById(R.id.level2Btn);
//        undo = (Button) findViewById(R.id.undo);
//        submit = (Button) findViewById(R.id.submit);
//
//        // Set up the map of level 1 by default
//        mapImage = (ImageView) findViewById(R.id.mapImage);
//        mapImage.setBackground(getResources().getDrawable(R.drawable.black));
//
//
//
//
//        // Set up buttons
//        level1Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mapView.setmCanvasBitmap(1);
//            }
//        });
//
//        level2Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mapView.setmCanvasBitmap(2);
//            }
//        });
//
//        undo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mapView.onClickUndo();
//            }
//        });
//
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MapActivity.this, "not done yet", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//
//}
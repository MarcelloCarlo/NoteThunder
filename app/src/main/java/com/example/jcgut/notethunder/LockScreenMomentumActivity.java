package com.example.jcgut.notethunder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.jcgut.notethunder.domain.Memo;
import com.example.jcgut.notethunder.interfaces.DetailInterface;

import java.sql.SQLException;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.example.jcgut.notethunder.MainActivity.REQ_CAMERA;
import static com.example.jcgut.notethunder.MainActivity.REQ_GALLERY;

/**
 * Created by jcgut on 3/8/2018.
 * Modified : 3/8/2018
 */

public class LockScreenMomentumActivity extends Activity {

    private LinearLayout linearLayout;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager windowManager;
    Button btnCancel,btnSave,btnCamera,btnGallery;
    EditText txtTitle,txtContext;
    int id = 0;
    Uri fileUri = null;
    DetailInterface detailInterface = null;
    ImageView imgThumb;
    String title ="";
    String content="";
    String date="";

    @Override
    public void onCreate(Bundle savedInstance) {
        setContentView(R.layout.activity_splash_screen);
        super.onCreate(savedInstance);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, intentFilter);
        windowManager = ((WindowManager) getSystemService(WINDOW_SERVICE));
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN //draw on status bar
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_FULLSCREEN /*| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON*/ | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, //hiding the home screen button
                PixelFormat.TRANSPARENT);
    }

    private void init() {
        linearLayout = new LinearLayout(this);
        windowManager.addView(linearLayout, layoutParams);
        ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_detail, linearLayout);
        btnCancel = linearLayout.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(listener);
        btnSave = linearLayout.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(listener);
        btnCamera = linearLayout.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(listener);
        btnGallery = linearLayout.findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(listener);
        txtTitle = linearLayout.findViewById(R.id.editTitle);
        txtContext = linearLayout.findViewById(R.id.editContent);


    }
    View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View view) {
            Intent i = null;
            switch (view.getId()){

                case R.id.btnCancel:
                    windowManager.removeView(linearLayout);
                    linearLayout = null;
                    break;
                case R.id.btnSave:
                    try {
                        detailInterface.saveToList(makeMemo());
                    } catch (SQLException x){
                        x.printStackTrace();
                    }
                    break;
                case R.id.btnCamera :
                    title = txtTitle.getText().toString();
                    content = txtContext.getText().toString();
                    i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(i, REQ_CAMERA);
                    break;
                case R.id.btnGallery :
                    title = txtTitle.getText().toString();
                    content = txtContext.getText().toString();
                    i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    startActivityForResult( Intent.createChooser(i,"Select Picture") , REQ_GALLERY);
                    break;
                default:
                    break;
            }

        }

    };

    @Override
    public void onDestroy() {
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CAMERA :
                if(resultCode==RESULT_OK) {
                    fileUri = data.getData();
                    if(fileUri!=null) {
                        Glide.with(this).load(fileUri).into(imgThumb);
                    }
                }
                break;
            case REQ_GALLERY :
                if(resultCode==RESULT_OK) {
                    fileUri = data.getData();
                    if(fileUri!=null) {
                        Log.w("img", "change");
                        Glide.with(this).load(fileUri).into(imgThumb);
                    }
                }
                break;
        }
    }

    private Memo makeMemo() {
        Memo memo = new Memo();
        memo.setImg(String.valueOf(fileUri));
        memo.setTitle(txtTitle.getText().toString());
        memo.setMemo(txtContext.getText().toString());
        memo.setDate(new Date(System.currentTimeMillis()));
        return memo;
    }

    BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && linearLayout == null) {
                init();
            }
        }
    };
}
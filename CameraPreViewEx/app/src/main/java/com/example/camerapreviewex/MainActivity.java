package com.example.camerapreviewex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.IOException;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    Button btnPhoto;
    FrameLayout preViewFrame;
    CameraSurfaceView cameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoPermissions.Companion.loadAllPermissions(this,101);
        btnPhoto=findViewById(R.id.btnPhoto);
        preViewFrame=findViewById(R.id.preViewFrame);
        cameraView=new CameraSurfaceView(this);
        preViewFrame.addView(cameraView);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }//onCreate메서드 끝~~

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this,requestCode,permissions,this);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
            showToast("권한 설정을 거부했습니다.");
        }
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
                    String outUrl= MediaStore.Images.Media.insertImage(getContentResolver(),
                            bitmap,"사진제목","내용 정의");
                    if(outUrl==null){
                        showToast("사진을 갤러리에 저장할 수 없습니다.");
                        return;
                    }else {
                        Uri uri=Uri.parse(outUrl);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    }
                    camera.startPreview();
                }catch (Exception e) {
                    showToast("사진을 찍을 수 없습니다.");
                }
            }
        });
    }

    //토스트 메서드
    void showToast(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }

    public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder holder;
        private Camera camera=null;
        public CameraSurfaceView(Context context) {
            super(context);
            holder=getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera=Camera.open();
            setCameraOrientation();
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                showToast("카메라 미리보기를 생성할 수 있습니다.");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera=null;
        }

        public boolean capture(Camera.PictureCallback callback) {
            if(camera != null) {
                camera.takePicture(null,null, callback);
                return true;
            }else {
                return false;
            }
        }

        public void setCameraOrientation() {
            if(camera==null) {
                return;
            }
            Camera.CameraInfo info=new Camera.CameraInfo();
            Camera.getCameraInfo(0,info);
            WindowManager manager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
            int rotation=manager.getDefaultDisplay().getRotation();
            int degrees=0;
            switch (rotation){
                case Surface.ROTATION_0:
                    degrees=0;
                    break;
                case Surface.ROTATION_90:
                    degrees=90;
                    break;
                case Surface.ROTATION_180:
                    degrees=180;
                    break;
                case Surface.ROTATION_270:
                    degrees=270;
                    break;
            }
            int result;
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result=(info.orientation+degrees) % 360;
                result=(360-result) %360;
            }else{
                result=(info.orientation-degrees+360)%360;
            }
            camera.setDisplayOrientation(result);
        }
    }
}
package com.example.auto_take_picture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/5 0005.
 */
public class AutioTakePhotoActivity extends Activity implements  SurfaceHolder.Callback{
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    Camera mCamera;
    byte[] photoBytes;
    private ImageView image;
    private RelativeLayout rela_result;
    public static int cameraId;
    private boolean isBack=true;
    private FaceView faceView;
    private TextView txt_turn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        initViews();
        holder.addCallback(this);
    }

    private void initViews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        rela_result=(RelativeLayout)findViewById(R.id.result) ;  //在预览界面之上覆盖一层页面，显示拍摄的图片
        image=(ImageView)findViewById(R.id.image);               //显示拍摄的图片
        surfaceView=(SurfaceView)findViewById(R.id.surfaceview);
        holder= surfaceView.getHolder();
        faceView=(FaceView)findViewById(R.id.face_view);        //自定义的view 。用来绘制检测人脸的方框；
        cameraId=findBackCamera();                              //摄像头的id，区分前置还是后置摄像头
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        txt_turn=(TextView)findViewById(R.id.turn);
        txt_turn.setText("前置/后置------当前：后置");
    }


    public void take(View view){          //点击拍照的方法。
        Camera.Parameters parameters =mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                if(b){        //如果焦点获取成功，拍照
                    mCamera.takePicture(null,null,pictureCallBack);  //pictureCallBack 为拍照的回掉。
                }
            }
        });
    }

    /**
     * 摄像头的切换
     * @param view
     */
    public void turn(View view){
            if(isBack){
                cameraId=findFrontCamera();  //前置摄像头id
                txt_turn.setText("前置/后置------当前：前置");
            }else{
                cameraId=findBackCamera();  //后置
                txt_turn.setText("前置/后置------当前：后置");
            }
        isBack=!isBack;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release(); // 释放照相机
        }
        mCamera = Camera.open(cameraId);
        setCameraParams(1080,1920);//随便设的值
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mainHandler.sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 点击完成保存图片，并将路径回传给上个页面
     * @param view
     */
    public void finish(View view){          //完成
        File file=new File(Environment.getExternalStorageDirectory().getPath() + "/test2.jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fo=new FileOutputStream(file);
            fo.write(photoBytes);
            fo.close();
            Toast.makeText(AutioTakePhotoActivity.this,"已保存图片", Toast.LENGTH_SHORT).show();
            Intent intent =new Intent(AutioTakePhotoActivity.this,AutioTakeActiity.class);                         //完成拍照，回到mainActivity,并将图片的路径回传
            intent.putExtra("picPath", Environment.getExternalStorageDirectory().getPath() + "/test2.jpg");
            intent.putExtra("isBack",isBack);
            setResult(RESULT_OK,intent);
            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消，放弃该图片
     * @param view
     */
    public void cancle(View view){          //取消
        photoBytes=null;
        rela_result.setVisibility(View.GONE);
        mCamera.startPreview();
    }


    /**
     * 显示拍出的图片
     */
    private  void show(byte[] photoBytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        Matrix matrix=new Matrix();
        if(isBack){  //后置摄像头
            matrix.setRotate(90);
        }else{
            matrix.setRotate(-90); //前置摄像头
        }
        Bitmap bit=  bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
        }
        if(bit!=null){
            image.setImageBitmap(bit);
        }
        rela_result.setVisibility(View.VISIBLE);
    }

    private Camera.PictureCallback  pictureCallBack =new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {     //bytes 即是拍照回来的内容，将内容写入本地即可
            photoBytes=bytes;
            show(photoBytes);
        }
    };
    /**
     * 拿到前置摄像头id
     */
    public static int findFrontCamera() {
        int cameraId = -1;
        int numberCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    /**
     * 拿到后置摄像头id
     */
    public static int findBackCamera() {
        int cameraId = -1;
        int numberCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("zjun","surfaceCreated");
        try {
            if(mCamera==null){
                mCamera= Camera.open(cameraId);

                mCamera.setPreviewDisplay(holder);
               setCameraParams(1080,1920);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("zjun","surfaceChanged");
        mCamera.startPreview();
        mainHandler.sendEmptyMessage(1);
    }



    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.release(); // 释放照相机
            mCamera = null;
        }
    }

    /**
     * 设置参数
     * @param width
     * @param height
     */
    private void setCameraParams(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        /**从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            picSize = parameters.getPictureSize();
        }
        // 根据选出的PictureSize重新设置SurfaceView大小
        parameters.setPictureSize(picSize.width,picSize.height);

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.e("zjun", "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);

    }

    private MainHandler mainHandler = new MainHandler();

    private void startGoogleDetect() {
        Camera.Parameters parameters =mCamera.getParameters();
        if (parameters.getMaxNumDetectedFaces() > 0) {
            if (faceView != null) {
                faceView.clearFaces();
                faceView.setVisibility(View.VISIBLE);
            }
            mCamera.setFaceDetectionListener(new GoogleDetectListenerImpl(AutioTakePhotoActivity.this, mainHandler));
            mCamera.startFaceDetection();
        }
    }
    private class MainHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case 1:
                    startGoogleDetect();
                    Log.e("renlei110", "开启人脸识别");
                    break;
                case 2:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Camera.Face[] faces = (Camera.Face[]) msg.obj;
                            faceView.setFaces(faces);
                            Log.e("renlei111", "收到人脸识别的信息");
                        }
                    });

                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     *            h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i("zjun", "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

}

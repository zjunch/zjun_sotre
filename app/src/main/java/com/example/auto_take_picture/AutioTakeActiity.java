package com.example.auto_take_picture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/5/5 0005.
 */
public class AutioTakeActiity extends Activity {
    private static final int REQUEST_CAMERA_CODE_TEACHER = 1;// 拍照
    private ImageView ima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto_result);
        ima=(ImageView)findViewById(R.id.ima);
    }



    /**
     * 点击拍照
     * @param view
     */
    public void takePhoto(View view){
        getPerssiom();
    }

    /**
     * android6.0 申请拍照权限
     */
    private void getPerssiom(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            gotoTake();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_CODE_TEACHER);
        }
    }

    /**
     * 跳转至自定义的拍照页面
     */
    private void gotoTake(){
        Intent intent =new Intent(this,AutioTakePhotoActivity.class);
        startActivityForResult(intent ,1);
    }


    /**
     * 拍照完成之后拿到路径设置imageview
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1){
               boolean isBack= data.getBooleanExtra("isBack",true);
                String path  =  data.getStringExtra("picPath");
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                Matrix matrix=new Matrix();
                if(isBack){//由于拍照时旋转了90度。这里通矩阵旋转回来。(后置摄像头)
                    matrix.setRotate(90);
                }else{
                    matrix.setRotate(-90);             // ( 前置摄像头)
                }
                Bitmap bit=  bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                ima.setImageBitmap(bit);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_CODE_TEACHER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    gotoTake();
                } else {
                    Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }



}

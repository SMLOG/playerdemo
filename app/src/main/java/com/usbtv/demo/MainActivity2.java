package com.usbtv.demo;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity-Test";
    String rootPath = "";  //外置 SD 卡路径
    private List<String> unPermissionList = new ArrayList<String>(); //申请未得到授权的权限列表
    private final int RequestCode = 100;//权限请求码
    private AlertDialog mPermissionDialog;
    private String mPackName ;  //获取 a'p'k 包名
    private String[] permissionList = new String[]{    //申请的权限列表
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        checkPermission();
        mPackName = getPackageName();
        rootPath = getStoragePath(this,true);  //获取 可以插拔的 sd 卡  路径
        Log.i(TAG," rootPath： " + rootPath);
        if (DocumentsUtils.checkWritableRootPath(this, rootPath)) {   //检查sd卡路径是否有 权限 没有显示dialog
            showOpenDocumentTree();
        }




    }

    private void showOpenDocumentTree() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = getSystemService(StorageManager.class);

            StorageVolume volume = sm.getStorageVolume(new File(rootPath));

            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }

        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DocumentsUtils.OPEN_DOCUMENT_TREE_CODE:
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    DocumentsUtils.saveTreeUri(this, rootPath, uri);

                }
                break;
            default:
                break;
        }
    }

    //权限判断和申请
    public void checkPermission() {
        unPermissionList.clear();//清空申请的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissionList.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissionList[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                unPermissionList.add(permissionList[i]);//添加还未授予的权限到unPermissionList中
            }
        }

        //有权限没有通过，需要申请
        if (unPermissionList.size() > 0) {
            ActivityCompat.requestPermissions( this,permissionList, 100);
            Log.i(TAG, "check 有权限未通过");
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.i(TAG, "check 权限都已经申请通过");
            try {
                writeDataToSD();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 5.请求权限后回调的方法
     *
     * @param requestCode  是我们自己定义的权限请求码
     * @param permissions  是我们请求的权限名称数组
     * @param grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限
     *                     名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG,"申请结果反馈");
        boolean hasPermissionDismiss = false;
        if (100 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true; //有权限没有通过
                    Log.i(TAG,"有权限没有被通过");
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {//如果有没有被允许的权限
            showPermissionDialog();
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.i(TAG, "onRequestPermissionsResult 权限都已经申请通过");

        }
    }


    /**
     * 不再提示权限时的展示对话框
     */

    private void showPermissionDialog() {


        Log.i(TAG,"mPackName: " + mPackName);
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + mPackName);     //去设置里面设置
                            Intent intent = new Intent(Settings.
                                    ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();


    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }




    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储路径，true返回外置SD卡路径
     * @return
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


    public  void writeDataToSD() throws IOException {
        //写文件的例子 文件不存在,会创建
        String  str = "just a test\n";
        String strRead = "";

        String  sdkOut = getStoragePath(this,true);  //获取 可以插拔的 sd 卡  路径
        Log.i(TAG,"sdkOut:"+sdkOut);
        String  filePath = sdkOut + "/test";
        Log.i(TAG," sdkOut: " + filePath);
        File file = new File(filePath);
        if (!file.exists()){
            if(DocumentsUtils.mkdirs(this,file)){
                Log.i(TAG,"创建文件夹：" + filePath);
            }else{
                Log.i(TAG,"创建文件夹失败：" + filePath);
            }

        }

        String  fileWritePath = filePath + "/test.txt";
        File fileWrite = new File(fileWritePath);


        Log.i(TAG,"  准备写入" );
        try {
            OutputStream outputStream = DocumentsUtils.getOutputStream(this,fileWrite);  //获取输出流
            //  OutputStream outputStream = new FileOutputStream(fileWrite);
            outputStream.write(str.getBytes());
            outputStream.close();
            Log.i(TAG," 写入成功" );
            Toast.makeText(this,"路径：" + fileWritePath + "成功",Toast.LENGTH_SHORT ).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG," 写入失败" );
            Toast.makeText(this,"路径：" + fileWritePath + "失败",Toast.LENGTH_SHORT ).show();
        }



    }

    @Override
    public void onClick(View view) {

    }
}
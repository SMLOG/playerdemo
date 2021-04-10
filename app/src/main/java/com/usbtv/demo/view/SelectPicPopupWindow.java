package com.usbtv.demo.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.usbtv.demo.R;

import java.util.Timer;
import java.util.TimerTask;

public class SelectPicPopupWindow extends Activity implements OnClickListener{
    private Button btn_take_photo, btn_pick_photo, btn_cancel;
    private LinearLayout layout;
    private TextView tip_text;
    private int time = 10*60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);
        btn_take_photo = (Button) this.findViewById(R.id.btn_take_photo);
        btn_pick_photo = (Button) this.findViewById(R.id.btn_pick_photo);
        btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
        tip_text = (TextView) this.findViewById(R.id.tip_text);
        layout=(LinearLayout)findViewById(R.id.pop_layout);


        //添加选择窗口范围监听可以优先获取触点，即不再执行onTouchEvent()函数，点击其他地方时执行onTouchEvent()函数销毁Activity
        layout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！",
                        Toast.LENGTH_SHORT).show();
            }
        });
        //添加按钮监听
        btn_cancel.setOnClickListener(this);
        btn_pick_photo.setOnClickListener(this);
        btn_take_photo.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timer timer = new Timer();
        Handler delayHandle = new Handler();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                delayHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        time--;
                        tip_text.setText("休息10钟,还需等待 "+time+" 秒");
                        if(time<=0){
                            try{
                                timer.cancel();
                                SelectPicPopupWindow.this.finish();

                                // Utils.exec("id");
                                // Utils.exec("input keyevent 26");
                                //Utils.exec("sendevent /dev/input/event0 0 0 0");
                                System.out.print("OK");
                                //  Runtime.getRuntime().exec("sendevent /dev/input/event0 1 116 1");
                                //  Runtime.getRuntime().exec("sendevent /dev/input/event0 0 0 0");
                            }catch (Throwable e){
                                e.printStackTrace();
                            }

                            //PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            //mPowerManager.reboot("standby");
                            return;
                        }
                    }
                },0);
            }
        }, 0, 1000);

    }

    //实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event){
        finish();
        return true;
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_photo:
                break;
            case R.id.btn_pick_photo:
                break;
            case R.id.btn_cancel:
                break;
            default:
                break;
        }
        finish();
    }
}
package com.nurmemet.nur.nurvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nurmemet.nur.nurvideoplayer.listener.OnMediaListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import tv.danmaku.ijk.media.example.application.Settings;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Nurmemet on 2020/4/9
 * Email: nur01@qq.com
 * qq:643229571
 * <p>
 * ijkPlayer的封装
 */
public class TvVideoPlayer extends LinearLayout implements View.OnClickListener {
    private final String TAG = "TvVideoPlayer";
    private IMediaPlayer iMediaPlayer;
    private final float MAX_LIANG_DU = 255f;
    private final AudioManager am;
    private final float maxVolume;
    private IjkVideoView mVideoView;
    private View mTitleControl, mBottomControl, mVolumeControl, mCenterSBLayout, mLayoutBox, mProgressBar, mCenterPlayBtn;
    private ImageView mBgImage, mBackIv, mVolumeIV, mScreenView, mLockImage, mRCImage, volumeIcon, mVideoSeekBarImage;

    private TextView mVideoMaxLenTv, mTitleView, mVideoDurationTv, mVideoSeekBarTimeTv, mVideoSeekBarMaxTime;
    private NurPlayButton mPlayBtn;
    private SeekBar mVideoSeekBar, mVolumeSeekBar;
    private RelativeLayout mAdverLayout, mMaxAdverLayout;
    private Context mContext;
    private boolean controlIsShow = true;//控制器在是否显示
    private boolean showControll = false;//正在滑动progress
    private boolean isDragSeekProgress = false;//正在滑动progress
    private boolean isLock;
    private Handler mControlHandler;
    private Handler mUiHandler;
    private boolean isShowVolumeControl;
    private boolean isTouchLRScreen;
    private int oldVolumeProgress;
    private int oldLiangduProgress;
    private Activity mActivity;
    private OnClickListener onBackPressListener;
    private int mVideoViewHeight, mVideoViewWidth;
    private Runnable mUiRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
        }
    };


    /**
     * 控制器自动关闭
     */
    private Runnable mControlRunnable = new Runnable() {
        @Override
        public void run() {
            if (controlIsShow && !showControll &&!isDragSeekProgress) {
                changeControl();
            }
        }
    };


    /**
     * （亮度/声音）
     */
    private Runnable mLRControlRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowVolumeControl && !isTouchLRScreen) {
                changeVolumeControl();
            }
        }
    };

    private Runnable resetIsDragSeekProgressRunnable = new Runnable() {
        @Override
        public void run() {
            isDragSeekProgress=false;
        }
    };
    private OnMediaListener mediaListener;


    public TvVideoPlayer(Context context) {
        this(context, null);
    }

    public TvVideoPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvVideoPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.nur_video_layout, this);
        Settings mSettings = new Settings(context);
        mUiHandler = new Handler();
        mControlHandler = new Handler();
        initLayout();
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 3
        float current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float a = maxVolume / 200;
        oldVolumeProgress = (int) (current / a);

        mVideoView.setOnPreparedListener(preparedListener);
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                if (what == -10000) {
                    Toast.makeText(mContext, "网路未连接，请检查网络设置", Toast.LENGTH_SHORT).show();
                    pause();
                    //return true;
                }
                if(mediaListener!=null){
                    return  mediaListener.onError(mp,what,extra);
                }
                return false;
            }
        });
    }


    /**
     * 初始化View
     */
    private void initLayout() {
        mVideoView = findViewById(R.id.nur_ijk_video_player);
        mVideoView.setHudView((TableLayout) findViewById(R.id.hud_view));

        mTitleView = findViewById(R.id.nur_videoName);
        mVolumeIV = findViewById(R.id.nur_video_ktvIv);

        mPlayBtn = findViewById(R.id.nur_video_playIv);
        mScreenView = findViewById(R.id.nur_video_changeWindowTv);
        mBackIv = findViewById(R.id.nur_video_backIv);

        mTitleControl = findViewById(R.id.nur_video_toolbarControl);
        mBottomControl = findViewById(R.id.nur_video_bottomControl);
        mLockImage = findViewById(R.id.nur_video_view_LockIv);
        mRCImage = findViewById(R.id.nur_video_view_RC_btn);
        mAdverLayout = findViewById(R.id.nur_video_adver_layout);
        mMaxAdverLayout = findViewById(R.id.nur_video_max_adver_layout);
        mLayoutBox = findViewById(R.id.nur_ijk_video_player_box);

        mVideoDurationTv = findViewById(R.id.nur_video_videoSeekTv);
        mVideoMaxLenTv = findViewById(R.id.nur_video_videoDur);

        mCenterPlayBtn = findViewById(R.id.nur_video_centerPlayBtn);

        mVideoSeekBar = findViewById(R.id.nur_video_seekBar);

        mBgImage = findViewById(R.id.nur_video_bgImage);


        mCenterSBLayout = findViewById(R.id.nur_videoSeekBarBox);
        mVideoSeekBarImage = findViewById(R.id.nur_videoSeekBarImage);
        mVideoSeekBarTimeTv = findViewById(R.id.nur_videoSeekBarTimeTv);
        mVideoSeekBarMaxTime = findViewById(R.id.nur_videoSeekBarMaxTime);

        mProgressBar = findViewById(R.id.nur_video_progressBar);

        mLayoutBox.setOnTouchListener(new NurOnTouch(mContext, nurTouchListener));
        mLockImage.setOnClickListener(this);
        mCenterPlayBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mScreenView.setOnClickListener(this);

        mVideoSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        mVolumeControl = findViewById(R.id.nur_video_volumeControl);
        mVolumeSeekBar = findViewById(R.id.nur_volumeSeekBar);
        volumeIcon = findViewById(R.id.nur_video_volumeIcon);

    }

    /**
     * 背景色
     */
    public void setBgColor(@ColorInt int color) {
        mLayoutBox.setBackgroundColor(color);
    }


    /**
     * 声道控制器
     *
     * @return
     */
    public ImageView getVolumeImageView() {
        return mVolumeIV;
    }

    /**
     * back image view
     */
    public View getBackIv() {
        return mBackIv;
    }

    /**
     * 小广告view
     */
    public RelativeLayout getMinADLayout() {
        return mAdverLayout;
    }

    /**
     * 小广告view
     */
    public void setMinADLayout(View v) {
        mAdverLayout.removeAllViews();
        mAdverLayout.addView(v);
    }

    /**
     * 大广告view
     */
    public RelativeLayout getMaxADLayout() {
        return mMaxAdverLayout;
    }

    /**
     * 大广告view
     */
    public void setMaxADLayout(View v) {
        mMaxAdverLayout.addView(v);
    }

    public ImageView getRCImage() {
        mRCImage.setVisibility(VISIBLE);
        return mRCImage;
    }

    /**
     * 获取media player
     */
    public IMediaPlayer getMediaPlayer() {
        return iMediaPlayer;
    }

    /**
     * 关闭全部控制器
     */
    public void hideControllers() {
        if (controlIsShow) {
            changeControl();
        }

        mProgressBar.setVisibility(View.INVISIBLE);
        mCenterPlayBtn.setVisibility(INVISIBLE);
    }

    /**
     * 打开全部控制器
     */
    public void showControllers() {
        if (!controlIsShow)
            changeControl();
    }

    /**
     * 全屏按钮
     */
    public ImageView getScreenView() {
        return mScreenView;
    }

    /**
     * 更新（播放进度等等）
     */
    private int oldDuration = -1111;
    private int videoMaxDuration = -11;
    private boolean _startPlay = false;


    /**
     * 更新（播放进度等等）
     */
    private void updateUI() {
        int progress = mVideoView.getCurrentPosition();
        boolean playing = mVideoView.isPlaying();
        if (playing) {
            if (oldDuration == progress && videoMaxDuration != progress) {
                mProgressBar.setVisibility(View.VISIBLE);
                mUiHandler.postDelayed(mUiRunnable, 50);
                return;
            } else {
                mBgImage.setVisibility(GONE);
                if (mProgressBar.getVisibility() != INVISIBLE)
                    mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
        oldDuration = progress;
        if (!showControll) {
            mVideoDurationTv.setText(stringForTime(progress));
            mVideoSeekBar.setProgress(progress);
            if (mediaListener != null) {
                mediaListener.onProgress(progress, videoMaxDuration);
            }
        }
        if (playing) {
            if (mediaListener != null && !_startPlay) {
                mediaListener.onStart();
            }
            _startPlay = true;
        }
        if (playing || (!_startPlay && progress != videoMaxDuration)) {
            if (mCenterPlayBtn.getVisibility() != INVISIBLE) {
                mCenterPlayBtn.setVisibility(INVISIBLE);
                mPlayBtn.change(false);
            }
            mUiHandler.postDelayed(mUiRunnable, 50);
        } else {
            mCenterPlayBtn.setVisibility(VISIBLE);
            mPlayBtn.change(true);
            if (mediaListener != null)
                mediaListener.onEndPlay();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.nur_video_centerPlayBtn) {
            start();
        } else if (id == R.id.nur_video_view_LockIv) {
            controlIsShow = true;
            changeControl();
            isLock = !isLock;
            if (isLock)
                mLockImage.setImageResource(R.mipmap.nur_ic_lock);
            else
                mLockImage.setImageResource(R.mipmap.nur_ic_unlock);
        } else if (id == R.id.nur_video_playIv) {
            if (!isLock) {
                if (mVideoView.isPlaying())
                    pause();
                else start();
            }
        } else if (id == R.id.nur_video_backIv) {
            if (onBackPressListener != null) {
                onBackPressListener.onClick(v);
            }
        }
    }

    /**
     * 返回按钮点击
     */
    public void setOnBackPressListener(OnClickListener onBackPressListener) {
        this.onBackPressListener = onBackPressListener;
    }

    /**
     * 获取背景view
     *
     * @return
     */
    public ImageView getThumbImageView() {
        return mBgImage;
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void initPlayer(Activity activity, Uri uri, String title) {
        if (title != null)
            mTitleView.setText(title);
        mActivity = activity;
        mVideoView.setVideoURI(uri);
        autoDismiss();
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }


    /**
     * 开始播放
     */
    public void start() {
        start(0);
    }

    /**
     * 开始播放
     */
    public void start(int progress) {
        mCenterPlayBtn.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(VISIBLE);
        mPlayBtn.change(false);
        if (progress > 0) {
            mVideoView.seekTo(progress);
        }
        mVideoView.start();
        autoDismiss();
        mUiHandler.removeCallbacks(mUiRunnable);
        mUiHandler.postDelayed(mUiRunnable, 50);
    }


    /**
     * 暂停
     */
    public void pause() {
        mUiHandler.removeCallbacks(mUiRunnable);
        mCenterPlayBtn.setVisibility(VISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
        mPlayBtn.change(true);
        mVideoView.pause();

        if (mediaListener != null) {
            mediaListener.onPause();
        }
    }

    /**
     * 视频加载完成, 准备好播放视频的回调
     */
    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            iMediaPlayer = mp;

            videoMaxDuration = mVideoView.getDuration();
            mVideoViewHeight = mp.getVideoHeight();
            mVideoViewWidth = mp.getVideoWidth();
            mVideoSeekBar.setMax(videoMaxDuration);
            mVideoMaxLenTv.setText(stringForTime(videoMaxDuration));
            mProgressBar.setVisibility(INVISIBLE);
        }
    };

    /**
     * 获取视频高宽度
     */
    public int[] getVideoWH() {
        return new int[]{mVideoViewWidth, mVideoViewHeight};
    }

    /**
     * 获取ObjectAnimator
     */
    private ObjectAnimator getObjectAnimator(float start, float end, String propertyName, View view) {
        return ObjectAnimator.ofFloat(view, propertyName, start, end);
    }

    /**
     * 获取ObjectAnimators
     */
    private List<Animator> getObjectAnimator(float start, float end, String propertyName, View... view) {
        if (view == null) {
            return null;
        }
        List<Animator> animators = new ArrayList<>();
        int length = view.length;
        for (int i = 0; i < length; i++) {
            animators.add(getObjectAnimator(start, end, propertyName, view[i]));
        }
        return animators;
    }

    /**
     * 开始动画
     */
    private void startAnim(View view, float start, float end, String propertyName) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, propertyName, start, end);
        anim.setDuration(350);
        anim.start();
    }

    /**
     * 开始动画
     */
    private void startAnim(List<Animator> animators) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setDuration(350);
        animatorSet.start();
    }


    /**
     * 显示(隐藏)控制器
     */
    private void changeControl() {
        int dp_56 = dip2px(-56);

        int anim1Start = 0;
        int anim2Start = 0;
        int anim3Start = 0;

        int rcAnimStart = 0;

        int anim1End = dip2px(40);
        int anim2End = dip2px(-66);
        int anim3End = dp_56;

        int rcAnimEnd = dip2px(56);

        if (!controlIsShow) {//要显示（现在的状态是隐藏）
            anim1Start = anim1End;
            anim2Start = anim2End;
            anim3Start = anim3End;

            rcAnimStart = rcAnimEnd;

            anim1End = 0;
            anim2End = 0;
            anim3End = 0;
            rcAnimEnd = 0;
        }
        String translationY = "translationY";
        String translationX = "translationX";
        ObjectAnimator objectAnimator = getObjectAnimator(anim3Start, anim3End, translationX, mLockImage);
        if (isLock) {
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(objectAnimator);
            startAnim(animators);
        } else {
            List<Animator> animator = getObjectAnimator(anim1Start, anim1End, translationY, mAdverLayout, mBottomControl);
            animator.add(getObjectAnimator(anim2Start, anim2End, translationY, mTitleControl));
            animator.add(getObjectAnimator(rcAnimStart, rcAnimEnd, translationX, mRCImage));
            animator.add(objectAnimator);
            startAnim(animator);
        }
        controlIsShow = !controlIsShow;
        autoDismiss();
    }

    /**
     * 3秒后知道关闭（隐藏）
     * 显示的话3秒后知道隐藏
     */
    private void autoDismiss() {
        if (controlIsShow) {
            mControlHandler.removeCallbacks(mControlRunnable);
            mControlHandler.postDelayed(mControlRunnable, 3000);
        }
    }

    /**
     * 3秒后知道关闭（隐藏）
     * 显示的话3秒后知道隐藏
     */
    private void autoDismiss(Runnable runnable) {
        if (runnable != null) {
            mControlHandler.removeCallbacks(runnable);
            mControlHandler.postDelayed(runnable, 1000);
        }
    }

    /**
     * seekBarChangeListener
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (showControll) {
                videoSeekChange(progress);
            }else if(fromUser){
                isDragSeekProgress=true;
                mUiHandler.removeCallbacks(resetIsDragSeekProgressRunnable);
                mUiHandler.postDelayed(resetIsDragSeekProgressRunnable,3000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            showControll = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVideoView.seekTo(seekBar.getProgress());
            autoDismiss();
            hideVideoSeek();
            showControll = false;
        }
    };

    /**
     * 显示中间seek
     */
    private void videoSeekChange(int progress) {
        if (mCenterSBLayout.getVisibility() != VISIBLE) {
            mCenterSBLayout.setVisibility(VISIBLE);
            _playBtnIsShow = mCenterPlayBtn.getVisibility() == VISIBLE;
            mCenterPlayBtn.setVisibility(INVISIBLE);
        }
        if (progress > oldProgress) {
            mVideoSeekBarImage.setImageResource(R.mipmap.nur_ic_kuaijin_r);
        } else {
            mVideoSeekBarImage.setImageResource(R.mipmap.nur_ic_kuaijin);
        }
        int max = videoMaxDuration;
        if (progress < 0) {
            progress = 0;
        } else if (progress > max) {
            progress = max;
        }

        String play_time = stringForTime(progress);
        String play_sum_time = stringForTime(max);
        mVideoSeekBarTimeTv.setText(play_time);
        mVideoSeekBarMaxTime.setText("/ " + play_sum_time);

        if (oldProgress == 0) {
            oldProgress = progress;
        }
        moveProgress = progress;
    }

    private int oldProgress = 0;
    private int moveProgress;
    private boolean _playBtnIsShow = false;

    /**
     * 关闭中间seek
     */
    private void hideVideoSeek() {
        oldProgress = 0;
        mVideoView.seekTo(moveProgress);
        if (_playBtnIsShow) {
            mCenterPlayBtn.setVisibility(VISIBLE);
        }
        mCenterSBLayout.setVisibility(INVISIBLE);
    }

    /**
     * NurTouchListener
     * （单）双击-滑动等等
     */
    private NurOnTouch.NurTouchListener nurTouchListener = new NurOnTouch.NurTouchListener() {
        @Override
        public void onClick() {
            changeControl();
        }

        @Override
        public void onDoubleClick() {
            if (!isLock) {
                if (mVideoView.isPlaying())
                    pause();
                else start();
            }
        }

        @Override
        public void onMoveSeek(float f) {
            if (isLock || !mVideoView.isPlaying()) {
                return;
            }
            int progress = (int) (mVideoView.getCurrentPosition() + (100 * f));
            videoSeekChange(progress);
        }

        @Override
        public void onMoveLeft(float f) {
            if (isLock) {
                return;
            }
            setVolume(f);
        }

        @Override
        public void onMoveRight(float f) {
            if (isLock) {
                return;
            }
            int progress = (int) f + oldLiangduProgress;
            if (progress > MAX_LIANG_DU) {
                progress = (int) MAX_LIANG_DU;
            }
            if (progress < 0) {
                progress = 1;
            }
            mVolumeSeekBar.setMax((int) MAX_LIANG_DU);
            setWindowBrightness(progress);
            setProgress(progress, R.mipmap.nur_ic_brightness);
        }

        @Override
        public void onActionUp(int changeType) {
            if (isLock) {
                return;
            }
            if (changeType == NurOnTouch.changeTypeVideoSeek) {
                hideVideoSeek();
            } else if (changeType == NurOnTouch.changeTypeLiangdu) {
                oldLiangduProgress = mVolumeSeekBar.getProgress();
                isTouchLRScreen = false;
                autoDismiss(mLRControlRunnable);
            } else if (changeType == NurOnTouch.changeTypeVolume) {
                isTouchLRScreen = false;
                autoDismiss(mLRControlRunnable);
                oldVolumeProgress = mVolumeSeekBar.getProgress();
            }
        }
    };

    /**
     * 设置亮度
     */
    private void setWindowBrightness(int brightness) {
        if (mActivity == null) return;
        Window window = mActivity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    /**
     * 显示-隐藏 ---亮度&声音
     */
    private void changeVolumeControl() {
        float start = dip2px(80);
        float end = dip2px(-30);
        if (!isShowVolumeControl) {
            start = end;
            end = dip2px(80);
        }
        startAnim(mVolumeControl, start, end, "translationY");
        isShowVolumeControl = !isShowVolumeControl;
    }

    /**
     * 声音
     */
    private void setVolume(float f) {
        mVolumeSeekBar.setMax(200);

        int progress = (int) f + oldVolumeProgress;
        int res = R.mipmap.nur_ic_volume;
        if (progress <= 0) {
            res = R.mipmap.nur_ic_volume_x;
        }
        setProgress(progress, res);
        if (am == null) {
            return;
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) ((maxVolume / 200) * progress), 0);
    }

    /**
     * 监听声音大小
     */
    public boolean onKeyDown(int keyCode) {
        int value = 10;
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                setVolume(value);
                oldVolumeProgress += value;
                ret = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setVolume(-value);
                oldVolumeProgress -= value;
                ret = true;
                break;

            case KeyEvent.KEYCODE_ENTER:     //确定键enter
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if(!isDragSeekProgress)
                {
                    changeControl();
                    mVideoSeekBar.requestFocus();
                    isDragSeekProgress=true;
                    mUiHandler.removeCallbacks(resetIsDragSeekProgressRunnable);
                    mUiHandler.postDelayed(resetIsDragSeekProgressRunnable,3000);

                }else{
                    if(mVideoView.isPlaying())
                        this.pause();
                    else this.start(this.oldProgress);
                }


                break;
        }
        isTouchLRScreen = false;
        autoDismiss(mLRControlRunnable);
        return ret;
    }

    /**
     * 亮度（声音）的seek bar
     */
    private void setProgress(int progress, int res) {
        isTouchLRScreen = true;
        volumeIcon.setImageResource(res);
        mVolumeSeekBar.setProgress(progress);
        if (!isShowVolumeControl)
            changeVolumeControl();
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    StringBuilder mFormatBuilder = new StringBuilder();
    Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    /**
     * 将长度转换为时间
     *
     * @param timeMs
     * @return
     */
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //      /**
//     * release
//     */
    public void stopPlayback() {
        mVideoView.stopPlayback();
        mVideoView.release(true);
        mVideoView.stopBackgroundPlay();
        IjkMediaPlayer.native_profileEnd();
    }

    /**
     * 监听（播放，暂停）
     */
    public void setOnMediaListener(OnMediaListener mediaListener) {
        this.mediaListener = mediaListener;
    }
}

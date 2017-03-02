package bruce.chang.project01;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import bruce.chang.project01.utils.CacheFileUtils;
import bruce.chang.project01.utils.DensityUtils;

/**
 * 视频播放器
 * 自定义 controller 页面
 */
public class CustomVideoViewActivity extends AppCompatActivity {

    //更新页面数据
    private static final int UPDATEUI = 1;
    //点击屏幕旋转后，开启自动旋转功能
    private static final int AUTO_ROTATION = 2;
    //隐藏桌面控制
    public static final int HIDE_CONTROL = 3;
    CustomVideoView videoView;
    LinearLayout controlarbar_layout;
    SeekBar play_seek;//播放的进度条
    ImageView pause_image;//暂停播放按钮
    TextView time_current_tv;//当前时间
    TextView time_total_tv;//总的时间
    ImageView volume_img;//音量图片
    SeekBar volume_seek;//音量的进度条
    ImageView screen_img;//全屏的图片
    int screenWidth, screenHeight;
    RelativeLayout video_layout;

    AudioManager mAudioManager;
    private int streamMaxVolume;
    private int streamVolume;
    private boolean ifFullScreen;

    FrameLayout progress_layout;//亮度和音量所在的布局。
    ImageView operation_bg;//音量  还是  亮度图片
    ImageView operation_percent;//进度


    float lastX = 0, lastY = 0;
    //滑动的范围
    float threshold = 54;
    // 是否符合在Y 轴滑动
    private boolean isAdjust = false;

    private VolumeReceiver mVolumeReceiver;// 声音的接收者
    Button btNextAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        //获取系统音频服务
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initListener();

        //当前设备的最大音量
        streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //设备当前的音量
        streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(streamMaxVolume);
        volume_seek.setProgress(streamVolume);
        if (streamVolume == 0) {
            volume_img.setImageResource(R.drawable.mute);
        }

        String path = CacheFileUtils.empVideoPath + File.separator + "demo.mp4";
        videoView.setVideoURI(Uri.parse(path));// 设置视频路径

        mVolumeReceiver = new VolumeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.canPause()) {
            pause_image.setImageResource(R.drawable.pause_btn_style);
            videoView.pause();
        }
        if (uiHandler.hasMessages(UPDATEUI)) {
            uiHandler.removeMessages(UPDATEUI);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        pause_image.setImageResource(R.drawable.pause_btn_style);
        videoView.start();
        uiHandler.sendEmptyMessage(UPDATEUI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (videoView.canPause()) {
            videoView.stopPlayback();
        }
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(0);// 移除所有消息
            uiHandler = null;
        }

        if (mVolumeReceiver != null) {
            unregisterReceiver(mVolumeReceiver);
        }
    }

    /**
     * 根据毫秒数，格式化时间，并显示
     *
     * @param textView
     * @param millSeconds
     */
    private void updateTextViewWithTimeFormat(TextView textView, int millSeconds) {
        int second = millSeconds / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String str;
        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        textView.setText(str);
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            {
                switch (msg.what) {
                    case UPDATEUI:

                        //获取视频当前播放时间
                        int currentPosition = videoView.getCurrentPosition();
                        //获取视频播放的总时间
                        int totalDuration = videoView.getDuration();

                        updateTextViewWithTimeFormat(time_current_tv, currentPosition);
                        updateTextViewWithTimeFormat(time_total_tv, totalDuration);
                        play_seek.setMax(totalDuration);
                        play_seek.setProgress(currentPosition);
                        //自己给自己刷新的过程
                        uiHandler.sendEmptyMessageDelayed(UPDATEUI, 500);

                        break;

                    case HIDE_CONTROL:
                        controlarbar_layout.setVisibility(View.GONE);
                        break;
                    case AUTO_ROTATION:
                        //开启自动旋转，响应屏幕旋转事件
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        uiHandler.removeMessages(AUTO_ROTATION);
                        break;

                }
            }

        }
    };

    /**
     * 监听屏幕方向的改变
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //横屏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setSystemUiHide();// 隐藏最上面那一栏

            setVideoViewScal(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);// 设置为全屏
            volume_img.setVisibility(View.VISIBLE);
            volume_seek.setVisibility(View.VISIBLE);
            screen_img.setImageResource(R.drawable.exit_full_screen);

            // 强制移除半屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ifFullScreen = true;
        } else
        //竖屏
        {

            setSystemUiShow();// 显示最上面那一栏
            setVideoViewScal(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(this, 240));
            volume_img.setVisibility(View.GONE);
            volume_seek.setVisibility(View.GONE);
            screen_img.setImageResource(R.drawable.full_screen);
            // 强制移除全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            ifFullScreen = false;
        }
    }

    /**
     * 设置VideoView的宽和高，
     * 以及设置
     * VideoView父类-->>RelativeLayout的宽高
     *
     * @param width
     * @param height
     */
    private void setVideoViewScal(int width, int height) {
        ViewGroup.LayoutParams params1 = videoView.getLayoutParams();
        params1.width = width;
        params1.height = height;
        videoView.setLayoutParams(params1);

        ViewGroup.LayoutParams params2 = videoView.getLayoutParams();
        params2.width = width;
        params2.height = height;
        video_layout.setLayoutParams(params2);

    }

    private void initListener() {

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                uiHandler.sendEmptyMessage(UPDATEUI);
            }
        });

        /**
         * play_seek 播放SeekBar的事件
         */
        play_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextViewWithTimeFormat(time_current_tv, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                uiHandler.removeMessages(UPDATEUI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                videoView.seekTo(progress);
                uiHandler.sendEmptyMessage(UPDATEUI);
            }
        });

        /**
         * volume_seek  音量SeekBar的事件
         */
        volume_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置当前设备的音量
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /**
         * 暂停按钮的点击事件
         */
        pause_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //正在播放的时候点击视频暂停，变成播放状态，反之相反
                if (videoView.isPlaying()) {
                    pause_image.setImageResource(R.drawable.play_btn_style);
                    videoView.pause();
                    uiHandler.removeMessages(UPDATEUI);
                } else {
                    pause_image.setImageResource(R.drawable.pause_btn_style);
                    videoView.start();
                    uiHandler.sendEmptyMessage(UPDATEUI);
                }
            }
        });
        /**
         * 全屏按钮的点击事件
         */
        screen_img.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              //全屏的时候变成竖屏
                                              if (ifFullScreen) {
                                                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                                  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                                  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                              } else

                                              {
                                                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                                  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                                  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                              }
                                              uiHandler.sendEmptyMessageDelayed(AUTO_ROTATION, 2000);
                                          }
                                      }
        );
        controlarbar_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /**
         * videoView 触摸事件
         */
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ifFullScreen) {

                    float x = event.getX();
                    float y = event.getY();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (controlarbar_layout.getVisibility() == View.GONE) {
                                controlarbar_layout.setVisibility(View.VISIBLE);
                            }

                            lastX = x;
                            lastY = y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //得到滑动时候x、y的变化量
                            float changeX = x - lastX;
                            float changeY = y - lastY;
                            //取绝对值
                            float absX = Math.abs(changeX);
                            float absY = Math.abs(changeY);

                            //x  y两方向都大于threshold，证明两个方向都滑动的比较多
                            if (absX > threshold && absY > threshold) {
                                if (absY > absX) {
                                    isAdjust = true;
                                } else {
                                    isAdjust = false;
                                }
                            } else if (absX > threshold && absY < threshold) {//x 轴滑动的多
                                isAdjust = false;
                            } else if (absX < threshold && absY > threshold) { //y 轴滑动的多
                                isAdjust = true;
                            }

                            if (x < screenHeight / 2) {
                                if (isAdjust) {
                                    changeBrightness(-changeY);
                                }
                            } else {
                                if (isAdjust) {
                                    changeVolume(-changeY);
                                }
                            }

                            lastX = x;
                            lastY = y;

                            break;

                        case MotionEvent.ACTION_UP:
                            lastX = 0;
                            lastY = 0;
                            //隐藏掉音量或者亮度的图标
                            progress_layout.setVisibility(View.GONE);
                            uiHandler.sendEmptyMessageDelayed(HIDE_CONTROL, 2000);
                            break;
                    }
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (controlarbar_layout.getVisibility() == View.GONE) {
                                controlarbar_layout.setVisibility(View.VISIBLE);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            uiHandler.sendEmptyMessageDelayed(HIDE_CONTROL, 2000);
                            break;
                    }
                }
                return true;
            }

        });

        btNextAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomVideoViewActivity.this, VideoViewActivity.class));
            }
        });
    }

    /**
     * 改变手机音量
     */
    private void changeVolume(float offset) {
        if (progress_layout.getVisibility() == View.GONE) {
            progress_layout.setVisibility(View.VISIBLE);
        }
        operation_bg.setImageResource(R.drawable.video_voice_bg);

        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //当前音量的改变值
        int index = (int) (offset / screenWidth * max * 1.25);
        //保证音量改变值大于0，小于max
        int volume = Math.max(current + index, 0);
        volume = Math.min(volume, max);
        //设置音量值
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        volume_seek.setProgress(volume);


        ViewGroup.LayoutParams params = operation_percent.getLayoutParams();
        params.width = (int) (DensityUtils.dp2px(this, 94) * (volume * 1.0f / max));
        operation_percent.setLayoutParams(params);

    }

    /**
     * 改变屏幕亮度
     */
    private void changeBrightness(float offset) {
        if (progress_layout.getVisibility() == View.GONE) {
            progress_layout.setVisibility(View.VISIBLE);
        }
        operation_bg.setImageResource(R.drawable.video_brightness_bg);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        float buttonBrightness = params.screenBrightness;
        float index = offset / screenWidth;

        buttonBrightness = Math.max(buttonBrightness + index, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
        buttonBrightness = Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, buttonBrightness);
        //设置当前亮度
        params.screenBrightness = buttonBrightness;
        getWindow().setAttributes(params);

        ViewGroup.LayoutParams params1 = operation_percent.getLayoutParams();
        params1.width = (int) (DensityUtils.dp2px(this, 94) * buttonBrightness);
        operation_percent.setLayoutParams(params1);
    }

    private void initView() {
        videoView = (CustomVideoView) findViewById(R.id.videoView);
        controlarbar_layout = (LinearLayout) findViewById(R.id.controlarbar_layout);
        play_seek = (SeekBar) findViewById(R.id.play_seek);
        pause_image = (ImageView) findViewById(R.id.pause_image);
        time_current_tv = (TextView) findViewById(R.id.time_current_tv);
        time_total_tv = (TextView) findViewById(R.id.time_total_tv);
        volume_img = (ImageView) findViewById(R.id.volume_img);
        volume_seek = (SeekBar) findViewById(R.id.volume_seek);
        screen_img = (ImageView) findViewById(R.id.screen_img);
        video_layout = (RelativeLayout) findViewById(R.id.video_layout);
        progress_layout = (FrameLayout) findViewById(R.id.progress_layout);
        operation_bg = ((ImageView) findViewById(R.id.operation_bg));
        operation_percent = (ImageView) findViewById(R.id.operation_percent);
        btNextAct = (Button) findViewById(R.id.btNextAct);
    }

    //音量的广播接收器
    public class VolumeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volume == 0) {
                    volume_img.setImageResource(R.drawable.mute);
                } else {
                    volume_img.setImageResource(volume);
                }
                volume_seek.setProgress(volume);

            }
        }

    }


    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }


    // 隐藏SystemUi
    private void setSystemUiHide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // 显示SystemUi
    private void setSystemUiShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

}



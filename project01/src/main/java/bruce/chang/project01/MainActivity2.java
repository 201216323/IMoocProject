package bruce.chang.project01;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * 视频播放器
 * 自定义 controller 页面
 */
public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {


    private static final int UPDATEUI = 1;
    VideoView videoView;
    LinearLayout controlarbar_layout;
    SeekBar play_seek;//播放的进度条
    ImageView pause_image;//暂停播放按钮
    TextView time_current_tv;//当前时间
    TextView time_total_tv;//总的时间
    ImageView volume_img;//音量图片
    SeekBar volume_seek;//音量的进度条
    ImageView screen_img;//全屏的图片


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        videoView = (VideoView) findViewById(R.id.videoView);
        controlarbar_layout = (LinearLayout) findViewById(R.id.controlarbar_layout);
        play_seek = (SeekBar) findViewById(R.id.play_seek);
        pause_image = (ImageView) findViewById(R.id.pause_image);
        time_current_tv = (TextView) findViewById(R.id.time_current_tv);
        time_total_tv = (TextView) findViewById(R.id.time_total_tv);
        volume_seek = (SeekBar) findViewById(R.id.volume_seek);
        screen_img = (ImageView) findViewById(R.id.screen_img);
        pause_image.setOnClickListener(this);
        screen_img.setOnClickListener(this);


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo.mp4";
        /**
         * 本地视频播放
         */
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
        uiHandler.sendEmptyMessage(UPDATEUI);


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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //正在播放的时候点击视频暂停，变成播放状态，反之相反
            case R.id.pause_image:
                if (videoView.isPlaying()) {
                    pause_image.setImageResource(R.drawable.play_btn_style);
                    videoView.pause();
                    uiHandler.removeMessages(UPDATEUI);
                } else {
                    pause_image.setImageResource(R.drawable.pause_btn_style);
                    videoView.start();
                    uiHandler.sendEmptyMessage(UPDATEUI);
                }
                break;

            case R.id.screen_img:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeMessages(UPDATEUI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

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
                if (msg.what == UPDATEUI) {
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
                }
            }
        }
    };

}



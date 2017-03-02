package bruce.chang.project01;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import java.io.File;

import bruce.chang.project01.utils.CacheFileUtils;

/**
 * 视频播放器
 * VideoView 基本使用
 */
public class VideoViewActivity extends AppCompatActivity implements View.OnClickListener {


    CustomVideoView videoView;
    Button btStart, btPause, btResume, btStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (CustomVideoView) findViewById(R.id.videoView);
        btStart = (Button) findViewById(R.id.btStart);
        btPause = (Button) findViewById(R.id.btPause);
        btResume = (Button) findViewById(R.id.btResume);
        btStop = (Button) findViewById(R.id.btStop);


        String path = CacheFileUtils.empVideoPath + File.separator + "demo.mp4";
        videoView.setVideoURI(Uri.parse(path));// 设置视频路径
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo.mp4";
        /**
         * 本地视频播放
         */
//        videoView.setVideoPath(path);
        /**
         * 网络视频播放
         */
//        videoView.setVideoURI(Uri.parse("http://lxdqncdn.miaopai.com/stream/8HpxTaLbJPJQMRtkD4yy5g__.mp4?ssig=83d05ea273b927fd14a8420d5e73875a&time_stamp=1488353584950"));
        /**
         *使用MediaController控制视频播放
         */
        MediaController controller = new MediaController(this);
        /**
         * 设置videoView与MediaController建立关联
         */
        videoView.setMediaController(controller);
        /**
         * MediaController与videoView建立关联
         */
        controller.setMediaPlayer(videoView);


        btStart.setOnClickListener(this);
        btPause.setOnClickListener(this);
        btResume.setOnClickListener(this);
        btStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btStart:
                videoView.start();
                break;
            case R.id.btPause:
                videoView.pause();
                break;
            case R.id.btResume:
                videoView.resume();
                break;

            case R.id.btStop:
                videoView.suspend();
                break;
        }
    }
}

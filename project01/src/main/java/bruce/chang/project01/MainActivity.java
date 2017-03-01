package bruce.chang.project01;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * 视频播放器
 * VideoView 基本使用
 */
public class MainActivity extends AppCompatActivity {


    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.videoView);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo.mp4";
        /**
         * 本地视频播放
         */
        videoView.setVideoPath(path);
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
    }
}

# Project01

## VideoView简单使用：
```
   videoView = (VideoView) findViewById(R.id.videoView);
        /**
         * 本地视频播放
         */
        videoView.setVideoPath("");
        /**
         * 网络视频播放
         */
        videoView.setVideoURI(Uri.parse(""));
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
```




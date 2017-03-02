package bruce.chang.project01;

import android.app.Application;

import bruce.chang.project01.utils.CacheFileUtils;

public class App extends Application {

    private App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        CacheFileUtils.initFiles();
        CacheFileUtils.copyAssetsFilesToSD(mApp, "video", CacheFileUtils.empVideoPath);
    }
}

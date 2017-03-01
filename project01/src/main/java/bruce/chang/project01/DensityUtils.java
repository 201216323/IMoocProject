package bruce.chang.project01;

import android.content.Context;

/**
 * date：2017/2/13
 * des：
 * Create by suqi
 */

public class DensityUtils {


    public static int dp2px(Context mContext,float dpVal) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpVal * density + 0.5f);
    }

    public static int px3dp(Context mContext, float pxVal) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxVal / density + 0.5f);
    }
}

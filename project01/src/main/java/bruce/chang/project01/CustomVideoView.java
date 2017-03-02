package bruce.chang.project01;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by: BruceChang
 * Date on : 2017/3/2.
 * Time on: 10:24
 * Progect_Name:IMoocProject
 * Source Github：
 * Description:
 */

public class CustomVideoView extends VideoView {
    private int defaultWidth;
    private int defaultHeight;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defaultWidth = context.getResources().getDisplayMetrics().widthPixels;
        defaultHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(defaultWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

package fi.digi.savonia.movesense.Tools;

import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class OnSingleClickListener implements OnClickListener {

    private static final long MIN_CLICK_INTERVAL=5000;
    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View view) {

        synchronized (this)
        {
            long currentClickTime= SystemClock.uptimeMillis();
            long elapsedTime=currentClickTime-mLastClickTime;
            mLastClickTime=currentClickTime;

            if(elapsedTime<=MIN_CLICK_INTERVAL)
                return;

            onSingleClick(view);
        }
    }
}

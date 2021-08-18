package fi.digi.savonia.movesense.Tools;

import android.os.Handler;
import android.os.HandlerThread;

public class ConcurrencyHelper {

    private HandlerThread networkThread = new HandlerThread("networkThread");
    private HandlerThread dataThread = new HandlerThread("dataThread");
    private Handler networkHandler;
    private Handler dataHandler;

    public enum ThreadType
    {
        Network,
        Data
    }

    private static volatile ConcurrencyHelper instance;

    public static ConcurrencyHelper GetInstance()
    {
        if(instance == null)
        {
            synchronized (ConcurrencyHelper.class){
                if(instance==null){
                    instance = new ConcurrencyHelper();
                }
            }
        }
        return instance;
    }

    private ConcurrencyHelper()
    {
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper());
        dataThread.start();
        dataHandler = new Handler(dataThread.getLooper());

    }

    public void Run(Runnable runnable, ThreadType threadType)
    {
        switch (threadType)
        {
            case Network:
                networkHandler.post(runnable);
                break;
            case Data:
                dataHandler.post(runnable);
                break;
        }
    }

}

package fi.digi.savonia.movesense.Tools.Listeners;

import java.io.IOException;

import okhttp3.ResponseBody;

public interface HttpActionListener {
    void onResult(String result, int id);
    void onError(IOException error, int id);

}

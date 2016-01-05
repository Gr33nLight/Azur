package musicstream.gr33napps.com.musicstream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VkAudioArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by bruno on 12/10/2015.
 */
public class PlayerUtils {
    private static final String TAG = "Gr33nDebug";
    private TestActivity c;


    public PlayerUtils(Context c) {
        this.c = (TestActivity) c;
    }

    public void downLoadFromUrl(String mp3, String title, String artist) {
        final String songTitle = title, songArtist = artist;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mp3, new FileAsyncHttpResponseHandler(c) {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, File file) {
                Toast.makeText(c, "Failed to Donwload song", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, File file) {
                File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Music");
                if (!folder.exists()) {
                    if (!folder.mkdir())
                        Toast.makeText(c, "Cant create folder! ", Toast.LENGTH_SHORT).show();

                }
                File to = null;
                try {
                    to = new File(folder, URLEncoder.encode(songTitle + "" + songArtist + ".mp3", "UTF-8").replace("+", " "));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    to.createNewFile();
                    copyFile(file, to);
                    Toast.makeText(c, "Song saved in /Music/ folder ", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }


}

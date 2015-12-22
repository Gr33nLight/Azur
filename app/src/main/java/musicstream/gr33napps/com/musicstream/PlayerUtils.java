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
    private SearchFragment s;
    private FavouritesFragment favs;
    private int nextIndex = 1, previousIndex = 0;
    private TestActivity c;
    private VKRequest request;
    public String currentId = "", mp3 = "";
    private List<VKSong> songsList;

    public PlayerUtils(FavouritesFragment favs, SearchFragment s, Context c) {
        this.s = s;
        this.favs = favs;
        this.c = (TestActivity) c;
    }

    public PlayerUtils(Context c){
        this.c=(TestActivity) c;
    }

    public void nextSong() {
        VkAudioArray songsList = s.getAdapter().getSongs();
        Log.d(TAG, "Next song" + previousIndex);
        if (nextIndex < s.getAdapter().getSongs().getCount()) {
            c.playSong(songsList.get(nextIndex).url, songsList.get(nextIndex).title, songsList.get(nextIndex).artist, String.valueOf(songsList.get(nextIndex).id));
            s.getAdapter().setSelectedPos(nextIndex - 1);
            s.getAdapter().notifyDataSetChanged();
        } else {
            c.playSong(songsList.get(nextIndex - 1).url, songsList.get(nextIndex - 1).title, songsList.get(nextIndex - 1).artist, String.valueOf(songsList.get(nextIndex - 1).id));
            s.getAdapter().setSelectedPos(nextIndex - 1);
            s.getAdapter().notifyDataSetChanged();
            Log.d(TAG, "Limit test" + previousIndex);
        }

    }

    public void nextSongFavs() {
        songsList = favs.getAdapter().getSongs();
        if (nextIndex < favs.getAdapter().getSongs().size()) {
            getSongFroId(songsList.get(nextIndex).ownid, songsList.get(nextIndex).id, new Callbacks() {
                @Override
                public void successCallback(String response) {
                    c.playSong(response, songsList.get(nextIndex).title, songsList.get(nextIndex).artist, songsList.get(nextIndex).id);
                    favs.getAdapter().setSelectedPos(nextIndex - 1);
                    favs.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void failCallback(String response) {
                    Toast.makeText(c, "Error playing song", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            getSongFroId(songsList.get(nextIndex - 1).ownid, songsList.get(nextIndex - 1).id, new Callbacks() {
                @Override
                public void successCallback(String response) {
                    c.playSong(response, songsList.get(nextIndex - 1).title, songsList.get(nextIndex - 1).artist, songsList.get(nextIndex - 1).id);
                    favs.getAdapter().setSelectedPos(nextIndex - 1);
                    favs.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void failCallback(String response) {
                    Toast.makeText(c, "Error playing song", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(TAG, "Limit test" + previousIndex);
        }

    }

    public void prevSong() {
        VkAudioArray songsList = s.getAdapter().getSongs();
        c.playSong(songsList.get(previousIndex).url, songsList.get(previousIndex).title, songsList.get(previousIndex).artist, String.valueOf(songsList.get(previousIndex).id));
        s.getAdapter().setSelectedPos(previousIndex);
        s.getAdapter().notifyDataSetChanged();
    }

    public void prevSongFavs() {
        songsList = favs.getAdapter().getSongs();
        getSongFroId(songsList.get(previousIndex).ownid, songsList.get(previousIndex).id, new Callbacks() {
            @Override
            public void successCallback(String response) {
                c.playSong(response, songsList.get(previousIndex).title, songsList.get(previousIndex).artist, songsList.get(previousIndex).id);
                favs.getAdapter().setSelectedPos(previousIndex);
                favs.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void failCallback(String response) {
                Toast.makeText(c, "Error playing song", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateIndex() {
        VkAudioArray songsList = s.getAdapter().getSongs();
        int songListSize = songsList.getCount();
        for (int c = 0; c < songsList.getCount(); c++) {
            if (String.valueOf(songsList.get(c).id).equals(currentId)) {
                Log.d(TAG, "" + songsList.getCount());
                if (c > 0 && c < songListSize) {
                    nextIndex = c + 1;
                    previousIndex = c - 1;
                    break;
                }
                if (c == 0 && c < songListSize) {
                    nextIndex = c + 1;
                    previousIndex = 0;
                    break;
                }
                if (c > 0 && c == songListSize) {
                    nextIndex = c;
                    previousIndex = c - 1;
                    break;
                }
            }
        }
        Log.d(TAG, "[SEARCH] nextindex: " + nextIndex + " previousindex: " + previousIndex);
    }

    public void updateIndexFavs() {
        List<VKSong> songsList = favs.getAdapter().getSongs();
        int songListSize = songsList.size();
        for (int c = 0; c < songListSize; c++) {
            if (String.valueOf(songsList.get(c).id).equals(currentId)) {
                if (c > 0 && c < songListSize) {
                    nextIndex = c + 1;
                    previousIndex = c - 1;
                    break;
                }
                if (c == 0 && c < songListSize) {
                    nextIndex = c + 1;
                    previousIndex = 0;
                    break;
                }
                if (c > 0 && c == songListSize) {
                    nextIndex = c;
                    previousIndex = c - 1;
                    break;
                }
            }
        }
        Log.d(TAG, "[FAVS] nextindex: " + nextIndex + " previousindex: " + previousIndex);
    }

    public void resetIndexes() {
        previousIndex = 0;
        nextIndex = 1;
        s.getAdapter().setSelectedPos(-1);
    }


    private void getSongFroId(String ownid, String vkid, final Callbacks callback) {
        request = VKApi.audio().getById(VKParameters.from("audios", ownid + "_" + vkid));
        Log.d(TAG, "Query: " + ownid + "_" + vkid);
        request.setRequestListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                JSONObject out = response.json;
                JSONArray jsonarray;
                try {
                    jsonarray = out.getJSONArray("response");
                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    mp3 = jsonobject.getString("url");
                    callback.successCallback(mp3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(c, "Error playing song", Toast.LENGTH_SHORT).show();
                callback.failCallback("error");
            }
        });
        request.start();
    }

    public void downLoadFromUrl(String mp3,String title,String artist) {
        final String songTitle=title,songArtist=artist;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mp3, new FileAsyncHttpResponseHandler(c) {
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, File file) {
                Toast.makeText(c, "Failed to Donwload song", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, File file) {
                File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VKMusic/");
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
                    Toast.makeText(c, "Song saved in /VKMusic/ folder ", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }



}

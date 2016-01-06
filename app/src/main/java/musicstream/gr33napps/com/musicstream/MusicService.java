package musicstream.gr33napps.com.musicstream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;

import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service {

    private int id = 1234;
    private static final String TAG = "Debug";
    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //search song list
    private VkAudioArray songs = new VkAudioArray();
    //favourite song list
    private List<VKSong> favSongs;
    //current position songs
    private int songPosn;
    //current position fav songs
    private int songFavPosn;
    private  RemoteViews views,bigViews;


    private VKRequest request;

    private VKSong favSong;

    public IBinder getMusicBind() {
        return musicBind;
    }


    public int getSongPosn() {
        return songPosn;
    }

    public int getSongFavPosn() {
        return songFavPosn;
    }

    private TestActivity mainInterface;
    private boolean search;

    public void setSongs(VkAudioArray songs) {
        this.songs = songs;
        showNotification();

    }

    public void setFavSongs(List<VKSong> songs) {
        this.favSongs = songs;
        for (int i = 0; i < songs.size(); i++)
            Log.d(TAG, "Song:" + songs.get(i).getMp3());
        Log.d(TAG, "Size:" + songs.size());
    }


    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public void setMainInterface(TestActivity mainInterface) {
        this.mainInterface = mainInterface;
    }

    public void initMusicPlayer() {
        player = new MediaPlayer();
        //set player properties
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);

    }

    Notification status;
    private final String LOG_TAG = "NotificationService";

    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, TestActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setImageViewResource(R.id.status_bar_play,
                android.R.drawable.ic_media_play);
        bigViews.setImageViewResource(R.id.status_bar_play,
                android.R.drawable.ic_media_pause);

        views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title");

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name");

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.mipmap.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
            prevSong(true);

        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
            if(player.isPlaying()){
                player.pause();
                views.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_play);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_play);
                status.contentView = views;
                status.bigContentView = bigViews;
                status.flags = Notification.FLAG_ONGOING_EVENT;
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            }
            else {
                player.start();
                views.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_pause);
                bigViews.setImageViewResource(R.id.status_bar_play,
                        android.R.drawable.ic_media_pause);
                status.contentView = views;
                status.bigContentView = bigViews;
                status.flags = Notification.FLAG_ONGOING_EVENT;
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            }

        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
            nextSong(true);

        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMusicPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initMusicPlayer();
        return musicBind;
    }

    public void pause() {
//        Intent notIntent = new Intent(getBaseContext(), TestActivity.class);
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(getBaseContext(), 0,
//                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder = new Notification.Builder(getBaseContext());
//
//        builder.setContentIntent(pendInt)
//                .setSmallIcon(android.R.drawable.ic_media_pause)
//                .setOngoing(true)
//                .setContentTitle(currentSong.title)
//                .setContentText(currentSong.artist);
//        Notification not = builder.build();
//        startForeground(id, not);
        player.pause();
    }

    public void play() {
//        Intent notIntent = new Intent(getBaseContext(), TestActivity.class);
//        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(getBaseContext(), 0,
//                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder = new Notification.Builder(getBaseContext());
//
//        builder.setContentIntent(pendInt)
//                .setSmallIcon(android.R.drawable.ic_media_play)
//                .setOngoing(true)
//                .setContentTitle(currentSong.title)
//                .setContentText(currentSong.artist);
//        Notification not = builder.build();
//        startForeground(id, not);
        player.start();
    }

    private VKApiAudio currentSong;

    public VKApiAudio getCurrentSong() {
        return currentSong;
    }

    public VKSong getCurrentFavSong() {
        return favSong;
    }

    public void playSong(final boolean search) {
        try {
            if (search) {
                final VKApiAudio song = songs.get(songPosn);
                currentSong = song;
                player.reset();
                player.setDataSource(song.url);
                mainInterface.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainInterface.playCallBack(song);
                        views.setTextViewText(R.id.status_bar_track_name, song.title);
                        views.setTextViewText(R.id.status_bar_artist_name, song.artist);
                        bigViews.setTextViewText(R.id.status_bar_track_name, song.title);
                        bigViews.setTextViewText(R.id.status_bar_artist_name, song.artist);
                        status.contentView = views;
                        status.bigContentView = bigViews;
                        status.flags = Notification.FLAG_ONGOING_EVENT;
                        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
//                        Intent notIntent = new Intent(getBaseContext(), TestActivity.class);
//                        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        PendingIntent pendInt = PendingIntent.getActivity(getBaseContext(), 0,
//                                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                        Notification.Builder builder = new Notification.Builder(getBaseContext());
//
//                        builder.setContentIntent(pendInt)
//                                .setSmallIcon(android.R.drawable.ic_media_play)
//                                .setOngoing(true)
//                                .setContentTitle(song.title)
//                                .setContentText(song.artist);
//                        Notification not = builder.build();
//                        startForeground(id, not);
                    }
                });
                player.prepareAsync();
            } else {
                player.pause();
                favSong = favSongs.get(songFavPosn);
                Log.d(TAG, "Play from favs pos:" + songPosn + " " + favSongs.get(songFavPosn).getMp3());
                getSongFroId(favSong.ownid, favSong.id, new Callbacks() {
                    @Override
                    public void successCallback(String response) {
                        player.reset();
                        try {
                            player.setDataSource(response);
                            VKSong favSong = favSongs.get(songFavPosn);
                            final VKApiAudio finalSong = new VKApiAudio();
                            finalSong.artist = favSong.getArtist();
                            finalSong.title = favSong.getTitle();
//                            Intent notIntent = new Intent(getBaseContext(), TestActivity.class);
//                            notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            PendingIntent pendInt = PendingIntent.getActivity(getBaseContext(), 0,
//                                    notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                            Notification.Builder builder = new Notification.Builder(getBaseContext());
//
//                            builder.setContentIntent(pendInt)
//                                    .setSmallIcon(android.R.drawable.ic_media_play)
//                                    .setOngoing(true)
//                                    .setContentTitle(finalSong.title)
//                                    .setContentText(finalSong.artist);
//                            Notification not = builder.build();
//                            startForeground(id, not);
                            mainInterface.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    views.setTextViewText(R.id.status_bar_track_name, finalSong.title);
                                    views.setTextViewText(R.id.status_bar_artist_name, finalSong.artist);
                                    bigViews.setTextViewText(R.id.status_bar_track_name, finalSong.title);
                                    bigViews.setTextViewText(R.id.status_bar_artist_name, finalSong.artist);
                                    status.contentView = views;
                                    status.bigContentView = bigViews;
                                    status.flags = Notification.FLAG_ONGOING_EVENT;
                                    startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
                                    mainInterface.playCallBack(finalSong);

                                }
                            });
                            player.prepareAsync();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    callback.successCallback(jsonobject.getString("url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.e(TAG, "error playing song");
            }
        });
        request.start();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Toast.makeText(getApplicationContext(), "go and fuck yourself", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();

    }

    public void setSong(int songIndex, boolean search) {
        if (search)
            songPosn = songIndex;
        else
            songFavPosn = songIndex;
    }

    public void nextSong(boolean search) {

        if (search) {
            this.songPosn++;
            if (this.songPosn >= songs.size()) songPosn = songs.size() - 1;
        } else {
            this.songFavPosn++;
            if (this.songFavPosn >= favSongs.size()) songFavPosn = favSongs.size() - 1;
        }
        this.playSong(search);
    }

    public void prevSong(boolean search) {

        if (search) {
            this.songPosn--;
            if (this.songPosn <= 0) songPosn = 0;
        } else {
            this.songFavPosn--;
            if (this.songFavPosn <= 0) songFavPosn = 0;
        }
        this.playSong(search);
    }
}

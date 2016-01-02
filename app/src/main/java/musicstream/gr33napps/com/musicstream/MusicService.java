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
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;

import junit.framework.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaPlayer.*;

public class MusicService extends Service implements
        OnPreparedListener, OnErrorListener,
        OnCompletionListener {
    private static final String TAG = "Debug";
    private final IBinder musicBind = new MusicBinder();
    //media player
    private MediaPlayer player;
    //search song list
    private VkAudioArray songs;
    //favourite song list
    private List<VKSong> favSongs;
    //current position songs
    private int songPosn;
    //current position fav songs
    private int songFavPosn;

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

    }
    public void setFavSongs(List<VKSong> songs) {
        this.favSongs = songs;
        for (int i=0;i<songs.size();i++)
            Log.d(TAG, "Song:"  + songs.get(i).getMp3());
        Log.d(TAG, "Size:"  + songs.size());
    }

    public void start(){
        Intent notificationIntent = new Intent(getApplicationContext(), TestActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                1234, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = getApplicationContext().getResources();
        Notification.Builder builder = new Notification.Builder(getApplicationContext());

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setTicker("AzureMusic")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("AzureMusic")
                .setContentText("AzureMusic");
        Notification n = builder.build();
        startForeground(1234,n);
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
        //player.setWakeMode(getApplicationContext(),
        //        PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
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
    public void pause(){
        player.pause();
    }

    public void play(){
        player.start();
    }
    private VKApiAudio currentSong;

    public VKApiAudio getCurrentSong() {
        return currentSong;
    }

    public VKSong getCurrentFavSong() {
        return favSong;
    }

    public void playSong(boolean search) {
        Log.e(TAG, "called from search " + search);
        this.search = search;

        //play a song
        player.reset();
        VKApiAudio song = songs.get(songPosn);
        currentSong = song;
        try {
            if (search) {
                player.setDataSource(song.url);
                mainInterface.playCallBack(song);
            }else {
                favSong =  favSongs.get(songFavPosn);
                Log.d(TAG, "Play from favs pos:" + songPosn + " " + favSongs.get(songFavPosn).getMp3());
                player.setDataSource(favSongs.get(songFavPosn).getMp3());
                VKSong favSong = favSongs.get(songFavPosn);
                VKApiAudio finalSong = new VKApiAudio();
                finalSong.artist = favSong.getArtist();
                finalSong.title = favSong.getTitle();
                finalSong.url = favSong.getMp3();

                mainInterface.playCallBack(finalSong);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        player.prepareAsync();



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
    public void onCompletion(MediaPlayer mp) {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {


    }

    public void setSong(int songIndex, boolean search){
        if (search)
            songPosn=songIndex;
        else
            songFavPosn = songIndex;
    }

    public void nextSong(boolean search){

        if (search) {
            this.songPosn++;
            if (this.songPosn >= songs.size()) songPosn = songs.size() - 1;
        }else {
            this.songFavPosn++;
            if (this.songFavPosn >= favSongs.size()) songFavPosn = favSongs.size() - 1;
        }
        this.playSong(search);
    }
    public void prevSong(boolean search){

        if (search) {
            this.songPosn--;
            if (this.songPosn <= 0) songPosn = 0;
        }else {
            this.songFavPosn--;
            if (this.songFavPosn <= 0) songFavPosn = 0;
        }
        this.playSong(search);
    }
}

package musicstream.gr33napps.com.musicstream;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;

import junit.framework.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaPlayer.*;

/**
 * Created by W8 on 29/12/2015.
 */
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

    private TestActivity mainInterface;

    public void setSongs(VkAudioArray songs) {
        this.songs = songs;

    }
    public void setFavSongs(List<VKSong> songs) {
        this.favSongs = songs;
        for (int i=0;i<songs.size();i++)
            Log.d(TAG, "Song:"  + songs.get(i).getMp3());
        Log.d(TAG, "Size:"  + songs.size());
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
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
    public void playSong(boolean search) {

        //play a song
        player.reset();
        VKApiAudio song = songs.get(songPosn);
        int currSong = songPosn;
        try {
            if (search) {
                player.setDataSource(song.url.toString());
            }else{
                Log.d(TAG, "Play from favs pos:"+songPosn +" " + favSongs.get(songFavPosn).getMp3());
                player.setDataSource(favSongs.get(songFavPosn).getMp3());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();

        if (search)
            mainInterface.playCallBack(song);
        else{
            VKSong favSong = favSongs.get(songFavPosn);
            VKApiAudio finalSong = new VKApiAudio();
            finalSong.artist = favSong.getArtist();
            finalSong.title = favSong.getTitle();
            finalSong.url = favSong.getMp3();

            mainInterface.playCallBack(finalSong);
        }

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
        mp.start();


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

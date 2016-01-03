package musicstream.gr33napps.com.musicstream;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "Gr33nDebug";
    private ImageView playpause, fwd, bwd;
    private TextView songTitle, artistName, totalTime, currentTime;
    private ProgressBar progressBar;
    private SeekBar seekBar;

    public String currentTitle = "", currentArtist = "";
    public static boolean prevPlayed = false;
    public static int lengthms, seekpos = 0;
    private VKRequest request;
    private VkAudioArray data = new VkAudioArray();
    private SearchAdapter adapter;
    private MaterialSearchView searchView;
    private LinearLayout playerLayout;
    private TabLayout tabLayout;
    private SearchFragment s;
    private FavouritesFragment favs;
    private DBHelper songsDb;
    private SQLiteDatabase db;
    public PlayerUtils utils;
    public boolean isSearchSelected = true;
    private Intent playIntent;
    private MusicService musicSrv;
    private boolean musicBound;
    private MediaPlayer player;
    private TestActivity act;
    private Updater updater;
    private int favPos;

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            musicSrv.start();
            //pass list
            musicSrv.setSongs(data);
            //pass fav list
            musicSrv.setFavSongs(favs.getSongsFromDB());
            musicBound = true;

            Log.d(TAG, "binded!");
            musicSrv.setMainInterface(act);

            musicSrv.getPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    adapter.notifyDataSetChanged();
                    if (isSearchSelected){
                        VKApiAudio audio = musicSrv.getCurrentSong();
                        currentArtist = audio.artist;
                        currentTitle = audio.title;

                    }else{
                        VKSong favSong = musicSrv.getCurrentFavSong();
                        currentArtist = favSong.artist;
                        currentTitle =favSong.title;
                    }


                    playpause.setImageResource(R.drawable.ic_pause);

                    if (updater != null)
                        updater.ferma();
                    lengthms = musicSrv.getPlayer().getDuration();
                    totalTime.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(lengthms),
                            TimeUnit.MILLISECONDS.toSeconds(lengthms) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lengthms))
                    ));
                    playpause.setImageResource(R.drawable.ic_pause);
                    songTitle.setText(currentTitle);
                    artistName.setText(currentArtist);
                    progressBar.setVisibility(View.INVISIBLE);
                    musicSrv.getPlayer().start();
                    player = musicSrv.getPlayer();

                    runUpdater();
                }
            });
            musicSrv.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (isSearchSelected)
                        musicSrv.nextSong(true);
                    else
                        musicSrv.nextSong(false);

                }

            });

            musicSrv.getPlayer().setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    seekBar.setSecondaryProgress(percent);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void playCallBack(VKApiAudio song) {
        Log.d(TAG, song.artist + " " + song.title);
        currentTitle = song.title;
        currentArtist = song.artist;


        songTitle.setText("");
        artistName.setText("");
       // progressBar.setVisibility(View.INVISIBLE);

    }

    public void songPicked(int pos) {


        playpause.setImageResource(R.drawable.ic_play);
        playerLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        musicSrv.setSong(pos, isSearchSelected);
        if (isSearchSelected) {
            favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
            favs.getAdapter().setSelectedPos(-1);
            favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
            musicSrv.playSong(true);
        } else {
            s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
            s.getAdapter().setSelectedPos(-1);
            s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
            musicSrv.playSong(false);
        }
        prevPlayed = true;

    }


    class Updater extends Thread {

        boolean running = true;

        @Override
        public void run() {
            while (seekBar.getProgress() <= 100 && running) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (seekBar != null && currentTime != null) {
                                seekBar.setProgress((int) (((float) player.getCurrentPosition() / lengthms) * 100));

                                int total = player.getCurrentPosition();
                                currentTime.setText(String.format("%d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(total),
                                        TimeUnit.MILLISECONDS.toSeconds(total) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(total))
                                ));

                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void ferma() {
            running = false;
        }
    }

    private void runUpdater() {
       updater = new Updater();
        updater.start();
    }

    final static int myID = 1234;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        s = new SearchFragment();
        favs = new FavouritesFragment();
        act = this;
        songsDb = new DBHelper(getBaseContext());
        adapter = new SearchAdapter(data, this);
        initGUI();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        utils = new PlayerUtils(favs, s, this);
        request = VKApi.audio().getPopular(VKParameters.from("only_eng", "1", "genre_id", "5"));
        request.addExtraParameter("count", 150);
        setReqListener(request);
        request.start();
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePlayback();
            }
        });
        fwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                playpause.setImageResource(R.drawable.ic_play);
                if (isSearchSelected) {

                    musicSrv.nextSong(true);
                    s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
                    s.getAdapter().setSelectedPos(musicSrv.getSongPosn());
                    s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());

                } else {
                    musicSrv.nextSong(false);

                    favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
                    favs.getAdapter().setSelectedPos(musicSrv.getSongFavPosn());
                    favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
                }

            }
        });
        bwd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playpause.setImageResource(R.drawable.ic_play);

                progressBar.setVisibility(View.VISIBLE);
                if (isSearchSelected) {
                    musicSrv.prevSong(true);
                    s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
                    s.getAdapter().setSelectedPos(musicSrv.getSongPosn());
                    s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
                } else{
                    musicSrv.prevSong(false);
                    favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
                    favs.getAdapter().setSelectedPos(musicSrv.getSongFavPosn());
                    favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
                }

            }
        });


       /* player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnBufferingUpdateListener(this);
          */
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                s.getAdapter().notifyDataSetChanged();
                data.clear();
                // utils.resetIndexes();
                s.showLoading();
                request = VKApi.audio().search(VKParameters.from("q", query, "only_eng", "1"));
                request.addExtraParameter("count", 150);
                setReqListener(request);
                request.start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

    }//onCreate


    public VkAudioArray getData() {
        return data;
    }

    public SQLiteDatabase getReadDB() {
        return songsDb.getReadableDatabase();
    }

    public void addSongToDb(VKSong song) {
        db = songsDb.getWritableDatabase();
// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBHelper.FeedEntry.COLUMN_NAME_ID, song.getId());
        values.put(DBHelper.FeedEntry.COLUMN_NAME_OWNID, song.getOwnid());
        values.put(DBHelper.FeedEntry.COLUMN_NAME_MP3, song.getMp3());
        values.put(DBHelper.FeedEntry.COLUMN_NAME_TITLE, song.getTitle());
        values.put(DBHelper.FeedEntry.COLUMN_NAME_ARTIST, song.getArtist());
// Insert the new row, returning the primary key value of the new row
        db.insert(
                DBHelper.FeedEntry.TABLE_NAME, null,
                values);
        favs.setNewAudioData();
        db.close();
    }

    public void removeSongFromDB(String id) {
        db = songsDb.getWritableDatabase();
        String selection = DBHelper.FeedEntry.COLUMN_NAME_ID + "=" + "'" + id + "'";
        db.delete("songs", selection, null);
        favs.setNewAudioData();
        db.close();
    }

    private void initGUI() {

        playerLayout = (LinearLayout) findViewById(R.id.playerLayout);
        playerLayout.setVisibility(View.GONE);
        totalTime = (TextView) findViewById(R.id.totalTime);
        currentTime = (TextView) findViewById(R.id.currentTime);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicSrv.getPlayer().pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicSrv.getPlayer().start();
            }
        });
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        playpause = (ImageView) findViewById(R.id.playpause);
        fwd = (ImageView) findViewById(R.id.fwd);
        bwd = (ImageView) findViewById(R.id.bwd);
        songTitle = (TextView) findViewById(R.id.songTitle);
        artistName = (TextView) findViewById(R.id.artistName);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        playpause.setImageResource(R.drawable.ic_play);
    }

    public void togglePlayback() {
        if (!(musicSrv.getPlayer().isPlaying()) && prevPlayed) {
            playpause.setImageResource(R.drawable.ic_pause);
            musicSrv.play();
        } else if (musicSrv.getPlayer().isPlaying()) {
            playpause.setImageResource(R.drawable.ic_play);
            musicSrv.pause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);

        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(s, "SEARCH");
        adapter.addFragment(favs, "MY MUSIC");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
//        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                if (tab.getText().equals("MY MUSIC") && musicSrv != null)
//                    musicSrv.setFavSongs(favs.getSongsFromDB());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                if (tab.getText().equals("MY MUSIC") && musicSrv != null)
//                    musicSrv.setFavSongs(favs.getSongsFromDB());
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//                if (tab.getText().equals("MY MUSIC") && musicSrv != null)
//                    musicSrv.setFavSongs(favs.getSongsFromDB());
//            }
//        });
    }


    private void setReqListener(VKRequest request) {
        request.setRequestListener(new VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VkAudioArray dataFetched = (VkAudioArray) response.parsedModel;
                for (int i = 0; i < dataFetched.size(); i++)
                    data.add(dataFetched.get(i));
                Log.d(TAG, "onComplete");
                s.setNewAudioData(data);
                Log.e(TAG,"data :"+data.toString());
                s.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getBaseContext(), "Error retrieving list", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle("About us")
                    .setMessage("App Developed by Gr33nApps Studios All Rights Reserved")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        }

        if (itemId == R.id.action_logout) {
           /* player.reset();
            player.release();
*/
            VKSdk.logout();
            if (!VKSdk.isLoggedIn()) {
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        songsDb = new DBHelper(getBaseContext());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        musicSrv = null;

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
        if (updater != null)
            updater.ferma();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view.getId() == R.id.seekBar) {
            if (musicSrv.getPlayer() != null) {
                SeekBar sb = (SeekBar) view;
                seekpos = (lengthms / 100) * sb.getProgress();
                musicSrv.getPlayer().seekTo(seekpos);
            }
        }
        return false;
    }
/*
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        seekBar.setSecondaryProgress(i);
    }



    public void playSong(String mp3, String title, String artist, String songId) {

        try {
            player.reset();
            s.getAdapter().notifyItemChanged(s.getAdapter().getSelectedPos());
            favs.getAdapter().notifyItemChanged(favs.getAdapter().getSelectedPos());
            playpause.setImageResource(R.drawable.ic_play);
            currentTitle = title;
            currentArtist = artist;
            utils.currentId = songId;
            player.setDataSource(mp3);
            player.prepareAsync();
            if (isSearchSelected) utils.updateIndex();
            else utils.updateIndexFavs();
            artistName.setText("");
            songTitle.setText("");
            playerLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            prevPlayed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }*/

    //
//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        switch (focusChange) {
//
//            case AudioManager.AUDIOFOCUS_LOSS:
//                // Lost focus for an unbounded amount of time: stop playback and release media player
//                if (player.isPlaying()) player.stop();
//                player.release();
//                player = null;
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                // Lost focus for a short time, but we have to stop
//                // playback. We don't release the media player because playback
//                // is likely to resume
//                if (player.isPlaying()) player.pause();
//                break;
//
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                // Lost focus for a short time, but it's ok to keep playing
//                // at an attenuated level
//                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
//                break;
//        }
//    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}


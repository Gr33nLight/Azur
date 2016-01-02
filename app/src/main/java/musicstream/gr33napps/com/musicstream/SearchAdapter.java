package musicstream.gr33napps.com.musicstream;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VkAudioArray;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SongViewHolder> {
    private static final String TAG = "Gr33nDebug";
    private VkAudioArray songs;
    private int selectedPos = -1;
    private PlayerUtils utils;
    TestActivity mainActivityRef;


    SearchAdapter(VkAudioArray songs, Context c) {
        this.songs = songs;
        mainActivityRef = (TestActivity) c;
    }

//    private void primarySeekBarProgressUpdater() {
//        if (player.isPlaying()) {
//            Runnable notification = new Runnable() {
//                public void run() {

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    public VkAudioArray getSongs() {
        return songs;

    }

    public void setSongs(VkAudioArray songs) {
        this.songs = songs;
    }


    public int getSelectedPos() {
        return selectedPos;
    }


    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_layout_search, viewGroup, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SongViewHolder songViewHolder, int i) {
//        for (int c = 0; c < songs.size(); c++) {
//            Log.d("SongList", songs.get(c).title);
//        }
        songViewHolder.title.setText(songs.get(i).title);
        songViewHolder.artist.setText(songs.get(i).artist);
        songViewHolder.mp3 = songs.get(i).url;
        songViewHolder.vkid = String.valueOf(songs.get(i).getId());
        songViewHolder.ownid = String.valueOf(songs.get(i).owner_id);
        if (selectedPos == i) {
            songViewHolder.title.setTextColor(mainActivityRef.getResources().getColor(R.color.theme_accent));
            songViewHolder.artist.setTextColor(mainActivityRef.getResources().getColor(R.color.theme_accent));
        } else {
            songViewHolder.title.setTextColor(Color.GRAY);
            songViewHolder.artist.setTextColor(Color.GRAY);
        }

        songViewHolder.i = i;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView artist;
        String mp3, vkid, ownid;
        RelativeLayout songLayout;
        ImageButton overflowBtn;
        Integer i;
        SongViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            utils = new PlayerUtils(mainActivityRef);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            songLayout = (RelativeLayout) itemView.findViewById(R.id.songLayout);
            overflowBtn = (ImageButton) itemView.findViewById(R.id.overflowBtn);
            overflowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openBottomSheet();
                }
            });
            songLayout.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            notifyItemChanged(selectedPos);
            notifyDataSetChanged();
            selectedPos = getLayoutPosition();
            Log.d(TAG, "selectedPos" + selectedPos);
            mainActivityRef.isSearchSelected = true;
            mainActivityRef.songPicked(selectedPos);
            //mainActivityRef.playSong(mp3, title.getText().toString(), artist.getText().toString(), vkid);
        }



        private void openBottomSheet() {

            View view = mainActivityRef.getLayoutInflater().inflate(R.layout.bottom_sheet_search, null);
            LinearLayout layoutAddFav = (LinearLayout) view.findViewById(R.id.layoutAddFav);
            LinearLayout layoutDl = (LinearLayout) view.findViewById(R.id.layoutDl);
            final Dialog mBottomSheetDialog = new Dialog(mainActivityRef,
                    R.style.MaterialDialogSheet);
            mBottomSheetDialog.setContentView(view);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
            mBottomSheetDialog.show();


            layoutAddFav.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    VKSong song = new VKSong(title.getText().toString(), artist.getText().toString(), mp3, vkid, ownid);
                    mainActivityRef.addSongToDb(song);
                    System.out.println(song.getMp3());
                    Toast.makeText(mainActivityRef, "Added to favourites", Toast.LENGTH_SHORT).show();
                    mBottomSheetDialog.dismiss();
                }
            });

            layoutDl.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(mainActivityRef, "Downloading...", Toast.LENGTH_SHORT).show();
                    mainActivityRef.isSearchSelected = false;
                    //utils.downLoadFromUrl(mp3, title.getText().toString(), artist.getText().toString());
                    mBottomSheetDialog.dismiss();
                }
            });

        }
    }
}
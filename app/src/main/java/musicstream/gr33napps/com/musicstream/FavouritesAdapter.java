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

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.SongViewHolder> {
    private static final String TAG = "Gr33nDebug";
    private List<VKSong> songs;
    private int selectedPos = -1;
    private VKRequest request;
    TestActivity mainActivityRef;


    FavouritesAdapter(List<VKSong> songs, Context c) {
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

    public List<VKSong> getSongs() {
        return songs;

    }

    public void setSongs(List<VKSong> songs) {
        this.songs = songs;
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
        songViewHolder.mp3 = songs.get(i).mp3;
        songViewHolder.vkid = songs.get(i).id;
        songViewHolder.ownid = songs.get(i).ownid;
        if (selectedPos == i) {
            songViewHolder.title.setTextColor(mainActivityRef.getResources().getColor(R.color.theme_accent));
            songViewHolder.artist.setTextColor(mainActivityRef.getResources().getColor(R.color.theme_accent));
        } else {
            songViewHolder.title.setTextColor(Color.GRAY);
            songViewHolder.artist.setTextColor(Color.GRAY);
        }
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
        String mp3 = "not set", vkid, ownid;
        RelativeLayout songLayout;
        ImageButton overflowBtn;

        SongViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
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
            mainActivityRef.isSearchSelected = false;
            notifyDataSetChanged();
            selectedPos = getLayoutPosition();
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
                        mainActivityRef.songPicked(selectedPos);
                       // mainActivityRef.playSong(mp3, title, artist, vkid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Toast.makeText(mainActivityRef, "Error playing song", Toast.LENGTH_SHORT).show();
                }
            });
            request.start();
        }


        private void openBottomSheet() {

            View view = mainActivityRef.getLayoutInflater().inflate(R.layout.bottom_sheet, null);
            LinearLayout layoutUnFav = (LinearLayout) view.findViewById(R.id.layoutUnFav);
            LinearLayout layoutDl = (LinearLayout) view.findViewById(R.id.layoutDl);
            final Dialog mBottomSheetDialog = new Dialog(mainActivityRef,
                    R.style.MaterialDialogSheet);
            mBottomSheetDialog.setContentView(view);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
            mBottomSheetDialog.show();


            layoutUnFav.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mainActivityRef.removeSongFromDB(vkid);
                    mainActivityRef.isSearchSelected = false;
                    selectedPos = -1;
                    mBottomSheetDialog.dismiss();
                }
            });

            layoutDl.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(mainActivityRef, "Downloading...", Toast.LENGTH_SHORT).show();
                    mainActivityRef.isSearchSelected = false;
                    request = VKApi.audio().getById(VKParameters.from("audios", ownid + "_" + vkid));
                    request.setRequestListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            JSONObject out = response.json;
                            JSONArray jsonarray;
                            try {
                                jsonarray = out.getJSONArray("response");
                                JSONObject jsonobject = jsonarray.getJSONObject(0);
                                mp3 = jsonobject.getString("url");
                                //utils.downLoadFromUrl(mp3, title.getText().toString(), artist.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            Toast.makeText(mainActivityRef, "Failed to Donwload song", Toast.LENGTH_SHORT).show();
                        }
                    });
                    request.start();
                    mBottomSheetDialog.dismiss();
                }
            });

        }


    }

    public int getSelectedPos() {
        return selectedPos;
    }


}
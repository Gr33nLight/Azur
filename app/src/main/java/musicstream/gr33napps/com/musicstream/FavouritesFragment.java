package musicstream.gr33napps.com.musicstream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment{
    private static final String TAG = "Gr33nDebug";
    private FavouritesAdapter adapter;
    private RecyclerView recyclerView;
    private List<VKSong> data = new ArrayList<>();

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FavouritesAdapter(data, getContext());
        //getSongsFromDB();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rv, container, false);
        Log.d(TAG,"onCreateView");
        recyclerView = (RecyclerView) v.findViewById(R.id.rv);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        return v;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public List<VKSong>  getSongsFromDB() {
        data.clear();

        // Select All Query
        String selectQuery = "SELECT  * FROM songs";
        SQLiteDatabase db = ((TestActivity) getActivity()).getReadDB();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                System.out.println(cursor.toString());
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String ownid = cursor.getString(cursor.getColumnIndex("ownid"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String artist =  cursor.getString(cursor.getColumnIndex("artist"));
                String mp3 =  cursor.getString(cursor.getColumnIndex("mp3"));
                VKSong s = new VKSong(title,artist,mp3,id,ownid);
                System.out.println("added song:" + s.toString());
                data.add(s);
            } while (cursor.moveToNext());
            adapter.setSongs(data);
            adapter.notifyDataSetChanged();
        }
        db.close();
        return data;
    }

    public void setNewAudioData() {
        getSongsFromDB();
        adapter.notifyDataSetChanged();
    }

    public FavouritesAdapter getAdapter() {
        return adapter;
    }

}
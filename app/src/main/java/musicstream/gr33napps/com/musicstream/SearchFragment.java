package musicstream.gr33napps.com.musicstream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.vk.sdk.api.model.VkAudioArray;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class SearchFragment extends Fragment{
    private static final String TAG = "Gr33nDebug";
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private VkAudioArray data = new VkAudioArray();
    private ProgressBar searchLoading;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SearchAdapter(data, getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rv, container, false);
        searchLoading =(ProgressBar) v.findViewById(R.id.loading);
        recyclerView = (RecyclerView) v.findViewById(R.id.rv);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext()).build());
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        return v;
    }

    public void setNewAudioData(VkAudioArray data) {
        searchLoading.setVisibility(View.GONE);
        adapter.setSongs(data);
        adapter.notifyDataSetChanged();
    }

    public SearchAdapter getAdapter() {
        return adapter;
    }

    public void showLoading(){
        searchLoading.setVisibility(View.VISIBLE);
    }
    public void hideLoading(){
        searchLoading.setVisibility(View.GONE);
    }
}
package com.example.sangeetapplication.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sangeetapplication.Adapters.SearchAdapter;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class search_music extends Fragment {

    SearchView searchView;
    RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<FileModel> allSongs = new ArrayList<>();
    private MainActivity activity;
    private MediaServices mediaServices;
    public List<FileModel> playlist = new ArrayList<>();

    public search_music() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    public static search_music newInstance(String param1, String param2) {
        search_music fragment = new search_music();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_music, container, false);

        recyclerView = view.findViewById(R.id.search_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SearchAdapter(allSongs, activity);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                fetchSongs();
                if (activity.isBound() && activity.getMediaPlayerService() != null) {
                    mediaServices = activity.getMediaPlayerService();
                    mediaServices.setDataSource(musicFile.getMusicURL(), musicFile.getMusic_name(), musicFile.getImgURL(), musicFile.getArtist_name(), musicFile.getMusic_ID(), playlist);
                    mediaServices.start();
                } else {
                    Toast.makeText(activity, "Service is not yet bound", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterSongs(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterSongs(s);
                return true;
            }
        });

        recommendRandomSongs();


        return view;
    }

    private void filterSongs(String query) {
        if (TextUtils.isEmpty(query)) {
            recommendRandomSongs();
        } else {
            List<FileModel> filteredSongs = new ArrayList<>();

            for (FileModel musicFile : allSongs) {
                if (musicFile.getMusic_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(musicFile);
                }
            }
            if (!filteredSongs.isEmpty()) {
                adapter.setFilteredList(filteredSongs);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void recommendRandomSongs() {
        int numberOfRecommendations = 10;
        adapter.setFilteredList(new ArrayList<>());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("musics");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FileModel song = dataSnapshot.getValue(FileModel.class);
                    allSongs.add(song);
                }
                Collections.shuffle(allSongs);
                List<FileModel> recommendedSongs = allSongs.subList(0, Math.min(numberOfRecommendations, allSongs.size()));
                adapter.setFilteredList(recommendedSongs);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchSongs() {
        FirebaseDatabase.getInstance().getReference("musics")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FileModel musicFile = dataSnapshot.getValue(FileModel.class);
                            playlist.add(musicFile);
                        }
                        Collections.shuffle(playlist);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
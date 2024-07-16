package com.example.sangeetapplication.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.Adapters.PlayListAdapter;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class library_music extends Fragment {

    RecyclerView likedRecycler;
    PlayListAdapter adapter;
    List<FileModel> song_list = new ArrayList<>();
    private Set<String> sharedSongs;
    private MainActivity activity;
    private MediaServices mediaServices;

    public library_music() {
        // Required empty public constructor
    }


    public static library_music newInstance(String param1, String param2) {
        library_music fragment = new library_music();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_library_music, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("liked_songs", Context.MODE_PRIVATE);
        sharedSongs = sharedPreferences.getStringSet("musicID", new HashSet<>());

        likedRecycler = view.findViewById(R.id.playlist_library);
        likedRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlayListAdapter(song_list, activity);
        likedRecycler.setAdapter(adapter);
        addSongtoList();

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                if (activity.isBound() && activity.getMediaPlayerService() != null) {
                    mediaServices = activity.getMediaPlayerService();
                    mediaServices.setDataSource(musicFile.getMusicURL(), musicFile.getMusic_name(), musicFile.getImgURL(), musicFile.getArtist_name(), musicFile.getMusic_ID(), song_list);
                    mediaServices.start();
                    mediaServices.currentSongIndex = song_list.indexOf(musicFile);
                } else {
                    Toast.makeText(activity, "Service is not yet bound", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void addSongtoList() {
        FirebaseDatabase.getInstance().getReference("musics")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FileModel songFile = dataSnapshot.getValue(FileModel.class);
                            if (sharedSongs != null && sharedSongs.contains(songFile.getMusic_ID())) {
                                song_list.add(songFile);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
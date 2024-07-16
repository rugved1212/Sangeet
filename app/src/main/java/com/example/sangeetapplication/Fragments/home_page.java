package com.example.sangeetapplication.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sangeetapplication.Adapters.FileAdapter;
import com.example.sangeetapplication.Adapters.MusicAdapter;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class home_page extends Fragment {

    private RecyclerView recyclerRomantic;
    private RecyclerView recyclerSpiritual;
    private RecyclerView recyclerRock;
    private RecyclerView recyclerSad;
    private RecyclerView recyclerChill;
    private RecyclerView recyclerRecommened;
    private RecyclerView recycleRecent;
    private MusicAdapter recommenedAdapter;
    private List<FileModel> recommenedMusic;
    private FileAdapter romAdapter;
    private FileAdapter spiAdapter;
    private FileAdapter rockAdapter;
    private FileAdapter sadAdapter;
    private FileAdapter chillAdapter;
    private FileAdapter recentAdapter;
    private List<FileModel> romanticMusic;
    private List<FileModel> spiritualMusic;
    private List<FileModel> rockMusic;
    private List<FileModel> chillMusic;
    private List<FileModel> sadMusic;
    private List<FileModel> recentMusic;
    private DatabaseReference songsRef;
    private MainActivity activity;
    private MediaServices mediaServices;
    public List<FileModel> playlist = new ArrayList<>();

    public home_page() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    public static home_page newInstance(String param1, String param2) {
        home_page fragment = new home_page();
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
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        songsRef = FirebaseDatabase.getInstance().getReference("musics");


        recyclerRomantic = view.findViewById(R.id.love_song_played);
        romanticMusic = new ArrayList<>();
        romAdapter = new FileAdapter(romanticMusic);

        defining(recyclerRomantic, romAdapter, "Romantic");

        recyclerSpiritual = view.findViewById(R.id.spiritual_song_played);
        spiritualMusic = new ArrayList<>();
        spiAdapter = new FileAdapter(spiritualMusic);

        defining(recyclerSpiritual, spiAdapter, "Spiritual");

        recyclerRock = view.findViewById(R.id.rock_song_played);
        rockMusic = new ArrayList<>();
        rockAdapter = new FileAdapter(rockMusic);

        defining(recyclerRock, rockAdapter, "Rock");

        recyclerChill = view.findViewById(R.id.chill_song_played);
        chillMusic = new ArrayList<>();
        chillAdapter = new FileAdapter(chillMusic);

        defining(recyclerChill, chillAdapter, "Chill");

        recyclerSad = view.findViewById(R.id.sad_song_played);
        sadMusic = new ArrayList<>();
        sadAdapter = new FileAdapter(sadMusic);

        defining(recyclerSad, sadAdapter, "Sad");

        recyclerRecommened = view.findViewById(R.id.recommened_song_played);
        recommenedMusic = new ArrayList<>();
        recommenedAdapter = new MusicAdapter(recommenedMusic);
        recyclerRecommened.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecommened.setAdapter(recommenedAdapter);
        loadRecommenedSong(recommenedAdapter);

        recycleRecent = view.findViewById(R.id.recent_song_played);
        recentMusic = new ArrayList<>();
        recentAdapter = new FileAdapter(recentMusic);
        recycleRecent.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recycleRecent.setAdapter(recentAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadRecentSong(recentAdapter);
                new Handler().postDelayed(this, 1000);
            }
        }, 1000);

        return view;
    }

    private void loadRecentSong(FileAdapter adapter) {
        recentMusic.clear();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("musics_played", Context.MODE_PRIVATE);
        for (int i = 5; i >= 0; i--) {
            String name = sharedPreferences.getString("name"+i, null);
            String artist = sharedPreferences.getString("artist"+i, null);
            String img = sharedPreferences.getString("imgURL"+i, null);
            String url = sharedPreferences.getString("songURL"+i, null);
            FileModel songModel = new FileModel(name, artist, img, url);
            if (name!=null) {
                recentMusic.add(songModel);
            }
        }

        adapter.setSongList(recentMusic);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                onitemclickfunction(musicFile);
            }
        });
    }

    private void defining(RecyclerView recyclerView, FileAdapter adapter, String type) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        loadSong(adapter, type);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                onitemclickfunction(musicFile);
            }
        });
    }

    private void onitemclickfunction(FileModel musicFile) {
        fetchSongs();
        if (activity.isBound() && activity.getMediaPlayerService() != null) {
            mediaServices = activity.getMediaPlayerService();
            mediaServices.setDataSource(musicFile.getMusicURL(), musicFile.getMusic_name(), musicFile.getImgURL(), musicFile.getArtist_name(), musicFile.getMusic_ID(), playlist);
            mediaServices.start();
        } else {
            Toast.makeText(activity, "Service is not yet bound", Toast.LENGTH_SHORT).show();
        }
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

    private void loadSong(FileAdapter adapter, String type) {
        Query query = songsRef.orderByChild("musicType").equalTo(type);
        query.limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FileModel> songs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FileModel song = snapshot.getValue(FileModel.class);
                    songs.add(song);
                }
                Collections.shuffle(songs);

                adapter.setSongList(songs);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading songs from Firebase Database: " + error.getMessage());
            }
        });
    }
    private void loadRecommenedSong(MusicAdapter adapter) {
        songsRef.limitToLast(8).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FileModel> songs = new ArrayList<>();
                for (DataSnapshot snaps : snapshot.getChildren()) {
                    FileModel recommenedsong = snaps.getValue(FileModel.class);
                    if (recommenedsong != null) {
                        songs.add(recommenedsong);
                    }
                }
                Collections.shuffle(songs);
                adapter.setMusicList(songs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading songs from Firebase Database: " + error.getMessage());
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                onitemclickfunction(musicFile);
            }
        });
    }
}
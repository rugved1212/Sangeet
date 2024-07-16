package com.example.sangeetapplication.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.sangeetapplication.BottomSheet.BottomSheet;
import com.example.sangeetapplication.BottomSheet.PlaylistSheet;
import com.example.sangeetapplication.Fragments.home_page;
import com.example.sangeetapplication.Fragments.library_music;
import com.example.sangeetapplication.Fragments.search_music;
import com.example.sangeetapplication.Interfaces.UpdateUI;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private MediaServices mediaServices;
    public boolean isBound = false;
    private SharedPreferences sharedPreferences;
    public Set<String> likedSong = new HashSet<>();
    CardView manage_song;
    ImageButton play_card;
    TextView NameOfMusic;
    ImageView ImgOfMusic;
    TextView ArtistOfMusic;
    ImageButton listBtn;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaServices.LocalBinder binder = (MediaServices.LocalBinder) service;
            mediaServices = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    public boolean isBound() {
        return isBound;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, MediaServices.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("liked_songs", Context.MODE_PRIVATE);
        likedSong = getLikedSongs();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.home) {
                fragment = new home_page();
            } else if (item.getItemId() == R.id.search) {
                fragment = new search_music();
            }else if (item.getItemId() == R.id.liked_song) {
                fragment = new library_music();
            }
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        loadFragment(new home_page());

        manage_song = findViewById(R.id.manage_song);
        play_card = findViewById(R.id.playbtn);
        NameOfMusic = findViewById(R.id.name);
        ImgOfMusic = findViewById(R.id.image);
        ArtistOfMusic = findViewById(R.id.artist);
        listBtn = findViewById(R.id.list);


        manage_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheet bottomSheet = new BottomSheet(mediaServices, MainActivity.this);
                bottomSheet.show(getSupportFragmentManager(), "");
            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaylistSheet playlistSheet = new PlaylistSheet(mediaServices, MainActivity.this);
                playlistSheet.show(getSupportFragmentManager(), "");
            }
        });

        play_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaServices != null) {
                    if (mediaServices.isPlaying()) {
                        mediaServices.pause();
                        play_card.setBackgroundResource(R.drawable.play);
                    } else {
                        mediaServices.start();
                        play_card.setBackgroundResource(R.drawable.pause);
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUI();
                new Handler().postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaServices.savelastPlayedSong(mediaServices.musicName, mediaServices.artistName, mediaServices.musicImg, mediaServices.musicUrl, mediaServices.getCurrentPosition());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaServices.savelastPlayedSong(mediaServices.musicName, mediaServices.artistName, mediaServices.musicImg, mediaServices.musicUrl, mediaServices.getCurrentPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaServices.savelastPlayedSong(mediaServices.musicName, mediaServices.artistName, mediaServices.musicImg, mediaServices.musicUrl, mediaServices.getCurrentPosition());
    }

    public MediaServices getMediaPlayerService() {
        return mediaServices;
    }
    public void addLikedSong(String songID) {
        likedSong.add(songID);
        saveLikedSongs();
    }
    public void removeLikedSong(String songID) {
        likedSong.remove(songID);
        saveLikedSongs();
    }
    private void saveLikedSongs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("musicID", likedSong);
        editor.apply();
    }
    public Set<String> getLikedSongs() {
        return sharedPreferences.getStringSet("musicID", new HashSet<>());
    }

    private void updateUI() {
        if (mediaServices!=null) {
            manage_song.setVisibility(View.VISIBLE);
            NameOfMusic.setText(mediaServices.musicName);
            ArtistOfMusic.setText(mediaServices.artistName);
            Glide.with(getBaseContext())
                    .load(mediaServices.musicImg)
                    .placeholder(R.drawable.default_music_background1)
                    .error(R.drawable.default_music_background1)
                    .into(ImgOfMusic);

            if (mediaServices.isPlaying()) {
                play_card.setBackgroundResource(R.drawable.pause);
            } else {
                play_card.setBackgroundResource(R.drawable.play);
            }
        } else {
            manage_song.setVisibility(View.GONE);
        }
    }

}
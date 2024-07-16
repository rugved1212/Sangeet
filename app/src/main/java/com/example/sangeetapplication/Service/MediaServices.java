package com.example.sangeetapplication.Service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.UpdateUI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MediaServices extends Service {
    private MediaPlayer mediaPlayer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final IBinder binder = new LocalBinder();
    public List<FileModel> playlistsong = new ArrayList<>();
    public String musicName, artistName, musicImg, musicUrl, musicID;
    private static final int MAX_SONGS = 5;
    private List<FileModel> playedSongs = new ArrayList<>();
    public String lastplayedsong, lastplayedartist, lastplayedimg, lastplayedmusicUrl;
    public int lastplayedcurrenttime;
    public int currentSongIndex = -1;
    private UpdateUI updateUI;
    public boolean isShuffle = false;

    public void setUpdateUI(UpdateUI updateUI) {
        this.updateUI = updateUI;
    }

    public MediaServices() {
    }

    public class LocalBinder extends Binder {
        public MediaServices getService() {
            return MediaServices.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        sharedPreferences = getSharedPreferences("musics_played", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        updatePlayedSongList();
        getlastPlayedSong();
        if (lastplayedsong != null && !lastplayedsong.isEmpty()) {
            musicName = lastplayedsong;
            artistName = lastplayedartist;
            musicImg = lastplayedimg;
            musicUrl = lastplayedmusicUrl;
            try {
                mediaPlayer.setDataSource(lastplayedmusicUrl);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(lastplayedcurrenttime);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playSongintheList();
                }
            });

            if (updateUI != null) {
                updateUI.onSongChange();
                updateUI.onSongPausePlay();
            }

            if (playlistsong.isEmpty()) {
                createPlaylist();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setDataSource(String url, String name, String img, String artist, String ID, List<FileModel> playlist) {
        musicImg = img;
        musicName = name;
        artistName = artist;
        musicUrl = url;
        playlistsong = playlist;
        musicID = ID;

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();

            savePlayedSong(new FileModel(name, artist, img, url));
            savelastPlayedSong(name, artist, img, url, getCurrentPosition());
            if (updateUI != null) {
                updateUI.onSongChange();
                updateUI.onSongPausePlay();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (isShuffle) {
                        playRandomSonginList();
                    } else {
                        playSongintheList();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playSongintheList() {
        currentSongIndex = (currentSongIndex + 1) % playlistsong.size();
        FileModel nextSong = playlistsong.get(currentSongIndex);
        setDataSource(nextSong.getMusicURL(), nextSong.getMusic_name(), nextSong.getImgURL(), nextSong.getArtist_name(), nextSong.getMusic_ID(), playlistsong);
        start();
        if (updateUI != null) {
            updateUI.onSongChange();
            updateUI.onSongPausePlay();
        }
    }

    private void playRandomSonginList() {
        currentSongIndex = new Random().nextInt(playedSongs.size());
        FileModel nextSong = playlistsong.get(currentSongIndex);
        setDataSource(nextSong.getMusicURL(), nextSong.getMusic_name(), nextSong.getImgURL(), nextSong.getArtist_name(), nextSong.getMusic_ID(), playlistsong);
        start();
        if (updateUI != null) {
            updateUI.onSongChange();
            updateUI.onSongPausePlay();
        }
    }

    public void loop(boolean status) {
        mediaPlayer.setLooping(status);
    }

    public boolean isloop() {
        return mediaPlayer.isLooping();
    }

    public void playNext() {
        currentSongIndex = (currentSongIndex + 1) % playlistsong.size();
        FileModel nextSong = playlistsong.get(currentSongIndex);
        setDataSource(nextSong.getMusicURL(), nextSong.getMusic_name(), nextSong.getImgURL(), nextSong.getArtist_name(), nextSong.getMusic_ID(), playlistsong);
        start();
        if (updateUI != null) {
            updateUI.onSongChange();
            updateUI.onSongPausePlay();
        }
    }
    public void playPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex = (currentSongIndex - 1) % playlistsong.size();
            FileModel nextSong = playlistsong.get(currentSongIndex);
            setDataSource(nextSong.getMusicURL(), nextSong.getMusic_name(), nextSong.getImgURL(), nextSong.getArtist_name(), nextSong.getMusic_ID(), playlistsong);
            start();
            if (updateUI != null) {
                updateUI.onSongChange();
                updateUI.onSongPausePlay();
            }
        }
    }
    public void playFromPlaylist(int index) {
        currentSongIndex = index;
        FileModel nextSong = playlistsong.get(currentSongIndex);
        setDataSource(nextSong.getMusicURL(), nextSong.getMusic_name(), nextSong.getImgURL(), nextSong.getArtist_name(), nextSong.getMusic_ID(), playlistsong);
        start();
        if (updateUI != null) {
            updateUI.onSongChange();
            updateUI.onSongPausePlay();
        }
    }
    public void start() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (updateUI != null) {
                updateUI.onSongPausePlay();
            }
        }
    }
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            savelastPlayedSong(musicName, artistName, musicImg, musicUrl, getCurrentPosition());
            mediaPlayer.pause();
            if (updateUI != null) {
                updateUI.onSongPausePlay();
            }
        }
    }
    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (updateUI != null) {
                updateUI.onSongPausePlay();
            }
        }
    }
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        savelastPlayedSong(musicName, artistName, musicImg, musicUrl, getCurrentPosition());
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }
    private void updatePlayedSongList() {
        for (int i = 0; i < MAX_SONGS; i++) {
            String name = sharedPreferences.getString("name"+i, null);
            String artist = sharedPreferences.getString("artist"+i, null);
            String img = sharedPreferences.getString("imgURL"+i, null);
            String url = sharedPreferences.getString("songURL"+i, null);
            FileModel songModel = new FileModel(name, artist, img, url);
            if (name!=null) {
                playedSongs.add(songModel);
            }
        }
    }
    private void savePlayedSong(FileModel song) {
        if (playedSongs.size() >= MAX_SONGS) {
            playedSongs.remove(0);
        }
        for (FileModel music : playedSongs) {
            if (music.getMusic_name().equals(song.getMusic_name())) {
                return;
            }
        }
        playedSongs.add(song);

        savedToSharedPreferance();
    }
    private void savedToSharedPreferance() {
        editor.clear();
        for (int i = 0; i < playedSongs.size(); i++) {
            FileModel music = playedSongs.get(i);
            editor.putString("name"+i, music.getMusic_name());
            editor.putString("artist"+i, music.getArtist_name());
            editor.putString("imgURL"+i, music.getImgURL());
            editor.putString("songURL"+i, music.getMusicURL());
        }
        editor.apply();
    }
    public void savelastPlayedSong(String name, String artist, String img, String url, int currentduration) {
        editor.putString("lastname", name);
        editor.putString("lastartist", artist);
        editor.putString("lastimgURL", img);
        editor.putString("lastsongURL", url);
        editor.putInt("lastcurrentduration", currentduration);
        editor.apply();
    }
    private void getlastPlayedSong() {
        lastplayedsong = sharedPreferences.getString("lastname",null);
        lastplayedartist = sharedPreferences.getString("lastartist",null);
        lastplayedimg = sharedPreferences.getString("lastimgURL",null);
        lastplayedmusicUrl = sharedPreferences.getString("lastsongURL",null);
        lastplayedcurrenttime = sharedPreferences.getInt("lastcurrentduration",0);
    }

    private void createPlaylist() {
        FirebaseDatabase.getInstance().getReference("musics")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FileModel musicFile = dataSnapshot.getValue(FileModel.class);
                            playlistsong.add(musicFile);
                        }
                        Collections.shuffle(playlistsong);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
package com.example.sangeetapplication.DataFile;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class LikedSongs {
    private static final String PREFS_NAME = "liked_songs";
    private static final String KEY_LIKED_SONGS = "musicID";
    private final SharedPreferences sharedPreferences;

    public LikedSongs(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Set<String> getLikedSongs() {
        return sharedPreferences.getStringSet(KEY_LIKED_SONGS, new HashSet<>());
    }

    public void addLikedSong(String songId) {
        Set<String> likedSongs = getLikedSongs();
        likedSongs.add(songId);
        saveLikedSongs(likedSongs);
    }

    public void removeLikedSong(String songId) {
        Set<String> likedSongs = getLikedSongs();
        likedSongs.remove(songId);
        saveLikedSongs(likedSongs);
    }

    private void saveLikedSongs(Set<String> likedSongs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_LIKED_SONGS, likedSongs);
        editor.apply();
    }
}

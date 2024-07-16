package com.example.sangeetapplication.DataFile;

public class FileModel {
    private String music_ID;
    private String music_name;
    private String artist_name;
    private String musicURL;
    private String musicType;
    private String storageFile;
    private String imgURL;

    public FileModel() {}
    public FileModel(String music_name, String artist_name, String imgURL, String musicURL) {
        this.music_name = music_name;
        this.artist_name = artist_name;
        this.imgURL = imgURL;
        this.musicURL = musicURL;
    }

    public String getMusic_name() {
        return music_name;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public String getMusicURL() {
        return musicURL;
    }

    public String getStorageFile(){
        return storageFile;
    }

    public String getImgURL() {
        return imgURL;
    }
    public String getMusic_ID() {
        return music_ID;
    }
}

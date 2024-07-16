package com.example.sangeetapplication.BottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.Adapters.UpcomingSongAdapter;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSheet extends BottomSheetDialogFragment {

    MediaServices mediaServices;
    MainActivity mainActivity;

    public PlaylistSheet(MediaServices mediaServices, MainActivity mainActivity) {
        this.mediaServices = mediaServices;
        this.mainActivity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_bottom_sheet, container, false);

        List<FileModel> songList = mediaServices.playlistsong;


        RecyclerView recyclerView = view.findViewById(R.id.upcoming_song_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        UpcomingSongAdapter adapter = new UpcomingSongAdapter(songList, mainActivity);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();


        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(FileModel musicFile) {
                int songIndex = songList.indexOf(musicFile);
                mediaServices.playFromPlaylist(songIndex);
                dismiss();
            }
        });

        return view;
    }
}

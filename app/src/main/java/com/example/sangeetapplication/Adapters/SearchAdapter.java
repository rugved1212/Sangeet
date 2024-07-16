package com.example.sangeetapplication.Adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<FileModel> allSongs;
    private List<FileModel> filteredSongs;
    private OnItemClickListener listener;
    private MainActivity mainActivity;


    public SearchAdapter(List<FileModel> allSongs, MainActivity mainActivity) {
        this.allSongs = allSongs;
        this.filteredSongs = new ArrayList<>(allSongs);
        this.mainActivity = mainActivity;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFilteredList(List<FileModel> filteredList) {
        this.filteredSongs = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_music_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel musicFile = filteredSongs.get(position);
        holder.musicName.setText(musicFile.getMusic_name());
        if (musicFile.getArtist_name() != null) {
            holder.artistName.setText(musicFile.getArtist_name());
        }
        Glide.with(holder.itemView.getContext())
                .load(musicFile.getImgURL())
                .placeholder(R.drawable.default_music_background1)
                .error(R.drawable.default_music_background1)
                .into(holder.imgView);


        holder.isLiked = mainActivity.likedSong.contains(musicFile.getMusic_ID());
        holder.like_btn.setBackgroundResource(holder.isLiked ? R.drawable.like : R.drawable.heart);

        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.isLiked) {
                    holder.isLiked = false;
                    holder.like_btn.setBackgroundResource(R.drawable.heart);
                    mainActivity.removeLikedSong(musicFile.getMusic_ID());
                } else {
                    holder.isLiked = true;
                    holder.like_btn.setBackgroundResource(R.drawable.like);
                    mainActivity.addLikedSong(musicFile.getMusic_ID());
                }
                mainActivity.likedSong = mainActivity.getLikedSongs();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView musicName;
        TextView artistName;
        ImageView imgView;
        ImageButton like_btn;
        boolean isLiked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicName = itemView.findViewById(R.id.name_of_song);
            artistName = itemView.findViewById(R.id.name_of_artist);
            imgView = itemView.findViewById(R.id.image_of_song);
            like_btn = itemView.findViewById(R.id.like_song);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(filteredSongs.get(position));
                    }
                }
            });
        }
    }
}

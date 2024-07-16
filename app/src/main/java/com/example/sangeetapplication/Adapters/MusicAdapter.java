package com.example.sangeetapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sangeetapplication.DataFile.FileModel;
import com.example.sangeetapplication.Interfaces.OnItemClickListener;
import com.example.sangeetapplication.R;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private List<FileModel> musicList;
    private OnItemClickListener listener;

    public MusicAdapter(List<FileModel> musicList) {
        this.musicList = musicList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_music_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel music = musicList.get(position);
        holder.nameTextView.setText(music.getMusic_name());
        if (music.getArtist_name() != null) {
            holder.artistTextView.setText(music.getArtist_name());
        }

        Glide.with(holder.itemView.getContext())
                .load(music.getImgURL())
                .placeholder(R.drawable.default_music_background1)
                .error(R.drawable.default_music_background1)
                .into(holder.imgOfSong);
    }

    public void setMusicList(List<FileModel> newData) {
        this.musicList = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        TextView artistTextView;
        ImageView imgOfSong;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_of_song);
            artistTextView = itemView.findViewById(R.id.name_of_artist);
            imgOfSong = itemView.findViewById(R.id.image_of_song);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position!= RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(musicList.get(position));
                    }
                }
            });
        }
    }
}

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

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private List<FileModel> songList;
    private OnItemClickListener listener;

    public FileAdapter(List<FileModel> songList) {
        this.songList = songList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_music_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel song = songList.get(position);
        holder.titleTextView.setText(song.getMusic_name());

        Glide.with(holder.itemView.getContext())
                .load(song.getImgURL())
                .placeholder(R.drawable.default_music_background1)
                .error(R.drawable.default_music_background1)
                .into(holder.imageView);
    }

    public void setSongList(List<FileModel> songList) {
        this.songList = songList;
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.namesong);
            imageView = itemView.findViewById(R.id.imagesong);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(songList.get(position));
                    }
                }
            });
        }
    }
}

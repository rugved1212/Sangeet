package com.example.sangeetapplication.BottomSheet;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.sangeetapplication.Activity.MainActivity;
import com.example.sangeetapplication.Interfaces.UpdateUI;
import com.example.sangeetapplication.R;
import com.example.sangeetapplication.Service.MediaServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheet extends BottomSheetDialogFragment implements UpdateUI {
    BottomSheetBehavior behavior;
    private MediaServices mediaServices;
    private TextView curentTime;
    private TextView musicDuration;
    private SeekBar seekBar;
    private Handler handler;
    public ImageButton playlistBtn;
    public ImageButton loopBtn;
    public ImageButton playPrevious;
    public ImageButton playNext;
    public ImageButton like_btn;
    public ImageButton shuffle;
    ImageView playing_song_image;
    TextView playing_song_name;
    MainActivity mainActivity;

    public BottomSheet(MediaServices mediaServices, MainActivity mainActivity) {
        this.mediaServices = mediaServices;
        this.mainActivity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        if (mediaServices!=null) {
            mediaServices.setUpdateUI(this);
        }

        ImageButton play_or_pause_btn = view.findViewById(R.id.play_or_pause_btn);

        playing_song_image = view.findViewById(R.id.playing_song_image);
        playing_song_name = view.findViewById(R.id.playing_song_name);
        curentTime = view.findViewById(R.id.current_time);
        musicDuration = view.findViewById(R.id.max_time);
        seekBar = view.findViewById(R.id.music_seekbar);
        like_btn = view.findViewById(R.id.like_btn);
        shuffle = view.findViewById(R.id.shuffle);


        if (mediaServices!=null) {
            playing_song_name.setText(mediaServices.musicName);
            Glide.with(this)
                    .load(mediaServices.musicImg)
                    .error(R.drawable.default_music_background1)
                    .placeholder(R.drawable.default_music_background1)
                    .into(playing_song_image);
            curentTime.setText(formatDuration(mediaServices.getCurrentPosition()));
            musicDuration.setText(formatDuration(mediaServices.getDuration()));
            if (mainActivity!=null) {
                if (mainActivity.likedSong.contains(mediaServices.musicID)) {
                    like_btn.setBackgroundResource(R.drawable.like);
                } else {
                    like_btn.setBackgroundResource(R.drawable.heart);
                }
            }

            if (mediaServices.isPlaying()) {
                play_or_pause_btn.setBackgroundResource(R.drawable.pause);
            } else {
                play_or_pause_btn.setBackgroundResource(R.drawable.play);
            }
            if (mediaServices.isShuffle) {
                shuffle.setBackgroundResource(R.drawable.shuffle_on);
            } else {
                shuffle.setBackgroundResource(R.drawable.shuffle_off);
            }
        }

        like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity.likedSong.contains(mediaServices.musicID)) {
                    like_btn.setBackgroundResource(R.drawable.heart);
                    mainActivity.removeLikedSong(mediaServices.musicID);
                } else {
                    like_btn.setBackgroundResource(R.drawable.like);
                    mainActivity.addLikedSong(mediaServices.musicID);
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaServices.isShuffle) {
                    mediaServices.isShuffle = false;
                    shuffle.setBackgroundResource(R.drawable.shuffle_off);
                    Toast.makeText(getContext(), "OFF", Toast.LENGTH_SHORT).show();
                } else {
                    mediaServices.isShuffle = true;
                    shuffle.setBackgroundResource(R.drawable.shuffle_on);
                    Toast.makeText(getContext(), "ON", Toast.LENGTH_SHORT).show();
                }
            }
        });


        play_or_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaServices!=null) {
                    if (mediaServices.isPlaying()) {
                        mediaServices.pause();
                    } else {
                        mediaServices.start();
                    }
                }
            }
        });

        handler = new Handler();
        updateSeekBar();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && mediaServices!=null) {
                    mediaServices.seekTo(i);
                    curentTime.setText(formatDuration(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBar();
            }
        });

        loopBtn = view.findViewById(R.id.loop);
        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaServices.isloop()) {
                    mediaServices.loop(false);
                    loopBtn.setBackgroundResource(R.drawable.music_loop_off);
                } else {
                    mediaServices.loop(true);
                    loopBtn.setBackgroundResource(R.drawable.music_loop_on);
                }
            }
        });

        playNext = view.findViewById(R.id.go_to_next_song);
        playNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaServices.playNext();
            }
        });

        playPrevious = view.findViewById(R.id.go_to_previous_song);
        playPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaServices.playPrevious();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void updateSeekBar() {
        if (mediaServices != null) {
            seekBar.setMax(mediaServices.getDuration());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaServices!=null && mediaServices.isPlaying()) {
                        int currentPosition = mediaServices.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        curentTime.setText(formatDuration(currentPosition));
                        musicDuration.setText(formatDuration(mediaServices.getDuration()));
                    }
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    private String formatDuration(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onSongChange() {
        if (getView() != null && isAdded()) {
            TextView playingSongName = getView().findViewById(R.id.playing_song_name);
            ImageView playingSongImage = getView().findViewById(R.id.playing_song_image);

            playingSongName.setText(mediaServices.musicName);
            Glide.with(this)
                    .load(mediaServices.musicImg)
                    .error(R.drawable.default_music_background1)
                    .placeholder(R.drawable.default_music_background1)
                    .into(playingSongImage);

            TextView NameOfMusic = requireActivity().findViewById(R.id.name);
            ImageView ImgOfMusic = requireActivity().findViewById(R.id.image);
            TextView ArtistOfMusic = requireActivity().findViewById(R.id.artist);

            NameOfMusic.setText(mediaServices.musicName);
            ArtistOfMusic.setText(mediaServices.artistName);
            Glide.with(requireActivity())
                    .load(mediaServices.musicImg)
                    .placeholder(R.drawable.default_music_background1)
                    .error(R.drawable.default_music_background1)
                    .into(ImgOfMusic);
        }
    }

    @Override
    public void onSongPausePlay() {
        if (getView() != null && isAdded()) {
            ImageButton play_or_pause_btn = getView().findViewById(R.id.play_or_pause_btn);
            ImageButton play_card = requireActivity().findViewById(R.id.playbtn);

            if (mediaServices!=null) {
                if (mediaServices.isPlaying()) {
                    play_or_pause_btn.setBackgroundResource(R.drawable.pause);
                    play_card.setBackgroundResource(R.drawable.pause);
                } else {
                    play_or_pause_btn.setBackgroundResource(R.drawable.play);
                    play_card.setBackgroundResource(R.drawable.play);
                }
            }
        }
    }
}

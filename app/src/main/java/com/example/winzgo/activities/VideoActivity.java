package com.example.winzgo.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.example.winzgo.MyApplication;
import com.example.winzgo.R;
import com.example.winzgo.databinding.ActivityVideoBinding;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

public class VideoActivity extends AppCompatActivity {

    private ExoPlayer player;
    private ActivityVideoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        player = new ExoPlayer.Builder(this).build();

        Dialog dialog = Constants.showProgressDialog(this);
        StorageReference storageReference = ((MyApplication) getApplication()).firebaseStorage.getReference();
        StorageReference ref = storageReference.child("video/recharge_video.mp4");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                dialog.dismiss();
                startVideo(uri);
//                String url = uri.toString();
//                Glide.with(VideoActivity.this).load(url).placeholder(R.drawable.upiqr).into(binding.qrIv);
            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Log.d("Manual.java", "onFailure: " + e);
            }
        });
    }

    private void startVideo(Uri videoUrl) {
        binding.playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
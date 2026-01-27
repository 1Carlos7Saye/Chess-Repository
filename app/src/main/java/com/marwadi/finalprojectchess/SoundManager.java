package com.marwadi.finalprojectchess;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {
    private final MediaPlayer movePlayer;
    private final MediaPlayer capturePlayer;
    private final MediaPlayer checkPlayer;
    private final MediaPlayer checkmatePlayer;

    public SoundManager(Context context) {
        // R.raw references the files you just pasted!
        movePlayer = MediaPlayer.create(context, R.raw.move);
        capturePlayer = MediaPlayer.create(context, R.raw.capture);
        checkPlayer = MediaPlayer.create(context, R.raw.check);
        checkmatePlayer = MediaPlayer.create(context, R.raw.checkmate);
    }

    public void playMove() {
        if (movePlayer != null) {
            movePlayer.seekTo(0); // Restart sound from beginning
            movePlayer.start();
        }
    }

    public void playCapture() {
        if (capturePlayer != null) {
            capturePlayer.seekTo(0);
            capturePlayer.start();
        }
    }

    public void playCheck() {
        if (checkPlayer != null) {
            checkPlayer.seekTo(0);
            checkPlayer.start();
        }
    }

    public void playCheckmate() {
        if (checkmatePlayer != null) {
            checkmatePlayer.seekTo(0);
            checkmatePlayer.start();
        }
    }

    // Free up resources to prevent memory leaks
    public void release() {
        if (movePlayer != null) movePlayer.release();
        if (capturePlayer != null) capturePlayer.release();
        if (checkPlayer != null) checkPlayer.release();
        if (checkmatePlayer != null) checkmatePlayer.release();
    }
}
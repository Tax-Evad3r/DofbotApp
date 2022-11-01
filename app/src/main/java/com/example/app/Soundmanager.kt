package com.example.app

import android.content.Context
import android.media.MediaPlayer


    var mMediaPlayer: MediaPlayer? = null

    //binding.playButton.SetOnClickListener{}
    // 1. Plays the water sound
    fun playSound(context: Context? , filename: String?) {
        if (mMediaPlayer == null) {
            mMediaPlayer =
                MediaPlayer.create(context, R.raw.george_ezra_shotgun_jesse_bloch_bootleg)
            // mMediaPlayer!!.isLooping = true

            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }
    // 2. Pause playback
    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }

    // 3. Stops playback
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }


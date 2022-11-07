package com.example.app

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer


var mMediaPlayer: MediaPlayer? = null

    //binding.playButton.SetOnClickListener{}
    // 1. Plays the water sound
    fun playSound(context: Context? , filename: String?) {
        if (mMediaPlayer == null) {
            var temp = playParse(context,filename)
           // "res/raw/george_ezra_shotgun_jesse_bloch_bootleg.mp3"
            // R.raw.george_ezra_shotgun_jesse_bloch_bootleg
           // var myUri : Uri = Uri.create(temp)
            mMediaPlayer =
                MediaPlayer.create(context, temp)
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

fun playParse(context: Context? , filename: String?): Int {
    if (context != null) {
        return context.resources.getIdentifier(filename, "raw", "com.example.app")
    }
    return 0
}

    // 4. Destroys the MediaPlayer instance when the app is closed
   // override
    fun onStop() {
       // super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
}


package com.example.app

import android.content.*
import android.media.MediaPlayer
import java.io.IOException


var mMediaPlayer: MediaPlayer? = null

fun playSound(context: Context, filename: String?) {
    if (mMediaPlayer == null) {
        try {
            var afd = context.assets.openFd("sounds/george_ezra_shotgun_jesse_bloch_bootleg.mp3")
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(afd.fileDescriptor)
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

fun importSounds(context: Context?): MutableList<String> {
    //retrieve all files in sounds folder
    val assetsList = context!!.assets.list("sounds")

    //create a list to hold all found sounds
    val soundList : MutableList<String> = mutableListOf()

    //if assets exist loop through and add to list
    if (assetsList != null) {
        for (assetName in assetsList) {
            soundList.add(assetName)
        }
    }

    //returned found
    return soundList
}

// Destroys the MediaPlayer instance when the app is closed
fun onStop() {
    if (mMediaPlayer != null) {
        mMediaPlayer!!.release()
        mMediaPlayer = null
    }
}


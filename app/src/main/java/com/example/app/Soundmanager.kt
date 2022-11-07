package com.example.app

import android.content.Context
import android.media.MediaPlayer
import java.io.IOException


var mMediaPlayer: MediaPlayer? = null

//Create new mediaplayer with the sound right sound file.
fun playSound(context: Context, filename: String?) {
    if (mMediaPlayer != null) {
        stopSound()
    }
    try {
        var afd = context.assets.openFd("sounds/george_ezra_shotgun_jesse_bloch_bootleg.mp3")
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setDataSource(afd.fileDescriptor)
        mMediaPlayer!!.prepare()
        mMediaPlayer!!.start()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

//Stops playback
fun stopSound() {
    mMediaPlayer!!.stop()
    mMediaPlayer!!.release()
    mMediaPlayer = null
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

//TODO: fix override.

// Destroys the MediaPlayer instance when the app is closed
//override
fun onStop() {
    if (mMediaPlayer != null) {
        mMediaPlayer!!.release()
        mMediaPlayer = null
    }
}


package com.example.app

import android.content.Context
import android.media.MediaPlayer
import java.io.IOException

var mMediaPlayer: MediaPlayer? = null

//TODO: This needs more cleanup

//Create new mediaplayer with the sound right sound file.
fun playSound(context: Context, filename: String?) {
    if (mMediaPlayer != null) {
        stopSound()
    }
    try {
        var afd = context.assets.openFd("sounds/$filename")
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setDataSource(afd)
        mMediaPlayer!!.prepare()
        mMediaPlayer!!.start()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

//Create new mediaplayer with the sound right sound file.
fun playSounds(context: Context, list: MutableList<String>) {
    if (list.size > 0) {
        if (mMediaPlayer != null) {
            stopSound()
        }
        val next = list.removeAt(0)
        try {
            var afd = context.assets.openFd("sounds/$next")
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(afd)
            mMediaPlayer!!.setOnCompletionListener {
                playSounds(context, list)
            }
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

//Stops playback
fun stopSound() {
    try {
        mMediaPlayer!!.stop()
        mMediaPlayer!!.release()
        mMediaPlayer = null
    } catch (e : Exception) {
        e.printStackTrace()
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
fun stopPlayerOnStop() {
    if (mMediaPlayer != null) {
        stopSound()
    }
}


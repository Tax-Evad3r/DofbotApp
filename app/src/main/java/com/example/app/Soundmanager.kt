package com.example.app

import android.content.Context
import android.media.MediaPlayer
import java.io.IOException

var mMediaPlayer: MediaPlayer? = null

//Create new MediaPlayer use source files provided as list
fun playSounds(context: Context, list: MutableList<String>) {
    //only continue if provided list contains any sounds
    if (list.size > 0) {
        //if an existing player is found destroy it
        if (mMediaPlayer != null) {
            stopSound()
        }
        //get next sound as filename from list
        val next = list.removeAt(0)
        //create new player with the next sound and add onComplete listener to recursively play next sound when done.
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

//Stop playback and remove current MediaPlayer
fun stopSound() {
    try {
        mMediaPlayer!!.stop()
        mMediaPlayer!!.release()
        mMediaPlayer = null
    } catch (e : Exception) {
        e.printStackTrace()
    }
}

//locate all sounds then create list containing all filenames for later use
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

//helper function for onStop and onViewDestroyed overloads (in SecondFragment)
fun stopPlayerOnStop() {
    if (mMediaPlayer != null) {
        stopSound()
    }
}


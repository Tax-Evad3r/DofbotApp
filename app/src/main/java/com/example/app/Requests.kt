package com.example.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

@Serializable
    data class Data(
        var bottom_rotation: MutableList<MutableList<Int>>?,
        var joint_1: MutableList<MutableList<Int>>?,
        var joint_2: MutableList<MutableList<Int>>?,
        var joint_3: MutableList<MutableList<Int>>?,
        var claw_rotation: MutableList<MutableList<Int>>?,
        var claw_grip: MutableList<MutableList<Int>>?
    )
    {

        fun toJson() : String {
            return Json.encodeToString(this)
        }

        operator fun plus(dat1: Data) : Data {

            //if rhs is empty return lhs directly
            if(this.bottom_rotation?.size == 0 && this.joint_1?.size == 0 && this.joint_2?.size == 0 && this.joint_3?.size == 0 && this. claw_rotation?.size == 0 && this.claw_grip?.size == 0) {
                return dat1
            }

            //get longest time in first animation and add it to next
            val maxTime : MutableList<Int> = mutableListOf()

            //for each servo create temporary variable
            //store all servo data from lhs and their durations
            val bottomRotation : MutableList<MutableList<Int>> = ArrayList()
            this.bottom_rotation?.forEach {
                maxTime.add((it[0]))
                bottomRotation.add(it)
            }
            val joint1 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_1?.forEach {
                maxTime.add((it[0]))
                joint1.add(it)
            }
            val joint2 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_2?.forEach {
                maxTime.add((it[0]))
                joint2.add(it)
            }
            val joint3 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_3?.forEach {
                maxTime.add((it[0]))
                joint3.add(it)
            }
            val clawRotation : MutableList<MutableList<Int>> = ArrayList()
            this.claw_rotation?.forEach {
                maxTime.add((it[0]))
                clawRotation.add(it)
            }
            val clawGrip : MutableList<MutableList<Int>> = ArrayList()
            this.claw_grip?.forEach {
                maxTime.add((it[0]))
                clawGrip.add(it)
            }

            //find longest duration from all lhs servos
            var time = 0
            maxTime.forEach {
                if (it > time) {
                    time = it
                }
            }

            //if a motion does not use all servos add NO_ACTION with appropriate duration
            if (sumDuration(bottomRotation) < time) {
                bottomRotation.add(mutableListOf(time, -1))
            }
            if (sumDuration(joint1) < time) {
                joint1.add(mutableListOf(time, -1))
            }
            if (sumDuration(joint2) < time) {
                joint2.add(mutableListOf(time, -1))
            }
            if (sumDuration(joint3) < time) {
                joint3.add(mutableListOf(time, -1))
            }
            if (sumDuration(clawRotation) < time) {
                clawRotation.add(mutableListOf(time, -1))
            }
            if (sumDuration(clawGrip) < time) {
                clawGrip.add(mutableListOf(time, -1))
            }

            //add all servo motions from rhs and displace with duration of first animation
            dat1.bottom_rotation?.forEach {
                bottomRotation.add(mutableListOf(it[0]+time, it[1]))
            }
            dat1.joint_1?.forEach {
                joint1.add(mutableListOf(it[0]+time, it[1]))
            }
            dat1.joint_2?.forEach {
                joint2.add(mutableListOf(it[0]+time, it[1]))
            }
            dat1.joint_3?.forEach {
                joint3.add(mutableListOf(it[0]+time, it[1]))
            }
            dat1.claw_rotation?.forEach {
                clawRotation.add(mutableListOf(it[0]+time, it[1]))
            }
            dat1.claw_grip?.forEach {
                clawGrip.add(mutableListOf(it[0]+time, it[1]))
            }

            //return final data object containing both motions from lhs and rhs
            return Data(bottomRotation, joint1, joint2, joint3, clawRotation, clawGrip)
        }

        private fun sumDuration(servo : MutableList<MutableList<Int>>) : Int {
            var sum = 0
            for (event in servo) {
                sum += event[0]
            }
            return sum;
        }

    }

    class SendData : ViewModel() {

        fun send(jsonBody: String) {
            // Create a new coroutine to move the execution off the UI thread
            viewModelScope.launch(Dispatchers.IO) {
                println(HttpConnection().send(jsonBody))
            }
        }
    }

    class HttpConnection {
        fun send (jsonBody: String) : String {

            //val mURL = URL("http://172.26.105.103:5000/motion")
            val mURL = URL("http://192.168.50.169:5000/motion")
            //val mURL = URL("http://10.255.145.74:5000/motion")

            try {
                //make http connection
                with(mURL.openConnection() as HttpURLConnection) {
                    // set request method
                    requestMethod = "POST"
                    //set content type of POST data
                    setRequestProperty("Content-Type", "application/json")
                    //set accepted return data type
                    setRequestProperty("Accept", "application/json")
                    //set request timeout to prohibit app crashing
                    connectTimeout = 5000

                    //write parameter data
                    val wr = OutputStreamWriter(outputStream)
                    wr.write(jsonBody)
                    wr.flush()

                    println("URL : $url")
                    println("Response Code : $responseCode")

                    if (responseCode == 200)
                        return "Success"
                    return "Fail bad response code"
                }
            }catch (e : SocketTimeoutException) {
                return "Could not connect to robot!"
            }

        }

    }

fun importMotionFromFile(context: Context): List<Data> {

    //retrieve all files in motions folder
    val assetsList = context.assets.list("motions")

    //create a list to hold all parsed motions
    val motionList : MutableList<Data> = mutableListOf()

    //if assets exist loop through and parse to list
    if (assetsList != null) {
        for (assetName in assetsList) {
            lateinit var jsonString: String
            try {
                jsonString = context.assets.open("motions/$assetName")
                    .bufferedReader()
                    .use { it.readText() }
                motionList.add(Json { ignoreUnknownKeys = true }.decodeFromString(jsonString))
            } catch (ioException: IOException) {
                println(ioException)
            }
        }
    }

    //returned parsed motions
    return motionList
}
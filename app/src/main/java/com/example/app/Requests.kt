package com.example.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
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

            if(this.bottom_rotation?.size == 0 && this.joint_1?.size == 0 && this.joint_2?.size == 0 && this.joint_3?.size == 0 && this. claw_rotation?.size == 0 && this.claw_grip?.size == 0) {
                return dat1
            }

            //get longest time in first animation and add it to next
            val maxTime : MutableList<Int> = ArrayList()

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

            var time = 0
            maxTime.forEach {
                if (it > time) {
                    time = it
                }
            }

            if (bottomRotation.size == 0) {
                bottomRotation.add(mutableListOf(time, -1))
            }
            if (joint1.size == 0) {
                joint1.add(mutableListOf(time, -1))
            }
            if (joint2.size == 0) {
                joint2.add(mutableListOf(time, -1))
            }
            if (joint3.size == 0) {
                joint3.add(mutableListOf(time, -1))
            }
            if (clawRotation.size == 0) {
                clawRotation.add(mutableListOf(time, -1))
            }
            if (clawGrip.size == 0) {
                clawGrip.add(mutableListOf(time, -1))
            }

            dat1.bottom_rotation?.forEach {
                it[0] += time
                bottomRotation.add(it)
            }
            dat1.joint_1?.forEach {
                it[0] += time
                joint1.add(it)
            }
            dat1.joint_2?.forEach {
                it[0] += time
                joint2.add(it)
            }
            dat1.joint_3?.forEach {
                it[0] += time
                joint3.add(it)
            }
            dat1.claw_rotation?.forEach {
                it[0] += time
                clawRotation.add(it)
            }
            dat1.claw_grip?.forEach {
                it[0] += time
                clawGrip.add(it)
            }

            return Data(bottomRotation, joint1, joint2, joint3, clawRotation, clawGrip)
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

            val mURL = URL("http://172.18.42.63:5000/motion")
            //val mURL = URL("http://192.168.50.172:5000/motion")
            //val mURL = URL("http://10.255.145.74:5000/motion")

            //make http connection
            with(mURL.openConnection() as HttpURLConnection) {
                // set request method
                requestMethod = "POST"
                //set content type of POST data
                setRequestProperty("Content-Type", "application/json")
                //set accepted return data type
                setRequestProperty("Accept", "application/json")

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

        }

    }
package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.app.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

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
        operator fun plus(dat1: Data) : Data {

            //get longest time in first animation and add it to next
            var maxTime : MutableList<Int> = ArrayList()

            var bottomRotation : MutableList<MutableList<Int>> = ArrayList()
            this.bottom_rotation?.forEach {
                maxTime.add((it[0]))
                bottomRotation.add(it)
            }

            var joint1 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_1?.forEach {
                maxTime.add((it[0]))
                joint1.add(it)
            }
            var joint2 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_2?.forEach {
                maxTime.add((it[0]))
                joint2.add(it)
            }
            var joint3 : MutableList<MutableList<Int>> = ArrayList()
            this.joint_3?.forEach {
                maxTime.add((it[0]))
                joint3.add(it)
            }
            var clawRotation : MutableList<MutableList<Int>> = ArrayList()
            this.claw_rotation?.forEach {
                maxTime.add((it[0]))
                clawRotation.add(it)
            }
            var clawGrip : MutableList<MutableList<Int>> = ArrayList()
            this.claw_grip?.forEach {
                maxTime.add((it[0]))
                clawGrip.add(it)
            }

            var time : Int = 0
            maxTime.forEach {
                if (it > time) {
                    time = it
                }
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

    class SendData(): ViewModel() {

        fun send(jsonBody: String) {
            // Create a new coroutine to move the execution off the UI thread
            viewModelScope.launch(Dispatchers.IO) {
                println(HttpConnection().send(jsonBody))
            }
        }
    }

    class HttpConnection () {
        fun send (jsonBody: String) : String {

            //val mURL = URL("http://172.28.176.226:5000/motion")
            val mURL = URL("http://192.168.50.172:5000/motion")
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
                val wr = OutputStreamWriter(outputStream);
                wr.write(jsonBody);
                wr.flush();

                println("URL : $url")
                println("Response Code : $responseCode")

                if (responseCode == 200)
                    return "Success"
                return "Fail bad response code"
            }

            return "Fail request failed"
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTest.setOnClickListener {

            val switch2 = binding.switch2.isChecked
            val switch1 = binding.switch3.isChecked

            val motion1 = Data(mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 90)),null, null, null, null, null)
            val motion2 = Data(null, null, null, mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 90)), null, mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 180)))

            var requestdata = Data(null, null, null, null, null, null)

            if (switch1) {
                println("Adding motion1")
                requestdata += motion1
            }
            if (switch2) {
                println("Adding motion2")
                requestdata += motion2
            }

            val jsonRequestdata = Json.encodeToString(requestdata)

            println("Current json= $jsonRequestdata")
            SendData().send(jsonRequestdata)

        }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonSend.setOnClickListener {

            val resetData = Data(mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)))

            val resetDataJson = Json.encodeToString(resetData)

            println(SendData().send(resetDataJson))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
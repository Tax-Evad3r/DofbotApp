package com.example.app

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.app.databinding.FragmentFirstBinding
import java.io.BufferedReader
import java.io.InputStreamReader
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        var selected = true

        binding.buttonSend.setOnClickListener {

            //allow requests from main thread
            val policy = ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)

            //servo data to be sent
            var reqParam = "{ \"1\": [[1000,0]]}"
            var temp = ""

            //switch between data
            if (selected) {
                temp = "{ \"1\": [[1000,90]]}"
                selected = false
            } else {
                temp = "{ \"1\": [[1000,0]]}"
                selected = true
            }


            reqParam = binding.jsonData.text.toString()
            binding.jsonData.setText(temp)


            //address of robotarm
            val mURL = URL("http://192.168.137.168:5000/motion")

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
                wr.write(reqParam);
                wr.flush();

                println("URL : $url")
                println("Response Code : $responseCode")


            }

            binding.textviewFirst.text = "done"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
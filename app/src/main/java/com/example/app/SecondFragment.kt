package com.example.app

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.app.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llTop.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)
        binding.dragView.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        //binding.buttonSecond.setOnClickListener {
           // findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        //}
    }

    val dragListener = View.OnDragListener { view, event ->
    when(event.action){
        DragEvent.ACTION_DRAG_STARTED -> {
            event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        }
        DragEvent.ACTION_DRAG_ENTERED -> {
            view.invalidate()
            true
        }
        DragEvent.ACTION_DRAG_LOCATION -> true
        DragEvent.ACTION_DRAG_EXITED -> {
            view.invalidate()
            true
        }
        DragEvent.ACTION_DROP -> {
            val item = event.clipData.getItemAt(0)
            val dragData = item.text
            //val toast = Toast.makeText(this@SecondFragment, dragData, Toast.LENGTH_SHORT)
            //toast.show()

            view.invalidate()

            val v = event.localState as View
            val owner = v.parent as ViewGroup
            owner.removeView(v)
            val destination = view as LinearLayout
            destination.addView(v)
            v.visibility = View.VISIBLE
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {
            view.invalidate()
            true
        }
        else -> false
    }
}
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.app

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        binding.llBottom.setOnDragListener(dragListener)
        binding.imageView.setOnLongClickListener {
            val clipText = "Does this work?"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, null, 0)

            true
        }
    }

    val dragListener = View.OnDragListener { view, event ->
    when(event.action){
        DragEvent.ACTION_DRAG_STARTED -> {
            if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                (view as? ImageView)?.setColorFilter(Color.BLUE)
                view.invalidate()
                true
                }
            else {
                false
            }

        }
        DragEvent.ACTION_DRAG_ENTERED -> {
            binding.llBottom.alpha = 0.3f
            view.invalidate()
            true
        }
        DragEvent.ACTION_DRAG_LOCATION ->
            true
        DragEvent.ACTION_DRAG_EXITED -> {
            binding.llBottom.alpha = 1.0f
            view.visibility = View.VISIBLE
            view.invalidate()
            true
        }
        DragEvent.ACTION_DROP -> {
            binding.llBottom.alpha = 1.0f
            val item : ClipData.Item = event.clipData.getItemAt(0)
            val dragData = item.text


            view.visibility = View.VISIBLE;
            view.invalidate()

            val parent = view.parent as LinearLayout

            parent.removeView(view)

            val dropArea = view as LinearLayout
            dropArea.addView(view)
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {

            view.visibility = View.VISIBLE;
            view.invalidate()
            when(event.result){
                true ->
                    Toast.makeText(activity, "The drop was handled.", Toast.LENGTH_LONG)
                else ->
                    Toast.makeText(activity, "The drop didn't work.", Toast.LENGTH_LONG)
            }.show()
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
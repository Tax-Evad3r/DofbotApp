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
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.core.view.get
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

        binding.llRight.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)

        // for loop to create all buttons for movements
        for (i in 0 until binding.llRight.childCount) {
            var x = binding.llRight.getChildAt(i);
            x.setOnLongClickListener {
                val clipText = "Hello"
                val item = ClipData.Item(clipText)
                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                val data = ClipData(clipText, mimeTypes, item)

                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(data, dragShadowBuilder, it, 0)

                it.visibility = View.INVISIBLE
                true
            }
        }
    }

    val dragListener = View.OnDragListener { view, event ->
    when(event.action){
        DragEvent.ACTION_DRAG_STARTED -> {
            event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        }
        DragEvent.ACTION_DRAG_ENTERED -> {
            val v = event.localState as View
            v.setVisibility(View.VISIBLE);
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
 /*           if (destination == binding.llBottom) {
                val childcount = destination.childCount
                val children = ArrayList<View>(childcount)

                for (i in 1 until childcount) {
                    if (destination.getChildAt(i) != null)
                        children[i] = destination.getChildAt(i);
                }

                destination.removeAllViews()

                destination.addView(v)
                v.visibility = View.VISIBLE

                for (x in 1 until children.count()) {
                    children[x] = event.localState as View
                    destination.addView(children[x])
                    children[x].visibility = View.VISIBLE
                }
            }
            else {*/
                destination.addView(v)
                v.visibility = View.VISIBLE
            // }
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {
            val v = event.localState as View
            v.setVisibility(View.VISIBLE);
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
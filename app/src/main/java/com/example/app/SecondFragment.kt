package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
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

    private lateinit var motion_animation:AnimatorSet
    private lateinit var sound_animation:AnimatorSet
    private var motionShown = true

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


        val scale:Float = this.requireContext().resources.displayMetrics.density
        binding.scRightMotions.cameraDistance = 8000 * scale
        binding.scRightSounds.cameraDistance = 8000 * scale

        motion_animation = AnimatorInflater.loadAnimator(activity, R.animator.motion_animator) as AnimatorSet
        sound_animation = AnimatorInflater.loadAnimator(activity, R.animator.sound_animator) as AnimatorSet

        binding.buttonQuickRun.setOnClickListener {
            if (motionShown)
            {
                motion_animation.setTarget(binding.scRightMotions)
                sound_animation.setTarget(binding.scRightSounds)
                motion_animation.start()
                sound_animation.start()
                motionShown = false
            }
            else
            {
                motion_animation.setTarget(binding.scRightSounds)
                sound_animation.setTarget(binding.scRightMotions)
                motion_animation.start()
                sound_animation.start()
                motionShown = true
            }

        }

        binding.llRightMotions.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)
        binding.motion.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }

        binding.buttonRun.setOnClickListener {
            println("Current queue")
            println(binding.llBottom.childCount)
            for (i in 0 until binding.llBottom.childCount) {
                println(binding.llBottom.getChildAt(i).contentDescription)
            }

            //FIX ME: Proof of concept until drag and drop is fully implemented,
            //        data should also probably not be stored as static variable

            //initialize two predefined motions
            val motion1 = Data(mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 90)),mutableListOf(), mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 90)), mutableListOf(), mutableListOf(), mutableListOf())
            val motion2 = Data(mutableListOf(mutableListOf(1000, 90), mutableListOf(2000, 180)), mutableListOf(), mutableListOf(), mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 90)), mutableListOf(), mutableListOf(mutableListOf(1000, 0), mutableListOf(2000, 180)))

            //initialize empty initial response
            var requestdata = Data(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

            //define motionCount as amount of motions on timeline
            val motionCount = binding.llBottom.childCount

            //if 1 or more motions on time line add 1:st motion to request
            if (motionCount > 0) {
                println("Adding motion1")
                requestdata += motion1
            }
            //if 2 or more motions on time line add 2:nd motion to request
            if (motionCount > 1) {
                println("Adding motion2")
                requestdata += motion2
            }

            //convert request to json
            val jsonRequestdata = requestdata.toJson()
            println("Sending json= $jsonRequestdata")

            //send request
            SendData().send(jsonRequestdata)

        }

        binding.motion1.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        binding.motion2.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        binding.motion3.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        binding.motion4.setOnLongClickListener {
            val clipText = "Hello"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        binding.motion5.setOnLongClickListener {
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

    private val dragListener = View.OnDragListener { view, event ->
    when(event.action){
        DragEvent.ACTION_DRAG_STARTED -> {
            event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        }
        DragEvent.ACTION_DRAG_ENTERED -> {
            val v = event.localState as View
            v.visibility = View.VISIBLE;
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
            val v = event.localState as View
            v.visibility = View.VISIBLE;
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
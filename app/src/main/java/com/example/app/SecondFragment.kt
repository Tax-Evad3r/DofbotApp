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
import androidx.core.animation.doOnEnd
import com.example.app.databinding.FragmentSecondBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private lateinit var flip_left_in:AnimatorSet
    private lateinit var flip_left_out:AnimatorSet
    private lateinit var flip_right_in:AnimatorSet
    private lateinit var flip_right_out:AnimatorSet

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scale:Float = this.requireContext().resources.displayMetrics.density
        binding.scRightMotions.cameraDistance = 8000 * scale
        binding.scRightSounds.cameraDistance = 8000 * scale

        flip_left_in = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_in) as AnimatorSet
        flip_left_out = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_out) as AnimatorSet
        flip_right_in = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_in) as AnimatorSet
        flip_right_out = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_out) as AnimatorSet

        binding.tabsRight.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position != 0)
                {
                    binding.scRightSounds.visibility = View.VISIBLE
                    flip_left_in.setTarget(binding.scRightSounds)
                    flip_left_out.setTarget(binding.scRightMotions)
                    flip_left_in.start()
                    flip_left_out.start()
                    flip_left_in.doOnEnd { binding.scRightMotions.visibility = View.GONE }
                }
                else
                {
                    binding.scRightMotions.visibility = View.VISIBLE
                    flip_right_in.setTarget(binding.scRightMotions)
                    flip_right_out.setTarget(binding.scRightSounds)
                    flip_right_in.start()
                    flip_right_out.start()
                    flip_right_in.doOnEnd { binding.scRightSounds.visibility = View.GONE }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


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
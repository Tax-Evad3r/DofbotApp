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
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.contains
import com.example.app.databinding.FragmentSecondBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private lateinit var flipLeftIn:AnimatorSet
    private lateinit var flipLeftOut:AnimatorSet
    private lateinit var flipRightIn:AnimatorSet
    private lateinit var flipRightOut:AnimatorSet

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

        flipLeftIn = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_in) as AnimatorSet
        flipLeftOut = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_out) as AnimatorSet
        flipRightIn = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_in) as AnimatorSet
        flipRightOut = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_out) as AnimatorSet

        binding.tabsRight.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position != 0)
                {
                    binding.scRightSounds.visibility = View.VISIBLE
                    flipLeftIn.setTarget(binding.scRightSounds)
                    flipLeftOut.setTarget(binding.scRightMotions)
                    flipLeftIn.start()
                    flipLeftOut.start()
                    flipLeftIn.doOnEnd {
                        binding.scRightMotions.visibility = View.GONE
                        binding.scRightSounds.visibility = View.VISIBLE
                    }
                }
                else
                {
                    binding.scRightMotions.visibility = View.VISIBLE
                    flipRightIn.setTarget(binding.scRightMotions)
                    flipRightOut.setTarget(binding.scRightSounds)
                    flipRightIn.start()
                    flipRightOut.start()
                    flipRightIn.doOnEnd {
                        binding.scRightSounds.visibility = View.GONE
                        binding.scRightMotions.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        binding.llRightMotions.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)

        //FIX ME: get amount of motions from imports
        for (i in 0..10) {
            val destination = binding.llRightMotions
            val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
            motion1.contentDescription = "motion$i"
            if(i % 2 == 0) {
                Glide.with(this.requireContext()).load(R.drawable.gif_temp).into(motion1)
            } else {
                Glide.with(this.requireContext()).load(R.drawable.gif_temp2).into(motion1)
            }
            destination.addView(motion1)
            createDragAndDropListener(motion1)
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
            val v = event.localState as View
            val owner = v.parent as ViewGroup
            val destination = view as LinearLayout
            if (destination.contentDescription == "motion_timeline") {
                if (!destination.contains(v)) {
                    val motion1 = LayoutInflater.from(this.context)
                        .inflate(R.layout.motion_template, destination, false) as ImageView
                    motion1.contentDescription = v.contentDescription
                    if (v.contentDescription.substring(
                            v.contentDescription.length - 1,
                            v.contentDescription.length
                        ).toInt() % 2 == 0
                    ) {
                        Glide.with(this.requireContext()).load(R.drawable.gif_temp).into(motion1)
                    } else {
                        Glide.with(this.requireContext()).load(R.drawable.gif_temp2).into(motion1)
                    }
                    destination.addView(motion1)
                    createDragAndDropListener(motion1)
                }
            } else {
                owner.removeView(v)
            }
            println("dropped ${v.contentDescription}")
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

fun createDragAndDropListener(view: View) {
    view.setOnLongClickListener {
        val dragShadowBuilder = View.DragShadowBuilder(it)
        it.startDragAndDrop(ClipData.newPlainText("", ""), dragShadowBuilder, it, 0)
        it.visibility = View.INVISIBLE
        true
    }
}
package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.text.isDigitsOnly
import androidx.core.view.iterator
import com.example.app.databinding.FragmentSecondBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import androidx.core.animation.doOnEnd
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

        //import motions from asset files (located in "main/assets/motion")
        val availableMotions = importMotionFromFile(this.requireContext())

        //debug print of all stored motions
        println("Motion import done!")
        for (motion in availableMotions) {
            println(motion.toJson())
        }

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

        binding.buttonQuickRun.setOnClickListener {

            println("pressed quick run!")

            //repurpose quick run as reset button during development
            //define reset data
            val resetData = Data(mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)))

            //send reset data
            SendData().send(resetData.toJson())

        }

        binding.buttonRun.setOnClickListener {

            //debug print for all objects on timeline
            println("Current queue")
            println(binding.llBottom.childCount)
            for (i in 0 until binding.llBottom.childCount) {
                println(binding.llBottom.getChildAt(i).contentDescription)
            }

            //FIX ME: This may change before drag and drop is fully implemented

            //initialize empty initial response
            var requestdata = Data(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

            //list of motion id to add to request
            val motionNumList = mutableListOf<Int>()

            //loop through all motions in timeline
            for (tileNo in 0 until binding.llBottom.childCount) {
                //get content description defined in xml
                val desc = binding.llBottom.getChildAt(tileNo).contentDescription
                //extract last char (this is currently motion number)
                var motionNumParse = desc.substring(desc.length-1)
                //ignore temp motion (might not be needed in the end)
                if (motionNumParse.toIntOrNull() != null) {
                    //parse substring to usable in for later
                    var motionNum = motionNumParse.toInt()
                    if (motionNum < availableMotions.size) {
                        //if motion exist add to list
                        motionNumList.add(motionNum)
                    }
                }
            }
            //add motions to request
            for (i in motionNumList) {
                requestdata += availableMotions[i]
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
                val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
                motion1.contentDescription = v.contentDescription
                if(v.contentDescription.substring(v.contentDescription.length-1, v.contentDescription.length).toInt() % 2 == 0) {
                    Glide.with(this.requireContext()).load(R.drawable.gif_temp).into(motion1)
                } else {
                    Glide.with(this.requireContext()).load(R.drawable.gif_temp2).into(motion1)
                }
                destination.addView(motion1)
                createDragAndDropListener(motion1)
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
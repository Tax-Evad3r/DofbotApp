package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.ClipData
import android.content.ClipDescription
import android.media.MediaPlayer
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.app.databinding.FragmentSecondBinding
import androidx.core.animation.doOnEnd
import androidx.core.view.iterator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.bumptech.glide.Glide


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    var mfile  = "george_ezra_shotgun_jesse_bloch_bootleg"

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
        binding.llRightSounds.setOnDragListener(dragListener)
        binding.llBottomSounds.setOnDragListener(dragListener)

        for (i in availableMotions.indices) {
            val destination = binding.llRightMotions
            val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
            motion1.contentDescription = "motion$i"
            val res = this.resources.getIdentifier("motion$i", "drawable", "com.example.app")
            Glide.with(this.requireContext()).load(res).into(motion1)
            destination.addView(motion1)
            createDragAndDropListener(motion1)
        }

        // Do most of the same as above but for sound files
        for (v : View in binding.llRightSounds) {
            createDragAndDropListener(v)
        }

        binding.buttonQuickRun.setOnClickListener {

            println("pressed quick run!")

            //repurpose quick run as reset button during development
            //define reset data
            val resetData = Data(mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)))

            //send reset data
            SendData().send(resetData.toJson())

        }
        binding.playButton.setOnClickListener{
            playSound(this.context, mfile)
        }
        binding.pauseButton.setOnClickListener{
            pauseSound()
        }
        binding.stopButton.setOnClickListener{
            stopSound()
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

            //loop through all motions in timeline (skip first since it is not a motion)
            for (motion in binding.llBottom) {
                val motionId = getMotionId(motion)
                if( motionId != -1) {
                    motionNumList.add(motionId)
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
            if (owner.contentDescription == "motion_lib" && destination.contentDescription == "motion_timeline") {
                val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
                motion1.contentDescription = v.contentDescription
                val res = this.resources.getIdentifier("motion${getMotionId(v)}", "drawable", "com.example.app")
                Glide.with(this.requireContext()).load(res).into(motion1)
                destination.addView(motion1)
                createDragAndDropListener(motion1)
            } else if (owner.contentDescription == "sounds_lib" && destination.contentDescription == "sounds_timeline")
            {
                val sounds1 = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as ImageView
                sounds1.contentDescription = v.contentDescription
                destination.addView(sounds1)
                createDragAndDropListener(sounds1)
            } else if (owner.contentDescription == "motion_timeline" && destination.contentDescription == "motion_lib" || owner.contentDescription == "sounds_timeline" && destination.contentDescription == "sounds_lib") {
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
        stopSound()
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

fun getMotionId(view: View) : Int {
    var motionNumParse = view.contentDescription.filter { it.isDigit() }
    //ignore temp motion (might not be needed in the end)
    if (motionNumParse.toString().toIntOrNull() != null) {
        //parse substring to usable in for later
        return motionNumParse.toString().toInt()
    }
    return -1
}

package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.app.databinding.FragmentSecondBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.lang.Long.max

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private lateinit var flipLeftIn:AnimatorSet
    private lateinit var flipLeftOut:AnimatorSet
    private lateinit var flipRightIn:AnimatorSet
    private lateinit var flipRightOut:AnimatorSet

    private var tabSelected = 0 // 0=motion, 1 = sound
    private var timelineAlpha = 0.3f

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
        val importedSounds = importSounds(this.context)

        //motion duration array
        var motionDuration:MutableList<Int> = mutableListOf()
        for (motion in availableMotions) {
            motionDuration.add(animationDuration(motion))
        }
        //debug print of all imported sounds
        println("Sound import done!")
        for (sound in importedSounds) {
            println("Found sound: $sound")
        }

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


        binding.llBottomSounds.alpha = timelineAlpha
        binding.soundsText.alpha = timelineAlpha
        binding.motions.setOnClickListener(){
            if (tabSelected == 1) {
                setTimelineAlpha(binding,0, timelineAlpha)
                binding.scRightMotions.visibility = View.VISIBLE
                flipRightIn.setTarget(binding.scRightMotions)
                flipRightOut.setTarget(binding.scRightSounds)
                flipRightIn.start()
                flipRightOut.start()
                flipRightIn.doOnEnd {
                    binding.scRightSounds.visibility = View.GONE
                    binding.scRightMotions.visibility = View.VISIBLE
                    tabSelected = 0
                }
            }
        }

        binding.sounds.setOnClickListener(){
            if (tabSelected == 0) {
                setTimelineAlpha(binding,1, timelineAlpha)
                binding.scRightSounds.visibility = View.VISIBLE
                flipLeftIn.setTarget(binding.scRightSounds)
                flipLeftOut.setTarget(binding.scRightMotions)
                flipLeftIn.start()
                flipLeftOut.start()
                flipLeftIn.doOnEnd {
                    binding.scRightMotions.visibility = View.GONE
                    binding.scRightSounds.visibility = View.VISIBLE
                    tabSelected = 1
                    setTimelineAlpha(binding,tabSelected, timelineAlpha)
                }
            }
        }


        binding.llRightMotions.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)
        binding.llRightSounds.setOnDragListener(dragListener)
        binding.llBottomSounds.setOnDragListener(dragListener)
        binding.lltrash.setOnDragListener(dragListener)
        binding.trash.visibility = View.INVISIBLE

        //create new view for each motion depending on amount of imported motions
        for (i in availableMotions.indices) {
            val destination = binding.llRightMotions
            val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motionlib_template, destination, false) as ShapeableImageView
            motion1.contentDescription = "motion$i"
            val res = this.resources.getIdentifier("motion$i", "drawable", "com.example.app")
            Glide.with(this.requireContext()).load(Uri.parse("file:///android_asset/gifs/motion$i.gif")).into(motion1)
            destination.addView(motion1)
            createDragAndDropListener(motion1)
        }

        //create new view for each sound depending on amount of imported sounds
        for (i in importedSounds.indices) {
            val destination = binding.llRightSounds
            val sound = LayoutInflater.from(this.context).inflate(R.layout.soundlib_template, destination, false) as MaterialTextView
            sound.text = importedSounds[i].substring(0, importedSounds[i].indexOf("."))
            sound.contentDescription = "sound$i"
            destination.addView(sound)
            createDragAndDropListener(sound)
            createClickListener(this.requireContext(), sound, importedSounds[getId(sound)])
        }

        //Create alert dialog box that is displayed when connection error occurs
        val connectionError: AlertDialog.Builder = AlertDialog.Builder(context)

        binding.buttonQuickRun.setOnClickListener {

            println("pressed quick run!")

            //repurpose quick run as reset button during development
            //define reset data
            val resetData = Data(mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)), mutableListOf(mutableListOf(2000, 90)))

            //send reset data
            SendData().send(resetData.toJson(), connectionError)

        }

        val eraseMotion =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        //yes button pressed

                        //create variables for both timelines
                        val motionTimeline = binding.llBottom

                        //save add symbol in both timelines
                        val placeHolderMotion = motionTimeline.getChildAt(motionTimeline.childCount-1)
                        motionTimeline.removeView(placeHolderMotion)

                        binding.llBottom.removeAllViews()

                        //restore add symbol in each timeline
                        motionTimeline.addView(placeHolderMotion)
                    }
                }
            }

        val eraseSound =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        //yes button pressed

                        //create variables for both timelines
                        val soundTimeline = binding.llBottomSounds

                        //save add symbol in both timelines
                        val placeHolderSounds = soundTimeline.getChildAt(soundTimeline.childCount-1)
                        soundTimeline.removeView(placeHolderSounds)

                        binding.llBottomSounds.removeAllViews()

                        //restore add symbol in each timeline
                        soundTimeline.addView(placeHolderSounds)
                    }
                }
            }

        binding.buttonEraseMotion.setOnClickListener {

            //pop confirm dialog when user wants to reset
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(HtmlCompat.fromHtml("Are you sure you want to erase all <b>motions</b>?", HtmlCompat.FROM_HTML_MODE_LEGACY)).setPositiveButton("Yes", eraseMotion)
                .setNegativeButton("No", eraseMotion).show()
        }

        binding.buttonEraseSound.setOnClickListener {

            //pop confirm dialog when user wants to reset
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(HtmlCompat.fromHtml("Are you sure you want to erase all <b>sounds</b>?", HtmlCompat.FROM_HTML_MODE_LEGACY)).setPositiveButton("Yes", eraseSound)
                .setNegativeButton("No", eraseSound).show()
        }

        binding.buttonRun.setOnClickListener {

            //debug print for all objects on timeline
            println("Current queue")
            println(binding.llBottom.childCount)
            for (i in 0 until binding.llBottom.childCount) {
                println(binding.llBottom.getChildAt(i).contentDescription)
            }

            setTimelineAlpha(binding, 2, timelineAlpha)

            //initialize empty initial response
            var requestdata = Data(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

            //list of motion id to add to request
            val motionNumList = mutableListOf<Int>()

            //loop through all motions in timeline (skip first since it is not a motion)
            for (motion in binding.llBottom) {
                val motionId = getId(motion)
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
            SendData().send(jsonRequestdata, connectionError)
            
            //create sound list
            val soundsList = mutableListOf<String>()

            //loop through all sounds in timeline (skip first since it is not a sound)
            for (sound in binding.llBottomSounds) {
                val soundId = getId(sound)
                if( soundId != -1) {
                    soundsList.add(importedSounds[soundId])
                }
            }
            val soundsDuration = calculateSoundsLength(this.requireContext(), soundsList)
            playSounds(this.requireContext(), soundsList)
            val motionAnimationTime = motionRunAnimations(binding,this.requireActivity(),motionDuration)
            Handler(Looper.getMainLooper()).postDelayed({
                setTimelineAlpha(binding, tabSelected, timelineAlpha)
            }, max(soundsDuration,motionAnimationTime))

        }
    }

    private val dragListener = View.OnDragListener { view, event ->
    when(event.action){
        DragEvent.ACTION_DRAG_STARTED -> {
            event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        }
        DragEvent.ACTION_DRAG_ENTERED -> {
            val v = event.localState as View
            v.visibility = View.VISIBLE
            val owner = v.parent as ViewGroup
            val destination = view as LinearLayout
            if (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline")
                binding.trash.visibility = View.VISIBLE
            if (destination.contentDescription == "motion_timeline" && owner.contentDescription == "motion_lib")
            {
                binding.llBottom.alpha = 0.3f
            }
            else if (destination.contentDescription == "sounds_timeline" && owner.contentDescription == "sounds_lib")
            {
                binding.llBottomSounds.alpha = 0.3f
            }
            else if (destination.contentDescription == "trash" && (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline"))
            {
                binding.lltrash.alpha = 0.3f
            }
            view.invalidate()
            true
        }
        DragEvent.ACTION_DRAG_LOCATION -> true
        DragEvent.ACTION_DRAG_EXITED -> {
            setTimelineAlpha(binding,tabSelected, timelineAlpha)

            binding.lltrash.alpha = 1.0f
            view.invalidate()
            true
        }
        DragEvent.ACTION_DROP -> {
            setTimelineAlpha(binding,tabSelected, timelineAlpha)
            binding.lltrash.alpha = 1.0f
            binding.trash.visibility = View.INVISIBLE

            val v = event.localState as View
            val owner = v.parent as ViewGroup
            val destination = view as LinearLayout

            if (destination.contentDescription == "trash" && (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline")) {
                owner.removeView(v)
            } else if (owner.contentDescription == "motion_lib" && destination.contentDescription == "motion_timeline") {
                val placeHolder = destination[destination.childCount-1]
                destination.removeView(placeHolder)
                val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
                motion1.contentDescription = v.contentDescription
                val res = this.resources.getIdentifier("motion${getId(v)}", "drawable", "com.example.app")
                Glide.with(this.requireContext()).load(Uri.parse("file:///android_asset/gifs/motion${getId(v)}.gif")).into(motion1)
                destination.addView(motion1)
                createDragAndDropListener(motion1)
                destination.addView(placeHolder)
            } else if (owner.contentDescription == "sounds_lib" && destination.contentDescription == "sounds_timeline")
            {
                val placeHolder = destination[destination.childCount-1]
                destination.removeView(placeHolder)
                val sounds1 = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as TextView
                //val back = v.background as ColorDrawable
                //sounds1.setBackgroundColor(back.color)
                sounds1.contentDescription = v.contentDescription
                val v1 = event.localState as TextView
                sounds1.text = v1.text
                destination.addView(sounds1)
                createDragAndDropListener(sounds1)
                destination.addView(placeHolder)
            }

            println("dropped ${v.contentDescription}")
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {
            val v = event.localState as View
            v.visibility = View.VISIBLE
            binding.trash.visibility = View.INVISIBLE
            view.invalidate()
            true
        }
        else -> false
    }

}

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlayerOnStop()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        stopPlayerOnStop()
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

fun createClickListener(context : Context, view: View, name : String) {
    view.setOnClickListener {
        playSound(context, name, 2000)
    }
}

//helper function for extracting id from string (eg. "motion1" returns 1)
fun getId(view: View) : Int {
    var motionNumParse = view.contentDescription.filter { it.isDigit() }
    //ignore temp motion (might not be needed in the end)
    if (motionNumParse.toString().toIntOrNull() != null) {
        //parse substring to usable in for later
        return motionNumParse.toString().toInt()
    }
    return -1
}

fun setTimelineAlpha(binding : FragmentSecondBinding, tabSelected : Int, timelineAlpha : Float)
{
    when (tabSelected) {
        0 // motion tab selected
        -> {
            binding.llBottom.alpha = 1.0f
            binding.motionsText.alpha = 1.0f
            binding.llBottomSounds.alpha = timelineAlpha
            binding.soundsText.alpha = timelineAlpha
        }
        1 // sound tab selected
        -> {
            binding.llBottom.alpha = timelineAlpha
            binding.motionsText.alpha = timelineAlpha
            binding.llBottomSounds.alpha = 1.0f
            binding.soundsText.alpha = 1.0f
        }
        2 // both is visible
        -> {
            binding.llBottom.alpha = 1.0f
            binding.motionsText.alpha = 1.0f
            binding.llBottomSounds.alpha = 1.0f
            binding.soundsText.alpha = 1.0f
        }
    }
}


//plays a animation on each child of llBottom except for "+"
fun motionRunAnimations(binding : FragmentSecondBinding, activity : FragmentActivity, motionDuration : MutableList<Int> ) : Long {
    val startAnimation = R.animator.run_animation_start         //reference to animator
    val runAnimation = R.animator.run_animation_run           //reference to animator
    val endAnimation = R.animator.run_animation_end             //reference to animator

    var delay:Long = 0                                          //time in ms
    binding.hsvBottom.smoothScrollTo(0,0)
    for (i in 0 until binding.llBottom.childCount) {
        if(i == binding.llBottom.childCount-1){//element the empty + square
            continue
        }
        val motionStart:AnimatorSet = AnimatorInflater.loadAnimator(activity, startAnimation) as AnimatorSet
        val motionRun:AnimatorSet = AnimatorInflater.loadAnimator(activity, runAnimation) as AnimatorSet
        val motionEnd:AnimatorSet = AnimatorInflater.loadAnimator(activity, endAnimation) as AnimatorSet
        val x = binding.llBottom.getChildAt(i)
        val duration:Long = motionDuration[getId(x)].toLong()       //time in ms
        var startAnimationDuration:Long                             //time in ms
        var endAnimationDelay:Long = 0                              //time in ms

        if(duration > 1000){
            startAnimationDuration = 1000
            endAnimationDelay = duration - 1000
        }
        else{
            startAnimationDuration = duration
        }

        motionStart.duration = startAnimationDuration
        motionStart.startDelay = delay
        motionStart.setTarget(x)
        motionStart.start()

        motionRun.duration = endAnimationDelay
        motionRun.startDelay = delay + startAnimationDuration
        motionRun.setTarget(x)
        motionRun.start()

        motionEnd.startDelay = delay + endAnimationDelay
        motionEnd.setTarget(x)
        motionEnd.doOnEnd {
            binding.hsvBottom.smoothScrollTo(x.right - x.width - 100, 0)
        }
        motionEnd.start()

        delay += duration
    }
    return delay
}

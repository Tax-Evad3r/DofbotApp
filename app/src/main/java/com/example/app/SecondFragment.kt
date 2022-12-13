package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.app.databinding.FragmentSecondBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.IOException
import java.lang.Long.min

var resumeSession = false

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
    var animationDone = mutableListOf<Boolean>(true,true)

    private lateinit var availableMotions : List<Data>
    private lateinit var importedSounds : MutableList<String>

    private lateinit var connectionError: AlertDialog.Builder

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
        availableMotions = importMotionFromFile(this.requireContext())
        importedSounds = importSounds(this.context)

        //motion duration array
        var motionDuration:MutableList<Int> = mutableListOf()
        for (motion in availableMotions) {
            motionDuration.add(animationDuration(motion))
        }

        val scale:Float = this.requireContext().resources.displayMetrics.density
        binding.scRightMotions.cameraDistance = 8000 * scale
        binding.scRightSounds.cameraDistance = 8000 * scale

        flipLeftIn = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_in) as AnimatorSet
        flipLeftOut = AnimatorInflater.loadAnimator(activity, R.animator.flip_left_out) as AnimatorSet
        flipRightIn = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_in) as AnimatorSet
        flipRightOut = AnimatorInflater.loadAnimator(activity, R.animator.flip_right_out) as AnimatorSet


        binding.llBottomSounds.alpha = 0.3f
        binding.soundsText.alpha = 0.3f
        binding.motions.setOnClickListener(){
            if (tabSelected == 1) {
                setTimelineAlpha(binding,0)
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
                setTimelineAlpha(binding,1)
                binding.scRightSounds.visibility = View.VISIBLE
                flipLeftIn.setTarget(binding.scRightSounds)
                flipLeftOut.setTarget(binding.scRightMotions)
                flipLeftIn.start()
                flipLeftOut.start()
                flipLeftIn.doOnEnd {
                    binding.scRightMotions.visibility = View.GONE
                    binding.scRightSounds.visibility = View.VISIBLE
                    tabSelected = 1
                    setTimelineAlpha(binding,tabSelected)
                }
            }
        }


        binding.llRightMotions.setOnDragListener(dragListener)
        binding.llBottom.setOnDragListener(dragListener)
        binding.llRightSounds.setOnDragListener(dragListener)
        binding.llBottomSounds.setOnDragListener(dragListener)
        binding.llTrash.setOnDragListener(dragListener)
        binding.trash.visibility = View.INVISIBLE
        binding.llTrash.visibility = View.INVISIBLE
        binding.llPlay.setOnDragListener(dragListener)
        binding.llPlay.visibility = View.INVISIBLE

        var stationaryDestination = binding.llRightMotions
        val stationaryMotionView = LayoutInflater.from(this.context).inflate(R.layout.motionlib_template, stationaryDestination, false) as ShapeableImageView
        stationaryMotionView.contentDescription = "stationary"
        Glide.with(this.requireContext()).load(Uri.parse("file:///android_asset/gifs/hourglass.gif")).into(stationaryMotionView)
        stationaryDestination.addView(stationaryMotionView)
        createDragAndDropListener(stationaryMotionView)

        //create new view for each motion depending on amount of imported motions
        for (i in availableMotions.indices) {
            val destination = binding.llRightMotions
            val newMotionView = LayoutInflater.from(this.context).inflate(R.layout.motionlib_template, destination, false) as ShapeableImageView
            newMotionView.contentDescription = "motion$i"
            Glide.with(this.requireContext()).load(Uri.parse("file:///android_asset/gifs/motion$i.gif")).into(newMotionView)
            destination.addView(newMotionView)
            createDragAndDropListener(newMotionView)
        }

        stationaryDestination = binding.llRightSounds
        val stationarySoundView = LayoutInflater.from(this.context).inflate(R.layout.soundlib_template, stationaryDestination, false) as MaterialTextView
        stationarySoundView.text = "No sound"
        stationarySoundView.contentDescription = "stationary"
        stationaryDestination.addView(stationarySoundView)
        createDragAndDropListener(stationarySoundView)

        //create new view for each sound depending on amount of imported sounds
        for (i in importedSounds.indices) {
            val destination = binding.llRightSounds
            val newSoundView = LayoutInflater.from(this.context).inflate(R.layout.soundlib_template, destination, false) as MaterialTextView
            newSoundView.text = importedSounds[i].substring(0, importedSounds[i].indexOf("."))
            newSoundView.contentDescription = "sound$i"
            destination.addView(newSoundView)
            createDragAndDropListener(newSoundView)
        }

        //Create alert dialog box that is displayed when connection error occurs
        connectionError = AlertDialog.Builder(context)

        if (resumeSession) {
            loadFromFile(this.requireContext(), binding, importedSounds)
        }

        binding.buttonSave.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonQuickRun.setOnClickListener {

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

                        saveToFile(this.requireContext(), binding)
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

                        saveToFile(this.requireContext(), binding)
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
            if (animationDone[0] && animationDone[1]) {
                animationDone[0] = false
                animationDone[1] = false
                binding.buttonRun.alpha = 0.3f

                //debug print for all objects on timeline
                println("Current queue")
                println(binding.llBottom.childCount)
                for (i in 0 until binding.llBottom.childCount) {
                    println(binding.llBottom.getChildAt(i).contentDescription)
                }

                setTimelineAlpha(binding, 2)

                //initialize empty initial response
                var requestdata = Data(
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf(),
                    mutableListOf()
                )

                //list of motion id to add to request
                val motionNumList = mutableListOf<Int>()

                //loop through all motions in timeline and add motions to request
                for (motion in binding.llBottom) {
                    if(motion.contentDescription.contains("stationary") ){
                        requestdata += createStationaryMotion(getIntFromContentDescription(motion))
                        continue
                    }
                    val motionId = getId(motion)
                    if( motionId != -1) {           //(skip elements without "motionId")
                        //motionNumList.add(motionId)
                        requestdata += availableMotions[motionId]
                    }
                }
                //convert request to json
                val jsonRequestdata = requestdata.toJson()
                //TODO: Remove when debug is no longer needed!
                println("Sending json= $jsonRequestdata")

                //send request
                SendData().send(jsonRequestdata, connectionError)

                //create sound list
                val soundsList = mutableListOf<String>()

                //loop through all sounds in timeline (skip first since it is not a sound)
                for (sound in binding.llBottomSounds) {
                    if(sound.contentDescription.contains("stationary") ){
                        soundsList.add(sound.contentDescription.toString())
                        continue
                    }
                    val soundId = getId(sound)
                    if( soundId != -1) {
                        soundsList.add(importedSounds[soundId])
                    }
                }
                val soundsDuration : MutableList<Int> = calculateSoundsLength(this.requireContext(), importedSounds)

                playAnimatedSounds(this, binding, soundsList, soundsDuration, 0, tabSelected, animationDone)

                motionRunAnimations(binding, this.requireActivity(), motionDuration, animationDone, tabSelected)
                animationsDone(binding, animationDone, tabSelected)
            }
        }

    }

    private val dragListener = View.OnDragListener { view, event ->
        if (event.localState != null){
            when(event.action){
                DragEvent.ACTION_DRAG_STARTED -> {
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {

                        val v = event.localState as View
                        v.visibility = View.VISIBLE
                        val owner = v.parent as ViewGroup
                        val destination = view as LinearLayout
                        if (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline") {
                            binding.trash.visibility = View.VISIBLE
                            binding.llTrash.visibility = View.VISIBLE
                        }
                        if (owner.contentDescription == "motion_lib" || owner.contentDescription == "sounds_lib") {
                            binding.llPlay.visibility = View.VISIBLE
                        }
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
                            binding.llTrash.alpha = 0.3f
                        }
                        else if (destination.contentDescription == "play" && (owner.contentDescription == "motion_lib" || owner.contentDescription == "sounds_lib"))
                        {
                            binding.llPlay.alpha = 0.3f
                        }
                        view.invalidate()

                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    setTimelineAlpha(binding,tabSelected)

                    binding.llTrash.alpha = 1.0f
                    binding.llPlay.alpha = 1.0f
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    setTimelineAlpha(binding,tabSelected)
                    binding.llTrash.alpha = 1.0f
                    binding.trash.visibility = View.INVISIBLE
                    binding.llTrash.visibility = View.INVISIBLE
                    binding.llPlay.alpha = 1.0f
                    binding.llPlay.visibility = View.INVISIBLE

                    val v = event.localState as View
                    val owner = v.parent as ViewGroup
                    val destination = view as LinearLayout

                    if (destination.contentDescription == "trash" && (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline")) {
                        owner.removeView(v)
                    } else if (destination.contentDescription == "play" && owner.contentDescription == "motion_lib") {
                        var requestdata = availableMotions[getId(v)]
                        val jsonRequestdata = requestdata.toJson()
                        SendData().send(jsonRequestdata, connectionError)
                    } else if (destination.contentDescription == "play" && owner.contentDescription == "sounds_lib") {
                        playSound(this.requireContext(), importedSounds[getId(v)], 2000)
                    } else if (owner.contentDescription == "motion_lib" && destination.contentDescription == "motion_timeline") {
                        if(v.contentDescription == "stationary"){
                            addStationaryMotion(v, destination,"Enter sleep time in ms.")
                        }else{
                            val placeHolder = destination[destination.childCount-1]
                            destination.removeView(placeHolder)
                            val newMotionView = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ShapeableImageView
                            newMotionView.contentDescription = v.contentDescription
                            Glide.with(this.requireContext()).load(Uri.parse("file:///android_asset/gifs/motion${getId(v)}.gif")).into(newMotionView)
                            destination.addView(newMotionView)
                            createDragAndDropListener(newMotionView)
                            destination.addView(placeHolder)
                        }
                    } else if (owner.contentDescription == "sounds_lib" && destination.contentDescription == "sounds_timeline")
                    {
                        if(v.contentDescription == "stationary"){
                            addStationarySound(v, destination,"Enter sleep time in ms.")
                        } else {
                            val placeHolder = destination[destination.childCount-1]
                            destination.removeView(placeHolder)
                            val newSoundView = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as MaterialTextView
                            newSoundView.contentDescription = v.contentDescription
                            val v1 = event.localState as TextView
                            newSoundView.text = v1.text
                            destination.addView(newSoundView)
                            createDragAndDropListener(newSoundView)
                            destination.addView(placeHolder)
                        }
                    }
                    saveToFile(this.requireContext(), binding)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    val v = event.localState as View
                    v.visibility = View.VISIBLE
                    binding.trash.visibility = View.INVISIBLE
                    binding.llTrash.visibility = View.INVISIBLE
                    binding.llPlay.visibility = View.INVISIBLE
                    view.invalidate()
                    true
                }
                else -> false
            }
        } else {
            false
        }
}
    fun addStationaryMotion (view: View, destination: LinearLayout, titel: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.time_user_input_dialog,null)
        val editText = dialogLayout.findViewById<EditText>(R.id.ip_edit_text)
        with(builder){
            setTitle(titel)
            setPositiveButton("OK"){dialog,witch ->
                if (editText.text.toString() != "") {
                    val placeHolder = destination[destination.childCount - 1]
                    destination.removeView(placeHolder)
                    val newMotionView = LayoutInflater.from(this.context)
                        .inflate(R.layout.motion_template, destination, false) as ShapeableImageView
                    newMotionView.contentDescription =
                        view.contentDescription.toString() + editText.text.toString()
                    Glide.with(this.context)
                        .load(Uri.parse("file:///android_asset/gifs/hourglass.gif"))
                        .into(newMotionView)
                    destination.addView(newMotionView)
                    createDragAndDropListener(newMotionView)
                    destination.addView(placeHolder)
                    saveToFile(view.context, binding)
                }
            }
            setNegativeButton("Cancel"){dialog,witch ->

            }
            setView(dialogLayout)
            show()
        }
    }

    fun addStationarySound(view: View, destination: LinearLayout, titel: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.time_user_input_dialog,null)
        val editText = dialogLayout.findViewById<EditText>(R.id.ip_edit_text)
        with(builder){
            setTitle(titel)
            setPositiveButton("OK"){dialog,witch ->
                if (editText.text.toString() != "") {
                    val placeHolder = destination[destination.childCount-1]
                    destination.removeView(placeHolder)
                    val newSoundView = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as MaterialTextView
                    newSoundView.contentDescription = "stationary" + editText.text.toString()
                    newSoundView.text = "No sound"
                    destination.addView(newSoundView)
                    createDragAndDropListener(newSoundView)
                    destination.addView(placeHolder)
                }

            }
            setNegativeButton("Cancel"){dialog,witch ->

            }
            setView(dialogLayout)
            show()
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
@SuppressLint("ClickableViewAccessibility")
fun createDragAndDropListener(view: View) {
    view.setOnTouchListener {
            it, _ ->
        val dragShadowBuilder = View.DragShadowBuilder(it)
        it.startDragAndDrop(ClipData.newPlainText("", ""), dragShadowBuilder, it, 0)
        true
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
//helper function for extracting id from string (eg. "motion1" returns 1)
fun getIntFromContentDescription(view: View) : Int {
    var motionNumParse = view.contentDescription.filter { it.isDigit() }
    //ignore temp motion (might not be needed in the end)
    if (motionNumParse.toString().toIntOrNull() != null) {
        //parse substring to usable in for later
        return motionNumParse.toString().toInt()
    }

    return -1
}

fun setTimelineAlpha(binding : FragmentSecondBinding, tabSelected : Int)
{
    when (tabSelected) {
        0 // motion tab selected
        -> {
            binding.llBottom.alpha = 1.0f
            binding.motionsText.alpha = 1.0f
            binding.llBottomSounds.alpha = 0.3f
            binding.soundsText.alpha = 0.3f
        }
        1 // sound tab selected
        -> {
            binding.llBottom.alpha = 0.3f
            binding.motionsText.alpha = 0.3f
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
fun motionRunAnimations(binding : FragmentSecondBinding, activity : FragmentActivity, motionDuration : MutableList<Int>, animationDone: MutableList<Boolean>, tabSelected: Int) {
    val startAnimation = R.animator.run_animation_start         //reference to animator
    val runAnimation = R.animator.run_animation_run           //reference to animator
    val endAnimation = R.animator.run_animation_end             //reference to animator


    var delay:Long = 0                                          //time in ms
    binding.hsvBottom.smoothScrollTo(0,0)
    if (binding.llBottom.childCount == 1)
    {
        animationDone[1] = true
        animationsDone(binding, animationDone, tabSelected)
    }
    for (i in 0 until binding.llBottom.childCount) {
        if(i == binding.llBottom.childCount-1){//element the empty + square
            continue
        }
        val motionStart:AnimatorSet = AnimatorInflater.loadAnimator(activity, startAnimation) as AnimatorSet
        val motionRun:AnimatorSet = AnimatorInflater.loadAnimator(activity, runAnimation) as AnimatorSet
        val motionEnd:AnimatorSet = AnimatorInflater.loadAnimator(activity, endAnimation) as AnimatorSet
        val x = binding.llBottom.getChildAt(i)
        var duration:Long = 0                                       //time in ms
        var startAnimationDuration:Long                             //time in ms
        var endAnimationDelay:Long = 0                              //time in ms
        if(x.contentDescription.contains("stationary")){
            duration = getIntFromContentDescription(x).toLong()
        }
        else if(x.contentDescription.contains("motion")){
            duration = motionDuration[getId(x)].toLong()
        }

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
            x.alpha = 0.3f
            if (i == binding.llBottom.childCount-2) {
                animationDone[1] = true
                animationsDone(binding, animationDone, tabSelected)
            }

        }
        motionEnd.start()

        delay += duration
    }
}


fun soundAnimation(binding : FragmentSecondBinding, activity : FragmentActivity, position : Int, soundsDuration : MutableList<Int>, animationDone : MutableList<Boolean>, tabSelected: Int)
{
    var bar = binding.hsvSounds[0] as LinearLayout
    if (bar.childCount > 1 && position < bar.childCount-1) {
        var soundStart: AnimatorSet = AnimatorInflater.loadAnimator(activity, R.animator.timeline_start) as AnimatorSet
        var soundEnd: AnimatorSet = AnimatorInflater.loadAnimator(activity, R.animator.timeline_end) as AnimatorSet

        var view = bar.getChildAt(position)
        var duration: Long = if (view.contentDescription.contains("stationary")){
            getIntFromContentDescription(view).toLong()
        } else{
            soundsDuration[getId(view)].toLong()
        }

        val maxDuration = min(duration/2, 1000)

        soundStart.setTarget(view)
        soundStart.duration = maxDuration

        soundEnd.setTarget(view)
        soundEnd.duration = maxDuration
        soundEnd.startDelay = duration - maxDuration
        soundEnd.doOnEnd {
            binding.hsvSounds.smoothScrollTo(view.right - view.width - 100, 0)
            //soundAnimation(hsv, activity, position + 1, soundsDuration)
            view.alpha = 0.3f
            if (position == bar.childCount-2)
            {
                animationDone[0] = true
                animationsDone(binding, animationDone, tabSelected)
            }
        }
        soundStart.start()
        soundEnd.start()
    }
}

fun playAnimatedSounds(fragment : SecondFragment, binding : FragmentSecondBinding, soundsList: MutableList<String>, soundsDuration: MutableList<Int>, position : Int, tabSelected : Int, animationDone : MutableList<Boolean>){
    //only continue if provided list contains any sounds
    if (soundsList.size == 0)
    {
        animationDone[0] = true
        animationsDone(binding, animationDone, tabSelected)
    }
    else if ( position < soundsList.size) {
        //if an existing player is found destroy it
        if (mMediaPlayer != null) {
            stopSound()
        }
        //get next sound as filename from list
        val next = soundsList[position]
        if(next.contains("stationary")){
            soundAnimation(binding, fragment.requireActivity(), position, soundsDuration, animationDone, tabSelected)
            val motionNumParse = next.filter { it.isDigit() }
            object : CountDownTimer(motionNumParse.toLong(),1000){
                override fun onTick(p0: Long) {
                }
                override fun onFinish() {
                    playAnimatedSounds(fragment, binding, soundsList, soundsDuration,position + 1, tabSelected, animationDone)

                    //playSounds(context, list)
                }
            }.start()
        } else {
            //create new player with the next sound and add onComplete listener to recursively play next sound when done.
            try {
                var afd = fragment.requireContext().assets.openFd("sounds/$next")
                mMediaPlayer = MediaPlayer()
                mMediaPlayer!!.setDataSource(afd)
                mMediaPlayer!!.setOnCompletionListener {
                    playAnimatedSounds(fragment, binding, soundsList, soundsDuration,position + 1, tabSelected, animationDone)
                }
                mMediaPlayer!!.prepare()
                mMediaPlayer!!.start()
                soundAnimation(binding, fragment.requireActivity(), position, soundsDuration, animationDone, tabSelected)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun animationsDone(binding: FragmentSecondBinding, animationDone : MutableList<Boolean>, tabSelected : Int)
{
    if (animationDone[0] && animationDone[1]) {
        binding.buttonRun.alpha = 0.7f
        for (view in (binding.hsvSounds[0] as LinearLayout)) {
            view.alpha = 1.0f
        }
        for (view in (binding.hsvBottom[0] as LinearLayout)) {
            view.alpha = 1.0f
        }
        setTimelineAlpha(binding, tabSelected)
    }
}

fun loadFromFile(context: Context, binding: FragmentSecondBinding, importedSounds : MutableList<String>) {
    try {
        val file = File(context.filesDir, "save.txt")
        val contents = file.readText()

        var messageTypes = contents.split(";")
        var messageSounds = messageTypes[0].split(",")
        var messageMotions = messageTypes[1].split(",")

        val placeHolderSounds = binding.llBottomSounds[binding.llBottomSounds.childCount-1]
        binding.llBottomSounds.removeView(placeHolderSounds)
        val placeHolderMotions = binding.llBottom[binding.llBottom.childCount-1]
        binding.llBottom.removeView(placeHolderMotions)

        binding.llBottomSounds.removeAllViews()
        binding.llBottom.removeAllViews()

        for (savedSound in messageSounds) {
            if (savedSound.toIntOrNull() != null) {
                val newSoundView = LayoutInflater.from(context).inflate(R.layout.sound_template, binding.llBottomSounds, false) as MaterialTextView
                newSoundView.contentDescription = "sound$savedSound"
                newSoundView.text = importedSounds[savedSound.toInt()].substring(0, importedSounds[savedSound.toInt()].indexOf("."))
                binding.llBottomSounds.addView(newSoundView)
                createDragAndDropListener(newSoundView)
            } else if (savedSound.contains("stationary")) {
                val newSoundView = LayoutInflater.from(context).inflate(R.layout.sound_template, binding.llBottomSounds, false) as MaterialTextView
                newSoundView.contentDescription = "stationary${savedSound.filter { it.isDigit() }}"
                newSoundView.text = "No sound"
                binding.llBottomSounds.addView(newSoundView)
                createDragAndDropListener(newSoundView)
            }
        }

        for (savedMotion in messageMotions) {
            if (savedMotion.toIntOrNull() != null) {
                val newMotionView = LayoutInflater.from(context).inflate(R.layout.motion_template, binding.llBottom, false) as ShapeableImageView
                newMotionView.contentDescription = "motion$savedMotion"
                Glide.with(context).load(Uri.parse("file:///android_asset/gifs/motion${savedMotion}.gif")).into(newMotionView)
                binding.llBottom.addView(newMotionView)
                createDragAndDropListener(newMotionView)
            } else if (savedMotion.contains("stationary")) {
                val newMotionView = LayoutInflater.from(context).inflate(R.layout.motion_template, binding.llBottom, false) as ShapeableImageView
                newMotionView.contentDescription = "stationary${savedMotion.filter { it.isDigit() }}"
                Glide.with(context).load(Uri.parse("file:///android_asset/gifs/hourglass.gif")).into(newMotionView)
                binding.llBottom.addView(newMotionView)
                createDragAndDropListener(newMotionView)
            }
        }

        binding.llBottomSounds.addView(placeHolderSounds)
        binding.llBottom.addView(placeHolderMotions)
    } catch (e : Exception) {
        println("Error loading from file!")
    }
}

fun saveToFile(context: Context, binding: FragmentSecondBinding) {
    var message = ""

    for (soundChild in binding.llBottomSounds) {
        if (getId(soundChild) != -1) {
            if (soundChild.contentDescription.contains("stationary")) {
                message += "stationary"
            }
            message += getId(soundChild).toString() + ","
        }
    }
    message += ";"
    for (motionChild in binding.llBottom) {
        if (getId(motionChild) != -1) {
            if (motionChild.contentDescription.contains("stationary")) {
                message += "stationary"
            }
            message += getId(motionChild).toString() + ","
        }
    }
    context.openFileOutput("save.txt", Context.MODE_PRIVATE).use {
        it.write(message.toByteArray())
    }
}
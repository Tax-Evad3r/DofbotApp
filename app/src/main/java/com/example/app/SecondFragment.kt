package com.example.app

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color.rgb
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.example.app.databinding.FragmentSecondBinding
import androidx.core.animation.doOnEnd
import androidx.core.view.contains
import androidx.core.view.iterator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var motionList : MutableList<Int> = mutableListOf()
    private lateinit var motionAdapter : MotionAdapter


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val adapter = recyclerView.adapter as MotionAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder.itemView.alpha = 1.0f
            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        //binding.recycleView.adapter = MotionAdapter(this.requireContext(), motionList)
        //binding.recycleView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
       // (binding.recycleView.adapter as MotionAdapter).notifyDataSetChanged()
        //binding.recycleView.adapter = motionAdapter

       // motionAdapter = MotionAdapter(this.requireContext(), motionList)
        //binding.recycleView.adapter = motionAdapter



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*
        for (i in 0..10)
        {
            //motionAdapter.addMotion(i%4)
            //newList.add(i%4)
            motionList.add(i % 4) // should (i % availableMotions.size)
            //motionAdapter.differ.submitList(motionList)

        }*/

        itemTouchHelper.attachToRecyclerView(binding.recycleView)
        motionAdapter = MotionAdapter(this.requireContext())
        motionAdapter.differ.submitList(motionList)
        binding.recycleView.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.HORIZONTAL,false)
        binding.recycleView.adapter = motionAdapter

        println("List size: $motionList")
        /*val newList = mutableListOf<Int>()
        newList.addAll(motionList)
        for (i in 0..10)
        {
            //motionAdapter.addMotion(i%4)
            newList.add(i%4)
        }
        motionAdapter.submitList(newList)*/

        //import motions from asset files (located in "main/assets/motion")
        val availableMotions = importMotionFromFile(this.requireContext())
        val importedSounds = importSounds(this.context)

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
        //(binding.recycleView.adapter as MotionAdapter).notifyDataSetChanged()
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
        binding.lltrash.setOnDragListener(dragListener)
        binding.recycleView.setOnDragListener(dragListener)
        binding.trash.visibility = View.INVISIBLE

        //create new view for each motion depending on amount of imported motions
        for (i in availableMotions.indices) {
            val destination = binding.llRightMotions
            val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
            motion1.contentDescription = "motion$i"
            val res = this.resources.getIdentifier("motion$i", "drawable", "com.example.app")
            Glide.with(this.requireContext()).load(res).into(motion1)
            destination.addView(motion1)
            createDragAndDropListener(motion1)
        }

        //create new view for each sound depending on amount of imported sounds
        for (i in importedSounds.indices) {
            val destination = binding.llRightSounds
            val sound = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as ImageView
            sound.setBackgroundColor(rgb((0..255).random(),(0..255).random(),(0..255).random()))
            sound.contentDescription = "sound$i"
            destination.addView(sound)
            createDragAndDropListener(sound)
            createClickListener(this.requireContext(), sound, importedSounds[getId(sound)])
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
            SendData().send(jsonRequestdata)
            
            //create sound list
            val soundsList = mutableListOf<String>()

            //loop through all sounds in timeline (skip first since it is not a sound)
            for (sound in binding.llBottomSounds) {
                val soundId = getId(sound)
                if( soundId != -1) {
                    soundsList.add(importedSounds[soundId])
                }
            }
            playSounds(this.requireContext(), soundsList)
        }
        var tmp = 0
        binding.buttonStart.setOnClickListener {
            //val motionAdapter = binding.recycleView.adapter
            //val currentSize = binding.recycleView.adapter.getItemCount()
           // val currentSize = motionAdapter?.itemCount
            //motionList.add(tmp++)
           // motionAdapter?.notifyItemInserted(motionList.size)
           //motionAdapter?.notifyDataSetChanged()
            //motionAdapter.addMotion(tmp)

            val newList = mutableListOf<Int>()
            newList.addAll(motionAdapter.differ.currentList)
            for (i in 0..10) {
                //motionAdapter.addMotion(i%4)
                newList.add(i % 4)
            }
            motionAdapter.differ.submitList(newList)
            println("List size: ${motionAdapter.differ.currentList.size}")
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
            binding.llBottom.alpha = 1.0f
            binding.llBottomSounds.alpha = 1.0f
            binding.lltrash.alpha = 1.0f
            view.invalidate()
            true
        }
        DragEvent.ACTION_DROP -> {
            binding.llBottom.alpha = 1.0f
            binding.llBottomSounds.alpha = 1.0f
            binding.lltrash.alpha = 1.0f
            binding.trash.visibility = View.INVISIBLE;

            val v = event.localState as View
            val owner = v.parent as ViewGroup
            val destination = view as LinearLayout

            if (destination.contentDescription == "trash" && (owner.contentDescription == "motion_timeline" || owner.contentDescription == "sounds_timeline")) {
                owner.removeView(v)
            } else if (owner.contentDescription == "motion_lib" && destination.contentDescription == "motion_timeline") {
                val motion1 = LayoutInflater.from(this.context).inflate(R.layout.motion_template, destination, false) as ImageView
                motion1.contentDescription = v.contentDescription
                val res = this.resources.getIdentifier("motion${getId(v)}", "drawable", "com.example.app")
                Glide.with(this.requireContext()).load(res).into(motion1)
                destination.addView(motion1)
                createDragAndDropListener(motion1)
            } else if (owner.contentDescription == "sounds_lib" && destination.contentDescription == "sounds_timeline")
            {
                val sounds1 = LayoutInflater.from(this.context).inflate(R.layout.sound_template, destination, false) as ImageView
                val back = v.background as ColorDrawable
                sounds1.setBackgroundColor(back.color)
                sounds1.contentDescription = v.contentDescription
                destination.addView(sounds1)
                createDragAndDropListener(sounds1)
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

package com.example.app

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

/*
class MotionAdapter(
    private val context: Context,
    private val motions: MutableList<Int>
) : //RecyclerView.Adapter<MotionAdapter.ViewHolder>(){
    ListAdapter<Int, MotionAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById<ImageView>(R.id.motion999) as ImageView
    }

    class DiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }


    // Inflates a motion_template in parent
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotionAdapter.ItemViewHolder {
        val motion = LayoutInflater.from(context)
            .inflate(R.layout.motion_template, parent, false) as ImageView
        return ItemViewHolder(motion)
    }

    override fun onBindViewHolder(viewHolder: MotionAdapter.ItemViewHolder, position: Int) {
        val imageView = viewHolder.image
        val motion = motions[position]
        println(motion)
        imageView.contentDescription = "motion$motion"
        val res = context.resources.getIdentifier("motion$motion", "drawable", "com.example.app")
        Glide.with(context).load(res).into(imageView)
    }
/*
    public fun addMotion(newMotion: Int) {
        motions.add(newMotion)
        submitList(motions)
    }
    */


    //override fun getItemCount() : Int {
    //     return motions.size
    //}
}
*/
/*
class MotionAdapter(private val context: Context) : RecyclerView.Adapter<MotionAdapter.MotionViewHolder> (){


    inner class  MotionViewHolder(private val itemViewBinding: MotionTemplateForRvBinding): RecyclerView.ViewHolder(itemViewBinding.root) {
        val image: ImageView = itemView.findViewById<ImageView>(R.id.motionTest) as ImageView
        fun bindView(motionItem: Int) {
            itemViewBinding.apply {
                image.contentDescription= "motion$motionItem"
                //val res = context.resources.getIdentifier("motion$motionItem", "drawable", "com.example.app")
                //Glide.with(context).load(res).into(image)
            }

            itemViewBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(motionItem)
                }
            }
        }
    }

    private val differCallBack  = object : DiffUtil.ItemCallback<Int>()
    {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return  oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return  oldItem == newItem
        }


    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotionViewHolder {
        //val motion = LayoutInflater.from(context).inflate(R.layout.motion_template, parent, false) as ImageView
        //return MotionViewHolder(motion)
        return MotionViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.motion_template_for_rv, parent, false)
        )
    }

    private var onItemClickListener: ((Int) -> Unit)? = null
    override fun onBindViewHolder(holder: MotionViewHolder, position: Int) {

        val motionItem = differ.currentList[position]
        holder.bindView(motionItem)

       // val res = context.resources.getIdentifier("motion$motionItem", "drawable", "com.example.app")
        //Glide.with(context).load(res).into(holder.itemView)

        val imageView = holder.image
        println(motionItem)
        imageView.contentDescription = "motion$motionItem"
        //imageView.setBackgroundColor(BLACK)
        val res = context.resources.getIdentifier("motion$motionItem", "drawable", "com.example.app")
        Glide.with(context).load(res).into(imageView)

    }

    fun moveItem(from: Int, to: Int) {
        val list = differ.currentList.toMutableList()
        val fromLocation = list[from]
        list.removeAt(from)
        if (to < from) {
            list.add(to + 1 , fromLocation)
        } else {
            list.add(to - 1, fromLocation)
        }
        differ.submitList(list)
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener

    }
}
*/
class MotionAdapter(private val context: Context) : RecyclerView.Adapter<MotionAdapter.MotionViewHolder> (){

    inner class  MotionViewHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {
        //val image: ImageView = itemView.findViewById<ImageView>(R.id.motionTest) as ImageView
        fun bindView(motionItem: Int) {
            itemView.apply {
                itemView.contentDescription= "motion$motionItem"
                val res = context.resources.getIdentifier("motion$motionItem", "drawable", "com.example.app")
                Glide.with(context).load(res).into(itemView as ImageView)
            }

            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(motionItem)
                }
                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(ClipData.newPlainText("", ""), dragShadowBuilder, it, 0)
                //it.visibility = View.INVISIBLE
            }
            /*itemView.setOnLongClickListener {
                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(ClipData.newPlainText("", ""), dragShadowBuilder, it, 0)
                it.visibility = View.INVISIBLE
                true
            }*/


        }
    }

    private val differCallBack  = object : DiffUtil.ItemCallback<Int>()
    {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return  oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return  oldItem == newItem
        }


    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotionViewHolder {
        val motion = LayoutInflater.from(context).inflate(R.layout.motion_template, parent, false) as ImageView
        return MotionViewHolder(motion)
        /*
        return MotionViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.motion_template_for_rv, parent, false)
        )
         */
    }

    private var onItemClickListener: ((Int) -> Unit)? = null
    override fun onBindViewHolder(holder: MotionViewHolder, position: Int) {
        val motionItem = differ.currentList[position]
        holder.bindView(motionItem)
    }

    fun moveItem(from: Int, to: Int) {
        val list = differ.currentList.toMutableList()
        val fromLocation = list[from]
        list.removeAt(from)
        if (to < from) {
            list.add(to + 1 , fromLocation)
        } else {
            list.add(to - 1, fromLocation)
        }
        differ.submitList(list)
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener

    }
}

//https://proandroiddev.com/make-drag-and-drop-in-recyclerview-easy-and-pretty-aadb2b693b85
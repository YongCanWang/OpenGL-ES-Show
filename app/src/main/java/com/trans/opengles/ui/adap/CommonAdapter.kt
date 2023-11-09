package com.trans.opengles.ui.adap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trans.opengles.R
import com.trans.opengles.ui.bean.SampleItem


/**
 * @author Tom灿
 * @description:
 * @date :2023/11/9 13:29
 */
class CommonAdapter() : RecyclerView.Adapter<CommonAdapter.ViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener
    private var datas = ArrayList<SampleItem>()

    constructor(datas: ArrayList<SampleItem>) : this() {
        this.datas = datas
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = datas[position].name
        holder.des.text = datas[position].description
        holder.itemView.setOnClickListener {
            onItemClickListener?.itemClick(datas[position])
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val des: TextView

        init {
            // Define click listener for the ViewHolder's View
            name = view.findViewById<View>(R.id.nameView) as TextView
            des = view.findViewById<View>(R.id.descriptionView) as TextView
        }
    }


    interface OnItemClickListener {
        fun itemClick(sampleItem: SampleItem)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

}



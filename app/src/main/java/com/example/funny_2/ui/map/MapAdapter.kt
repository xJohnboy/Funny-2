package com.example.funny_2.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_2.R
import com.example.funny_2.data.Entities
import kotlinx.android.synthetic.main.recycler_map.view.*

class MapAdapter : RecyclerView.Adapter<MapAdapter.VHolder>() {
    private val data = ArrayList<Entities>()
    var onViewClick: ((Entities) -> Unit)? = null

    fun addMap(append: ArrayList<Entities>) {
        data.addAll(append)
        notifyDataSetChanged()
    }

    fun addListMap(append: Entities) {
        data.add(append)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_map, parent, false)
        return VHolder(v)
    }

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        holder.bindItem(data[position])
        holder.textPos.text = (position + 1).toString()
    }

    override fun getItemCount(): Int = data.size

    inner class VHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textPos = v.textCodeMap
        fun bindItem(data: Entities) {
            itemView.textNameMap.text = data.store_name
        }

        init {
            v.setOnClickListener {
                onViewClick?.invoke(data[adapterPosition])
            }
        }
    }
}
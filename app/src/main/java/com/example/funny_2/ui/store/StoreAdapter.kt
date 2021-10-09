package com.example.funny_2.ui.store

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_2.R
import com.example.funny_2.data.StoreData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_store.view.*

class StoreAdapter : RecyclerView.Adapter<StoreAdapter.VHolder>() {
    private val data = ArrayList<StoreData>()
    var onViewClick: ((StoreData) -> Unit)? = null
    fun addProduct(append: ArrayList<StoreData>) {
        data.addAll(append)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_store, parent, false)
        return VHolder(v)
    }

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        holder.bindItem(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class VHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image = v.imageViewProduct

        @SuppressLint("SetTextI18n")
        fun bindItem(data: StoreData) {
            itemView.textTitle.text = data.title
            itemView.textPrice.text = "$ ${data.price}"
            Picasso.get().load(data.image).into(image)
        }

        init {
            itemView.setOnClickListener {
                onViewClick?.invoke(data[adapterPosition])
            }
        }
    }
}
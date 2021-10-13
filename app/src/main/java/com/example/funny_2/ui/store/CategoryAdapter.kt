package com.example.funny_2.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_2.R
import kotlinx.android.synthetic.main.recycler_category.view.*
import java.util.*
import kotlin.collections.ArrayList

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.VHolder>() {
    private val data = ArrayList<String>()
    var onViewClick: ((String) -> Unit)? = null
    var categorySelected = CategoryActivity.ALL_ITEMS

    fun addAll(append: ArrayList<String>) {
        data.clear()
        data.addAll(append)
        data.add(4, CategoryActivity.ALL_ITEMS)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_category, parent, false)
        return VHolder(v)
    }

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        holder.bindItem(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class VHolder(v: View) : RecyclerView.ViewHolder(v) {
        val matCardCategory = v.matCardCategory
        fun bindItem(data: String) {
            itemView.textCategory.text = data.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }

            matCardCategory.isChecked = data == categorySelected
        }

        init {
            v.setOnClickListener {
                onViewClick?.invoke(data[adapterPosition])
                matCardCategory.isChecked = !matCardCategory.isChecked
            }
        }
    }
}

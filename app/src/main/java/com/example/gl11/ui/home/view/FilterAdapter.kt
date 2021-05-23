package com.example.gl11.ui.home.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gl11.R
import com.example.gl11.entity.DataType


class FilterAdapter(
    private val context: Context,
    private val dataList: MutableList<FilterDto> = mutableListOf())
    : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    interface OnSelectListener{
        fun onSelect(filter:FilterDto)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailView: ImageView = itemView.findViewById(R.id.thumbnail)
        val nameView: TextView = itemView.findViewById(R.id.name)
        val root:CardView = itemView as CardView
    }
    var onSelectListener:OnSelectListener? = null

    private val inflater: LayoutInflater by lazy{ LayoutInflater.from(context) }
    private val selectColor:Int by lazy{ ContextCompat.getColor(context, R.color.design_default_color_background)}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        = ViewHolder(inflater.inflate(R.layout.list_item_filter, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataList[position].let{
            when(it.type){
                DataType.STATION->{
                    holder.thumbnailView.setImageResource(R.drawable.ic_b_train)
                    holder.nameView.setText(R.string.filter_station)
                }
                DataType.FAVORITE->{
                    holder.thumbnailView.setImageResource(R.drawable.ic_b_fav_pos)
                    holder.nameView.setText(R.string.filter_favorite)
                }
                DataType.USER->{
                    it.user?.let{ user->
                        holder.thumbnailView.setImageURI(Uri.parse(user.thumbnail))
                        holder.nameView.text = user.name
                    } ?:run{
                        holder.thumbnailView.setImageResource(R.drawable.user)
                        holder.nameView.text = ""
                    }
                }
            }
            holder.root.cardElevation = if(it.isSelected){ 10f } else{ 0f }
            holder.root.setOnClickListener { _-> onSelectListener?.onSelect(it) }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
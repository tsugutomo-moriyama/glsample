package com.example.gl11.ui.home.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gl11.R
import com.example.gl11.entity.DataType


class ContentsAdapter(
    private val context: Context,
    val top:ContentsDto,
    private val dataList: MutableList<ContentsDto> = mutableListOf())
    : RecyclerView.Adapter<ContentsAdapter.ViewHolder>() {

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailView: ImageView = itemView.findViewById(R.id.thumbnail)
        val nameView: TextView = itemView.findViewById(R.id.name)
        val contentsView: TextView = itemView.findViewById(R.id.contents)
        val favView:ImageView = itemView.findViewById(R.id.fav)
        val likeView:ImageView = itemView.findViewById(R.id.like)
        val likeCountView:TextView = itemView.findViewById(R.id.like_count)
    }
    class TopViewHolder(itemView: View) : ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.title)
        val distanceView: TextView = itemView.findViewById(R.id.distance)
        val coordinateView: TextView = itemView.findViewById(R.id.coordinate)
    }

    private val inflater: LayoutInflater by lazy{ LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            0->{
                TopViewHolder(inflater.inflate(R.layout.list_item_contents_top, parent, false))
            }
            else->{
                ViewHolder(inflater.inflate(R.layout.list_item_contents_reply, parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(position == 0){ top } else {dataList[position-1] }.let{
            when(it.type){
                DataType.STATION->{
                    holder.thumbnailView.setImageResource(R.drawable.ic_w_train)
                    holder.nameView.visibility = View.GONE
                    holder.contentsView.text = it.contents
                    (holder as? TopViewHolder)?.let{ h->
                        h.distanceView.text = it.distance
                        h.coordinateView.text = it.coordinate
                        h.titleView.text = it.title
                    }
                    holder.favView.visibility = View.VISIBLE
                    holder.favView.setImageResource(
                        if(it.isFav){ R.drawable.ic_y_fav }else{ R.drawable.ic_w_fav })
                    holder.likeView.visibility = View.INVISIBLE
                    holder.likeCountView.visibility = View.INVISIBLE
                }
                DataType.FAVORITE->{
                    holder.thumbnailView.setImageResource(R.drawable.ic_w_fav_pos)
                    holder.nameView.visibility = View.GONE
                    holder.contentsView.text = it.contents
                    (holder as? TopViewHolder)?.let{ h->
                        h.distanceView.text = it.distance
                        h.coordinateView.text = it.coordinate
                        h.titleView.text = it.title
                    }
                    holder.favView.visibility = View.GONE
                    holder.likeView.visibility = View.GONE
                    holder.likeCountView.visibility = View.GONE
                }
                DataType.USER->{
                    it.user?.let{ user->
                        holder.thumbnailView.setImageURI(Uri.parse(user.thumbnail))
                        holder.nameView.text = user.name
                    } ?:run{
                        holder.thumbnailView.setImageResource(R.drawable.user)
                        holder.nameView.text = ""
                    }
                    holder.contentsView.text = it.contents
                    (holder as? TopViewHolder)?.let{ h->
                        h.distanceView.text = it.distance
                        h.coordinateView.text = it.coordinate
                        h.titleView.text = it.title
                    }
                    holder.favView.visibility = View.VISIBLE
                    holder.likeView.visibility = View.VISIBLE
                    holder.likeCountView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size + 1
}
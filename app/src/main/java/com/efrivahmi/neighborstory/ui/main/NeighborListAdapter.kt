package com.efrivahmi.neighborstory.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.efrivahmi.neighborstory.R
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.databinding.ItemNeighborBinding
import com.efrivahmi.neighborstory.ui.detail.DetailNeighborActivity

class NeighborListAdapter : PagingDataAdapter<ListStoryItem, NeighborListAdapter.ListViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }

    inner class ListViewHolder(private val binding: ItemNeighborBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listStoryItem: ListStoryItem) {
            binding.apply {
                if (listStoryItem.photoUrl.isNullOrEmpty()) {
                    ivStory.setImageResource(R.drawable.black)
                } else {
                    Glide.with(itemView.context)
                        .load(listStoryItem.photoUrl)
                        .into(ivStory)
                }
                tvTitle.text = listStoryItem.name ?: ""
                tvDesc.text = listStoryItem.description ?: ""
            }
            itemView.setOnClickListener{
                val intentToDetail = Intent(itemView.context, DetailNeighborActivity::class.java)
                intentToDetail.putExtra("DATA", listStoryItem)
                itemView.context.startActivity(intentToDetail)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemNeighborBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listStoryItem = getItem(position)
        if (listStoryItem != null) {
            holder.bind(listStoryItem)
        }
    }
}

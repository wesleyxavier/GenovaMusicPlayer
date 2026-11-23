package com.genova.musicplayer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.genova.musicplayer.R
import com.genova.musicplayer.domain.playlist.Playlist

class PlaylistListAdapter(
    private val context: Context,
    private val onMoreClick: (Playlist, View) -> Unit
) : BaseAdapter() {

    private val items = ArrayList<Playlist>()
    private val inflater = LayoutInflater.from(context)

    fun submitList(playlists: List<Playlist>) {
        items.clear()
        items.addAll(playlists)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Playlist = items[position]

    override fun getItemId(position: Int): Long = items[position].id.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_playlist, parent, false)
            holder = ViewHolder(
                tvName = view.findViewById(R.id.tv_playlist_name),
                tvCount = view.findViewById(R.id.tv_playlist_count),
                btnMore = view.findViewById(R.id.btn_playlist_more)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val playlist = items[position]
        holder.tvName.text = playlist.name
        holder.tvCount.text = context.getString(
            R.string.playlist_track_count_format,
            playlist.trackIds.size
        )
        holder.btnMore.setOnClickListener {
            onMoreClick(playlist, holder.btnMore)
        }
        return view
    }

    private class ViewHolder(
        val tvName: TextView,
        val tvCount: TextView,
        val btnMore: ImageButton
    )
}

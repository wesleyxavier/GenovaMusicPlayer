package com.genova.musicplayer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.genova.musicplayer.R
import com.genova.musicplayer.data.AlbumArtCacheLoader
import com.genova.musicplayer.domain.library.Track
import java.util.Locale

class TrackListAdapter(
    private val context: Context,
    private val onMoreClicked: (Track, View) -> Unit
) : BaseAdapter() {

    private val items = mutableListOf<Track>()
    private var activeTrackId: Long? = null
    private val thumbSizePx = (48 * context.resources.displayMetrics.density).toInt().coerceAtLeast(48)
    private val activeColor = context.getColor(R.color.nv_primary)
    private val defaultTitleColor = context.getColor(R.color.nv_on_surface)

    fun submitList(tracks: List<Track>, currentTrackId: Long?) {
        items.clear()
        items.addAll(tracks)
        activeTrackId = currentTrackId
        notifyDataSetChanged()
    }

    fun updateActiveTrack(currentTrackId: Long?) {
        if (activeTrackId != currentTrackId) {
            activeTrackId = currentTrackId
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Track = items[position]

    override fun getItemId(position: Int): Long = items[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_track, parent, false)
            holder = ViewHolder(
                art = view.findViewById(R.id.iv_track_art),
                activeIndicator = view.findViewById(R.id.view_active_indicator),
                title = view.findViewById(R.id.tv_track_title),
                subtitle = view.findViewById(R.id.tv_track_subtitle),
                duration = view.findViewById(R.id.tv_track_duration),
                moreButton = view.findViewById(R.id.btn_track_more)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val track = items[position]
        val isActive = track.id == activeTrackId
        holder.title.text = track.title
        holder.title.setTextColor(if (isActive) activeColor else defaultTitleColor)
        holder.subtitle.text = track.artist
        holder.duration.text = formatDuration(track.durationMs)
        holder.activeIndicator.visibility = if (isActive) View.VISIBLE else View.GONE

        AlbumArtCacheLoader.loadThumb(context, track.contentUri, thumbSizePx, holder.art)
        holder.moreButton.setOnClickListener { anchor ->
            onMoreClicked(track, anchor)
        }

        return view
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private class ViewHolder(
        val art: ImageView,
        val activeIndicator: View,
        val title: TextView,
        val subtitle: TextView,
        val duration: TextView,
        val moreButton: ImageButton
    )
}

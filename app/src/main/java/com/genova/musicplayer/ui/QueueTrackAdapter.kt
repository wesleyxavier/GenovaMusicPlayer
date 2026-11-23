package com.genova.musicplayer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
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

class QueueTrackAdapter(
    private val context: Context,
    private val onMoreClicked: (position: Int, track: Track, anchor: View) -> Unit,
    private val onSwapItems: (fromPosition: Int, toPosition: Int) -> Unit
) : BaseAdapter() {

    private val items = mutableListOf<Track>()
    private var activeIndex: Int = -1
    private val thumbSizePx = (42 * context.resources.displayMetrics.density).toInt().coerceAtLeast(42)
    private val rowHeightPx = (68f * context.resources.displayMetrics.density).coerceAtLeast(68f)
    private val activeColor = context.getColor(R.color.nv_primary)
    private val defaultColor = context.getColor(R.color.nv_on_surface)

    fun submitList(tracks: List<Track>, currentIndex: Int) {
        items.clear()
        items.addAll(tracks)
        activeIndex = currentIndex
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Track = items[position]

    override fun getItemId(position: Int): Long = items[position].id * 31L + position

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_queue_track, parent, false)
            holder = ViewHolder(
                pos = view.findViewById(R.id.tv_queue_item_pos),
                art = view.findViewById(R.id.iv_queue_item_art),
                title = view.findViewById(R.id.tv_queue_item_title),
                subtitle = view.findViewById(R.id.tv_queue_item_subtitle),
                moreButton = view.findViewById(R.id.btn_queue_item_more),
                dragButton = view.findViewById(R.id.btn_queue_item_drag)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val track = items[position]
        val isActive = position == activeIndex
        view.setBackgroundResource(if (isActive) R.drawable.bg_card_surface_high else R.drawable.bg_card_surface)
        holder.pos.text = (position + 1).toString()
        holder.pos.setTextColor(if (isActive) activeColor else context.getColor(R.color.nv_on_surface_variant))
        holder.title.text = track.title
        holder.title.setTextColor(if (isActive) activeColor else defaultColor)

        val formatLabel = if (track.isHiFi) "FLAC" else track.mimeType.substringAfter('/').uppercase(Locale.US)
        holder.subtitle.text = "${track.artist} • $formatLabel • ${formatDuration(track.durationMs)}"

        AlbumArtCacheLoader.loadThumb(context, track.contentUri, thumbSizePx, holder.art)
        holder.moreButton.setOnClickListener { anchor ->
            onMoreClicked(position, track, anchor)
        }

        var dragPos = position
        var lastRawY = 0f
        holder.dragButton.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragPos = position
                    lastRawY = event.rawY
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - lastRawY
                    if (deltaY > rowHeightPx * 0.7f && dragPos < items.lastIndex) {
                        val next = dragPos + 1
                        onSwapItems(dragPos, next)
                        dragPos = next
                        lastRawY = event.rawY
                    } else if (deltaY < -rowHeightPx * 0.7f && dragPos > 0) {
                        val prev = dragPos - 1
                        onSwapItems(dragPos, prev)
                        dragPos = prev
                        lastRawY = event.rawY
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    true
                }
                else -> false
            }
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
        val pos: TextView,
        val art: ImageView,
        val title: TextView,
        val subtitle: TextView,
        val moreButton: ImageButton,
        val dragButton: ImageButton
    )
}

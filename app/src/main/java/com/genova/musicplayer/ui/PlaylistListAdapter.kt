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
import com.genova.musicplayer.domain.playlist.Playlist

class PlaylistListAdapter(
    private val context: Context,
    private val onQuickPlayClick: (Playlist) -> Unit,
    private val onMoreClick: (Playlist, View) -> Unit
) : BaseAdapter() {

    private val items = ArrayList<Playlist>()
    private var trackMap: Map<Long, Track> = emptyMap()
    private var isGridMode: Boolean = true
    private val inflater = LayoutInflater.from(context)
    private val thumbSizePx = (64 * context.resources.displayMetrics.density).toInt().coerceAtLeast(64)

    fun submitList(
        playlists: List<Playlist>,
        tracksById: Map<Long, Track> = emptyMap(),
        gridMode: Boolean = true
    ) {
        items.clear()
        items.addAll(playlists)
        trackMap = tracksById
        isGridMode = gridMode
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Playlist = items[position]

    override fun getItemId(position: Int): Long = items[position].id.hashCode().toLong()

    override fun getViewTypeCount(): Int = 2

    override fun getItemViewType(position: Int): Int = if (isGridMode) 0 else 1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val playlist = items[position]
        val resolvedTracks = playlist.trackIds.mapNotNull { trackMap[it] }
        val durationText = formatSummaryDuration(resolvedTracks.sumOf { it.durationMs.coerceAtLeast(0L) })
        val summaryText = context.getString(R.string.playlist_summary_format, playlist.trackIds.size, durationText)
        val firstTrack = resolvedTracks.firstOrNull()

        return if (isGridMode) {
            val view = convertView ?: inflater.inflate(R.layout.item_playlist_grid, parent, false)
            val ivArt = view.findViewById<ImageView>(R.id.iv_playlist_grid_art)
            val tvPinned = view.findViewById<TextView>(R.id.tv_playlist_grid_pinned)
            val btnPlay = view.findViewById<ImageButton>(R.id.btn_playlist_grid_play)
            val tvName = view.findViewById<TextView>(R.id.tv_playlist_grid_name)
            val tvCount = view.findViewById<TextView>(R.id.tv_playlist_grid_count)
            val btnMore = view.findViewById<ImageButton>(R.id.btn_playlist_grid_more)

            tvName.text = playlist.name
            tvCount.text = summaryText
            tvPinned.visibility = if (playlist.isPinned) View.VISIBLE else View.GONE
            bindPlaylistArt(ivArt, firstTrack)

            btnPlay.setOnClickListener { onQuickPlayClick(playlist) }
            btnMore.setOnClickListener { onMoreClick(playlist, btnMore) }
            view
        } else {
            val view = convertView ?: inflater.inflate(R.layout.item_playlist, parent, false)
            val ivArt = view.findViewById<ImageView>(R.id.iv_playlist_art)
            val tvPinned = view.findViewById<TextView>(R.id.tv_playlist_pinned)
            val tvName = view.findViewById<TextView>(R.id.tv_playlist_name)
            val tvCount = view.findViewById<TextView>(R.id.tv_playlist_count)
            val btnPlay = view.findViewById<ImageButton>(R.id.btn_playlist_quick_play)
            val btnMore = view.findViewById<ImageButton>(R.id.btn_playlist_more)

            tvName.text = playlist.name
            tvCount.text = summaryText
            tvPinned.visibility = if (playlist.isPinned) View.VISIBLE else View.GONE
            bindPlaylistArt(ivArt, firstTrack)

            btnPlay.setOnClickListener { onQuickPlayClick(playlist) }
            btnMore.setOnClickListener { onMoreClick(playlist, btnMore) }
            view
        }
    }

    private fun bindPlaylistArt(imageView: ImageView, firstTrack: Track?) {
        if (firstTrack != null) {
            imageView.imageTintList = null
            imageView.setPadding(0, 0, 0, 0)
            AlbumArtCacheLoader.loadThumb(context, firstTrack.contentUri, thumbSizePx, imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_queue)
            imageView.setColorFilter(context.getColor(R.color.nv_primary))
        }
    }

    private fun formatSummaryDuration(totalMs: Long): String {
        val totalMin = (totalMs / 60_000L).coerceAtLeast(0L)
        val hours = totalMin / 60L
        val mins = totalMin % 60L
        return if (hours > 0L) "${hours}h ${mins}m" else "${mins}m"
    }
}

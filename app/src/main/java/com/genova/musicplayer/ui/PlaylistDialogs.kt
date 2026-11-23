package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.AlbumArtCacheLoader
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsViewModel
import java.util.Locale

object PlaylistDialogs {

    fun showCreatePlaylistDialog(
        activity: Activity,
        onConfirm: (name: String, isPinned: Boolean, coverStyle: Int) -> Boolean
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_create_playlist, null)
        val etName = view.findViewById<EditText>(R.id.et_create_playlist_name)
        val tvCounter = view.findViewById<TextView>(R.id.tv_create_playlist_counter)
        val switchPin = view.findViewById<Switch>(R.id.switch_create_playlist_pin)
        val tvArtStyle = view.findViewById<TextView>(R.id.tv_create_playlist_art_style)
        val frameArt = view.findViewById<View>(R.id.frame_create_playlist_art)

        var selectedStyle = 0
        val styleColors = intArrayOf(
            R.color.nv_primary_container,
            R.color.nv_surface_container_highest,
            R.color.nv_primary,
            R.color.nv_on_primary
        )
        tvArtStyle.setOnClickListener {
            selectedStyle = (selectedStyle + 1) % styleColors.size
            frameArt.setBackgroundColor(activity.getColor(styleColors[selectedStyle]))
        }

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCounter.text = activity.getString(R.string.playlist_name_counter, s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.playlist_create_title)
            .setView(view)
            .setPositiveButton(R.string.playlist_create_confirm, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val ok = onConfirm(etName.text.toString(), switchPin.isChecked, selectedStyle)
                if (ok) dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun showAddToPlaylistsDialog(
        activity: Activity,
        track: Track,
        viewModel: PlaylistsViewModel,
        onCompleted: () -> Unit = {}
    ) {
        viewModel.loadPlaylists()
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_to_playlists, null)
        val ivArt = view.findViewById<ImageView>(R.id.iv_add_playlist_track_art)
        val tvTitle = view.findViewById<TextView>(R.id.tv_add_playlist_track_title)
        val tvArtist = view.findViewById<TextView>(R.id.tv_add_playlist_track_artist)
        val tvHifi = view.findViewById<TextView>(R.id.tv_add_playlist_track_hifi)
        val btnCreateNew = view.findViewById<View>(R.id.btn_add_playlist_create_new)
        val etSearch = view.findViewById<EditText>(R.id.et_add_playlist_search)
        val listChoices = view.findViewById<ListView>(R.id.list_add_playlist_choices)
        val tvSelectedCount = view.findViewById<TextView>(R.id.tv_add_playlist_selected_count)

        tvTitle.text = track.title
        tvArtist.text = track.artist
        tvHifi.visibility = if (track.isHiFi) View.VISIBLE else View.GONE
        val thumbSize = (44 * activity.resources.displayMetrics.density).toInt().coerceAtLeast(44)
        AlbumArtCacheLoader.loadThumb(activity, track.contentUri, thumbSize, ivArt)

        val selectedIds = viewModel.getState().playlists
            .filter { it.trackIds.contains(track.id) }
            .map { it.id }
            .toMutableSet()

        var visiblePlaylists: List<Playlist> = emptyList()

        fun refreshChoices() {
            val query = etSearch.text?.toString()?.trim().orEmpty().lowercase(Locale.getDefault())
            val all = viewModel.getState().playlists
            visiblePlaylists = if (query.isEmpty()) all else all.filter {
                it.name.lowercase(Locale.getDefault()).contains(query)
            }
            val labels = visiblePlaylists.map { "${it.name} (${it.trackIds.size})" }
            listChoices.adapter = ArrayAdapter(
                activity,
                android.R.layout.simple_list_item_multiple_choice,
                labels
            )
            visiblePlaylists.forEachIndexed { idx, playlist ->
                listChoices.setItemChecked(idx, selectedIds.contains(playlist.id))
            }
            tvSelectedCount.text = activity.getString(R.string.add_to_playlist_selected_count, selectedIds.size)
        }

        listChoices.setOnItemClickListener { _, _, position, _ ->
            val playlist = visiblePlaylists.getOrNull(position) ?: return@setOnItemClickListener
            if (listChoices.isItemChecked(position)) {
                selectedIds.add(playlist.id)
            } else {
                selectedIds.remove(playlist.id)
            }
            tvSelectedCount.text = activity.getString(R.string.add_to_playlist_selected_count, selectedIds.size)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshChoices()
            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnCreateNew.setOnClickListener {
            showCreatePlaylistDialog(activity) { name, isPinned, coverStyle ->
                val created = viewModel.createPlaylist(name, isPinned, coverStyle)
                if (created != null) {
                    selectedIds.add(created.id)
                    refreshChoices()
                    true
                } else {
                    val msg = when (val fb = viewModel.getState().feedback) {
                        PlaylistFeedback.EmptyName -> activity.getString(R.string.playlist_error_empty_name)
                        is PlaylistFeedback.DuplicateName -> activity.getString(R.string.playlist_error_duplicate_name, fb.name)
                        PlaylistFeedback.NameTooLong -> activity.getString(R.string.playlist_error_name_too_long)
                        else -> null
                    }
                    if (msg != null) Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearFeedback()
                    false
                }
            }
        }

        refreshChoices()

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_choose_playlist_title)
            .setView(view)
            .setPositiveButton(R.string.action_done) { _, _ ->
                viewModel.updateTrackMemberships(track.id, selectedIds)
                onCompleted()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}

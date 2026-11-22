package com.genova.musicplayer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.genova.musicplayer.R
import com.genova.musicplayer.domain.folder.AuthorizedFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolderListAdapter(
    private val context: Context,
    private val onRemoveClicked: (AuthorizedFolder) -> Unit
) : BaseAdapter() {

    private val items = mutableListOf<AuthorizedFolder>()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    fun submitList(folders: List<AuthorizedFolder>) {
        items.clear()
        items.addAll(folders)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): AuthorizedFolder = items[position]

    override fun getItemId(position: Int): Long = items[position].normalizedPath.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false)
            holder = ViewHolder(
                name = view.findViewById(R.id.tv_folder_name),
                count = view.findViewById(R.id.tv_folder_count),
                path = view.findViewById(R.id.tv_folder_path),
                added = view.findViewById(R.id.tv_folder_added),
                removeButton = view.findViewById(R.id.btn_remove_folder)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val folder = items[position]
        holder.name.text = folder.displayName
        holder.count.text = context.getString(R.string.library_count_format, folder.trackCount)
        holder.path.text = folder.normalizedPath
        val formattedDate = if (folder.addedAtEpochMs > 0L) {
            dateFormatter.format(Date(folder.addedAtEpochMs))
        } else {
            "-"
        }
        holder.added.text = context.getString(R.string.folder_added_at, formattedDate)
        holder.removeButton.setOnClickListener { onRemoveClicked(folder) }

        return view
    }

    private class ViewHolder(
        val name: TextView,
        val count: TextView,
        val path: TextView,
        val added: TextView,
        val removeButton: ImageButton
    )
}

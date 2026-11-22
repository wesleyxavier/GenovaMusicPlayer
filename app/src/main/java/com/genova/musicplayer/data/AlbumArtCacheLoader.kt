package com.genova.musicplayer.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.genova.musicplayer.R
import java.util.concurrent.Executors

/**
 * Asynchronous album art loader backed by LruCache and a bounded ExecutorService.
 * Never decodes bitmaps on the main thread.
 */
object AlbumArtCacheLoader {

    private val maxKb = (Runtime.getRuntime().maxMemory() / 1024L).toInt()
    private val cacheSizeKb = (maxKb / 8).coerceAtLeast(4 * 1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return (bitmap.byteCount / 1024).coerceAtLeast(1)
        }
    }

    private val executor = Executors.newFixedThreadPool(3)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadThumb(context: Context, contentUri: String, targetSizePx: Int, imageView: ImageView) {
        val key = "$contentUri#$targetSizePx"
        imageView.tag = key

        val cached = memoryCache.get(key)
        if (cached != null && !cached.isRecycled) {
            imageView.setImageBitmap(cached)
            imageView.setPadding(0, 0, 0, 0)
            return
        }

        imageView.setImageResource(R.drawable.ic_music_note)
        val appContext = context.applicationContext

        executor.execute {
            val bitmap = decodeSampledArt(appContext, contentUri, targetSizePx)
            if (bitmap != null) {
                memoryCache.put(key, bitmap)
                mainHandler.post {
                    if (imageView.tag == key) {
                        imageView.setImageBitmap(bitmap)
                        imageView.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }
    }

    private fun decodeSampledArt(context: Context, contentUri: String, targetSizePx: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(contentUri))
            val bytes = retriever.embeddedPicture ?: return null

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, targetSizePx)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqSize: Int): Int {
        var inSampleSize = 1
        if (height > reqSize || width > reqSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqSize && (halfWidth / inSampleSize) >= reqSize) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

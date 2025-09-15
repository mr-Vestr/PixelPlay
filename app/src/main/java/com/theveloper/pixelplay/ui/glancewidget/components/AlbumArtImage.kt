package com.theveloper.pixelplay.ui.glancewidget.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import com.theveloper.pixelplay.R
import timber.log.Timber

private const val TAG_AAIG = "AlbumArtImageGlance"

internal object AlbumArtBitmapCache {
    private const val CACHE_SIZE_BYTES = 4 * 1024 * 1024 // 4 MiB
    private val lruCache = object : LruCache<String, Bitmap>(CACHE_SIZE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun getBitmap(key: String): Bitmap? = lruCache.get(key)

    fun putBitmap(key: String, bitmap: Bitmap) {
        if (getBitmap(key) == null) {
            lruCache.put(key, bitmap)
        }
    }

    fun getKey(byteArray: ByteArray): String {
        return byteArray.contentHashCode().toString()
    }
}

@Composable
fun AlbumArtImageGlance(
    bitmapData: ByteArray?,
    size: Dp? = null,
    context: Context,
    modifier: GlanceModifier = GlanceModifier,
    cornerRadius: Dp = 16.dp
) {
    Timber.tag(TAG_AAIG)
        .d("Init. bitmapData is null: ${bitmapData == null}. Requested Dp size: $size")
    if (bitmapData != null) Timber.tag(TAG_AAIG).d("bitmapData size: ${bitmapData.size} bytes")

    val sizingModifier = if (size != null) modifier.size(size) else modifier
    val widgetDpSize = LocalSize.current // Get the actual size of the composable

    val imageProvider = bitmapData?.let { data ->
        val cacheKey = AlbumArtBitmapCache.getKey(data)
        var bitmap = AlbumArtBitmapCache.getBitmap(cacheKey)

        if (bitmap != null) {
            Timber.tag(TAG_AAIG).d("Bitmap cache HIT for key: $cacheKey. Using cached bitmap.")
        } else {
            Timber.tag(TAG_AAIG).d("Bitmap cache MISS for key: $cacheKey. Decoding new bitmap.")
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(data, 0, data.size, options)
                Timber.tag(TAG_AAIG).d("Initial bounds: ${options.outWidth}x${options.outHeight}")

                val imageHeight = options.outHeight
                val imageWidth = options.outWidth
                var inSampleSize = 1

                // Determine target size in pixels
                val targetWidthPx: Int
                val targetHeightPx: Int
                with(context.resources.displayMetrics) {
                    if (size != null) {
                        // If size is provided, use it for both width and height (maintains square logic)
                        val targetSizePx = (size.value * density).toInt()
                        targetWidthPx = targetSizePx
                        targetHeightPx = targetSizePx
                        Timber.tag(TAG_AAIG).d("Target Px size for Dp $size: $targetSizePx")
                    } else {
                        // If size is not provided, use the actual widget size
                        targetWidthPx = (widgetDpSize.width.value * density).toInt()
                        targetHeightPx = (widgetDpSize.height.value * density).toInt()
                        Timber.tag(TAG_AAIG)
                            .d("Target Px size from widget DpSize ${widgetDpSize}: ${targetWidthPx}x${targetHeightPx}")
                    }
                }

                if (imageHeight > targetHeightPx || imageWidth > targetWidthPx) {
                    val halfHeight: Int = imageHeight / 2
                    val halfWidth: Int = imageWidth / 2
                    // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                    // height and width larger than the requested height and width.
                    while (halfHeight / inSampleSize >= targetHeightPx && halfWidth / inSampleSize >= targetWidthPx) {
                        inSampleSize *= 2
                    }
                }
                Timber.tag(TAG_AAIG).d("Calculated inSampleSize: $inSampleSize")

                options.inSampleSize = inSampleSize
                options.inJustDecodeBounds = false
                val sampledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

                if (sampledBitmap == null) {
                    Timber.tag(TAG_AAIG)
                        .e("BitmapFactory.decodeByteArray returned null after sampling.")
                    return@let null
                }
                Timber.tag(TAG_AAIG)
                    .d("Sampled bitmap size: ${sampledBitmap.width}x${sampledBitmap.height}")

                bitmap = sampledBitmap

                Timber.tag(TAG_AAIG)
                    .d("Final bitmap size: ${bitmap.width}x${bitmap.height}. Putting into cache with key: $cacheKey")
                bitmap.let { AlbumArtBitmapCache.putBitmap(cacheKey, it) }

            } catch (e: Exception) {
                Timber.tag(TAG_AAIG).e(e, "Error decoding or scaling bitmap: ${e.message}")
                bitmap = null
            }
        }
        bitmap?.let { ImageProvider(it) }
    }

    Box(
        modifier = sizingModifier
    ) {
        if (imageProvider != null) {
            Image(
                provider = imageProvider,
                contentDescription = "Album Art",
                modifier = GlanceModifier.fillMaxSize().cornerRadius(cornerRadius),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder with tint
            Image(
                provider = ImageProvider(R.drawable.rounded_album_24),
                contentDescription = "Album Art Placeholder",
                modifier = GlanceModifier.fillMaxSize().cornerRadius(cornerRadius),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
            )
        }
    }
}

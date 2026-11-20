package com.example.moes

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Utility helper to bind product images from HTTP URLs or local drawable resources.
 */
fun bindProductImage(context: Context, imageUrl: String, imageView: ImageView) {
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.jewelry_1)
            .error(R.drawable.jewelry_1)
            .into(imageView)
    } else if (imageUrl.isNotEmpty()) {
        val resId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
        if (resId != 0) {
            imageView.setImageResource(resId)
        } else {
            imageView.setImageResource(R.drawable.jewelry_1)
        }
    } else {
        imageView.setImageResource(R.drawable.jewelry_1)
    }
}

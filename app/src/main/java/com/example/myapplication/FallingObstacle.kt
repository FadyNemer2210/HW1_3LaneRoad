package com.example.myapplication

import android.widget.ImageView

data class FallingObstacle(
    val imageView: ImageView,
    var row: Int,
    var lane: Int
)
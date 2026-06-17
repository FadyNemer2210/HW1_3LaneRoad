package com.example.myapplication

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.media.MediaPlayer

class GameManager(
    private val context: Context,
    private val playerLane: () -> Int,
    private val obstacles: List<FallingObstacle>,
    private val life1: ImageView,
    private val life2: ImageView,
    private val life3: ImageView,
    private val onGameTick: () -> Unit,
    private val onGameOver: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())

    private var lives = 3
    private var isRunning = false
    private var canCrash = true
    private var tickSpeed = 300L
    private val playerRow = 5

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            onGameTick()
            checkCollisionByMatrix()

            handler.postDelayed(this, tickSpeed)
        }
    }

    private fun checkCollisionByMatrix() {
        if (!canCrash) return

        for (obstacle in obstacles) {
            if (
                obstacle.lane == playerLane() &&
                obstacle.row >= playerRow &&
                obstacle.row <= playerRow + 1
            ) {
                handleCrash(obstacle)
                break
            }
        }
    }

    private fun handleCrash(obstacle: FallingObstacle) {
        canCrash = false
        lives--

        Toast.makeText(context, "Bottle incoming 😂", Toast.LENGTH_SHORT).show()
        vibrate()
        playCrashSound()
        updateLivesUI()

        obstacle.row = -4

        if (lives == 0) {
            stopGame()
            onGameOver()
        } else {
            handler.postDelayed({
                canCrash = true
            }, 400)
        }
    }

    private fun updateLivesUI() {
        life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    fun restartGame() {
        lives = 3
        canCrash = true
        updateLivesUI()
        startGame()
    }

    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(200)
        }
    }

    fun startGame() {
        if (isRunning) return
        isRunning = true
        handler.post(gameRunnable)
    }

    fun stopGame() {
        isRunning = false
        handler.removeCallbacks(gameRunnable)
    }

    fun setSpeed(speed: Long) {
        tickSpeed = speed
    }

    private fun playCrashSound() {

        val mediaPlayer =
            MediaPlayer.create(
                context,
                R.raw.crash_sound
            )

        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }

}
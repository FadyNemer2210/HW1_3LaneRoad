package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gameLayout: FrameLayout
    private lateinit var player: ImageView
    private lateinit var leftButton: ImageView
    private lateinit var rightButton: ImageView

    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView

    private lateinit var gameOverLayout: View
    private lateinit var startOverButton: Button

    private lateinit var gameManager: GameManager

    private val obstacles = mutableListOf<FallingObstacle>()

    private var currentLane = 1

    private val rows = 7
    private val lanes = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupButtons()

        gameManager = GameManager(
            context = this,
            playerLane = { currentLane },
            obstacles = obstacles,
            life1 = life1,
            life2 = life2,
            life3 = life3,
            onGameTick = { moveObstaclesByMatrix() },
            onGameOver = { showGameOver() }
        )

        gameLayout.post {
            movePlayerToLane()
            createObstacles()
            gameManager.startGame()
        }
    }

    private fun initViews() {
        gameLayout = findViewById(R.id.gameLayout)
        player = findViewById(R.id.player)
        leftButton = findViewById(R.id.leftButton)
        rightButton = findViewById(R.id.rightButton)

        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)

        gameOverLayout = findViewById(R.id.gameOverLayout)
        startOverButton = findViewById(R.id.startOverButton)
    }

    private fun setupButtons() {
        leftButton.setOnClickListener {
            if (currentLane > 0) {
                currentLane--
                movePlayerToLane()
            }
        }

        rightButton.setOnClickListener {
            if (currentLane < lanes - 1) {
                currentLane++
                movePlayerToLane()
            }
        }

        startOverButton.setOnClickListener {
            gameOverLayout.visibility = View.GONE
            resetAllObstacles()
            gameManager.restartGame()
        }
    }

    private fun createObstacles() {
        repeat(3) { index ->
            val image = ImageView(this)
            val size = resources.getDimensionPixelSize(R.dimen.ucl_trophy_size)

            image.layoutParams = FrameLayout.LayoutParams(size, size)
            image.setImageResource(R.drawable.ucl_trophy)
            image.scaleType = ImageView.ScaleType.FIT_CENTER

            gameLayout.addView(image)

            val obstacle = FallingObstacle(
                imageView = image,
                row = -index * 3,
                lane = Random.nextInt(0, lanes)
            )

            obstacles.add(obstacle)
            drawObstacle(obstacle)
        }
    }

    private fun moveObstaclesByMatrix() {
        for (obstacle in obstacles) {
            obstacle.row++

            if (obstacle.row > rows) {
                resetObstacle(obstacle)
            }

            drawObstacle(obstacle)
        }
    }

    private fun resetObstacle(obstacle: FallingObstacle) {
        obstacle.row = -Random.nextInt(2, 6)
        obstacle.lane = Random.nextInt(0, lanes)
        drawObstacle(obstacle)
    }

    private fun resetAllObstacles() {
        obstacles.forEachIndexed { index, obstacle ->
            obstacle.row = -index * 3
            obstacle.lane = Random.nextInt(0, lanes)
            drawObstacle(obstacle)
        }
    }

    private fun movePlayerToLane() {
        val laneWidth = gameLayout.width / lanes
        val playerCenterX = laneWidth * currentLane + laneWidth / 2
        player.x = (playerCenterX - player.width / 2).toFloat()
    }

    private fun drawObstacle(obstacle: FallingObstacle) {
        val laneWidth = gameLayout.width / lanes
        val rowHeight = gameLayout.height / rows

        val centerX = laneWidth * obstacle.lane + laneWidth / 2
        val centerY = rowHeight * obstacle.row + rowHeight / 2

        obstacle.imageView.x = (centerX - obstacle.imageView.width / 2).toFloat()
        obstacle.imageView.y = (centerY - obstacle.imageView.height / 2).toFloat()
    }

    private fun showGameOver() {
        gameOverLayout.visibility = View.VISIBLE
    }
}
package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.random.Random
import android.app.AlertDialog

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

    private lateinit var backToMenuButton: Button

    private lateinit var menuLayout: View

    private lateinit var slowModeButton: Button

    private lateinit var fastModeButton: Button

    private lateinit var sensorModeButton: Button

    private var gameSpeed = 300L

    private lateinit var scoreText: TextView

    private var score = 0

    private lateinit var collectSound: android.media.MediaPlayer
    private lateinit var distanceText: TextView

    private var distance = 0
    private lateinit var sensorManager: SensorManager
    private var lastMoveTime = 0L
    private var sensorMode = false
    private lateinit var gameManager: GameManager

    private val obstacles = mutableListOf<FallingObstacle>()

    private val coins = mutableListOf<Coin>()

    private var currentLane = 2

    private val rows = 7
    private val lanes = 5

    private val sensorListener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent?) {

            if (!sensorMode) return

            val x = event?.values?.get(0) ?: return

            val currentTime = System.currentTimeMillis()

            if (currentTime - lastMoveTime < 500)
                return

            if (x > 3) {
                if (currentLane > 0) {
                    currentLane--
                    movePlayerToLane()
                    lastMoveTime = currentTime
                }
            }

            if (x < -3) {
                if (currentLane < lanes - 1) {
                    currentLane++
                    movePlayerToLane()
                    lastMoveTime = currentTime
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        updateScore()
        updateDistance()
        collectSound = android.media.MediaPlayer.create(this, R.raw.trophy_collect_sound)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
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
            createCoin()
        }
    }

    private fun initViews() {
        gameLayout = findViewById(R.id.gameLayout)
        player = findViewById(R.id.player)
        leftButton = findViewById(R.id.leftButton)
        rightButton = findViewById(R.id.rightButton)

        menuLayout = findViewById(R.id.menuLayout)
        backToMenuButton = findViewById(R.id.backToMenuButton)
        slowModeButton = findViewById(R.id.slowModeButton)
        fastModeButton = findViewById(R.id.fastModeButton)
        sensorModeButton = findViewById(R.id.sensorModeButton)

        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)
        scoreText = findViewById(R.id.scoreText)
        distanceText = findViewById(R.id.distanceText)
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

        slowModeButton.setOnClickListener {
            stopSensorMode()

            gameSpeed = 450L
            menuLayout.visibility = View.GONE

            resetGameValues()

            gameManager.setSpeed(gameSpeed)
            gameManager.restartGame()
        }

        fastModeButton.setOnClickListener {
            stopSensorMode()

            gameSpeed = 300L
            menuLayout.visibility = View.GONE

            resetGameValues()

            gameManager.setSpeed(gameSpeed)
            gameManager.restartGame()
        }

        sensorModeButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sensor Mode")
                .setMessage("Choose sensor speed")
                .setPositiveButton("Fast") { _, _ ->
                    startSensorGame(300L)
                }
                .setNegativeButton("Slow") { _, _ ->
                    startSensorGame(450L)
                }
                .show()
        }
        startOverButton.setOnClickListener {
            gameOverLayout.visibility = View.GONE

            resetGameValues()
            gameManager.restartGame()
        }

        backToMenuButton.setOnClickListener {
            gameManager.stopGame()
            stopSensorMode()

            gameOverLayout.visibility = View.GONE
            menuLayout.visibility = View.VISIBLE

            resetGameValues()
        }
    }

    private fun startSensorGame(speed: Long) {
        gameSpeed = speed

        menuLayout.visibility = View.GONE
        gameOverLayout.visibility = View.GONE

        resetGameValues()

        gameManager.setSpeed(gameSpeed)
        startSensorMode()
        gameManager.restartGame()
    }

    private fun stopSensorMode() {
        sensorMode = false
        sensorManager.unregisterListener(sensorListener)

        leftButton.visibility = View.VISIBLE
        rightButton.visibility = View.VISIBLE
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

    private fun createCoin() {
        val image = ImageView(this)

        val size = resources.getDimensionPixelSize(R.dimen.life_ball_size)

        image.layoutParams = FrameLayout.LayoutParams(size, size)
        image.setImageResource(R.drawable.coin)
        image.scaleType = ImageView.ScaleType.FIT_CENTER

        gameLayout.addView(image)

        val coin = Coin(
            imageView = image,
            row = -4,
            lane = Random.nextInt(0, lanes)
        )

        coins.add(coin)
        drawCoin(coin)
    }

    private fun moveObstaclesByMatrix() {
        distance += 1
        updateDistance()
        for (obstacle in obstacles) {
            obstacle.row++

            if (obstacle.row > rows) {
                resetObstacle(obstacle)
            }

            drawObstacle(obstacle)
        }

        for (coin in coins) {
            coin.row++

            if (coin.row > rows) {
                resetCoin(coin)
            }

            drawCoin(coin)
        }

        checkCoinsCollection()
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

    private fun drawCoin(coin: Coin) {
        val laneWidth = gameLayout.width / lanes
        val rowHeight = gameLayout.height / rows

        val centerX = laneWidth * coin.lane + laneWidth / 2
        val centerY = rowHeight * coin.row + rowHeight / 2

        coin.imageView.x = (centerX - coin.imageView.width / 2).toFloat()
        coin.imageView.y = (centerY - coin.imageView.height / 2).toFloat()
    }

    private fun checkCoinsCollection() {
        for (coin in coins) {
            if (
                coin.lane == currentLane &&
                coin.row >= 5 &&
                coin.row <= 6
            ) {
                score++
                updateScore()
                playCollectSound()

                resetCoin(coin)
            }
        }
    }

    private fun playCollectSound() {

        collectSound.seekTo(0)
        collectSound.start()

    }

    private fun resetObstacle(obstacle: FallingObstacle) {
        obstacle.row = -Random.nextInt(2, 6)
        obstacle.lane = Random.nextInt(0, lanes)
        drawObstacle(obstacle)
    }

    private fun resetCoin(coin: Coin) {
        do {
            coin.row = -Random.nextInt(3, 8)
            coin.lane = Random.nextInt(0, lanes)
        } while (isCellOccupied(coin.row, coin.lane))

        drawCoin(coin)
    }

    private fun showGameOver() {
        gameOverLayout.visibility = View.VISIBLE
    }

    private fun startSensorMode() {

        sensorMode = true

        leftButton.visibility = View.GONE
        rightButton.visibility = View.GONE

        val accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun isCellOccupied(row: Int, lane: Int): Boolean {

        for (obstacle in obstacles) {
            if (obstacle.row == row && obstacle.lane == lane) {
                return true
            }
        }

        return false
    }


    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(sensorListener)
    }

    private fun updateScore() {
        scoreText.text = "Coins: $score"
    }

    private fun updateDistance() {
        distanceText.text = "Distance: " + distance + "m"
    }

    private fun resetCoins() {
        for (coin in coins) {
            resetCoin(coin)
        }
    }
    private fun resetGameValues() {
        score = 0
        updateScore()

        distance = 0
        updateDistance()

        currentLane = 2
        movePlayerToLane()

        resetAllObstacles()
        resetCoins()
    }
}
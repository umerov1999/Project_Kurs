package dev.umerov.project.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.settings.Settings
import dev.umerov.project.settings.theme.ThemesController
import dev.umerov.project.util.Utils
import dev.umerov.project.view.snake.Delegate
import dev.umerov.project.view.snake.Direction
import dev.umerov.project.view.snake.SnakeModel
import dev.umerov.project.view.snake.SnakeView
import java.util.concurrent.atomic.AtomicReference

class SnakeActivity : AppCompatActivity(), Delegate, SensorEventListener {
    private var snakeView: SnakeView? = null

    @get:Synchronized
    override val snake = SnakeModel()
    private val gameOn = AtomicReference<Boolean>()
    private var gameEngine: Thread? = null
    private var scoreView: MaterialTextView? = null
    private var highScoreView: MaterialTextView? = null

    private var SM: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private val audios = ArrayList<Int>()

    private var soundPool: SoundPool? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemesController.currentStyle())
        Utils.prepareDensity(this)
        setContentView(R.layout.activity_snake)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.decorView.layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        savedInstanceState?.let {
            it.getParcelableCompat<SnakeModel>("snake")?.let { st ->
                snake.copy(st)
            }
        }

        snakeView = findViewById(R.id.snakeView)
        snakeView?.delegate = this

        val btnUp = findViewById<ImageButton>(R.id.arrow_up)
        val btnDown = findViewById<ImageButton>(R.id.arrow_down)
        val btnLeft = findViewById<ImageButton>(R.id.arrow_left)
        val btnRight = findViewById<ImageButton>(R.id.arrow_right)
        val btnReset = findViewById<ImageButton>(R.id.restartButton)
        scoreView = findViewById(R.id.score)
        highScoreView = findViewById(R.id.highScore)

        btnUp.setOnClickListener { moveUp() }
        btnDown.setOnClickListener { moveDown() }
        btnLeft.setOnClickListener { moveLeft() }
        btnRight.setOnClickListener { moveRight() }
        btnReset.setOnClickListener {
            snake.reset()
            highScoreView?.text = getString(R.string.high_score, snake.highScore)
            scoreView?.text = getString(R.string.score, snake.score)
        }

        if (Settings.get().main().enableAccelerometer) {
            SM = getSystemService(SENSOR_SERVICE) as SensorManager?
            accelerometerSensor = SM?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        val values = event.values
        if (resources.configuration.orientation ==
            Configuration.ORIENTATION_PORTRAIT
        ) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    if (values[0] > 4.0f) {
                        moveLeft()
                    } else {
                        if (values[0] < -4.0f) {
                            moveRight()
                        }
                    }
                }
            }
        } else {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    if (values[1] > 4f) {
                        moveRight()
                    } else if (values[1] < -4f) {
                        moveLeft()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("snake", snake)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    private fun startGame() {
        highScoreView?.text = getString(R.string.high_score, snake.highScore)
        scoreView?.text = getString(R.string.score, snake.score)

        snakeView?.draw()
        gameOn.set(true)
        gameEngine = Thread {
            try {
                while (gameOn.get()) {
                    if (snake.isGameOver || snakeView?.isNeedChange() == false) {
                        snakeView?.draw()
                        continue
                    }

                    snake.move()

                    if (snakeView?.isSnakeBite == true) {
                        snakeView?.refresh()
                        snake.extend()
                        snake.score++
                        runOnUiThread {
                            scoreView?.text = getString(R.string.score, snake.score)
                            if (audios.size >= 2) {
                                soundPool?.play(
                                    audios[0],
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    1,
                                    0,
                                    1f
                                )
                            }
                        }
                    }

                    if (snakeView?.isDistance == true) {
                        snake.isGameOver = true
                        if (snake.score > snake.highScore) {
                            Settings.get().main().snakeScore = snake.score
                        }
                        runOnUiThread {
                            if (audios.size >= 2) {
                                soundPool?.play(
                                    audios[1],
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    1,
                                    0,
                                    1f
                                )
                            }
                        }
                    }

                    if (snakeView?.collision() == true) {
                        snake.isGameOver = true
                        if (snake.score > snake.highScore) {
                            Settings.get().main().snakeScore = snake.score
                        }
                        runOnUiThread {
                            if (audios.size >= 2) {
                                soundPool?.play(
                                    audios[1],
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    Settings.get().main().snakeVolumeValue.toFloat() / 10,
                                    1,
                                    0,
                                    1f
                                )
                            }
                        }
                    }

                    snakeView?.draw()
                }
            } catch (ignore: Exception) {
            }
        }
        gameEngine?.start()
    }

    override fun moveUp() {
        if (snake.direction != Direction.UP && snake.direction != Direction.DOWN) {
            snake.moveUp()
        }
    }

    override fun moveDown() {
        if (snake.direction != Direction.DOWN && snake.direction != Direction.UP) {
            snake.moveDown()
        }
    }

    override fun moveLeft() {
        if (snake.direction != Direction.LEFT && snake.direction != Direction.RIGHT) {
            snake.moveLeft()
        }
    }

    override fun moveRight() {
        if (snake.direction != Direction.RIGHT && snake.direction != Direction.LEFT) {
            snake.moveRight()
        }
    }

    override fun onPause() {
        SM?.unregisterListener(this)
        gameOn.set(false)
        gameEngine?.join()
        gameEngine = null

        soundPool?.release()
        soundPool = null
        audios.clear()
        super.onPause()
    }

    override fun onSurfaceCreated(pixelFormat: Int, width: Int, height: Int) {
        soundPool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

        try {
            soundPool?.let {
                audios.add(it.load(assets.openFd("1.ogg"), 1))
                audios.add(it.load(assets.openFd("2.ogg"), 1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startGame()
        SM?.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, SnakeActivity::class.java)
        }
    }
}

package com.example.kviddicsstopper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.concurrent.thread

class EasterEgg : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FlappyView(this))
    }
}

class FlappyView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var birdY = 500f
    private var velocity = 0f
    private val gravity = 1f
    private val lift = -20f
    private var isPlaying = false
    private var isGameOver = false

    private var pipeX = 1200f
    private var pipeWidth = 200f
    private var pipeGap = 400f // Widened for easier gameplay
    private var pipeTopHeight = Random.nextInt(200, 800).toFloat()

    private var score = 0
    private var bestScore = 0
    private val prefs = context.getSharedPreferences("flappyBirdPrefs", Context.MODE_PRIVATE)

    private val scorePaint = Paint().apply {
        color = Color.BLACK
        textSize = 64f
        typeface = Typeface.DEFAULT_BOLD
    }

    init {
        holder.addCallback(this)
        bestScore = prefs.getInt("BEST_SCORE", 0)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Thread {
            while (true) {
                if (holder.surface.isValid) {
                    val canvas = holder.lockCanvas()
                    update()
                    drawGame(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(16)
            }
        }.start()
    }

    private fun update() {
        if (!isPlaying || isGameOver) return

        velocity += gravity
        birdY += velocity

        // Move pipe
        pipeX -= 10f
        if (pipeX < -pipeWidth) {
            pipeX = width.toFloat()
            pipeTopHeight = Random.nextInt(200, height - 400).toFloat()
            score++
        }

        // Collision detection
        val birdRadius = 50f
        val birdX = 200f

        val hitsPipeX = birdX + birdRadius > pipeX && birdX - birdRadius < pipeX + pipeWidth
        val hitsPipeY = birdY - birdRadius < pipeTopHeight || birdY + birdRadius > pipeTopHeight + pipeGap

        if ((hitsPipeX && hitsPipeY) || birdY + birdRadius > height || birdY - birdRadius < 0) {
            isGameOver = true
            isPlaying = false

            if (score > bestScore) {
                bestScore = score
                prefs.edit().putInt("BEST_SCORE", bestScore).apply()
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.CYAN)
        val paint = Paint()

        // Bird
        paint.color = Color.YELLOW
        canvas.drawCircle(200f, birdY, 50f, paint)

        // Pipes
        paint.color = Color.GREEN
        canvas.drawRect(pipeX, 0f, pipeX + pipeWidth, pipeTopHeight, paint)
        canvas.drawRect(pipeX, pipeTopHeight + pipeGap, pipeX + pipeWidth, height.toFloat(), paint)

        // Score display
        canvas.drawText("Score: $score", 100f, 100f, scorePaint)
        canvas.drawText("Best: $bestScore", 100f, 180f, scorePaint)

        // Start / End Screens
        paint.color = Color.BLACK
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER

        if (!isPlaying && !isGameOver) {
            canvas.drawText("Tap to Start", width / 2f, height / 2f-200, paint)
            canvas.drawText("Please send best"
                , width / 2f, height / 2f + 100, paint)
            canvas.drawText("score screenshots to", width / 2f, height / 2f + 200, paint)
            canvas.drawText("geridamanager@gmail.com", width / 2f, height / 2f + 300, paint)
            canvas.drawText("Winning player will be", width / 2f, height / 2f + 500, paint)
            canvas.drawText("rewarded at the end of every", width / 2f, height / 2f + 600, paint)
            canvas.drawText("season (annual winter camp)", width / 2f, height / 2f + 700, paint)
        }

        if (isGameOver) {
            //canvas.drawText("Game Over", width / 2f, height / 2f-400, paint)
            canvas.drawText("Play Again", width / 2f, height / 2f + 150, paint)
            canvas.drawText("Exit", width / 2f, height / 2f - 200, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (!isPlaying && !isGameOver) {
                isPlaying = true
                birdY = 500f
                velocity = 0f
                score = 0
                pipeX = width.toFloat()
                pipeTopHeight = Random.nextInt(200, height - 400).toFloat()
            } else if (isGameOver) {
                val x = event.x
                val y = event.y

                if (y > height / 2f -50 && y < height / 2f + 300) {
                    // Play Again
                    isGameOver = false
                    isPlaying = false
                    birdY = 500f
                    velocity = 0f
                    score = 0
                    pipeX = width.toFloat()
                    pipeTopHeight = Random.nextInt(200, height - 400).toFloat()
                } else if (y <= height / 2f - 100 && y > height / 2f - 400) {
                    // Exit
                    (context as? android.app.Activity)?.finish()
                }
            } else {
                velocity = lift
            }
        }
        return true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}

class FlappyViewZen(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var birdY = 500f
    private var velocity = 0f
    private val gravity = 1f
    private val lift = -20f
    private var isPlaying = false
    private var isGameOver = false

    private var pipeX = 1200f
    private var pipeWidth = 200f
    private var pipeGap = 400f // Widened for easier gameplay
    private var pipeTopHeight = Random.nextInt(200, 800).toFloat()

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread {
            while (true) {
                if (holder.surface.isValid) {
                    val canvas = holder.lockCanvas()
                    update()
                    drawGame(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(16)
            }
        }
    }

    private fun update() {
        if (!isPlaying || isGameOver) return

        velocity += gravity
        birdY += velocity

        // Move pipe
        pipeX -= 10f
        if (pipeX < -pipeWidth) {
            pipeX = width.toFloat()
            pipeTopHeight = Random.nextInt(200, height - 400).toFloat()
        }

        // Collision detection
        val birdRadius = 50f
        val birdX = 200f

        val hitsPipeX = birdX + birdRadius > pipeX && birdX - birdRadius < pipeX + pipeWidth
        val hitsPipeY = birdY - birdRadius < pipeTopHeight || birdY + birdRadius > pipeTopHeight + pipeGap

        if (hitsPipeX && hitsPipeY || birdY + birdRadius > height || birdY - birdRadius < 0) {
            isGameOver = true
            isPlaying = false
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.CYAN)
        val paint = Paint()

        // Bird
        paint.color = Color.YELLOW
        canvas.drawCircle(200f, birdY, 50f, paint)

        // Pipes
        paint.color = Color.GREEN
        canvas.drawRect(pipeX, 0f, pipeX + pipeWidth, pipeTopHeight, paint)
        canvas.drawRect(pipeX, pipeTopHeight + pipeGap, pipeX + pipeWidth, height.toFloat(), paint)

        // Start / End Screens
        paint.color = Color.BLACK
        paint.textSize = 80f
        paint.textAlign = Paint.Align.CENTER

        if (!isPlaying && !isGameOver) {
            canvas.drawText("Tap to Start", width / 2f, height / 2f, paint)
        }

        if (isGameOver) {
            canvas.drawText("", width / 2f, height / 2f, paint)
            canvas.drawText("Play Again", width / 2f, height / 2f + 100, paint)
            canvas.drawText("Exit", width / 2f, height / 2f - 200, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (!isPlaying && !isGameOver) {
                isPlaying = true
                birdY = 500f
                velocity = 0f
            } else if (isGameOver) {
                val x = event.x
                val y = event.y
                val centerX = width / 2f

                if (y > height / 2f && y < height / 2f + 100) {
                    // Play Again area
                    isGameOver = false
                    isPlaying = false
                    birdY = 500f
                    velocity = 0f
                    pipeX = width.toFloat()
                } else if (y > height / 2f + 100) {
                    // Exit area — call activity finish
                    (context as? android.app.Activity)?.finish()
                }
            } else {
                velocity = lift
            }
        }
        return true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}

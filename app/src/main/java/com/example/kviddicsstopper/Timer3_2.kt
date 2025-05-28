package com.example.kviddicsstopper

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.widget.Button
import android.widget.Toast
import android.os.VibrationEffect
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.TextView
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val activeTimers = mutableListOf<CountDownTimer>()

class Timer3_2 : AppCompatActivity() {
    private var timerDuration: Long = 25000 // 25 seconds
    private var vibrator: Vibrator? = null
    lateinit var b1: String
    lateinit var b2: String
    lateinit var b3: String
    private var stop = false
    private var devBool = false

    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"
    @SuppressLint("MissingInflatedId")

    var w = arrayOf(false, false, false)

    override fun onDestroy() {
        super.onDestroy()
        for (timer in activeTimers) {
            timer.cancel()
        }
        activeTimers.clear()
    }

    override fun onPause() {
        super.onPause()
        for (timer in activeTimers) {
            timer.cancel()
        }
        activeTimers.clear()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p3timer)

        //main
        val button1 = findViewById<Button>(R.id.start)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)

        val intent = intent

        val selectedcolour = intent.getStringExtra("c1") ?: "Diak"


        button1.setBackgroundColor(getColorFromName(selectedcolour))
        button2.setBackgroundColor(getColorFromName(selectedcolour))
        button3.setBackgroundColor(getColorFromName(selectedcolour))



        //input
        b1 = intent.getStringExtra("b1") ?: "Player 1"
        b2 = intent.getStringExtra("b2") ?: "Player 2"
        b3 = intent.getStringExtra("b3") ?: "Player 3"

        //aux label
        val name1 = findViewById<TextView>(R.id.name1)
        val name2 = findViewById<TextView>(R.id.name2)
        val name3 = findViewById<TextView>(R.id.name3)

        //time count
        val count1 = findViewById<TextView>(R.id.minutes)
        val count2 = findViewById<TextView>(R.id.count2)
        val count3 = findViewById<TextView>(R.id.count3)

        //pause
        val p1 = findViewById<Button>(R.id.pause)
        val p2 = findViewById<Button>(R.id.p2)
        val p3 = findViewById<Button>(R.id.p3)
        val pAll = findViewById<Button>(R.id.pAll)

        /*p1.setBackgroundColor(getColorFromName("White"))
        p2.setBackgroundColor(getColorFromName("White"))
        p3.setBackgroundColor(getColorFromName("White"))*/

        p1.setBackgroundColor(getColorFromName("Diak"))
        p2.setBackgroundColor(getColorFromName("Diak"))
        p3.setBackgroundColor(getColorFromName("Diak"))

        //reset
        val r1 = findViewById<Button>(R.id.reset)
        val r2 = findViewById<Button>(R.id.r2)
        val r3 = findViewById<Button>(R.id.r3)

        if (selectedcolour != "AS Gyerek" && selectedcolour != "Diak") {
            r1.setBackgroundColor(getColorFromName(selectedcolour))
            r2.setBackgroundColor(getColorFromName(selectedcolour))
            r3.setBackgroundColor(getColorFromName(selectedcolour))
        } else {
            r1.setBackgroundColor(getColorFromName("Tobimug"))
            r2.setBackgroundColor(getColorFromName("Tobimug"))
            r3.setBackgroundColor(getColorFromName("Tobimug"))
        }

        if (selectedcolour == "Tanar" || selectedcolour == "AS Vezeto") {
            button1.setTextColor(getColorFromName("White"))
            button2.setTextColor(getColorFromName("White"))
            button3.setTextColor(getColorFromName("White"))
            r1.setTextColor(getColorFromName("White"))
            r2.setTextColor(getColorFromName("White"))
            r3.setTextColor(getColorFromName("White"))
        }


        val cellToRead = "B3"
        if (cellToRead.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = readDataFromSheet(cellToRead) // Call read function
                    withContext(Dispatchers.Main) {
                        timerDuration = result.toLong()*1000
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Timer3_2,
                            "Error reading data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("ReadDataError", "Error reading data", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please enter a cell to read", Toast.LENGTH_SHORT).show()
        }

        button1.text = b1
        button2.text = b2
        button3.text = b3
        name1.text = b1
        name2.text = b2
        name3.text = b3

        //navigation
        val back = findViewById<Button>(R.id.navTimer)

        back.setOnClickListener {
            val intent = Intent(this, TimerMenu::class.java)
            intent.putExtra("b1", b1)
            intent.putExtra("b2", b2)
            intent.putExtra("b3", b3)
            intent.putExtra("c1", selectedcolour)
            startActivity(intent)
        }

        button1.setOnClickListener {
            startTimer(timerDuration, button1, b1, 1, name1, count1, p1, r1, back, pAll, p2, p3)
        }

        button2.setOnClickListener {
            startTimer(timerDuration, button2, b2, 2, name2, count2, p2, r2, back, pAll, p1, p3)
        }

        button3.setOnClickListener {
            startTimer(timerDuration, button3, b3, 3, name3, count3, p3, r3, back, pAll, p1, p2)
        }
    }

    private fun startTimer(duration: Long, button: Button, text: String, n: Int, name: TextView,
                           c: TextView, p: Button, r: Button, back: Button, pAll: Button,
                           pOther1: Button, pOther2: Button) {
        var currentTimer: CountDownTimer? = null
        var isPaused = false
        var isAllPaused = false
        var remainingTime: Long = duration
        currentTimer?.cancel()
        p.text = "Pause"
        stop = false
        name.text = text
        //back.visibility = View.GONE
        w[n-1] = true

        currentTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remainingTime = millisUntilFinished // Update remaining time
                    c.text = "${(millisUntilFinished / 1000)+1}"
                    active(button, name, c, p, r)
                }
                r.setOnClickListener {
                    w[n-1] = false
                    if (!w[0] && !w[1] && !w[2]) {
                        back.visibility = View.VISIBLE
                    }
                    currentTimer?.cancel()
                    onFinish()
                }
            }

            override fun onFinish() {
                w[n-1] = false
                if (!w[0] && !w[1] && !w[2]) {
                    back.visibility = View.VISIBLE
                }
                if (!isPaused) { // Only finish if not paused
                    button.text = text
                    w[n - 1] = false
                    inactive(button, name, c, p, r)
                    vibratePhone()

                }
            }
        }.start()

        activeTimers.add(currentTimer)

        pAll.setOnClickListener {
            if (!isAllPaused) {
                pAll.text = "Resume All"
                if (p.text == "Pause" && p.visibility == View.VISIBLE) {
                    p.performClick()
                }
                if (pOther1.text == "Pause"  && pOther1.visibility == View.VISIBLE) {
                    pOther1.performClick()
                }
                if (pOther2.text == "Pause"  && pOther2.visibility == View.VISIBLE) {
                    pOther2.performClick()
                }
            } else {
                pAll.text = "Pause All"
                if (p.text == "Resume" && p.visibility == View.VISIBLE) {
                    p.performClick()
                }
                if (pOther1.text == "Resume" && pOther1.visibility == View.VISIBLE) {
                    pOther1.performClick()
                }
                if (pOther2.text == "Resume" && pOther2.visibility == View.VISIBLE) {
                    pOther2.performClick()
                }
            }
            isAllPaused = !isAllPaused
        }

        p.setOnClickListener {
            if (!isPaused) {
                isPaused = true
                currentTimer?.cancel() // Pause the timer
                p.text = "Resume" // Change the button text
                r.setOnClickListener {
                    vibratePhone()
                    w[n-1] = false
                    if (!w[0] && !w[1] && !w[2]) {
                        back.visibility = View.VISIBLE
                    }
                    currentTimer?.cancel()
                    isPaused = false
                    inactive(button, name, c, p, r)
                }
            } else {
                isPaused = false
                p.text = "Pause" // Change the button text back
                // Create a new timer with the remaining time and start it
                currentTimer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        remainingTime = millisUntilFinished
                        c.text = "${(millisUntilFinished / 1000)+1}"
                        active(button, name, c, p, r)
                    }

                    override fun onFinish() {
                        button.text = text
                        w[n - 1] = false
                        inactive(button, name, c, p, r)
                        vibratePhone()
                    }
                }.start()
            }
        }
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // Vibrate for 500ms
        } else {
            // For older devices
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun active(button: Button, name: TextView, c: TextView, p: Button, r: Button) {
        button.visibility = View.GONE
        name.visibility = View.VISIBLE
        c.visibility = View.VISIBLE
        p.visibility = View.VISIBLE
        r.visibility = View.VISIBLE
    }

    private fun inactive(button: Button, name: TextView, c: TextView, p: Button, r: Button) {
        button.visibility = View.VISIBLE
        name.visibility = View.GONE
        c.visibility = View.GONE
        p.visibility = View.GONE
        r.visibility = View.GONE
    }

    private suspend fun readDataFromSheet(cellToRead: String): String { // Added return type
        var connection: HttpURLConnection? = null
        try {
            val encodedCell = URLEncoder.encode(cellToRead, "UTF-8")
            val urlString = "$webAppUrl?readCell=$encodedCell"  // Use readCell parameter
            val url = URL(urlString)

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }

            return if (responseCode == HttpURLConnection.HTTP_OK) {
                responseText // Return the text from the sheet
            } else {
                "Error reading data. HTTP Error: $responseCode, Response: $responseText"
            }
        } catch (e: IOException) {
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    private fun getColorFromName(colorName: String): Int {
        return when (colorName) { //  Use lowercase for easier comparison
            "Tanar", "AS Vezeto" -> Color.BLACK
            "Diak", "AS Gyerek", "" -> Color.LTGRAY //  "gray" is also a valid spelling
            "Zolg's" -> Color.MAGENTA // Color.PURPLE is available from API 26
            "Tobimug" -> Color.RED
            "Hifi" -> Color.GREEN
            "Hugrabug" -> Color.YELLOW
            "Astrocomic" -> Color.CYAN
            "White" -> Color.WHITE
            else -> Color.WHITE // Default color if the name doesn't match.  You can change this to any color.
        }
    }
}

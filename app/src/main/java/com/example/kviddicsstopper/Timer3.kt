package com.example.kviddicsstopper

/*import androidx.appcompat.app.AppCompatActivity
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
import android.widget.EditText
import android.widget.TextView*/

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button


class Timer3 : AppCompatActivity() {

    /*private var timerDuration: Long = 25000 // 25 seconds
    private var vibrator: Vibrator? = null
    lateinit var b1: String
    lateinit var b2: String
    lateinit var b3: String
    private var stop = false
    private var devBool = false

    var w = arrayOf(false, false, false)*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p3timer)

        //main
        /*val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)

        //input
        b1 = "Kacc"
        b2 = "Bazsi"
        b3 = "Andris"

        //aux label
        val name1 = findViewById<TextView>(R.id.name1)
        val name2 = findViewById<TextView>(R.id.name2)
        val name3 = findViewById<TextView>(R.id.name3)

        //time count
        val count1 = findViewById<TextView>(R.id.count1)
        val count2 = findViewById<TextView>(R.id.count2)
        val count3 = findViewById<TextView>(R.id.count3)

        //pause
        val p1 = findViewById<Button>(R.id.p1)
        val p2 = findViewById<Button>(R.id.p2)
        val p3 = findViewById<Button>(R.id.p3)
        val pAll = findViewById<Button>(R.id.pAll)

        //reset
        val r1 = findViewById<Button>(R.id.r1)
        val r2 = findViewById<Button>(R.id.r2)
        val r3 = findViewById<Button>(R.id.r3)

        button1.text = b1
        button2.text = b2
        button3.text = b3
        name1.text = b1
        name2.text = b2
        name3.text = b3*/

        //navigation
        val back = findViewById<Button>(R.id.navTimer)

        back.setOnClickListener {
            startActivity(Intent(this, TimerMenu::class.java))
        }

/*        button1.setOnClickListener {
            startTimer(timerDuration, button1, b1, 1, name1, count1, p1, r1, back)
        }

        button2.setOnClickListener {
            startTimer(timerDuration, button2, b2, 2, name2, count2, p2, r2, back)
        }

        button3.setOnClickListener {
            startTimer(timerDuration, button3, b3, 3, name3, count3, p3, r3, back)
        }*/

        /*progress.setOnClickListener {//game
            stop = true

            this.b1 = input1.text.toString()
            this.b2 = input2.text.toString()
            this.b3 = input3.text.toString()

            button1.text = input1.text.toString()
            button2.text = input2.text.toString()
            button3.text = input3.text.toString()

            //visible
            button1.visibility = View.VISIBLE
            button2.visibility = View.VISIBLE
            button3.visibility = View.VISIBLE
            back.visibility = View.VISIBLE

            //gone
            input1.visibility = View.GONE
            input2.visibility = View.GONE
            input3.visibility = View.GONE
            name1.visibility = View.GONE
            name2.visibility = View.GONE
            name3.visibility = View.GONE
            count1.visibility = View.GONE
            count2.visibility = View.GONE
            count3.visibility = View.GONE
            p1.visibility = View.GONE
            p2.visibility = View.GONE
            p3.visibility = View.GONE
            r1.visibility = View.GONE
            r2.visibility = View.GONE
            r3.visibility = View.GONE
        }
        back.setOnClickListener {//start
            //visible
            input1.visibility = View.VISIBLE
            input2.visibility = View.VISIBLE
            input3.visibility = View.VISIBLE

            //gone
            button1.visibility = View.GONE
            button2.visibility = View.GONE
            button3.visibility = View.GONE
            back.visibility = View.GONE
            name1.visibility = View.GONE
            name2.visibility = View.GONE
            name3.visibility = View.GONE
            count1.visibility = View.GONE
            count2.visibility = View.GONE
            count3.visibility = View.GONE
            p1.visibility = View.GONE
            p2.visibility = View.GONE
            p3.visibility = View.GONE
            r1.visibility = View.GONE
            r2.visibility = View.GONE
            r3.visibility = View.GONE
        }*/
    }

    /*private fun startTimer(duration: Long, button: Button, text: String, n: Int, name: TextView,
                           c: TextView, p: Button, r: Button, back: Button) {
        var currentTimer: CountDownTimer? = null
        var isPaused = false
        var remainingTime: Long = duration
        currentTimer?.cancel()
        p.text = "Pause"
        stop = false
        name.text = text
        back.visibility = View.GONE
        w[n-1] = true

        currentTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remainingTime = millisUntilFinished // Update remaining time
                    c.text = "${millisUntilFinished / 1000}"
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
                    Toast.makeText(this@Timer3, "Timer Finished!", Toast.LENGTH_SHORT).show()

                }
            }
        }.start()

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
                        c.text = "${millisUntilFinished / 1000}"
                        active(button, name, c, p, r)
                    }

                    override fun onFinish() {
                        button.text = text
                        w[n - 1] = false
                        inactive(button, name, c, p, r)
                        vibratePhone()
                        Toast.makeText(this@Timer3, "Timer Finished!", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        }
    }

    private fun vibratePhone() {
        vibrator?.let { // Safe call to only proceed if vibrator is not null
            // Check if the device has a vibrator
            if (it.hasVibrator()) {
                // Vibrate for 500 milliseconds (half a second)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // For older devices
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
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
    }*/
}
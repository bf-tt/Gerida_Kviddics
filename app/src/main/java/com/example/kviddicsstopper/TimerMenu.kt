package com.example.kviddicsstopper

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.VibrationEffect
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.widget.ProgressBar


class TimerMenu : AppCompatActivity() {

    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"

    private var timerDuration: Long = 25000 // 25 seconds
    private var vibrator: Vibrator? = null
    private var stop = false
    private var devBool = false
    private var soleBool = false

    lateinit var b1: String
    lateinit var b2: String
    lateinit var b3: String
    lateinit var b4: String
    lateinit var b5: String
    lateinit var b6: String

    var w = arrayOf(false, false, false)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator



        //input
        val input1 = findViewById<EditText>(R.id.input1)
        val input2 = findViewById<EditText>(R.id.input2)
        val input3 = findViewById<EditText>(R.id.input3)
        val input4 = findViewById<EditText>(R.id.input4)
        val input5 = findViewById<EditText>(R.id.input5)
        val input6 = findViewById<EditText>(R.id.input6)

        val soleSwitch = findViewById<Switch>(R.id.soleBool)

        val colour1: Spinner = findViewById(R.id.colour1)

        val downloadProgress: ProgressBar = findViewById(R.id.ConstraintLayout)

        var selectedColour1 = intent.getStringExtra("c1") ?: ""
        var selectedColour2 = intent.getStringExtra("c2") ?: ""
        //val colorArray = arrayOf("", "Tobimug", "Purple", "Red", "Green", "Yellow", "Blue")
        val colorArray = arrayOf("", "Tobimug", "Hifi", "Astrocomic", "Zolg's", "Tanar", "Diak", "AS Vezeto", "AS Gyerek")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorArray)

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        colour1.adapter = arrayAdapter
        colour1.setSelection(colourToNum(selectedColour1))

        colour1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedColour1 = parent?.getItemAtPosition(position).toString()
                //Toast.makeText(this@TimerMenu, "Selected: $selectedColour1", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val colour2: Spinner = findViewById(R.id.colour2)


        val arrayAdapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorArray)
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)



        colour2.adapter = arrayAdapter2
        colour2.setSelection(colourToNum(selectedColour2))

        colour2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedColour2 = parent?.getItemAtPosition(position).toString()
                //Toast.makeText(this@TimerMenu, "Selected: $selectedColour2", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedColour2 = intent.getStringExtra("c2") ?: "Grey"
            }
        }

        if ((intent.getStringExtra("c2") ?: "random4") != "random4") {
            soleSwitch.performClick()
            soleBool = true
            input4.visibility = View.VISIBLE
            input5.visibility = View.VISIBLE
            input6.visibility = View.VISIBLE
            colour2.visibility = View.VISIBLE
        }


        //dev
        val dev = findViewById<Button>(R.id.navDev)
        val timeInput = findViewById<EditText>(R.id.timeInput)

        //navigation
        val progress = findViewById<Button>(R.id.progress)
        val progressOnline = findViewById<Button>(R.id.progressOnline)
        val back = findViewById<Button>(R.id.back)

        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        b1 = intent.getStringExtra("b1") ?: "Player 1"
        b2 = intent.getStringExtra("b2") ?: "Player 2"
        b3 = intent.getStringExtra("b3") ?: "Player 3"
        b4 = intent.getStringExtra("b4") ?: "Player 4"
        b5 = intent.getStringExtra("b5") ?: "Player 5"
        b6 = intent.getStringExtra("b6") ?: "Player 6"

        input1.setText(b1)
        input2.setText(b2)
        input3.setText(b3)
        input4.setText(b4)
        input5.setText(b5)
        input6.setText(b6)

        progress.setOnClickListener {
            b1 = input1.text.toString()
            b2 = input2.text.toString()
            b3 = input3.text.toString()
            b4 = input4.text.toString()
            b5 = input5.text.toString()
            b6 = input6.text.toString()

            if (!soleBool) {
                val intent = Intent(this, Timer3_2::class.java)
                intent.putExtra("b1", b1)
                intent.putExtra("b2", b2)
                intent.putExtra("b3", b3)
                intent.putExtra("c1", selectedColour1)
                startActivity(intent)
            } else {
                val intent = Intent(this, Timer6::class.java)
                intent.putExtra("b1", b1)
                intent.putExtra("b2", b2)
                intent.putExtra("b3", b3)
                intent.putExtra("b4", b4)
                intent.putExtra("b5", b5)
                intent.putExtra("b6", b6)
                intent.putExtra("c1", selectedColour1)
                intent.putExtra("c2", selectedColour2)
                startActivity(intent)
            }
        }

        progressOnline.setOnClickListener {
            if (selectedColour1 != "" && ((!soleBool) || (selectedColour2 != "" && soleBool))) {

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val team1 = textToCol(selectedColour1)
                        val team2 = textToCol(selectedColour2)

                        b1 = readDataFromSheet(team1 + "3", downloadProgress)
                        b2 = readDataFromSheet(team1 + "4", downloadProgress)
                        b3 = readDataFromSheet(team1 + "5", downloadProgress)
                        b4 = readDataFromSheet(team2 + "3", downloadProgress)
                        b5 = readDataFromSheet(team2 + "4", downloadProgress)
                        b6 = readDataFromSheet(team2 + "5", downloadProgress)
                        withContext(Dispatchers.Main) {
                            if (!soleBool) {
                                input1.setText(b1)
                                input2.setText(b2)
                                input3.setText(b3)
                            } else {
                                input1.setText(b1)
                                input2.setText(b2)
                                input3.setText(b3)
                                input4.setText(b4)
                                input5.setText(b5)
                                input6.setText(b6)
                            }
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@TimerMenu,
                                "Error reading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ReadDataError", "Error reading data", e)
                        }
                    }
                }
            } else {

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TimerMenu,
                            "Specify missing team",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        soleSwitch.setOnClickListener {
            soleBool = !soleBool
            if (soleBool) {
                input4.visibility = View.VISIBLE
                input5.visibility = View.VISIBLE
                input6.visibility = View.VISIBLE
                colour2.visibility = View.VISIBLE
            } else {
                input4.visibility = View.GONE
                input5.visibility = View.GONE
                input6.visibility = View.GONE
                colour2.visibility = View.GONE
            }
        }
    }

    private fun startTimer(duration: Long, button: Button, text: String, n: Int, name: TextView,
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
                    Toast.makeText(this@TimerMenu, "Timer Finished!", Toast.LENGTH_SHORT).show()

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
                        Toast.makeText(this@TimerMenu, "Timer Finished!", Toast.LENGTH_SHORT).show()
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
    }
    private fun colourToNum(colorName: String): Int {
        return when (colorName) { //  Use lowercase for easier comparison
            "Tobimug" -> 1
            "Hifi" -> 2
            "Astrocomic" -> 3
            "Zolg's" -> 4
            "Tanar" -> 5
            "Diak" -> 6
            "AS Vezeto" -> 7
            "AS Gyerek" -> 8
            else -> 0 // Default color if the name doesn't match.  You can change this to any color.
        }
    }

    private fun textToCol(colorName: String): String {
        return when (colorName) { //  Use lowercase for easier comparison
            "Tobimug" -> "D"
            "Hifi" -> "E"
            "Astrocomic" -> "F"
            "Zolg's" -> "G"
            "Tanar" -> "I"
            "Diak" -> "J"
            "AS Vezeto" -> "L"
            "AS Gyerek" ->  "M"
            else -> "X"
        }
    }

    private suspend fun readDataFromSheet(cellToRead: String, downloadProgress: ProgressBar): String { // Added return type
        var connection: HttpURLConnection? = null
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                downloadProgress.visibility = View.VISIBLE
            }
        }
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
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    downloadProgress.visibility = View.INVISIBLE
                }
            }
        }
    }
}



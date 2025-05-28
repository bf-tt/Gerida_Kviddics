package com.example.kviddicsstopper

import android.content.Intent
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener

import kotlinx.coroutines.*

import androidx.appcompat.app.AlertDialog
import java.io.File
import java.security.MessageDigest

private val activeTimers = mutableListOf<CountDownTimer>()

val correctPasswordHash = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4" // Hash for "1234"
class Referee : AppCompatActivity() {

    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"
    @SuppressLint("MissingInflatedId")

    private var timerDuration: Long = 600000 // 10 minutes
    private var vibrator: Vibrator? = null
    private var minuteChanged: Int = 0
    private var secondsChanged: Int = 0

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

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    @SuppressLint("MissingInflatedId", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.referee)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Enter Password")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = input.text.toString()
                val hashedInput = hashPassword(enteredPassword)

                if (hashedInput != correctPasswordHash) {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .show()

        lateinit var mainLayout: ConstraintLayout

        Toast.makeText(
            this@Referee,
            "Loading data...",
            Toast.LENGTH_SHORT
        ).show()

        var preSet1 = 0
        var preSet2 = 0
        var preSet3 = 0

        var sendTimeData = 1

        val back: Button = findViewById(R.id.back)

        val downloadProgress = findViewById<ProgressBar>(R.id.downloadProgress)

        back.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, MainActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }

        val spinner1: Spinner = findViewById(R.id.spinner1)
        val spinner2: Spinner = findViewById(R.id.spinner2)

        val t1Goal: Button = findViewById(R.id.t1Goal)
        val t2Goal: Button = findViewById(R.id.t2Goal)

        val t1Score: EditText = findViewById(R.id.t1Score)
        val t2Score: EditText = findViewById(R.id.t2Score)

        val start: Button = findViewById(R.id.start)
        val pause: Button = findViewById(R.id.pause)
        val reset: Button = findViewById(R.id.reset)

        val minutesDisplay: TextView = findViewById(R.id.minutes)
        val secondsDisplay: TextView = findViewById(R.id.seconds)
        val minutesText: TextView = findViewById(R.id.minutesText)
        val secondsText: TextView = findViewById(R.id.secondsText)

        val snitch1: Button = findViewById(R.id.snitch1)
        val snitch2: Button = findViewById(R.id.snitch2)

        val winner: TextView = findViewById(R.id.winner)

        val save: Button = findViewById(R.id.save)

        val mti: Button = findViewById(R.id.manualTime)

        val script: TextView = findViewById(R.id.script)

        val aboutText = """
        Quick-Check
        - everyone off the pitch and quiet
        - guards in position
""".trimIndent()

        //script.setText(aboutText)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = readDataFromSheet("B6", downloadProgress) // Call read function
                sendTimeData = readDataFromSheet("C10", downloadProgress).toInt()
                withContext(Dispatchers.Main) {
                    timerDuration = result.toLong()
                    minutesDisplay.text = SpannableStringBuilder("${(result.toInt() / 60000)}")
                    secondsDisplay.text = SpannableStringBuilder("${(result.toInt() / 1000) % 60}")
                    if ((result.toInt() / 1000).toInt() %60 == 0) {
                        secondsDisplay.text = SpannableStringBuilder("00")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Referee,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        minutesDisplay.text = SpannableStringBuilder("${(timerDuration / 60000)}")
        secondsDisplay.text = SpannableStringBuilder("${(timerDuration / 1000) % 60}")
        if ((timerDuration / 1000).toInt() %60 == 0) {
            secondsDisplay.text = SpannableStringBuilder("00")
        }

        start.setOnClickListener {
            startTimer(timerDuration, start, minutesDisplay, pause, reset, secondsDisplay, mti)
        }

        val colorArray = arrayOf("", "Tobimug", "Hifi", "Astrocomic", "Zolg's", "Tanar", "Diak", "AS Vezeto", "AS Gyerek")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorArray)

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.adapter = arrayAdapter
        spinner2.adapter = arrayAdapter
        var team1 = "A"
        var team2 = "A"

        val quarter: Spinner = findViewById(R.id.quarter)
        val quarterOptions = arrayOf("Q1", "Q2", "Q3", "Q4", "Snitch")

        val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, quarterOptions)

        arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quarter.adapter = arrayAdapter3

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var q = readDataFromSheet("B9", downloadProgress)
                withContext(Dispatchers.Main) {
                    if (q == "Snitch") {
                        quarter.setSelection(4)
                    } else if (q == "Q1"){
                        quarter.setSelection(0)
                    } else if (q == "Q2"){
                        quarter.setSelection(1)
                    } else if (q == "Q3"){
                        quarter.setSelection(2)
                    } else if (q == "Q4"){
                        quarter.setSelection(3)
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Referee,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s1 = readDataFromSheet("D13", downloadProgress)
                val s2 = readDataFromSheet("E13", downloadProgress)
                withContext(Dispatchers.Main) {
                    spinner1.setSelection(coltoNum(s1))
                    spinner2.setSelection(coltoNum(s2))
                    team1 = s1
                    team2 = s2
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Referee,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        mti.setOnClickListener {
            minuteChanged = timerDuration.toInt()/60000
            secondsChanged = timerDuration.toInt()/1000%60
            showInputPopup(minutesDisplay, secondsDisplay)
        }

        save.setOnClickListener {
            cut(team1, team2, downloadProgress)
        }

        quarter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (preSet3 != 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("B9", quarter.selectedItem.toString(), downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                } else {
                    preSet3 = 1
                }

                if (quarter.selectedItem.toString() == "Snitch") {
                    snitch1.visibility = View.VISIBLE
                    snitch2.visibility = View.VISIBLE
                    script.visibility = View.VISIBLE

                    t1Goal.visibility = View.INVISIBLE
                    t2Goal.visibility = View.INVISIBLE
                    minutesDisplay.visibility = View.INVISIBLE
                    secondsDisplay.visibility = View.INVISIBLE
                    start.visibility = View.INVISIBLE
                    pause.visibility = View.INVISIBLE
                    minutesText.visibility = View.INVISIBLE
                    secondsText.visibility = View.INVISIBLE
                    reset.visibility = View.INVISIBLE
                    mti.visibility = View.INVISIBLE


                } else {
                    snitch1.visibility = View.INVISIBLE
                    snitch2.visibility = View.INVISIBLE
                    winner.visibility = View.INVISIBLE
                    save.visibility = View.INVISIBLE
                    script.visibility = View.INVISIBLE

                    t1Goal.visibility = View.VISIBLE
                    t2Goal.visibility = View.VISIBLE
                    minutesDisplay.visibility = View.VISIBLE
                    secondsDisplay.visibility = View.VISIBLE
                    start.visibility = View.VISIBLE
                    pause.visibility = View.VISIBLE
                    minutesText.visibility = View.VISIBLE
                    secondsText.visibility = View.VISIBLE
                    reset.visibility = View.VISIBLE
                    mti.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {


                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val s1 = readDataFromSheet(team1 + "12", downloadProgress)
                        withContext(Dispatchers.Main) {
                            t1Score.text = SpannableStringBuilder(s1)
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error reading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ReadDataError", "Error reading data", e)
                        }
                    }
                }
                if (preSet1 >= 1) {
                    team1 = textToCol(parent?.getItemAtPosition(position).toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("D13", team1, downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                } else {
                    preSet1++
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (preSet2 >= 1) {

                    team2 = textToCol(parent?.getItemAtPosition(position).toString())

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("E13", team2, downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                    //Toast.makeText(this@Referee, team1, Toast.LENGTH_SHORT).show()
                } else {
                    preSet2++
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val s2 = readDataFromSheet(team2 + "12", downloadProgress)
                        withContext(Dispatchers.Main) {
                            t2Score.text = SpannableStringBuilder(s2)
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error reading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ReadDataError", "Error reading data", e)
                        }
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        t1Goal.setOnClickListener {
            if (team1 != "A") {
                t1Score.setText((t1Score.text.toString().toInt() + 10).toString())

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        var row = readDataFromSheet("O3", downloadProgress).toInt()+3
                        var gameTime = readDataFromSheet("B8", downloadProgress).toString()
                        var quarterNum = readDataFromSheet("B9", downloadProgress)

                        sendDataToCell("P" + row, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(java.util.Date()), downloadProgress)
                        sendDataToCell("Q" + row, quarterNum, downloadProgress)
                        sendDataToCell("R" + row, gameTime, downloadProgress)
                        sendDataToCell("S" + row, "Goal from " + colToText(team1), downloadProgress)
                        sendDataToCell("O3", (row-2).toString(), downloadProgress)
                    } catch (e: IOException) {
                        // Handle errors (show a message to the user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error sending data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "SendDataError",
                                "Error sending data",
                                e
                            ) // Log the error for debugging
                        }
                    }
                }
                /*CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val t1 = readDataFromSheet(team1 + "12")
                        withContext(Dispatchers.Main) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    sendDataToCell(team1 + "12", (t1.toInt() + 10).toString())
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val t1 = readDataFromSheet(team1 + "12")
                                            withContext(Dispatchers.Main) {
                                                t1Score.text = SpannableStringBuilder(t1)
                                            }
                                        } catch (e: IOException) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@Referee,
                                                    "Error reading data: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e("ReadDataError", "Error reading data", e)
                                            }
                                        }
                                    }
                                } catch (e: IOException) {
                                    // Handle errors (show a message to the user)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@Referee,
                                            "Error sending data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "SendDataError",
                                            "Error sending data",
                                            e
                                        ) // Log the error for debugging
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error reading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ReadDataError", "Error reading data", e)
                        }
                    }
                }*/
            }
        }

        t2Goal.setOnClickListener {
            if (team2 != "A") {
                t2Score.setText((t2Score.text.toString().toInt() + 10).toString())

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        var row = readDataFromSheet("O3", downloadProgress).toInt()+3
                        var gameTime = readDataFromSheet("B8", downloadProgress).toString()
                        var quarterNum = readDataFromSheet("B9", downloadProgress)

                        sendDataToCell("P" + row, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(java.util.Date()), downloadProgress)
                        sendDataToCell("Q" + row, quarterNum, downloadProgress)
                        sendDataToCell("R" + row, gameTime, downloadProgress)
                        sendDataToCell("S" + row, "Goal from " + colToText(team2), downloadProgress)
                        sendDataToCell("O3", (row-2).toString(), downloadProgress)
                    } catch (e: IOException) {
                        // Handle errors (show a message to the user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error sending data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "SendDataError",
                                "Error sending data",
                                e
                            ) // Log the error for debugging
                        }
                    }
                }
                /*CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val t2 = readDataFromSheet(team2 + "12")
                        withContext(Dispatchers.Main) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    sendDataToCell(team2 + "12", (t2.toInt() + 10).toString())
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val t2 = readDataFromSheet(team2 + "12")
                                            withContext(Dispatchers.Main) {
                                                t2Score.text = SpannableStringBuilder(t2)
                                            }
                                        } catch (e: IOException) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@Referee,
                                                    "Error reading data: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e("ReadDataError", "Error reading data", e)
                                            }
                                        }
                                    }
                                } catch (e: IOException) {
                                    // Handle errors (show a message to the user)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@Referee,
                                            "Error sending data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "SendDataError",
                                            "Error sending data",
                                            e
                                        ) // Log the error for debugging
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error reading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ReadDataError", "Error reading data", e)
                        }
                    }
                }*/
            }
        }

        snitch1.setOnClickListener {
            if (team1 != "A") {
                t1Score.setText((t1Score.text.toString().toInt() + 35).toString())
                snitch1.visibility = View.INVISIBLE
                snitch2.visibility = View.INVISIBLE
                winner.visibility = View.VISIBLE
                save.visibility = View.VISIBLE
                var winningTeam = ""
                if (t1Score.text.toString().toInt() > t2Score.text.toString().toInt()) {
                    winningTeam = colToText(team1)
                } else if (t1Score.text.toString().toInt() < t2Score.text.toString().toInt()) {
                    winningTeam = colToText(team2)
                } else {
                    winningTeam = "Draw"
                }
                winner.setText(winningTeam + " is the Winner!!!")
                if (winningTeam != "Draw") {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("M13", winningTeam, downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        var row = readDataFromSheet("O3", downloadProgress).toInt()+3
                        var quarterNum = readDataFromSheet("B9", downloadProgress)

                        sendDataToCell("P" + row, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(java.util.Date()), downloadProgress)
                        sendDataToCell("Q" + row, quarterNum, downloadProgress)
                        sendDataToCell("R" + row, "N/A", downloadProgress)
                        sendDataToCell("S" + row, "Snitch caught by " + colToText(team1)
                                + ", Game won by " + winningTeam, downloadProgress)
                        sendDataToCell("O3", (row-2).toString(), downloadProgress)
                    } catch (e: IOException) {
                        // Handle errors (show a message to the user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error sending data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "SendDataError",
                                "Error sending data",
                                e
                            ) // Log the error for debugging
                        }
                    }
                }
            }
        }

        snitch2.setOnClickListener {
            if (team2 != "A") {
                t2Score.setText((t2Score.text.toString().toInt() + 35).toString())
                snitch1.visibility = View.INVISIBLE
                snitch2.visibility = View.INVISIBLE
                winner.visibility = View.VISIBLE
                save.visibility = View.VISIBLE
                var winningTeam = ""
                if (t1Score.text.toString().toInt() > t2Score.text.toString().toInt()) {
                    winningTeam = colToText(team1)
                } else if (t1Score.text.toString().toInt() < t2Score.text.toString().toInt()) {
                    winningTeam = colToText(team2)
                } else {
                    winningTeam = "Draw"
                }
                winner.setText(winningTeam + " is the Winner!!!")
                if (winningTeam != "Draw") {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("M13", winningTeam, downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        var row = readDataFromSheet("O3", downloadProgress).toInt()+3
                        var gameTime = readDataFromSheet("B8", downloadProgress).toString()
                        var quarterNum = readDataFromSheet("B9", downloadProgress)

                        sendDataToCell("P" + row, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(java.util.Date()), downloadProgress)
                        sendDataToCell("Q" + row, quarterNum, downloadProgress)
                        sendDataToCell("R" + row, gameTime, downloadProgress)
                        sendDataToCell("S" + row, "Snitch caught by " + colToText(team2)
                                + ", Game won by " + winningTeam, downloadProgress)
                        sendDataToCell("O3", (row-2).toString(), downloadProgress)
                    } catch (e: IOException) {
                        // Handle errors (show a message to the user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Error sending data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "SendDataError",
                                "Error sending data",
                                e
                            ) // Log the error for debugging
                        }
                    }
                }
            }
        }


        t1Score.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (team1 != "A") {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (t1Score.text.toString() != "---") {
                                sendDataToCell(
                                    team1 + "12",
                                    t1Score.text.toString(),
                                    downloadProgress
                                )
                            } else {
                                sendDataToCell(
                                    team1 + "12",
                                    "0",
                                    downloadProgress
                                )
                            }
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                }


            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        t2Score.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (team2 != "A") {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {

                            if (t1Score.text.toString() != "---") {
                                sendDataToCell(team2 + "12", t2Score.text.toString(), downloadProgress)
                            } else {
                                sendDataToCell(team2 + "12", "0", downloadProgress)
                            }
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        t1Score.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                t1Score.clearFocus()
            }
        }
        t2Score.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                t2Score.clearFocus()
            }
        }

        secondsDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (sendTimeData == 1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            sendDataToCell("B8", minutesDisplay.text.toString() + ":" + secondsDisplay.text.toString(), downloadProgress)
                        } catch (e: IOException) {
                            // Handle errors (show a message to the user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@Referee,
                                    "Error sending data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "SendDataError",
                                    "Error sending data",
                                    e
                                ) // Log the error for debugging
                            }
                        }
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private suspend fun sendDataToCell(cell: String, value: String, downloadProgress: ProgressBar) {
        val cellNum = Regex("\\d+").findAll(cell).map { it.value }.toList()
        val valueNum = Regex("\\d+").findAll(cell).map { it.value }.toList()
        if ((cellNum[0] == "12" && valueNum.isNotEmpty()) || cellNum[0] != "12") {
            var connection: HttpURLConnection? = null
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    downloadProgress.visibility = View.VISIBLE
                }
            }
            try {
                // Construct the URL to send cell and value
                val urlString =
                    "$webAppUrl?cell=${URLEncoder.encode(cell, "UTF-8")}&value=${
                        URLEncoder.encode(
                            value,
                            "UTF-8"
                        )
                    }"
                val url = URL(urlString)

                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET" // Use GET

                val responseCode = connection.responseCode
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    if (responseText == "success") {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Data sent successfully to cell $cell",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Referee,
                                "Failed to send data to cell $cell: $responseText",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "SendDataError",
                                "Failed to send data to cell $cell: $responseText"
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Referee,
                            "Error: HTTP $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "SendDataError",
                            "HTTP Error: $responseCode, Response: $responseText"
                        )
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Referee, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SendDataError", "Exception: ${e.message}", e)
                }
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

    private fun startTimer(duration: Long, button: Button,
                           c1: TextView, p: Button, r: Button, c2: TextView, mti: Button) {
        var currentTimer: CountDownTimer? = null
        var isPaused = false
        var remainingTime: Long = duration
        p.text = "Pause"
        currentTimer?.cancel()

        currentTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remainingTime = millisUntilFinished // Update remaining time
                    c1.text = "${(millisUntilFinished / 60000)}"
                    c2.text = "${(millisUntilFinished / 1000) % 60}"
                }
                button.visibility = View.INVISIBLE
                mti.visibility = View.INVISIBLE
                r.setOnClickListener {
                    p.text = "Pause"
                    currentTimer?.cancel()
                    c1.text = "${(duration / 60000)}"
                    c2.text = "${(duration / 1000) % 60}"
                    if ((duration / 1000).toInt() %60 == 0) {
                        c2.text = "00"
                    }
                    onFinish()
                }
            }

            override fun onFinish() {
                if (!isPaused) {
                    vibratePhone()
                    button.visibility = View.VISIBLE
                    mti.visibility = View.VISIBLE
                }
            }
        }.start()

        activeTimers.add(currentTimer)

        p.setOnClickListener {
            if (!isPaused && button.visibility == View.INVISIBLE) {
                isPaused = true
                currentTimer?.cancel() // Pause the timer
                p.text = "Resume" // Change the button text
                r.setOnClickListener {
                    p.text = "Pause"
                    vibratePhone()
                    currentTimer?.cancel()
                    isPaused = false
                    button.visibility = View.VISIBLE
                    mti.visibility = View.VISIBLE
                    c1.text = "${(duration / 60000)}"
                    c2.text = "${(duration / 1000) % 60}"
                    if ((duration / 1000).toInt() %60 == 0) {
                        c2.text = "00"
                    }
                }
                button.visibility = View.INVISIBLE
                mti.visibility = View.INVISIBLE
            } else if (button.visibility == View.INVISIBLE){
                isPaused = false
                p.text = "Pause" // Change the button text back
                // Create a new timer with the remaining time and start it
                currentTimer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        remainingTime = millisUntilFinished
                        c1.text = "${(millisUntilFinished / 60000)}"
                        c2.text = "${(millisUntilFinished / 1000) % 60}"
                        r.setOnClickListener {
                            p.text = "Pause"
                            vibratePhone()
                            currentTimer?.cancel()
                            isPaused = false
                            c1.text = "${(duration / 60000)}"
                            c2.text = "${(duration / 1000) % 60}"
                            if ((duration / 1000).toInt() %60 == 0) {
                                c2.text = "00"
                            }
                            onFinish()
                        }
                        button.visibility = View.INVISIBLE
                        mti.visibility = View.INVISIBLE
                    }

                    override fun onFinish() {
                        vibratePhone()
                        button.visibility = View.VISIBLE
                        mti.visibility = View.VISIBLE
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
            else -> "A"
        }
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

    private fun coltoNum(colorName: String): Int {
        return when (colorName) { //  Use lowercase for easier comparison
            "D" -> 1
            "E" -> 2
            "F" -> 3
            "G" -> 4
            "I" -> 5
            "J" -> 6
            "L" -> 7
            "M" -> 8
            else -> 0 // Default color if the name doesn't match.  You can change this to any color.
        }
    }

    private fun colToText(letter: String): String {
        return when (letter) {
            "D" -> "Tobimug"
            "E" -> "Hifi"
            "F" -> "Astrocomic"
            "G" -> "Zolg's"
            "I" -> "Tanar"
            "J" -> "Diak"
            "L" -> "AS Vezeto"
            "M" -> "AS Gyerek"
            else -> "Unknown"
        }
    }

    private fun showInputPopup(mD: TextView, sD: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("CTI - Custom Time Input")

        // Create a LinearLayout to hold both input fields
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val input1 = EditText(this)
        input1.hint = "Minutes"
        input1.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(input1)

        val input2 = EditText(this)
        input2.hint = "Seconds"
        input2.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(input2)

        builder.setView(layout)

        builder.setPositiveButton("OK") { dialog, _ ->
            try {
                minuteChanged = input1.text.toString().toInt()
                secondsChanged = input2.text.toString().toInt()
                mD.setText(minuteChanged.toString())
                sD.setText(secondsChanged.toString())
                Toast.makeText(this, "Values: $minuteChanged and $secondsChanged", Toast.LENGTH_SHORT).show()
                timerDuration = (minuteChanged*60000 + secondsChanged*1000).toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun cut(t1: String, t2: String, downloadProgress: ProgressBar) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var start = readDataFromSheet("O4", downloadProgress).toInt()
                var logNum = readDataFromSheet("O3", downloadProgress).toInt()

                sendDataToCell(t1 + start, colToText(t1), downloadProgress)
                sendDataToCell(t2 + start, colToText(t2), downloadProgress)

                val nums = arrayOf(3, 4, 5, 6, 8, 9, 12)
                for (i in nums) {
                    sendDataToCell(t1 + (i+start).toString(), readDataFromSheet(t1 + i.toString(), downloadProgress), downloadProgress)
                    sendDataToCell(t2 + (i+start).toString(), readDataFromSheet(t2 + i.toString(), downloadProgress), downloadProgress)
                }

                sendDataToCell("L" + (start).toString(), "Winner", downloadProgress)
                sendDataToCell("M" + (start).toString(), readDataFromSheet("M" + "13", downloadProgress), downloadProgress)
                sendDataToCell("M13", "", downloadProgress)

                sendDataToCell(t1+"12", "0", downloadProgress)
                sendDataToCell(t2+"12", "0", downloadProgress)

                sendDataToCell("O3", "0", downloadProgress)
                if (logNum < 12) { logNum = 12 }
                sendDataToCell("O4", (start+logNum).toString(), downloadProgress)

                while (true) {
                    var count = 3
                    var rTime = readDataFromSheet("P" + 3, downloadProgress)
                    if (rTime == "") {
                        break
                    } else {
                        sendDataToCell("P" + (start-3+count).toString(), rTime, downloadProgress)
                        sendDataToCell("Q" + (start).toString(), readDataFromSheet("Q" + count, downloadProgress), downloadProgress)
                        sendDataToCell("R" + (start).toString(), readDataFromSheet("R" + count, downloadProgress), downloadProgress)
                        sendDataToCell("S" + (start).toString(), readDataFromSheet("S" + count, downloadProgress), downloadProgress)
                    }
                    count++
                }

            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Referee,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }


    }
}
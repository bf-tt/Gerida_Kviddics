package com.example.kviddicsstopper

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest


class Manager4 : AppCompatActivity() {

    val correctPasswordHash = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4" // Hash for "1234"

    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager)

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

        val back: Button = findViewById(R.id.back)

        back.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, MainActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }

        var readCellEditText = "B3"

        val s1 = findViewById<TextView>(R.id.score1)
        val s2 = findViewById<TextView>(R.id.score2)

        val update = findViewById<Button>(R.id.update)

        val downloadProgress = findViewById<ProgressBar>(R.id.progressBar)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val team1 = readDataFromSheet("D13", downloadProgress)
                val team2 = readDataFromSheet("E13", downloadProgress)
                val score1 = readDataFromSheet(team1 + "12", downloadProgress)
                val score2 = readDataFromSheet(team2 + "12", downloadProgress)
                withContext(Dispatchers.Main) {
                    s1.text = score1
                    s2.text = score2
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Manager4,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        update.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val team1 = readDataFromSheet("D13", downloadProgress)
                    val team2 = readDataFromSheet("E13", downloadProgress)
                    val score1 = readDataFromSheet(team1 + "12", downloadProgress)
                    val score2 = readDataFromSheet(team2 + "12", downloadProgress)
                    withContext(Dispatchers.Main) {
                        s1.text = score1
                        s2.text = score2
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager4,
                            "Error reading data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("ReadDataError", "Error reading data", e)
                    }
                }
            }
        }

        val h1 = findViewById<TextView>(R.id.h1)
        val h2 = findViewById<TextView>(R.id.h2)
        val h3 = findViewById<TextView>(R.id.h3)
        val t1 = findViewById<TextView>(R.id.t1)
        val t2 = findViewById<TextView>(R.id.t2)
        val o = findViewById<TextView>(R.id.o)
        val f = findViewById<TextView>(R.id.f)
        val upload = findViewById<TextView>(R.id.updateScore)

        var col = "X"

        val spinner: Spinner = findViewById(R.id.spinner)

        val colorArray = arrayOf("", "Tobimug", "Hifi", "Astrocomic", "Zolg's", "Tanar", "Diak", "AS Vezeto", "AS Gyerek")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorArray)

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = arrayAdapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                col = textToCol(parent?.getItemAtPosition(position).toString())
                Toast.makeText(
                    this@Manager4,
                    col,
                    Toast.LENGTH_SHORT
                ).show()
                //Toast.makeText(this@TimerMenu, "Selected: $selectedColour1", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val h1t = readDataFromSheet(col + "3", downloadProgress)
                        val h2t = readDataFromSheet(col + "4", downloadProgress)
                        val h3t = readDataFromSheet(col + "5", downloadProgress)
                        val t1t = readDataFromSheet(col + "6", downloadProgress)
                        val t2t = readDataFromSheet(col + "7", downloadProgress)
                        val ot = readDataFromSheet(col + "8", downloadProgress)
                        val ft = readDataFromSheet(col + "9", downloadProgress)
                        withContext(Dispatchers.Main) {
                            h1.text = h1t
                            h2.text = h2t
                            h3.text = h3t
                            t1.text = t1t
                            t2.text = t2t
                            o.text = ot
                            f.text = ft
                        }
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Manager4,
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

        val cellToRead = readCellEditText
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = readDataFromSheet(cellToRead, downloadProgress)
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Manager4,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        upload.setOnClickListener {

            val h1t = findViewById<TextView>(R.id.h1).text.toString().trim()
            val h2t = findViewById<TextView>(R.id.h2).text.toString().trim()
            val h3t = findViewById<TextView>(R.id.h3).text.toString().trim()
            val t1t = findViewById<TextView>(R.id.t1).text.toString().trim()
            val t2t = findViewById<TextView>(R.id.t2).text.toString().trim()
            val ot = findViewById<TextView>(R.id.o).text.toString().trim()
            val ft = findViewById<TextView>(R.id.f).text.toString().trim()
            val uploadt = findViewById<TextView>(R.id.updateScore).text.toString().trim()

            // Launch a coroutine to perform the network request (Important:  Don't do network stuff on the main thread!)
            CoroutineScope(Dispatchers.IO).launch {
                if (col != "X") {
                    try {
                        sendDataToCell(col + "3", h1t, downloadProgress)
                        sendDataToCell(col + "4", h2t, downloadProgress)
                        sendDataToCell(col + "5", h3t, downloadProgress)
                        sendDataToCell(col + "6", t1t, downloadProgress)
                        sendDataToCell(col + "7", t2t, downloadProgress)
                        sendDataToCell(col + "8", ot, downloadProgress)
                        sendDataToCell(col + "9", ft, downloadProgress)
                    } catch (e: IOException) {
                        // Handle errors (show a message to the user)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@Manager4,
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
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager4,
                            "Specify team",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private suspend fun sendDataToCell(cell: String, value: String, downloadProgress: ProgressBar) {
        var connection: HttpURLConnection? = null
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                downloadProgress.visibility = View.VISIBLE
            }
        }
        try {
            // Construct the URL to send cell and value
            val urlString =
                "$webAppUrl?cell=${URLEncoder.encode(cell, "UTF-8")}&value=${URLEncoder.encode(
                    value,
                    "UTF-8"
                )}"
            val url = URL(urlString)

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET" // Use GET

            val responseCode = connection.responseCode
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (responseText == "success") {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager4,
                            "Data sent successfully to cell $cell",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager4,
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
                        this@Manager4,
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
                Toast.makeText(this@Manager4, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
            else -> "X"
        }
    }
}
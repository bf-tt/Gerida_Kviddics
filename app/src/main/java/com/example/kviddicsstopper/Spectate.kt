package com.example.kviddicsstopper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.view.View
import android.widget.ProgressBar
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

class Spectate : AppCompatActivity() {
    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spectate)

        Toast.makeText(
            this@Spectate,
            "Loading data...",
            Toast.LENGTH_LONG
        ).show()

        val back: Button = findViewById(R.id.back)
        val update: Button = findViewById(R.id.update)
        val downloadProgress = findViewById<ProgressBar>(R.id.downloadProgress)

        back.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, MainActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }

        val o: TextView = findViewById(R.id.o)
        val f: TextView = findViewById(R.id.f)
        val t1: TextView = findViewById(R.id.t1)
        val t2: TextView = findViewById(R.id.t2)
        val h1: TextView = findViewById(R.id.h1)
        val h2: TextView = findViewById(R.id.h2)
        val h3: TextView = findViewById(R.id.h3)
        val s: TextView = findViewById(R.id.s1)
        val o_2: TextView = findViewById(R.id.o_2)
        val f_2: TextView = findViewById(R.id.f_2)
        val t1_2: TextView = findViewById(R.id.t1_2)
        val t2_2: TextView = findViewById(R.id.t2_2)
        val h1_2: TextView = findViewById(R.id.h1_2)
        val h2_2: TextView = findViewById(R.id.h2_2)
        val h3_2: TextView = findViewById(R.id.h3_2)
        val s_2: TextView = findViewById(R.id.s2)
        val team1: TextView = findViewById(R.id.team1)
        val team2: TextView = findViewById(R.id.team2)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var col1 = readDataFromSheet("D13", downloadProgress)
                var col2 = readDataFromSheet("E13", downloadProgress)
                val st = readDataFromSheet(col1 + "12", downloadProgress)
                val team1t = readDataFromSheet(col1 + "2", downloadProgress)
                val s_2t = readDataFromSheet(col2 + "12", downloadProgress)
                val team2t = readDataFromSheet(col2 + "2", downloadProgress)
                withContext(Dispatchers.Main) {
                    s.text = st
                    team1.text = team1t
                    s_2.text = s_2t
                    team2.text = team2t

                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Spectate,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var col1 = readDataFromSheet("D13", downloadProgress)
                var col2 = readDataFromSheet("E13", downloadProgress)

                val ot = readDataFromSheet(col1 + "9", downloadProgress)
                val ft = readDataFromSheet(col1 + "8", downloadProgress)
                val t1t = readDataFromSheet(col1 + "6", downloadProgress)
                val t2t = readDataFromSheet(col1 + "7", downloadProgress)
                val h1t = readDataFromSheet(col1 + "3", downloadProgress)
                val h2t = readDataFromSheet(col1 + "4", downloadProgress)
                val h3t = readDataFromSheet(col1 + "5", downloadProgress)
                val st = readDataFromSheet(col1 + "12", downloadProgress)
                val team1t = readDataFromSheet(col1 + "2", downloadProgress)
                val o_2t = readDataFromSheet(col2 + "9", downloadProgress)
                val f_2t = readDataFromSheet(col2 + "8", downloadProgress)
                val t1_2t = readDataFromSheet(col2 + "6", downloadProgress)
                val t2_2t = readDataFromSheet(col2 + "7", downloadProgress)
                val h1_2t = readDataFromSheet(col2 + "3", downloadProgress)
                val h2_2t = readDataFromSheet(col2 + "4", downloadProgress)
                val h3_2t = readDataFromSheet(col2 + "5", downloadProgress)
                val s_2t = readDataFromSheet(col2 + "12", downloadProgress)
                val team2t = readDataFromSheet(col2 + "2", downloadProgress)
                withContext(Dispatchers.Main) {
                    o.text = ot
                    f.text = ft
                    t1.text = t1t
                    t2.text = t2t
                    h1.text = h1t
                    h2.text = h2t
                    h3.text = h3t
                    s.text = st
                    team1.text = team1t
                    o_2.text = o_2t
                    f_2.text = f_2t
                    t1_2.text = t1_2t
                    t2_2.text = t2_2t
                    h1_2.text = h1_2t
                    h2_2.text = h2_2t
                    h3_2.text = h3_2t
                    s_2.text = s_2t
                    team2.text = team2t

                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Spectate,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        update.setOnClickListener {

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
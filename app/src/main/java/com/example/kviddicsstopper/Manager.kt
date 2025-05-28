package com.example.kviddicsstopper

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.util.Log
import android.widget.EditText
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


class Manager : AppCompatActivity() {

    private val webAppUrl =
        "https://script.google.com/macros/s/AKfycbwpl2wQe8jyNNSh-PtUn5FEqvv-7WlxFQxjmZFGefDwP6YOc3PNeb5O-qpxPgHbWhF__Q/exec"
    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager)

        val back: Button = findViewById(R.id.back)

        back.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, MainActivity::class.java)
            // Start the new Activity
            startActivity(intent)
        }

        var readCellEditText = "B3"

        val h1 = findViewById<TextView>(R.id.h1)
        val h2 = findViewById<TextView>(R.id.h2)
        val h3 = findViewById<TextView>(R.id.h3)
        val t1 = findViewById<TextView>(R.id.t1)
        val t2 = findViewById<TextView>(R.id.t2)
        val o = findViewById<TextView>(R.id.o)
        val f = findViewById<TextView>(R.id.f)
        //val upload = findViewById<TextView>(R.id.upload)


        val cellToRead = readCellEditText
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = readDataFromSheet(cellToRead) // Call read function
                withContext(Dispatchers.Main) {

                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Manager,
                        "Error reading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ReadDataError", "Error reading data", e)
                }
            }
        }

        back.setOnClickListener {

            val h1t = findViewById<TextView>(R.id.h1).text.toString().trim()
            val h2t = findViewById<TextView>(R.id.h2).text.toString().trim()
            val h3t = findViewById<TextView>(R.id.h3).text.toString().trim()
            val t1t = findViewById<TextView>(R.id.t1).text.toString().trim()
            val t2t = findViewById<TextView>(R.id.t2).text.toString().trim()
            val ot = findViewById<TextView>(R.id.o).text.toString().trim()
            val ft = findViewById<TextView>(R.id.f).text.toString().trim()
            val uploadt = findViewById<TextView>(R.id.back).text.toString().trim()

            var col = "D"

            // Launch a coroutine to perform the network request (Important:  Don't do network stuff on the main thread!)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    sendDataToCell(col + "3", h1t)
                    sendDataToCell(col + "4", h2t)
                    sendDataToCell(col + "5", h3t)
                    sendDataToCell(col + "6", t1t)
                    sendDataToCell(col + "7", t2t)
                    sendDataToCell(col + "8", ot)
                    sendDataToCell(col + "9", ft)
                } catch (e: IOException) {
                    // Handle errors (show a message to the user)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager,
                            "Error sending data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("SendDataError", "Error sending data", e) // Log the error for debugging
                    }
                }
            }
        }
    }

    private suspend fun sendDataToCell(cell: String, value: String) {
        var connection: HttpURLConnection? = null
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
                            this@Manager,
                            "Data sent successfully to cell $cell!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Manager,
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
                        this@Manager,
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
                Toast.makeText(this@Manager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SendDataError", "Exception: ${e.message}", e)
            }
        } finally {
            connection?.disconnect()
        }
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
}
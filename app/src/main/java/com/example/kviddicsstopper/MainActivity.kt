package com.example.kviddicsstopper

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        val navTimer: Button = findViewById(R.id.navTimer)
        val navManager: Button = findViewById(R.id.navManager)
        val navRef: Button = findViewById(R.id.navRef)
        val navSpec: Button = findViewById(R.id.navSpec)
        val navDev: Button = findViewById(R.id.navDev)
        val about: Button = findViewById(R.id.about)
        val menuText: TextView = findViewById(R.id.menuText)

        var eeC = 0
        menuText.setOnClickListener {
            eeC++
            if (eeC >= 10) {
                eeC = 0
                val intent = Intent(this, EasterEgg::class.java)
                // Start the new Activity
                startActivity(intent)
            }
        }

        navTimer.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, TimerMenu::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        navManager.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, Manager4::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        navRef.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, Referee::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        navSpec.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, Spectate::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        navDev.setOnClickListener {
            // Create an Intent to go to SecondSceneActivity
            val intent = Intent(this, Developer::class.java)
            // Start the new Activity
            startActivity(intent)
        }
        about.setOnClickListener {

            /*AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("App name: Gerida Manager\n\n" +
                        "Version: V2.0.0\n\n" +
                        "Developer: Zolg's Team (f. Kayenne)\n\n" +
                        "Detabez: https://docs.google.com/spreadsheets/d/1vG8vxHTvlqTFg5CDBgODvDAwqIvJ5t5Z6riO-28AHNU/edit?usp=sharing\n\n" +
                        "Contact: geridamanager@gmail.com / message Kayenne\n\n" +
                        "Privacy info: all data is collected and sold for profit")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)  // Optional: allows tapping outside to dismiss
                .show()*/

            val aboutText = """
            <b>App name: Gerida Manager</b><br><br>
            
            Version: V2.0.1</b><br><br>
    
            Developer: Zolg's Team (f. Kayenne)<br><br>
            
            Contact: <a href='mailto:geridamanager@gmail.com'>geridamanager@gmail.com</a><br><br>
            
            Privacy info: All possible data is collected and sold for profit. The resultant monitary funds are then all put on red.
            
""".trimIndent()

            val textView = TextView(this)
            textView.text = Html.fromHtml(aboutText, Html.FROM_HTML_MODE_LEGACY)
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.setPadding(40, 30, 40, 30)

            AlertDialog.Builder(this)
                .setTitle("About")
                .setView(textView)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}
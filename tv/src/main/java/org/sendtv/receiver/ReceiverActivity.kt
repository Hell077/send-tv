package org.sendtv.receiver

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

/** Development shell only. No network listener or pairing credentials exist yet. */
class ReceiverActivity : Activity() {
    private val preferences by lazy { getSharedPreferences("receiver", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        setContentView(R.layout.receiver_home)
        findViewById<TextView>(R.id.receiver_name).text =
            preferences.getString("name", getString(R.string.default_receiver_name))
        findViewById<Button>(R.id.settings).apply {
            setOnClickListener { showSettings() }
            requestFocus()
        }
    }

    private fun showSettings() {
        setContentView(R.layout.receiver_settings)
        val name = findViewById<EditText>(R.id.name_input)
        name.setText(preferences.getString("name", getString(R.string.default_receiver_name)))
        findViewById<Button>(R.id.save_name).setOnClickListener {
            val proposed = name.text.toString()
            if (ReceiverName.isValid(proposed)) {
                preferences.edit().putString("name", proposed).apply()
                showHome()
            } else {
                name.error = getString(R.string.invalid_name)
                name.requestFocus()
            }
        }
        findViewById<Button>(R.id.back_home).apply {
            setOnClickListener { showHome() }
            requestFocus()
        }
    }
}

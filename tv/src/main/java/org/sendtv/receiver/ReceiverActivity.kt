package org.sendtv.receiver

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.lang.ref.WeakReference
import java.util.Locale

class ReceiverActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("receiver", MODE_PRIVATE) }
    private val receiver get() = application as ReceiverApplication
    private val main = Handler(Looper.getMainLooper())
    private var approval: Dialog? = null
    private var approvalId: String? = null
    private var lastQr: String? = null
    private var home = true
    private val tick =
        object : Runnable {
            override fun run() {
                updateHome()
                updateApproval()
                main.postDelayed(this, 1000)
            }
        }

    override fun attachBaseContext(base: Context) {
        val language = base.getSharedPreferences("receiver", MODE_PRIVATE).getString("language", "")
        val localized =
            if (language in listOf("en", "ru")) {
                val config = Configuration(base.resources.configuration)
                config.setLocale(Locale.forLanguageTag(language!!))
                base.createConfigurationContext(config)
            } else {
                base
            }
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    override fun onResume() {
        super.onResume()
        receiver.visibleActivity = WeakReference(this)
        main.post(tick)
    }

    override fun onPause() {
        main.removeCallbacks(tick)
        receiver.visibleActivity = null
        approval?.dismiss()
        approval = null
        approvalId = null
        super.onPause()
    }

    private fun showHome() {
        home = true
        lastQr = null
        setContentView(R.layout.receiver_home)
        findViewById<Button>(R.id.settings).apply {
            setOnClickListener { showSettings() }
            requestFocus()
        }
        findViewById<Button>(R.id.toggle_server).setOnClickListener {
            if (!receiver.isEnabled()) {
                AlertDialog
                    .Builder(this)
                    .setTitle(R.string.start_receiver)
                    .setMessage(R.string.privacy)
                    .setNegativeButton(R.string.back, null)
                    .setPositiveButton(R.string.start_receiver) { _, _ ->
                        receiver.setEnabled(true)
                        updateHome()
                    }.show()
            } else {
                receiver.setEnabled(false)
                updateHome()
            }
        }
        findViewById<Button>(R.id.devices).setOnClickListener { showDevices() }
        updateHome()
    }

    private fun updateHome() {
        if (!home) return
        label(R.id.receiver_name, receiver.receiverName)
        label(R.id.network_state, getString(receiver.networkMessageId))
        val address = receiver.address.takeIf { receiver.isEnabled() }
        label(R.id.toggle_server, getString(if (receiver.isEnabled()) R.string.stop_receiver else R.string.start_receiver))
        label(R.id.home_title, getString(if (address == null) R.string.receiver_stopped else R.string.ready))
        label(R.id.home_instructions, getString(if (address == null) R.string.start_instructions else R.string.scan_instructions))
        val qr = findViewById<ImageView>(R.id.pairing_qr)
        qr.visibility = if (address == null) View.GONE else View.VISIBLE
        label(R.id.receiver_address, address?.let { "http://$it" } ?: "")
        if (address != null) {
            val url = "http://$address/#challenge=${receiver.pairing.challenge().nonce}"
            if (url != lastQr) {
                val matrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 400, 400)
                val pixels =
                    IntArray(400 * 400) { if (matrix[it % 400, it / 400]) android.graphics.Color.BLACK else android.graphics.Color.WHITE }
                qr.setImageBitmap(Bitmap.createBitmap(pixels, 400, 400, Bitmap.Config.ARGB_8888))
                lastQr = url
            }
        } else {
            lastQr = null
        }
    }

    private fun label(
        id: Int,
        value: String,
    ) {
        val view = findViewById<TextView>(id)
        if (view.text.toString() != value) view.text = value
    }

    private fun updateApproval() {
        val pending = receiver.pairing.pending().firstOrNull()
        if (pending?.id != approvalId) {
            approval?.dismiss()
            approval = null
            approvalId = pending?.id
            if (pending != null) {
                val dialog = Dialog(this, R.style.Theme_SendTV)
                dialog.setContentView(R.layout.receiver_approval)
                dialog.setCancelable(false)
                dialog.findViewById<TextView>(R.id.sender_name).text = pending.deviceName
                dialog.findViewById<TextView>(R.id.verification).text = getString(R.string.verification_code, pending.verificationCode)
                for ((view, allow) in listOf(R.id.deny to false, R.id.allow to true)) {
                    dialog.findViewById<Button>(view).setOnClickListener {
                        try {
                            receiver.pairing.decide(pending.id, allow)
                        } catch (_: Exception) {
                            storageFailure()
                        }
                        dialog.dismiss()
                        approval = null
                        approvalId = null
                    }
                }
                dialog.show()
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                dialog.findViewById<Button>(R.id.deny).requestFocus()
                approval = dialog
            }
        }
        if (pending != null) {
            val remaining = ((pending.expiresAt - System.nanoTime() / 1_000_000) / 1000).coerceAtLeast(0).toInt()
            val message = getString(R.string.approval_expires, ((remaining + 4) / 5) * 5)
            approval?.findViewById<TextView>(R.id.approval_expiry)?.let { if (it.text.toString() != message) it.text = message }
        }
    }

    private fun showDevices() {
        val devices = receiver.pairing.devices()
        val labels = devices.map { getString(R.string.revoke_device, it.name) } + getString(R.string.revoke_all)
        AlertDialog
            .Builder(this)
            .setTitle(if (devices.isEmpty()) R.string.no_devices else R.string.devices)
            .setItems(labels.toTypedArray()) { _, index ->
                try {
                    receiver.pairing.revoke(devices.getOrNull(index)?.id)
                } catch (_: Exception) {
                    storageFailure()
                }
            }.setNegativeButton(R.string.back, null)
            .show()
    }

    private fun storageFailure() = Toast.makeText(this, R.string.storage_failure, Toast.LENGTH_LONG).show()

    internal fun showLaunchFailure() {
        AlertDialog
            .Builder(this)
            .setTitle(R.string.no_browser)
            .setMessage(R.string.no_browser_help)
            .setPositiveButton(R.string.back, null)
            .show()
    }

    private fun showSettings() {
        home = false
        setContentView(R.layout.receiver_settings)
        findViewById<Button>(R.id.language).setOnClickListener {
            val options = arrayOf(getString(R.string.language_system), "English", "Русский")
            val codes = listOf("", "en", "ru")
            AlertDialog
                .Builder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(options, codes.indexOf(prefs.getString("language", "")).coerceAtLeast(0)) { dialog, which ->
                    prefs.edit().putString("language", codes[which]).apply()
                    dialog.dismiss()
                    recreate()
                }.setNegativeButton(R.string.back, null)
                .show()
        }
        val name = findViewById<EditText>(R.id.name_input)
        val port = findViewById<EditText>(R.id.port_input)
        name.setText(receiver.receiverName)
        port.setText(receiver.receiverPort.toString())
        findViewById<Button>(R.id.save_name).setOnClickListener {
            val proposed = name.text.toString()
            val number = port.text.toString().toIntOrNull()
            if (!ReceiverName.isValid(proposed)) {
                name.error = getString(R.string.invalid_name)
                name.requestFocus()
            } else if (number == null || number !in 1024..65535) {
                port.error = getString(R.string.invalid_port)
                port.requestFocus()
            } else {
                prefs
                    .edit()
                    .putString("name", proposed)
                    .putInt("port", number)
                    .apply()
                receiver.refreshNetwork()
                showHome()
            }
        }
        findViewById<Button>(R.id.back_home).apply {
            setOnClickListener { showHome() }
            requestFocus()
        }
    }
}

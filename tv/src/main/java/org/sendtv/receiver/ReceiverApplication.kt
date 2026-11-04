package org.sendtv.receiver

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.sendtv.receiver.network.LanServer
import org.sendtv.receiver.network.ReceiverApi
import org.sendtv.receiver.security.DeviceStore
import org.sendtv.receiver.security.PairingManager
import org.sendtv.receiver.security.TrustedDevice
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class ReceiverApplication : Application() {
    private val main = Handler(Looper.getMainLooper())
    private val lifecycle = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("receiver", MODE_PRIVATE) }
    val pairing by lazy { PairingManager(PreferencesDeviceStore()) }
    private var server: LanServer? = null
    private var api: ReceiverApi? = null
    private val networks = ConcurrentHashMap.newKeySet<Network>()

    @Volatile private var enabled = false

    @Volatile var address: String? = null
        private set

    @Volatile var networkMessage: String = "Receiver stopped"
        private set
    var visibleActivity: WeakReference<ReceiverActivity>? = null
    val receiverPort: Int get() = prefs.getInt("port", 8765)

    override fun onCreate() {
        super.onCreate()
        if (!prefs.contains("deviceId")) prefs.edit().putString("deviceId", UUID.randomUUID().toString()).commit()
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    networks.add(network)
                    refreshNetwork()
                }

                override fun onLost(network: Network) {
                    networks.remove(network)
                    refreshNetwork()
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    properties: android.net.LinkProperties,
                ) = refreshNetwork()
            }
        val request =
            NetworkRequest
                .Builder()
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build()
        getSystemService(ConnectivityManager::class.java).registerNetworkCallback(request, callback)
    }

    fun isEnabled(): Boolean = enabled

    fun setEnabled(value: Boolean) {
        enabled = value
        refreshNetwork()
    }

    fun refreshNetwork() {
        lifecycle.execute {
            val connectivity = getSystemService(ConnectivityManager::class.java)
            val lan =
                if (enabled) {
                    networks.firstNotNullOfOrNull { network ->
                        val caps = connectivity.getNetworkCapabilities(network)
                        if (caps == null ||
                            !(
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            )
                        ) {
                            return@firstNotNullOfOrNull null
                        }
                        connectivity
                            .getLinkProperties(network)
                            ?.linkAddresses
                            ?.map { it.address }
                            ?.firstOrNull { it is Inet4Address && it.isSiteLocalAddress && !it.isLoopbackAddress }
                    }
                } else {
                    null
                }
            val selectedPort = receiverPort
            val next = lan?.hostAddress?.let { "$it:$selectedPort" }
            if (next != null && next == address && server != null) return@execute
            api?.close()
            api = null
            server?.close()
            server = null
            address = null
            pairing.endSession()
            networkMessage = if (enabled) "No private IPv4 Wi-Fi/Ethernet address" else "Receiver stopped"
            if (lan != null && next != null) {
                try {
                    val nextApi =
                        ReceiverApi(next, pairing, {
                            prefs.getString("deviceId", "")!! to prefs.getString("name", getString(R.string.default_receiver_name))!!
                        }, { asset -> assets.open("sender/$asset").use { it.readBytes() } }, { action -> main.post(action) }, ::launch)
                    server = LanServer(lan, selectedPort, nextApi::handle)
                    api = nextApi
                    address = next
                    networkMessage = "Network available"
                } catch (_: Exception) {
                    networkMessage = "Cannot listen on this port; check Settings"
                }
            }
        }
    }

    internal fun launch(url: String): String? {
        if (!enabled) return "RECEIVER_STOPPED"
        val activity = visibleActivity?.get() ?: return "RETURN_TO_RECEIVER"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
        if (packageManager.resolveActivity(intent, 0) == null) return "NO_BROWSER"
        return try {
            activity.startActivity(intent)
            null
        } catch (_: ActivityNotFoundException) {
            "NO_BROWSER"
        } catch (_: SecurityException) {
            "LAUNCH_FAILED"
        }
    }

    private inner class PreferencesDeviceStore : DeviceStore {
        override fun load(): List<TrustedDevice> =
            runCatching {
                val array = JsonParser.parseString(prefs.getString("devices", "[]")).asJsonArray
                require(array.size() <= 32)
                array.map {
                    val item = it.asJsonObject
                    val device = TrustedDevice(item["id"].asString, item["name"].asString, item["tokenHash"].asString)
                    require(
                        Regex("[A-Za-z0-9_-]{43}").matches(device.id) &&
                            Regex("[a-f0-9]{64}").matches(device.tokenHash) &&
                            ReceiverName.isValid(device.name),
                    )
                    device
                }
            }.getOrElse { emptyList() }

        override fun save(devices: List<TrustedDevice>) {
            check(prefs.edit().putString("devices", Gson().toJson(devices)).commit())
        }
    }
}

package org.sendtv.receiver

import android.content.Context
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 34])
class ReceiverActivityTest {
    @Before
    fun clearPreferences() {
        RuntimeEnvironment
            .getApplication()
            .getSharedPreferences("receiver", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun homeStartsStoppedAndFocusesSettings() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            assertEquals("Living Room TV", activity.findViewById<TextView>(R.id.receiver_name).text)
            assertTrue(activity.findViewById<Button>(R.id.settings).hasFocus())
            assertEquals("Receiver stopped", activity.findViewById<TextView>(R.id.network_state).text)
        }
    }

    @Test
    fun ownerCanRenameAndNameSurvivesActivityRecreation() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            activity.findViewById<Button>(R.id.settings).performClick()
            assertTrue(activity.findViewById<Button>(R.id.back_home).hasFocus())
            assertEquals(
                R.id.save_name,
                activity.findViewById<Button>(R.id.back_home).focusSearch(View.FOCUS_RIGHT).id,
            )
            activity.findViewById<EditText>(R.id.name_input).setText("Bedroom TV")
            activity.findViewById<Button>(R.id.save_name).performClick()
            controller.recreate()
            assertEquals("Bedroom TV", controller.get().findViewById<TextView>(R.id.receiver_name).text)
        }
    }

    @Test
    fun malformedNameShowsErrorAndDoesNotPersist() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            activity.findViewById<Button>(R.id.settings).performClick()
            val input = activity.findViewById<EditText>(R.id.name_input)
            input.setText("\u202Espoofed")
            activity.findViewById<Button>(R.id.save_name).performClick()
            assertNotNull(input.error)
            activity.findViewById<Button>(R.id.back_home).performClick()
            assertEquals("Living Room TV", activity.findViewById<TextView>(R.id.receiver_name).text)
        }
    }

    @Test
    fun receiverRequestsOnlyNetworkPermissionsAndCannotHandleRemoteCommands() {
        val app = RuntimeEnvironment.getApplication()
        val info = app.packageManager.getPackageInfo(app.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
        assertEquals(setOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"), info.requestedPermissions?.toSet())
        val intent =
            android.content
                .Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://example.com"))
                .setPackage(app.packageName)
        assertTrue(app.packageManager.queryIntentActivities(intent, 0).isEmpty())
    }

    @Test
    fun pollingDoesNotRepeatUnchangedAccessibilityText() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            var changes = 0
            activity.findViewById<TextView>(R.id.network_state).addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) = Unit

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        changes++
                    }

                    override fun afterTextChanged(s: Editable?) = Unit
                },
            )
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
            assertEquals(0, changes)
        }
    }

    @Test
    fun dpadCanReachPortAndInvalidPortDoesNotSaveName() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            activity.findViewById<Button>(R.id.settings).performClick()
            val name = activity.findViewById<EditText>(R.id.name_input)
            assertEquals(R.id.port_input, name.focusSearch(View.FOCUS_DOWN).id)
            name.setText("Should not persist")
            val port = activity.findViewById<EditText>(R.id.port_input)
            port.setText("80")
            activity.findViewById<Button>(R.id.save_name).performClick()
            assertNotNull(port.error)
            activity.findViewById<Button>(R.id.back_home).performClick()
            assertEquals("Living Room TV", activity.findViewById<TextView>(R.id.receiver_name).text)
        }
    }
}

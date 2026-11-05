package org.sendtv.receiver

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Looper
import android.view.KeyEvent
import android.widget.Button
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowDialog
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30, 34])
class ReceiverIntegrationTest {
    @Test fun dpadCenterOnDefaultDenyCannotApprove() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val app = controller.get().application as ReceiverApplication
            val request = app.pairing.request(app.pairing.challenge().nonce, "Denied phone", "test")
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
            val dialog = ShadowDialog.getLatestDialog()
            assertTrue(dialog.findViewById<Button>(R.id.deny).hasFocus())
            dialog.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER))
            dialog.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER))
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals("denied", request.status)
            assertNull(request.accessToken)
            assertTrue(app.pairing.devices().isEmpty())
        }
    }

    @Test fun approvalBeginsOnDenyAndOnlyOwnerClickGrantsToken() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val app = controller.get().application as ReceiverApplication
            val request = app.pairing.request(app.pairing.challenge().nonce, "Phone", "test")
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
            val dialog = ShadowDialog.getLatestDialog()
            assertTrue(dialog.findViewById<Button>(R.id.deny).hasFocus())
            assertNull(request.accessToken)
            dialog.findViewById<Button>(R.id.allow).performClick()
            assertEquals("Phone", app.pairing.authenticate(request.accessToken!!).name)
            app.pairing.revoke(request.deviceId)
            assertEquals("denied", app.pairing.poll(request.id, request.pollToken).status)
        }
    }

    @Test fun dispatchUsesBrowsableViewIntentAndRequiresVisibleReceiver() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val app = activity.application as ReceiverApplication
            app.setEnabled(true)
            val url = "https://example.com/?fixture=redacted"
            val expected = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
            val handler =
                ResolveInfo().apply {
                    activityInfo =
                        ActivityInfo().apply {
                            packageName = "test.browser"
                            name = "TestBrowser"
                        }
                }
            shadowOf(app.packageManager).addResolveInfoForIntent(expected, handler)
            assertNull(app.launch(url))
            val sent = shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, sent.action)
            assertEquals(url, sent.dataString)
            assertTrue(sent.categories.contains(Intent.CATEGORY_BROWSABLE))
            controller.pause()
            assertEquals("RETURN_TO_RECEIVER", app.launch(url))
            app.setEnabled(false)
            assertEquals("RECEIVER_STOPPED", app.launch(url))
        }
    }

    @Test fun missingHandlerReturnsActionableFailureWithoutStartingActivity() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            val app = activity.application as ReceiverApplication
            app.setEnabled(true)
            assertEquals("NO_BROWSER", app.launch("https://example.com/"))
            assertEquals(activity.getString(R.string.no_browser_help), shadowOf(ShadowAlertDialog.getLatestAlertDialog()).message)
            assertNull(shadowOf(activity).nextStartedActivity)
            app.setEnabled(false)
        }
    }
}

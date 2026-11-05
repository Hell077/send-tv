package org.sendtv.receiver

import android.content.Context
import android.widget.Button
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30, 34], qualifiers = "ru")
class ReceiverLanguageTest {
    @Before fun clearPreferences() {
        RuntimeEnvironment
            .getApplication()
            .getSharedPreferences("receiver", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test fun russianSystemLanguageIncludesWarningAndCode() {
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            assertEquals("Приёмник остановлен", activity.findViewById<TextView>(R.id.network_state).text)
            assertEquals("Настройки", activity.findViewById<Button>(R.id.settings).text)
            assertTrue(activity.getString(R.string.privacy).contains("не шифрует"))
            assertTrue(activity.getString(R.string.privacy).contains("подтверждения на этом ТВ"))
            assertEquals("Код подтверждения: 123456", activity.getString(R.string.verification_code, "123456"))
            activity.findViewById<Button>(R.id.settings).performClick()
            assertEquals("Язык", activity.findViewById<Button>(R.id.language).text)
            assertTrue(activity.findViewById<Button>(R.id.back_home).hasFocus())
        }
    }

    @Test
    @Config(qualifiers = "en")
    fun russianOverrideUsesSameReceiverNameAsApiIdentity() {
        val app = RuntimeEnvironment.getApplication() as ReceiverApplication
        app
            .getSharedPreferences("receiver", Context.MODE_PRIVATE)
            .edit()
            .putString("language", "ru")
            .commit()
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            assertEquals("Настройки", activity.findViewById<Button>(R.id.settings).text)
            assertEquals(app.receiverName, activity.findViewById<TextView>(R.id.receiver_name).text)
            assertEquals("Living Room TV", app.receiverName)
        }
    }

    @Test
    @Config(qualifiers = "fr")
    fun unsupportedSavedLanguageFallsBackWithoutChangingAccess() {
        val app = RuntimeEnvironment.getApplication()
        val prefs = app.getSharedPreferences("receiver", Context.MODE_PRIVATE)
        prefs.edit().putString("language", "unknown").commit()
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            assertEquals("Settings", activity.findViewById<Button>(R.id.settings).text)
            assertTrue((app as ReceiverApplication).pairing.devices().isEmpty())
            activity.findViewById<Button>(R.id.settings).performClick()
            val save = activity.findViewById<Button>(R.id.save_name)
            assertEquals(R.id.language, save.focusSearch(android.view.View.FOCUS_RIGHT).id)
        }
    }

    @Test fun savedEnglishOverrideDoesNotChangeSystemLocaleOrIdentity() {
        val app = RuntimeEnvironment.getApplication()
        app
            .getSharedPreferences("receiver", Context.MODE_PRIVATE)
            .edit()
            .putString("language", "en")
            .putString("name", "Мой ТВ")
            .commit()
        Robolectric.buildActivity(ReceiverActivity::class.java).setup().use { controller ->
            val activity = controller.get()
            assertEquals("Settings", activity.findViewById<Button>(R.id.settings).text)
            assertEquals("Мой ТВ", activity.findViewById<TextView>(R.id.receiver_name).text)
            assertEquals("Настройки", app.getString(R.string.settings))
        }
    }
}

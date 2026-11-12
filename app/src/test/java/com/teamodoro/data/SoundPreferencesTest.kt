package com.teamodoro.data

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundPreferencesTest {

    @Test
    fun `focus tick defaults to off`() {
        assertFalse(SoundPreferences(FakeSharedPreferences()).isFocusTickEnabled())
    }

    @Test
    fun `focus tick reads back what was set`() {
        val preferences = SoundPreferences(FakeSharedPreferences())

        preferences.setFocusTickEnabled(true)

        assertTrue(preferences.isFocusTickEnabled())
    }

    @Test
    fun `focus tick persists across instances on the same store`() {
        val store = FakeSharedPreferences()
        SoundPreferences(store).setFocusTickEnabled(true)

        assertTrue(SoundPreferences(store).isFocusTickEnabled())
    }

    @Test
    fun `setting the tick leaves the sound choices unchanged`() {
        val preferences = SoundPreferences(FakeSharedPreferences())
        preferences.set(SoundEvent.FOCUS_START, TransitionSound.Silent)
        preferences.set(SoundEvent.FOCUS_END, TransitionSound.Custom("content://sounds/1"))

        preferences.setFocusTickEnabled(true)

        assertEquals(TransitionSound.Silent, preferences.get(SoundEvent.FOCUS_START))
        assertEquals(
            TransitionSound.Custom("content://sounds/1"),
            preferences.get(SoundEvent.FOCUS_END),
        )
    }
}

/** In-memory store: the repo has no Robolectric, so a test can't build a real one. */
private class FakeSharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()
    override fun getString(key: String, defValue: String?): String? =
        values[key] as String? ?: defValue
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
        throw UnsupportedOperationException()
    override fun getInt(key: String, defValue: Int): Int = values[key] as Int? ?: defValue
    override fun getLong(key: String, defValue: Long): Long = values[key] as Long? ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = values[key] as Float? ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        values[key] as Boolean? ?: defValue
    override fun contains(key: String): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor()
    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners += listener
    }
    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners -= listener
    }

    private inner class FakeEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false

        private fun put(key: String, value: Any?) = apply { pending[key] = value }

        override fun putString(key: String, value: String?) = put(key, value)
        override fun putStringSet(key: String, values: MutableSet<String>?) = put(key, values)
        override fun putInt(key: String, value: Int) = put(key, value)
        override fun putLong(key: String, value: Long) = put(key, value)
        override fun putFloat(key: String, value: Float) = put(key, value)
        override fun putBoolean(key: String, value: Boolean) = put(key, value)
        override fun remove(key: String) = put(key, null)
        override fun clear() = apply { clear = true }

        override fun commit(): Boolean {
            if (clear) values.clear()
            for ((key, value) in pending) {
                if (value == null) values.remove(key) else values[key] = value
                listeners.toList().forEach { it.onSharedPreferenceChanged(this@FakeSharedPreferences, key) }
            }
            return true
        }

        override fun apply() {
            commit()
        }
    }
}

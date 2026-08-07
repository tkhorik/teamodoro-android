package com.teamodoro.locale

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.teamodoro.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around AndroidX's per-app language APIs
 * (https://developer.android.com/guide/topics/resources/app-languages).
 *
 * There are two moving parts, both backed by the same [AppCompatDelegate] call:
 *
 * - API 33+: the system provides its own "Language" entry under
 *   Settings > Apps > Teamodoro, backed by the platform LocaleManager and driven
 *   by [android.R.attr.localeConfig] / res/xml/locales_config.xml in the manifest.
 *   [systemPickerAvailable] and [systemLanguageSettingsIntent] deep-link into it.
 * - API 26-32: there is no such system screen, so this app must offer its own
 *   picker. Persistence for that case is handled by AppCompat itself (the
 *   `autoStoreLocales` meta-data in AndroidManifest.xml), not by this class.
 *
 * [MainActivity][com.teamodoro.ui.MainActivity] must extend `AppCompatActivity`
 * for [AppCompatDelegate.setApplicationLocales] to actually take effect on a
 * Compose screen below API 33 - a plain `ComponentActivity` will silently no-op.
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** One entry in the in-app picker. [tag] is a BCP-47 tag, or null for "follow the system". */
    data class AppLanguage(
        val tag: String?,
        val displayName: String,
    )

    /** "System default" first, then every locale declared in locales_config.xml, each in its own language. */
    val supportedLanguages: List<AppLanguage> by lazy {
        buildList {
            add(AppLanguage(tag = null, displayName = context.getString(R.string.settings_language_system_default)))
            SUPPORTED_LANGUAGE_TAGS.forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                val name = locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
                add(AppLanguage(tag = tag, displayName = name))
            }
        }
    }

    /** The active override, or null when the app is following the system language. */
    fun currentTag(): String? {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) null else locales[0]?.toLanguageTag()
    }

    /** Applies [tag] app-wide, or pass null to clear the override and follow the system again. */
    fun setLanguage(tag: String?) {
        val locales = if (tag == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** True on API 33+, where Settings has a dedicated per-app language screen. */
    fun systemPickerAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** Deep-links into Settings > Apps > Teamodoro > Language. Only meaningful when [systemPickerAvailable] is true. */
    fun systemLanguageSettingsIntent(): Intent =
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))

    companion object {
        /** Keep in sync with res/xml/locales_config.xml and the values-<tag>/ resource directories. */
        val SUPPORTED_LANGUAGE_TAGS = listOf("en", "es", "fr", "de", "ru", "uk", "ro", "zh-CN", "zh-TW")
    }
}

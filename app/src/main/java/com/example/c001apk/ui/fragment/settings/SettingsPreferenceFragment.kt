package com.example.c001apk.ui.fragment.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.example.c001apk.R
import com.example.c001apk.util.PrefManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rikka.core.util.ResourceUtils
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    class SettingsPreferenceDataStore : PreferenceDataStore() {
        override fun getString(key: String?, defValue: String?): String {
            return when (key) {
                "darkTheme" -> PrefManager.darkTheme.toString()
                "themeColor" -> PrefManager.themeColor
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putString(key: String?, value: String?) {
            when (key) {
                "darkTheme" -> PrefManager.darkTheme = value!!.toInt()
                "themeColor" -> PrefManager.themeColor = value!!
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "blackDarkTheme" -> PrefManager.blackDarkTheme
                "followSystemAccent" -> PrefManager.followSystemAccent
                "showEmoji" -> PrefManager.showEmoji
                "allHuaji" -> PrefManager.allHuaji
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "blackDarkTheme" -> PrefManager.blackDarkTheme = value
                "followSystemAccent" -> PrefManager.followSystemAccent = value
                "showEmoji" -> PrefManager.showEmoji = value
                "allHuaji" -> PrefManager.allHuaji = value
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
        setPreferencesFromResource(R.xml.settings, rootKey)

        if (!PrefManager.isLogin){
            val displayOptions = findPreference("logout_preference_settings") as PreferenceCategory?
            preferenceScreen.removePreference(displayOptions!!)
        }

        /*if (PrefManager.isLogin) {
            val displayOptions = findPreference("login_preference_settings") as PreferenceCategory?
            preferenceScreen.removePreference(displayOptions!!)
        } else {
            val displayOptions = findPreference("logout_preference_settings") as PreferenceCategory?
            preferenceScreen.removePreference(displayOptions!!)
        }*/

        //val displayOptions= findPreference("login_preference_settings") as PreferenceCategory?
        //displayOptions!!.removePreference(findPreference("login")!!)


        findPreference<SimpleMenuPreference>("darkTheme")?.setOnPreferenceChangeListener { _, newValue ->
            val newMode = (newValue as String).toInt()
            if (PrefManager.darkTheme != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode)
                activity?.recreate()
            }
            true
        }

        findPreference<MaterialSwitchPreference>("blackDarkTheme")?.setOnPreferenceChangeListener { _, _ ->
            if (ResourceUtils.isNightMode(requireContext().resources.configuration))
                activity?.recreate()
            true
        }

        findPreference<MaterialSwitchPreference>("followSystemAccent")?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }

        findPreference<SimpleMenuPreference>("themeColor")?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }

        findPreference<MaterialSwitchPreference>("showEmoji")?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }

        findPreference<MaterialSwitchPreference>("allHuaji")?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }

        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.logoutTitle)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.isLogin = false
                    PrefManager.uid = ""
                    PrefManager.name = ""
                    PrefManager.token = ""
                }
                show()
            }
            true
        }


    }

}
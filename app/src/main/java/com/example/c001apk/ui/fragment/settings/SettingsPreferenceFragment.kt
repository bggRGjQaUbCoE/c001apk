package com.example.c001apk.ui.fragment.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BuildConfig
import com.example.c001apk.R
import com.example.c001apk.ui.activity.AboutActivity
import com.example.c001apk.ui.activity.BlackListActivity
import com.example.c001apk.ui.activity.SBCKActivity
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rikka.core.util.ResourceUtils
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        val recyclerView =
            super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.apply {
            //overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
        }
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(resources.getDrawable(R.drawable.divider, requireContext().theme))
    }

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
                "customToken" -> PrefManager.customToken
                "isFullImageQuality" -> PrefManager.isFullImageQuality
                "isClearKeyWord" -> PrefManager.isClearKeyWord
                "isKeepFeed" -> PrefManager.isKeepFeed
                "isRecordHistory" -> PrefManager.isRecordHistory
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "blackDarkTheme" -> PrefManager.blackDarkTheme = value
                "followSystemAccent" -> PrefManager.followSystemAccent = value
                "showEmoji" -> PrefManager.showEmoji = value
                "allHuaji" -> PrefManager.allHuaji = value
                "customToken" -> PrefManager.customToken = value
                "isFullImageQuality" -> PrefManager.isFullImageQuality = value
                "isClearKeyWord" -> PrefManager.isClearKeyWord = value
                "isKeepFeed" -> PrefManager.isKeepFeed = value
                "isRecordHistory" -> PrefManager.isRecordHistory = value
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }
    }

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
        setPreferencesFromResource(R.xml.settings, rootKey)

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

        findPreference<Preference>("about")?.summary =
            "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            true
        }


        findPreference<Preference>("sbparams")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), SBCKActivity::class.java))
            true
        }

        findPreference<Preference>("zmlmId")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.SZLMID)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle(requireContext().getString(R.string.zmlmId))
                setNeutralButton("重新生成DeviceCode"){_, _ ->
                    PrefManager.SZLMID = editText.text.toString()
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode()
                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.SZLMID = editText.text.toString()
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode()
                }
                show()
            }
            true
        }

        findPreference<Preference>("blackList")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), BlackListActivity::class.java))
            true
        }

    }

}
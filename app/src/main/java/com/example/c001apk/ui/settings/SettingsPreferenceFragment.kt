package com.example.c001apk.ui.settings

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.BuildConfig
import com.example.c001apk.R
import com.example.c001apk.ui.blacklist.BlackListActivity
import com.example.c001apk.ui.main.MainActivity
import com.example.c001apk.ui.others.AboutActivity
import com.example.c001apk.ui.settings.params.ParamsActivity
import com.example.c001apk.util.CacheDataManager
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils.getDeviceCode
import com.example.c001apk.util.TokenDeviceUtils.randHexString
import com.example.c001apk.util.doOnMainThreadIdle
import com.example.c001apk.util.setBottomPaddingSpace
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        (activity as? MainActivity)?.hideNavigationView()
                    } else if (dy < 0) {
                        (activity as? MainActivity)?.showNavigationView()
                    }
                }
            })

            doOnMainThreadIdle {
                recyclerView.setBottomPaddingSpace()
            }

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
                "darkTheme" -> PrefManager.darkTheme = value?.toInt() ?: 0
                "themeColor" -> PrefManager.themeColor = value ?: "MATERIAL_DEFAULT"
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return when (key) {
                "blackDarkTheme" -> PrefManager.blackDarkTheme
                "followSystemAccent" -> PrefManager.followSystemAccent
                "showEmoji" -> PrefManager.showEmoji
                "customToken" -> PrefManager.customToken
                "isRecordHistory" -> PrefManager.isRecordHistory
                "isIconMiniCard" -> PrefManager.isIconMiniCard
                "isOpenLinkOutside" -> PrefManager.isOpenLinkOutside
                "isColorFilter" -> PrefManager.isColorFilter
                "isCheckUpdate" -> PrefManager.isCheckUpdate
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putBoolean(key: String?, value: Boolean) {
            when (key) {
                "blackDarkTheme" -> PrefManager.blackDarkTheme = value
                "followSystemAccent" -> PrefManager.followSystemAccent = value
                "showEmoji" -> PrefManager.showEmoji = value
                "customToken" -> PrefManager.customToken = value
                "isRecordHistory" -> PrefManager.isRecordHistory = value
                "isIconMiniCard" -> PrefManager.isIconMiniCard = value
                "isOpenLinkOutside" -> PrefManager.isOpenLinkOutside = value
                "isColorFilter" -> PrefManager.isColorFilter = value
                "isCheckUpdate" -> PrefManager.isCheckUpdate = value
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<SimpleMenuPreference>("darkTheme")?.setOnPreferenceChangeListener { _, newValue ->
            val newMode = (newValue as String).toInt()
            if (PrefManager.darkTheme != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode)
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

        findPreference<Preference>("about")?.summary =
            "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            IntentUtil.startActivity<AboutActivity>(requireContext()) {}
            true
        }


        findPreference<Preference>("params")?.setOnPreferenceClickListener {
            IntentUtil.startActivity<ParamsActivity>(requireContext()) {}
            true
        }

        findPreference<Preference>("szlmId")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimaryDark,
                    0
                ), 128
            )
            editText.setText(PrefManager.SZLMID)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle(requireContext().getString(R.string.szlmId))
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.SZLMID = editText.text.toString()
                    PrefManager.xAppDevice = getDeviceCode(false)
                }
                if (BuildConfig.DEBUG) {
                    setNeutralButton(R.string.random_value) { _, _ ->
                        PrefManager.SZLMID = randHexString(16)
                        PrefManager.xAppDevice = getDeviceCode(false)
                    }
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("userBlackList")?.setOnPreferenceClickListener {
            IntentUtil.startActivity<BlackListActivity>(requireContext()) {
                putExtra("type", "user")
            }
            true
        }

        findPreference<Preference>("topicBlackList")?.setOnPreferenceClickListener {
            IntentUtil.startActivity<BlackListActivity>(requireContext()) {
                putExtra("type", "topic")
            }
            true
        }

        findPreference<Preference>("fontScale")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_font_scale, null, false)
            val slider: Slider = view.findViewById(R.id.slider)
            val fontScale: TextView = view.findViewById(R.id.fontScale)
            slider.apply {
                valueFrom = 0.80f
                valueTo = 1.30f
                value = PrefManager.FONTSCALE.toFloat()
                setLabelFormatter { value ->
                    String.format("%.2f", value)
                }
            }
            slider.addOnChangeListener { _, value, _ ->
                fontScale.text = "字体大小: ${String.format("%.2f", value)}"
                fontScale.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f * value)
            }
            fontScale.text = "字体大小: ${PrefManager.FONTSCALE}"
            fontScale.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f * PrefManager.FONTSCALE.toFloat())
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle(R.string.font_scale)
                setNegativeButton(android.R.string.cancel, null)
                setNeutralButton("重置") { _, _ ->
                    PrefManager.FONTSCALE = "1.00"
                    (requireActivity() as? MainActivity)?.recreate()
                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.FONTSCALE = String.format("%.2f", slider.value)
                    (requireActivity() as? MainActivity)?.recreate()
                }
                show()
            }
            true
        }


        findPreference<Preference>("clearCache")?.apply {
            summary = CacheDataManager.getTotalCacheSize(requireContext())
            setOnPreferenceClickListener {
                val currentSize = CacheDataManager.getTotalCacheSize(requireContext())
                summary = currentSize
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("确定清除缓存吗？")
                    setMessage("当前缓存$currentSize")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        CacheDataManager.clearAllCache(requireContext())
                        summary = "刚刚清理"
                    }
                    show()
                }
                true
            }
        }

        findPreference<Preference>("imageQuality")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("图片画质")
                val items = arrayOf("网络自适应", "原图", "普清")
                val index = when (PrefManager.imageQuality) {
                    "auto" -> 0
                    "origin" -> 1
                    else -> 2
                }
                setSingleChoiceItems(
                    items,
                    index
                ) { dialog: DialogInterface, position: Int ->
                    when (position) {
                        0 -> PrefManager.imageQuality = "auto"

                        1 -> PrefManager.imageQuality = "origin"

                        2 -> PrefManager.imageQuality = "thumbnail"
                    }
                    dialog.dismiss()
                }
                show()
            }
            true
        }

        findPreference<MaterialSwitchPreference>("isColorFilter")?.setOnPreferenceChangeListener { _, _ ->
            if (ResourceUtils.isNightMode(requireContext().resources.configuration))
                activity?.recreate()
            true
        }

    }

}
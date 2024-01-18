package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.util.TokenDeviceUtils
import com.example.c001apk.util.TokenDeviceUtils.Companion.randHexString
import com.example.c001apk.util.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ParamsPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        val recyclerView =
            super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.apply {
            isVerticalScrollBarEnabled = false
        }
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(resources.getDrawable(R.drawable.divider, requireContext().theme))
    }

    @SuppressLint("SetTextI18n", "RestrictedApi", "InflateParams")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sb_params, null)

        findPreference<Preference>("VERSION_NAME")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.VERSION_NAME)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("VERSION_NAME")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.VERSION_NAME =
                        editText.text.toString().ifEmpty { Constants.VERSION_NAME }
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("API_VERSION")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.API_VERSION)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("API_VERSION")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.API_VERSION =
                        editText.text.toString().ifEmpty { Constants.API_VERSION }
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("VERSION_CODE")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.VERSION_CODE)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("VERSION_CODE")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.VERSION_CODE =
                        editText.text.toString().ifEmpty { Constants.VERSION_CODE }
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("MANUFACTURER")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.MANUFACTURER)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("MANUFACTURER")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.MANUFACTURER = android.os.Build.MANUFACTURER
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.MANUFACTURER =
                        editText.text.toString().ifEmpty { Utils.randomManufacturer() }
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode(false)
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("BRAND")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.BRAND)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("BRAND")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.BRAND = android.os.Build.BRAND
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.BRAND = editText.text.toString().ifEmpty { Utils.randomBrand() }
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode(false)
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("MODEL")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.MODEL)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("MODEL")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.MODEL = android.os.Build.MODEL
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.MODEL =
                        editText.text.toString().ifEmpty { Utils.randomDeviceModel() }
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode(false)
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("BUILDNUMBER")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.BUILDNUMBER)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("BUILDNUMBER")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.BUILDNUMBER = android.os.Build.DISPLAY
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.BUILDNUMBER =
                        editText.text.toString().ifEmpty { randHexString(16) }
                    PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode(false)
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("SDK_INT")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.SDK_INT)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("SDK_INT")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.SDK_INT = android.os.Build.VERSION.SDK_INT.toString()
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.SDK_INT = editText.text.toString()
                        .ifEmpty { Utils.randomSdkInt() }
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("ANDROID_VERSION")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.ANDROID_VERSION)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("ANDROID_VERSION")
                setNeutralButton("使用系统信息") { _, _ ->
                    PrefManager.ANDROID_VERSION = android.os.Build.VERSION.RELEASE
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"

                }
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.ANDROID_VERSION =
                        editText.text.toString().ifEmpty { Utils.randomAndroidVersionRelease() }
                    PrefManager.USER_AGENT =
                        "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("USER_AGENT")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.USER_AGENT)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("USER_AGENT")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.USER_AGENT = editText.text.toString()
                        .ifEmpty { Constants.USER_AGENT }
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("xAppToken")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.xAppToken)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("X-App-Token")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.xAppToken = editText.text.toString()
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("xAppDevice")?.setOnPreferenceClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_x_app_token, null, false)
            val editText: EditText = view.findViewById(R.id.editText)
            editText.highlightColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimaryDark
                ), 128
            )
            editText.setText(PrefManager.xAppDevice)
            MaterialAlertDialogBuilder(requireContext()).apply {
                setView(view)
                setTitle("X-App-Device")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    PrefManager.xAppDevice = editText.text.toString()
                }
            }.create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                editText.requestFocus()
            }.show()
            true
        }

        findPreference<Preference>("regenerate")?.setOnPreferenceClickListener {
            PrefManager.xAppDevice = TokenDeviceUtils.getDeviceCode(true)
            ToastUtil.toast("已重新生成")
            true
        }

    }

}
package com.example.c001apk.ui.settings.params

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.core.graphics.ColorUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils.getDeviceCode
import com.example.c001apk.util.TokenDeviceUtils.randHexString
import com.example.c001apk.util.Utils.randomAndroidVersionRelease
import com.example.c001apk.util.Utils.randomBrand
import com.example.c001apk.util.Utils.randomDeviceModel
import com.example.c001apk.util.Utils.randomManufacturer
import com.example.c001apk.util.Utils.randomSdkInt
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ParamsPreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

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
        PrefManager.registerOnSharedPreferenceChangeListener(this)
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.params, null)

        findPreference<Preference>("VERSION_NAME")?.apply {
            summary = PrefManager.VERSION_NAME
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.VERSION_NAME)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("VERSION_NAME")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.VERSION_NAME =
                            editText.text.toString().ifEmpty { Constants.VERSION_NAME }
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("API_VERSION")?.apply {
            summary = PrefManager.API_VERSION
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.API_VERSION)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("API_VERSION")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.API_VERSION =
                            editText.text.toString().ifEmpty { Constants.API_VERSION }
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("VERSION_CODE")?.apply {
            summary = PrefManager.VERSION_CODE
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.VERSION_CODE)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("VERSION_CODE")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.VERSION_CODE =
                            editText.text.toString().ifEmpty { Constants.VERSION_CODE }
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("MANUFACTURER")?.apply {
            summary = PrefManager.MANUFACTURER
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.MANUFACTURER)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("MANUFACTURER")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.MANUFACTURER = android.os.Build.MANUFACTURER
                        PrefManager.xAppDevice = getDeviceCode(false)
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.MANUFACTURER =
                            editText.text.toString().ifEmpty { randomManufacturer() }
                        PrefManager.xAppDevice = getDeviceCode(false)
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.MANUFACTURER = randomManufacturer()
                        PrefManager.xAppDevice = getDeviceCode(false)
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("BRAND")?.apply {
            summary = PrefManager.BRAND
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.BRAND)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("BRAND")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.BRAND = android.os.Build.BRAND
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.BRAND = editText.text.toString().ifEmpty { randomBrand() }
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.BRAND = randomBrand()
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("MODEL")?.apply {
            summary = PrefManager.MODEL
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.MODEL)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("MODEL")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.MODEL = android.os.Build.MODEL
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.MODEL =
                            editText.text.toString().ifEmpty { randomDeviceModel() }
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.MODEL = randomDeviceModel()
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("BUILDNUMBER")?.apply {
            summary = PrefManager.BUILDNUMBER
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.BUILDNUMBER)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("BUILDNUMBER")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.BUILDNUMBER = android.os.Build.DISPLAY
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.BUILDNUMBER =
                            editText.text.toString().ifEmpty { randHexString(16) }
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.BUILDNUMBER = randHexString(16)
                        PrefManager.xAppDevice = getDeviceCode(false)
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("SDK_INT")?.apply {
            summary = PrefManager.SDK_INT
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.SDK_INT)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("SDK_INT")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.SDK_INT = android.os.Build.VERSION.SDK_INT.toString()
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.SDK_INT = editText.text.toString().ifEmpty { randomSdkInt() }
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.SDK_INT = randomSdkInt()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("ANDROID_VERSION")?.apply {
            summary = PrefManager.ANDROID_VERSION
            setOnPreferenceClickListener {
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
                editText.setText(PrefManager.ANDROID_VERSION)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(view)
                    setTitle("ANDROID_VERSION")
                    setNeutralButton(R.string.system_info) { _, _ ->
                        PrefManager.ANDROID_VERSION = android.os.Build.VERSION.RELEASE
                        updateUserAgent()
                    }
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        PrefManager.ANDROID_VERSION =
                            editText.text.toString().ifEmpty { randomAndroidVersionRelease() }
                        updateUserAgent()
                    }
                    setNegativeButton(R.string.random_value) { _, _ ->
                        PrefManager.ANDROID_VERSION = randomAndroidVersionRelease()
                        updateUserAgent()
                    }
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
                true
            }
        }

        findPreference<Preference>("USER_AGENT")?.apply {
            summary = PrefManager.USER_AGENT
            setOnPreferenceClickListener {
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
        }

        findPreference<Preference>("xAppToken")?.apply {
            summary = PrefManager.xAppToken
            setOnPreferenceClickListener {
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
        }

        findPreference<Preference>("xAppDevice")?.apply {
            summary = PrefManager.xAppDevice
            setOnPreferenceClickListener {
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
        }

        findPreference<Preference>("regenerate")?.setOnPreferenceClickListener {
            PrefManager.xAppDevice = getDeviceCode(true)
            Snackbar.make(requireView(), "已重新生成", Snackbar.LENGTH_SHORT).show()
            true
        }

    }

    private fun updateUserAgent() {
        PrefManager.USER_AGENT =
            "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${PrefManager.VERSION_NAME}-${PrefManager.VERSION_CODE}-${Constants.MODE}"
    }

    override fun onDestroyView() {
        PrefManager.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "VERSION_NAME" -> findPreference<Preference>(key)?.summary = PrefManager.VERSION_NAME
            "VERSION_CODE" -> findPreference<Preference>(key)?.summary = PrefManager.VERSION_CODE
            "API_VERSION" -> findPreference<Preference>(key)?.summary = PrefManager.API_VERSION
            "MANUFACTURER" -> findPreference<Preference>(key)?.summary = PrefManager.MANUFACTURER
            "BRAND" -> findPreference<Preference>(key)?.summary = PrefManager.BRAND
            "MODEL" -> findPreference<Preference>(key)?.summary = PrefManager.MODEL
            "BUILDNUMBER" -> findPreference<Preference>(key)?.summary = PrefManager.BUILDNUMBER
            "SDK_INT" -> findPreference<Preference>(key)?.summary = PrefManager.SDK_INT
            "ANDROID_VERSION" -> findPreference<Preference>(key)?.summary = PrefManager.ANDROID_VERSION
            "USER_AGENT" -> findPreference<Preference>(key)?.summary = PrefManager.USER_AGENT
            "xAppDevice" -> findPreference<Preference>(key)?.summary = PrefManager.xAppDevice
        }
    }
}
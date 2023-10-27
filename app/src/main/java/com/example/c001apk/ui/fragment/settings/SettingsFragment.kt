package com.example.c001apk.ui.fragment.settings

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.c001apk.BuildConfig
import com.example.c001apk.R
import com.example.c001apk.databinding.DialogAboutBinding
import com.example.c001apk.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rikka.material.app.LocaleDelegate

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu()

    }

    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.settings_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.feedback ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/bggRGjQaUbCoE/c001apk/issues")
                        )
                    )

                R.id.about -> AboutDialog().show(childFragmentManager, "about")

            }
            return@setOnMenuItemClickListener true
        }
    }

    class AboutDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val binding: DialogAboutBinding =
                DialogAboutBinding.inflate(layoutInflater, null, false)
            binding.designAboutTitle.setText(R.string.app_name)
            binding.designAboutInfo.movementMethod = LinkMovementMethod.getInstance()
            binding.designAboutInfo.text = HtmlCompat.fromHtml(
                getString(
                    R.string.about_view_source_code,
                    "<b><a href=\"https://github.com/bggRGjQaUbCoE/c001apk\">GitHub</a></b>",
                ), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.designAboutVersion.text = java.lang.String.format(
                LocaleDelegate.defaultLocale,
                "%s (%d)",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
            return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
        }
    }


}
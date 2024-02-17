/*
 *     Copyright (C) 2023  Akane Foundation
 *
 *     Gramophone is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gramophone is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.c001apk.ui.others

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Typeface
import android.os.Bundle
import com.example.c001apk.databinding.ActivityBugHandlerBinding
import com.example.c001apk.ui.base.BaseActivity

/**
 * BugHandlerActivity:
 *   An activity makes crash reporting easier.
 */
class BugHandlerActivity : BaseActivity<ActivityBugHandlerBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedText = intent.getStringExtra("exception_message")
        binding.error.typeface = Typeface.MONOSPACE
        binding.error.text = receivedText

        // Make our life easier by copying the log to clipboard
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("error msg", receivedText)
        clipboard.setPrimaryClip(clip)

    }
}
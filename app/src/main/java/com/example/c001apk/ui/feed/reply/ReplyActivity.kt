package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.graphics.ColorUtils
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityReplyBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.util.EmojiUtils
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.SmoothInputLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern


/*
* Copyright (C) 2018 AlexMofer
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ /**
 * 输入面板
 */

@AndroidEntryPoint
class ReplyActivity : BaseActivity<ActivityReplyBinding>(),
    View.OnClickListener, OnTouchListener, SmoothInputLayout.OnVisibilityChangeListener {

    private val viewModel by viewModels<ReplyViewModel>()
    private val type: String? by lazy { intent.getStringExtra("type") }
    private val rid: String? by lazy { intent.getStringExtra("rid") }
    private val username: String? by lazy { intent.getStringExtra("username") }
    private val imm by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val color by lazy { SurfaceColors.SURFACE_1.getColor(this) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.type = type
        viewModel.rid = rid

        binding.emojiBtn?.setOnClickListener(this)
        binding.checkBox.setOnClickListener(this)
        binding.editText.setOnTouchListener(this)
        binding.out.setOnTouchListener(this)
        if (binding.main is SmoothInputLayout)
            (binding.main as SmoothInputLayout).setOnVisibilityChangeListener(this)
        val radius = listOf(16.dp.toFloat(), 16.dp.toFloat(), 0f, 0f)
        val radiusBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(this@ReplyActivity.color)
            cornerRadii = floatArrayOf(
                radius[0], radius[0],
                radius[1], radius[1],
                radius[2], radius[2],
                radius[3], radius[3]
            )
        }
        if (binding.main is SmoothInputLayout) {
            binding.inputLayout.background = radiusBg
            binding.emojiLayout.setBackgroundColor(color)
        } else
            binding.bottomLayout?.background = radiusBg

        initPage()
        initEditText()
        initEmojiPanel()
        initObserve()
        showInput()

    }

    private fun initObserve() {
        viewModel.over.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                val intent = Intent()
                if (type == "createFeed") {
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show()
                } else {
                    intent.putExtra("response_data", viewModel.responseData)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.createDialog.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                val binding = ItemCaptchaBinding.inflate(
                    LayoutInflater.from(this), null, false
                )
                binding.captchaImg.setImageBitmap(it)
                binding.captchaText.highlightColor = ColorUtils.setAlphaComponent(
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorPrimaryDark,
                        0
                    ), 128
                )
                MaterialAlertDialogBuilder(this).apply {
                    setView(binding.root)
                    setTitle("captcha")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton("验证并继续") { _, _ ->
                        viewModel.requestValidateData = HashMap()
                        viewModel.requestValidateData["type"] = "err_request_captcha"
                        viewModel.requestValidateData["code"] = binding.captchaText.text.toString()
                        viewModel.requestValidateData["mobile"] = ""
                        viewModel.requestValidateData["idcard"] = ""
                        viewModel.requestValidateData["name"] = ""
                        viewModel.onPostRequestValidate()
                    }
                    show()
                }
            }
        }

    }

    private fun initPage() {
        binding.checkBox.text = if (type == "createFeed") "仅自己可见"
        else "回复并转发"
        binding.title.text = if (type == "createFeed") "发布动态"
        else "回复"
        if (type != "createFeed" && !username.isNullOrEmpty())
            binding.editText.hint = "回复: $username"

        binding.publish.setOnClickListener {
            if (type == "createFeed") {
                viewModel.createFeedData = HashMap()
                viewModel.createFeedData["id"] = ""
                viewModel.createFeedData["message"] = binding.editText.text.toString()
                viewModel.createFeedData["type"] = "feed"
                viewModel.createFeedData["pic"] = ""
                viewModel.createFeedData["status"] = if (binding.checkBox.isChecked) "-1" else "1"
                viewModel.onPostCreateFeed()
            } else {
                viewModel.replyData["message"] = binding.editText.text.toString()
                viewModel.replyData["replyAndForward"] =
                    if (binding.checkBox.isChecked) "1" else "0"
                viewModel.onPostReply()
            }
        }
        binding.publish.isClickable = false
    }

    private fun initEmojiPanel() {
        val data = EmojiUtils.emojiMap.toList()
        val list = ArrayList<List<Pair<String, Int>>>()
        for (i in 0..4) {
            list.add(data.subList(i * 27 + 4, (i + 1) * 27 + 4))
        }
        list.add(data.subList(139, 155))
        binding.emojiPanel.adapter = EmojiPagerAdapter(
            list,
            onClickEmoji = {
                with(binding.editText) {
                    if (it == "[c001apk]") {
                        onBackSpace()
                    } else
                        editableText.replace(selectionStart, selectionEnd, it)
                }
            },
            onCountStart = {
                countDownTimer.start()
            },
            onCountStop = {
                countDownTimer.cancel()
            }
        )
        binding.indicator.setViewPager(binding.emojiPanel)
    }

    private val countDownTimer: CountDownTimer = object : CountDownTimer(100000, 50) {
        override fun onTick(millisUntilFinished: Long) {
            onBackSpace()
        }

        override fun onFinish() {}
    }

    private fun onBackSpace() {
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        ViewCompat.performHapticFeedback(binding.editText, HapticFeedbackConstantsCompat.CONFIRM)
    }

    private fun initEditText() {
        binding.editText.addTextChangedListener(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
        binding.editText.removeTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {

        private val AT_PATTERN = Pattern.compile("@[\\w\\-._]+")
        private val TAG_PATTERN = Pattern.compile("#[^# @]+#")
        private val EMOJI_PATTERN = Pattern.compile("\\[[^\\]]+\\]")

        override fun afterTextChanged(editable: Editable) {
            if (binding.editText.text.toString().trim().isBlank()) {
                binding.publish.isClickable = false
                binding.publish.setTextColor(getColor(android.R.color.darker_gray))
            } else {
                binding.publish.isClickable = true
                binding.publish.setTextColor(
                    MaterialColors.getColor(
                        this@ReplyActivity,
                        com.google.android.material.R.attr.colorPrimary,
                        0
                    )
                )
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(
            charSequence: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            val spannable = charSequence as Spannable
            val total = start + count
            setEmoticonSpan(spannable, EMOJI_PATTERN, start, total)
            tintPatternColor(spannable, AT_PATTERN, start, total)
            tintPatternColor(spannable, TAG_PATTERN, start, total)
        }
    }

    private fun tintPatternColor(spannable: Spannable, pattern: Pattern, start: Int, total: Int) {
        val region = pattern.matcher(spannable).region(start, total)
        while (region.find()) {
            val group = region.group()
            spannable.setSpan(
                ForegroundColorSpan(
                    MaterialColors.getColor(
                        this@ReplyActivity,
                        com.google.android.material.R.attr.colorPrimary,
                        0
                    )
                ),
                region.start(),
                region.start() + group.length,
                33
            )
        }
    }

    private fun setEmoticonSpan(spannable: Spannable, pattern: Pattern, start: Int, total: Int) {
        val matcher = pattern.matcher(spannable).region(start, total)
        while (matcher.find()) {
            val group = matcher.group()
            EmojiUtils.emojiMap[group]?.let {
                getDrawable(it)?.let { emoji ->
                    val size = binding.editText.textSize
                    if (group in listOf("[楼主]", "[层主]", "[置顶]"))
                        emoji.setBounds(0, 0, (size * 2).toInt(), size.toInt())
                    else
                        emoji.setBounds(0, 0, (size * 1.4).toInt(), (size * 1.4).toInt())
                    val imageSpan = CenteredImageSpan(emoji, (size * 1.4).toInt(), group)
                    spannable.setSpan(
                        imageSpan,
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(this)
    }

    private fun showInput() {
        binding.emojiBtn?.setImageResource(R.drawable.ic_emoji)
        binding.editText.let {
            it.requestFocus()
            it.requestFocusFromTouch()
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun showEmoji() {
        binding.emojiBtn?.setImageResource(R.drawable.ic_keyboard)
        if (binding.main is SmoothInputLayout)
            (binding.main as SmoothInputLayout).showInputPane(true)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.emojiBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                if (binding.emojiBtn?.isSelected == true) {
                    binding.emojiBtn?.isSelected = false
                    showInput()
                } else {
                    binding.emojiBtn?.isSelected = true
                    showEmoji()
                }
            }

            R.id.checkBox ->
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (view.id) {
            R.id.out -> {
                finish()
            }

            R.id.editText -> {
                binding.emojiBtn?.isSelected = false
            }
        }
        return false
    }

    override fun onVisibilityChange(visibility: Int) {
        binding.emojiBtn?.isSelected = visibility == VISIBLE
        binding.emojiBtn?.setImageResource(
            if (visibility == VISIBLE) R.drawable.ic_keyboard
            else R.drawable.ic_emoji
        )
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            com.absinthe.libraries.utils.R.anim.anim_bottom_sheet_slide_up,
            com.absinthe.libraries.utils.R.anim.anim_bottom_sheet_slide_down
        )
    }

}


package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.graphics.ColorUtils
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.absinthe.libraries.utils.extensions.dp
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.OSSRequest
import com.alibaba.sdk.android.oss.model.ObjectMetadata
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.blankj.utilcode.util.FileUtils.getFileMD5ToString
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.Glide
import com.example.c001apk.BuildConfig
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityReplyBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.model.OSSUploadPrepareModel
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
    View.OnClickListener, OnTouchListener, SmoothInputLayout.OnVisibilityChangeListener,
    SmoothInputLayout.OnKeyboardChangeListener {

    private val viewModel by viewModels<ReplyViewModel>()
    private val type: String? by lazy { intent.getStringExtra("type") }
    private val rid: String? by lazy { intent.getStringExtra("rid") }
    private val username: String? by lazy { intent.getStringExtra("username") }
    private val imm by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val color by lazy { SurfaceColors.SURFACE_1.getColor(this) }
    private val dataList by lazy { EmojiUtils.emojiMap.toList() }
    private val recentList = ArrayList<List<Pair<String, Int>>>()
    private val emojiList = ArrayList<List<Pair<String, Int>>>()
    private val coolBList = ArrayList<List<Pair<String, Int>>>()
    private val list = listOf(recentList, emojiList, coolBList)
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var uriList: MutableList<Uri> = ArrayList()

    init {
        for (i in 0..3) {
            emojiList.add(dataList.subList(i * 27 + 4, (i + 1) * 27 + 4))
        }
        coolBList.add(dataList.subList(112, 139))
        coolBList.add(dataList.subList(139, 155))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.type = type
        viewModel.rid = rid

        initView()
        initPage()
        initEditText()
        initEmojiPanel()
        initObserve()
        initPhotoPick()
        showInput()

    }

    private fun initPhotoPick() {
        if (!ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
            return
        pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
                if (uris.isNotEmpty()) {
                    uris.forEach { uri ->
                        if (uriList.size == 9) {
                            Toast.makeText(this, "最多选择9张图片", Toast.LENGTH_SHORT).show()
                            return@registerForActivityResult
                        }
                        uriList.add(uri)

                        val file = UriUtils.uri2File(uri)
                        val md5 = getFileMD5ToString(file).lowercase()
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(file.path, options)
                        val width = options.outWidth
                        val height = options.outHeight
                        viewModel.imageList.add(
                            OSSUploadPrepareModel(
                                name = file.name,
                                resolution = "${width}x${height}",
                                md5 = md5,
                            )
                        )

                        val imageView = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                (55.dp * width.toFloat() / height.toFloat()).toInt(), 55.dp
                            ).apply {
                                setMargins(5.dp, 0, 0, 0)
                            }
                            setOnClickListener {
                                with(binding.imageLayout.indexOfChild(this)){
                                    binding.imageLayout.removeViewAt(this)
                                    uriList.removeAt(this)
                                    viewModel.imageList.removeAt(this)
                                    binding.imageLayout.isVisible = uriList.isNotEmpty()
                                }
                            }
                        }
                        Glide.with(this).load(uri).into(imageView)
                        binding.imageLayout.addView(imageView)
                    }
                }
                binding.imageLayout.isVisible = uriList.isNotEmpty()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.emojiBtn?.setOnClickListener(this)
        binding.imageBtn.setOnClickListener(this)
        binding.keyboardBtn?.setOnClickListener(this)
        binding.checkBox.setOnClickListener(this)
        binding.publish.setOnClickListener(this)
        binding.editText.setOnTouchListener(this)
        binding.out.setOnTouchListener(this)
        (binding.main as? SmoothInputLayout)?.setOnVisibilityChangeListener(this)
        (binding.main as? SmoothInputLayout)?.setOnKeyboardChangeListener(this)
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
    }

    private fun initObserve() {
        viewModel.uploadImage.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let { responseData ->

                viewModel.replyAndFeedData["pic"] =
                    responseData.fileInfo.joinToString(separator = ",") {
                        responseData.uploadPrepareInfo.uploadImagePrefix + "/" + it.uploadFileName
                    }

                val accessKeyId = responseData.uploadPrepareInfo.accessKeyId
                val accessKeySecret = responseData.uploadPrepareInfo.accessKeySecret
                val securityToken = responseData.uploadPrepareInfo.securityToken

                val endPoint = responseData.uploadPrepareInfo.endPoint
                val bucket = responseData.uploadPrepareInfo.bucket
                val callbackUrl = responseData.uploadPrepareInfo.callbackUrl

                val conf = ClientConfiguration()
                conf.connectionTimeout = 5 * 60 * 1000
                conf.socketTimeout = 5 * 60 * 1000
                conf.maxConcurrentRequest = 5
                conf.maxErrorRetry = 2
                OSSLog.enableLog()
                val credentialProvider = OSSStsTokenCredentialProvider(
                    accessKeyId,
                    accessKeySecret,
                    securityToken
                )
                val oss = OSSClient(
                    this,
                    endPoint,
                    credentialProvider,
                    conf
                )

                uriList.forEachIndexed { index, uri ->
                    val put = PutObjectRequest(
                        bucket,
                        responseData.fileInfo[index].uploadFileName, // objectKey -> oss上所存储文件的名称
                        uri // uri
                    )
                    val metadata = ObjectMetadata()
                    metadata.contentType = "application/octet-stream"
                    put.metadata = metadata
                    put.crC64 = OSSRequest.CRC64Config.YES
                    if (!TextUtils.isEmpty(callbackUrl)) {
                        put.callbackParam = object : HashMap<String?, String?>() {
                            init {
                                put("callbackUrl", callbackUrl)
                                put("callbackBody", "filename=${responseData.fileInfo[index].name}")
                            }
                        }
                    }
                    val task = oss.asyncPutObject(
                        put,
                        object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
                            override fun onSuccess(
                                request: PutObjectRequest?,
                                result: PutObjectResult?
                            ) {
                                Log.i("OSSUpload", "index: $index, uploadSuccess")
                                if (index == uriList.lastIndex) {
                                    if (type == "createFeed")
                                        viewModel.onPostCreateFeed()
                                    else
                                        viewModel.onPostReply()
                                }
                            }

                            override fun onFailure(
                                request: PutObjectRequest?,
                                clientException: ClientException?,
                                serviceException: ServiceException?
                            ) {

                                // Request exception
                                if (clientException != null) {
                                    // Local exception, such as a network exception
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, uploadFailed: clientException: ${clientException.message}"
                                    )
                                    clientException.printStackTrace()
                                }
                                if (serviceException != null) {
                                    // Service exception
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, OSSUpload: serviceException: ${serviceException.message}"
                                    )
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, ErrorCode=" + serviceException.errorCode
                                    )
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, RequestId=" + serviceException.requestId
                                    )
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, HostId=" + serviceException.hostId
                                    )
                                    Log.i(
                                        "OSSUpload",
                                        "index: $index, RawMessage=" + serviceException.rawMessage
                                    )
                                }
                            }
                        })
                    task.waitUntilFinished()
                }

            }
        }

        viewModel.recentEmojiLiveData.observe(this) {
            if (binding.emojiPanel.currentItem == 0 && recentList.isNotEmpty())
                return@observe
            recentList.clear()
            if (it.isNullOrEmpty()) {
                if (viewModel.isInit) {
                    viewModel.isInit = false
                    binding.emojiPanel.setCurrentItem(1, false)
                }
                recentList.add(0, emptyList())
            } else {
                recentList.add(0, it.map { item ->
                    Pair(item.data, EmojiUtils.emojiMap[item.data] ?: R.drawable.ic_logo)
                })
            }
            binding.emojiPanel.adapter?.notifyItemChanged(0)
        }

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
        binding.publish.isClickable = false
    }

    private fun initEmojiPanel() {
        for (i in 0..2) {
            binding.indicator.addView(
                TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        weight = 1f
                    }
                    gravity = Gravity.CENTER
                    text = listOf("最近", "默认", "酷币")[i]
                    background = getDrawable(R.drawable.selector_bg_trans)
                    setOnClickListener {
                        binding.emojiPanel.setCurrentItem(i, false)
                    }
                    if (i == 0 && BuildConfig.DEBUG) {
                        setOnLongClickListener {
                            viewModel.deleteAll()
                            true
                        }
                    }
                }
            )
            if (i != 2) {
                binding.indicator.addView(
                    View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            1.dp,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(
                            MaterialColors.getColor(
                                this@ReplyActivity,
                                com.google.android.material.R.attr.colorSurfaceVariant, 0
                            )
                        )
                    }
                )
            }
        }
        binding.emojiPanel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in 0 until binding.indicator.childCount) {
                    with(binding.indicator.getChildAt(i)) {
                        if (this is TextView) {
                            background = getDrawable(
                                if (i / 2 == position) R.drawable.selector_emoji_indicator_selected
                                else R.drawable.selector_emoji_indicator
                            )
                            setTextColor(
                                if (i / 2 == position)
                                    MaterialColors.getColor(
                                        this@ReplyActivity,
                                        com.google.android.material.R.attr.colorOnPrimary, 0
                                    )
                                else
                                    MaterialColors.getColor(
                                        this@ReplyActivity,
                                        com.google.android.material.R.attr.colorControlNormal, 0
                                    )
                            )
                        }
                    }
                }
            }
        })

        binding.emojiPanel.adapter = EmojiPagerAdapter(
            list,
            onClickEmoji = {
                with(binding.editText) {
                    if (it == "[c001apk]") {
                        onBackSpace()
                    } else {
                        editableText.replace(selectionStart, selectionEnd, it)
                        viewModel.updateRecentEmoji(it)
                    }
                }
            },
            onCountStart = {
                countDownTimer.start()
            },
            onCountStop = {
                countDownTimer.cancel()
            }
        )
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
        binding.editText.addTextChangedListener(OnTextInputListener("@") {
            //
        })
        binding.editText.setOnKeyListener(FastDeleteAtUserKeyListener())
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

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(
            charSequence: CharSequence,
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
        if (binding.main is SmoothInputLayout)
            (binding.main as? SmoothInputLayout)?.showKeyboard()
        else
            binding.editText.let {
                it.requestFocus()
                it.requestFocusFromTouch()
                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
            }
    }

    private fun showEmoji() {
        (binding.main as? SmoothInputLayout)?.showEmojiPanel(true)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imageBtn -> {
                if (!ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                    return
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            R.id.keyboardBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                with(binding.main as? SmoothInputLayout) {
                    if (binding.emojiLayout.isVisible) {
                        this?.closeEmojiPanel()
                    } else if (this?.isKeyBoardOpen == true) {
                        closeKeyboard(false)
                    } else {
                        showInput()
                    }
                }
            }

            R.id.emojiBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                if (binding.emojiBtn?.isSelected == true) {
                    showInput()
                } else {
                    showEmoji()
                }
            }

            R.id.checkBox ->
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)

            R.id.publish -> {
                if (type == "createFeed") {
                    viewModel.replyAndFeedData = HashMap()
                    viewModel.replyAndFeedData["id"] = ""
                    viewModel.replyAndFeedData["message"] = binding.editText.text.toString()
                    viewModel.replyAndFeedData["type"] = "feed"
                    viewModel.replyAndFeedData["status"] =
                        if (binding.checkBox.isChecked) "-1" else "1"
                    if (uriList.isNotEmpty()) {
                        viewModel.onPostOSSUploadPrepare()
                    } else {
                        viewModel.onPostCreateFeed()
                    }
                } else {
                    viewModel.replyAndFeedData["message"] = binding.editText.text.toString()
                    viewModel.replyAndFeedData["replyAndForward"] =
                        if (binding.checkBox.isChecked) "1" else "0"
                    if (uriList.isNotEmpty()) {
                        viewModel.onPostOSSUploadPrepare()
                    } else {
                        viewModel.onPostReply()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (view.id) {
            R.id.out -> {
                finish()
            }
        }
        return false
    }

    override fun onVisibilityChange(visibility: Int) { // 0->visible, 8->gone
        binding.emojiBtn?.isSelected = visibility == VISIBLE
        binding.emojiBtn?.setImageResource(
            if (visibility == VISIBLE) R.drawable.ic_keyboard
            else R.drawable.ic_emoji
        )
        if (binding.emojiBtn?.isSelected == true)
            binding.keyboardBtn?.setImageResource(R.drawable.outline_keyboard_hide_24)
        if (binding.emojiBtn?.isSelected == false && (binding.main as? SmoothInputLayout)?.isKeyBoardOpen == false)
            binding.keyboardBtn?.setImageResource(R.drawable.outline_keyboard_show_24)
    }

    override fun onKeyboardChanged(open: Boolean) {
        binding.keyboardBtn?.setImageResource(
            if (open || binding.emojiBtn?.isSelected == true) R.drawable.outline_keyboard_hide_24
            else R.drawable.outline_keyboard_show_24
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

class FastDeleteAtUserKeyListener : View.OnKeyListener {
    override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
        val editText = view as EditText
        if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN) {
            if (removeFastDelete(editText)) {
                return true
            }

            val text = editText.text
            val selectionStart = editText.selectionStart
            if (selectionStart <= 0) {
                return false
            }
            val charAt = text[selectionStart - 1]
            if (charAt != ' ' && charAt != ':' || selectionStart != editText.selectionEnd) {
                return false
            }
            val lastIndexOfAt = lastIndexOfAt(text, selectionStart)
            val lastIndexOfTopicStart = lastIndexOfTopicStart(text, selectionStart)
            if (lastIndexOfAt >= 0 && lastIndexOfAt > lastIndexOfTopicStart) {
                val cArr = CharArray(selectionStart - lastIndexOfAt)
                text.getChars(lastIndexOfAt, selectionStart, cArr, 0)
                if (!AT_PATTERN.matcher(String(cArr)).matches()) {
                    return false
                }
                text.delete(lastIndexOfAt, selectionStart)
                return true
            }
            if (lastIndexOfTopicStart >= 0 && lastIndexOfTopicStart > lastIndexOfAt) {
                val cArr2 = CharArray(selectionStart - lastIndexOfTopicStart)
                text.getChars(lastIndexOfTopicStart, selectionStart, cArr2, 0)
                if (TAG_PATTERN.matcher(String(cArr2)).matches()) {
                    text.delete(lastIndexOfTopicStart, selectionStart)
                    return true
                }
            }
        }
        return false
    }

    private fun lastIndexOfAt(editable: Editable, i: Int): Int {
        for (i2 in i - 1 downTo 0) {
            if (editable[i2] == '@') {
                return i2
            }
        }
        return -1
    }

    private fun lastIndexOfTopicStart(editable: Editable, i: Int): Int {
        var i2 = 0
        for (i3 in i - 1 downTo 0) {
            if (editable[i3] == '#') {
                i2++
            }
            if (i2 == 2) {
                return i3
            }
        }
        return -1
    }

    private fun removeFastDelete(editText: EditText): Boolean {
        val spannableStringBuilder = editText.text as SpannableStringBuilder
        var selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        if (selectionEnd == selectionStart && selectionStart > 0) {
            selectionStart--
        }
        var z = false
        for (fastDeleteSpan in
        spannableStringBuilder.getSpans(
            selectionStart, selectionEnd, FastDeleteSpan::class.java
        ) as Array<FastDeleteSpan?>) {
            val spanStart = spannableStringBuilder.getSpanStart(fastDeleteSpan)
            val spanEnd = spannableStringBuilder.getSpanEnd(fastDeleteSpan)
            spannableStringBuilder.delete(spanStart, spanEnd)
            spannableStringBuilder.removeSpan(fastDeleteSpan)
            if (spanEnd == selectionEnd) {
                z = true
            }
        }
        return z
    }

    companion object {
        private val AT_PATTERN = Pattern.compile("@[\\w\\-._]+[\\s:]")
        private val TAG_PATTERN = Pattern.compile("#[^# @]+#\\s")
    }

    class FastDeleteSpan

}

class OnTextInputListener(
    private val text: String,
    private val onTextChange: () -> Unit
) :
    TextWatcher {
    override fun afterTextChanged(editable: Editable) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (before == 0 && count == text.length
            && s.subSequence(start, start + count).toString() == text
        ) {
            onTextChange()
        }
    }
}
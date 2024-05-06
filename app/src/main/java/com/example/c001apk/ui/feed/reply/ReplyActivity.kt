package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.c001apk.BuildConfig
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityReplyBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.model.OSSUploadPrepareModel
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.feed.reply.attopic.AtTopicActivity
import com.example.c001apk.ui.feed.reply.emoji.EmojiPagerAdapter
import com.example.c001apk.util.EmojiUtils
import com.example.c001apk.util.ImageUtil.getImageDimensionsAndMD5
import com.example.c001apk.util.ImageUtil.showIMG
import com.example.c001apk.util.ImageUtil.toHex
import com.example.c001apk.util.dp
import com.example.c001apk.util.makeToast
import com.example.c001apk.util.ossUpload
import com.example.c001apk.view.EmojiTextWatcher
import com.example.c001apk.view.FastDeleteAtUserKeyListener
import com.example.c001apk.view.OnTextInputListener
import com.example.c001apk.view.SmoothInputLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


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

    private val targetType: String? by lazy { intent.getStringExtra("targetType") }
    private val targetId: String? by lazy { intent.getStringExtra("targetId") }
    private val title: String? by lazy { intent.getStringExtra("title") }

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
    private var imageList = ArrayList<OSSUploadPrepareModel>()
    private var typeList = ArrayList<String>()
    private var md5List = ArrayList<ByteArray?>()
    private var dialog: AlertDialog? = null
    private lateinit var atTopicResultLauncher: ActivityResultLauncher<Intent>
    private var isFromAt = false

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
        initEditText()
        initPage()
        initEmojiPanel()
        initObserve()
        initPhotoPick()
        initAtUser()

    }

    private fun initAtUser() {
        atTopicResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val list = result.data?.getStringExtra("data")
                    if (isFromAt) {
                        isFromAt = false
                        with(binding.editText.selectionStart) {
                            binding.editText.editableText.replace(this - 1, this, list)
                        }
                    } else {
                        binding.editText.editableText.append(list)
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) {
            delay(150)
            showInput()
        }
    }

    private fun initPhotoPick() {
        pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
                if (uris.isNotEmpty()) {
                    runCatching {
                        uris.forEach { uri ->
                            if (uriList.size == 9) {
                                Toast.makeText(this, "最多选择9张图片", Toast.LENGTH_SHORT).show()
                                return@registerForActivityResult
                            }

                            val result = getImageDimensionsAndMD5(contentResolver, uri)
                            val md5Byte = result.second
                            val md5 = md5Byte?.toHex() ?: ""
                            val width = result.first?.first ?: 0
                            val height = result.first?.second ?: 0
                            val type = result.first?.third ?: ""

                            typeList.add(type)
                            md5List.add(md5Byte)
                            imageList.add(
                                OSSUploadPrepareModel(
                                    name = "${
                                        UUID.randomUUID().toString().replace("-", "")
                                    }.${if (type.startsWith("image/")) type.substring(6) else type}",
                                    resolution = "${width}x${height}",
                                    md5 = md5,
                                )
                            )
                            uriList.add(uri)

                            val imageView = ImageView(this).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    (65.dp * width.toFloat() / height.toFloat()).toInt(), 65.dp
                                ).apply {
                                    setMargins(5.dp, 0, 0, 0)
                                }
                                setOnClickListener {
                                    with(binding.imageLayout.indexOfChild(this)) {
                                        binding.imageLayout.removeViewAt(this)
                                        uriList.removeAt(this)
                                        typeList.removeAt(this)
                                        md5List.removeAt(this)
                                        imageList.removeAt(this)
                                        binding.imageLayout.isVisible = uriList.isNotEmpty()
                                    }
                                }
                            }
                            Glide.with(this).load(uri).into(imageView)
                            binding.imageLayout.addView(imageView)
                        }
                    }.onFailure {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("获取图片信息失败")
                            .setMessage(it.message)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton("Log") { _, _ ->
                                MaterialAlertDialogBuilder(this)
                                    .setTitle("Log")
                                    .setMessage(it.stackTraceToString())
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                            }
                            .show()
                    }
                }
                binding.imageLayout.isVisible = uriList.isNotEmpty()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.emojiBtn?.setOnClickListener(this)
        binding.imageBtn.setOnClickListener(this)
        binding.atBtn.setOnClickListener(this)
        binding.tagBtn.setOnClickListener(this)
        binding.appBtn.setOnClickListener(this)
        binding.urlBtn.isVisible = false //type == "createFeed"
        if (type == "createFeed") {
            binding.urlBtn.setOnClickListener(this)
            binding.extraUrlLayout.setOnClickListener(this)
        }
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
        viewModel.loadShareUrl.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                closeDialog()

                viewModel.replyAndFeedData["extra_title"] = it.title
                viewModel.replyAndFeedData["extra_url"] = it.url

                binding.extraUrlLayout.isVisible = true
                binding.extraTitle.text = it.title
                binding.extraUrl.text = it.url
                if (it.logo.isNullOrEmpty()) {
                    binding.extraPic.apply {
                        setBackgroundColor(
                            MaterialColors.getColor(
                                this,
                                com.google.android.material.R.attr.colorPrimary,
                                0
                            )
                        )
                        val link = getDrawable(R.drawable.ic_link)
                        link?.setTint(
                            MaterialColors.getColor(
                                this,
                                com.google.android.material.R.attr.colorOnPrimary,
                                0
                            )
                        )
                        Glide.with(this).load(link).into(this)
                    }
                } else {
                    binding.extraPic.apply {
                        setBackgroundColor(Color.TRANSPARENT)
                        showIMG(this, it.logo)
                    }
                }
            }
        }

        viewModel.uploadImage.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let { responseData ->
                viewModel.replyAndFeedData["pic"] =
                    responseData.fileInfo.joinToString(separator = ",") {
                        responseData.uploadPrepareInfo.uploadImagePrefix + "/" + it.uploadFileName
                    }
                lifecycleScope.launch(Dispatchers.IO) {
                    ossUpload(
                        this@ReplyActivity, responseData, uriList, typeList, md5List,
                        iOnSuccess = { index ->
                            Log.i("OSSUpload", "uploadSuccess")
                            if (index == uriList.lastIndex) {
                                if (type == "createFeed")
                                    viewModel.onPostCreateFeed()
                                else
                                    viewModel.onPostReply()
                            }
                        },
                        iOnFailure = {
                            Log.i("OSSUpload", "uploadFailed")
                            runOnUiThread {
                                closeDialog()
                                Toast.makeText(this@ReplyActivity, "图片上传失败", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        closeDialog = {
                            closeDialog()
                        }
                    )
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
                closeDialog()
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
                closeDialog()
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.createDialog.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                closeDialog()

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
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    binding.captchaText.requestFocus()
                }.show()
            }
        }

    }

    private fun closeDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun initPage() {
        binding.checkBox.text = if (type == "createFeed") "仅自己可见"
        else "回复并转发"
        binding.title.text = if (type == "createFeed") "发布动态"
        else "回复"
        if (type != "createFeed" && !username.isNullOrEmpty())
            binding.editText.hint = "回复: $username"
        binding.publish.isClickable = false
        title?.let {
            binding.editText.editableText.append("#${title}# ")
        }
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
        binding.editText.apply {
            highlightColor = ColorUtils.setAlphaComponent(
                MaterialColors.getColor(
                    this@ReplyActivity,
                    com.google.android.material.R.attr.colorPrimaryDark,
                    0
                ), 128
            )
            addTextChangedListener(EmojiTextWatcher(this@ReplyActivity, binding.editText.textSize) {
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
            })
            addTextChangedListener(OnTextInputListener("@") {
                isFromAt = true
                launchAtTopic("user")
            })
            setOnKeyListener(FastDeleteAtUserKeyListener())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDialog()
        countDownTimer.cancel()
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

    @SuppressLint("InflateParams")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.extraUrlLayout -> {
                binding.extraUrlLayout.isVisible = false
                viewModel.replyAndFeedData["extra_title"] = ""
                viewModel.replyAndFeedData["extra_url"] = ""
            }

            R.id.urlBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                val urlView = LayoutInflater.from(this@ReplyActivity)
                    .inflate(R.layout.item_edittext, null, false)
                val editText: TextInputEditText = urlView.findViewById(R.id.editText)
                editText.highlightColor = ColorUtils.setAlphaComponent(
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorPrimaryDark,
                        0
                    ), 128
                )
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("添加网络链接")
                    setView(urlView)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.loadShareUrl(editText.text.toString())
                        showDialog()
                    }
                    setNegativeButton(android.R.string.cancel, null)
                }.create().apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    editText.requestFocus()
                }.show()
            }

            R.id.atBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchAtTopic("user")
            }

            R.id.tagBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchAtTopic("topic")
            }

            R.id.appBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)

            }

            R.id.imageBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchPick()
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
                    viewModel.replyAndFeedData["id"] = ""
                    viewModel.replyAndFeedData["message"] = binding.editText.text.toString()
                    viewModel.replyAndFeedData["type"] = "feed"
                    viewModel.replyAndFeedData["status"] =
                        if (binding.checkBox.isChecked) "-1" else "1"

                    targetType?.let {
                        if (it == "apk")
                            viewModel.replyAndFeedData["type"] = "comment"
                        viewModel.replyAndFeedData["targetType"] = it
                    }
                    targetId?.let {
                        viewModel.replyAndFeedData["targetId"] = it
                    }

                    if (uriList.isNotEmpty()) {
                        viewModel.onPostOSSUploadPrepare(imageList)
                    } else {
                        viewModel.onPostCreateFeed()
                    }
                } else {
                    viewModel.replyAndFeedData["message"] = binding.editText.text.toString()
                    viewModel.replyAndFeedData["replyAndForward"] =
                        if (binding.checkBox.isChecked) "1" else "0"
                    if (uriList.isNotEmpty()) {
                        viewModel.onPostOSSUploadPrepare(imageList)
                    } else {
                        viewModel.onPostReply()
                    }
                }
                showDialog()
            }

        }
    }

    @SuppressLint("InflateParams")
    private fun showDialog() {
        dialog = MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_MaterialAlertDialog_Rounded
        ).apply {
            setView(
                LayoutInflater.from(this@ReplyActivity)
                    .inflate(R.layout.dialog_refresh, null, false)
            )
            setCancelable(false)
        }.create()
        dialog?.show()
        val decorView: View? = dialog?.window?.decorView
        val paddingTop: Int = decorView?.paddingTop ?: 0
        val paddingBottom: Int = decorView?.paddingBottom ?: 0
        val paddingLeft: Int = decorView?.paddingLeft ?: 0
        val paddingRight: Int = decorView?.paddingRight ?: 0
        val width = 80.dp + paddingLeft + paddingRight
        val height = 80.dp + paddingTop + paddingBottom
        dialog?.window?.setLayout(width, height)
    }

    private fun launchAtTopic(type: String) {
        val intent = Intent(this, AtTopicActivity::class.java)
        intent.putExtra("type", type)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.right_in, R.anim.left_out
        )
        atTopicResultLauncher.launch(intent, options)
    }

    private fun launchPick() {
        (binding.main as? SmoothInputLayout)?.closeKeyboard(false)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.anim_bottom_sheet_slide_up, R.anim.anim_bottom_sheet_slide_down
        )
        try {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                options
            )
        } catch (e: ActivityNotFoundException) {
            makeToast("Activity Not Found")
            e.printStackTrace()
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
            R.anim.anim_bottom_sheet_slide_up,
            R.anim.anim_bottom_sheet_slide_down
        )
    }

}
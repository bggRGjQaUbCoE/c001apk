package com.example.c001apk.ui.fragment.feed.total

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.example.c001apk.logic.model.CheckResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.feed.IOnEmojiClickListener
import com.example.c001apk.ui.fragment.feed.IOnReplyClickListener
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.Emoji.initEmoji
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.CenteredImageSpan
import com.example.c001apk.view.ExtendEditText
import com.example.c001apk.view.HorizontalScrollAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import kotlin.concurrent.thread

class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment(), IOnReplyClickListener,
    IOnEmojiClickListener {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by lazy { ViewModelProvider(this)[ReplyTotalViewModel::class.java] }
    private lateinit var id: String
    private lateinit var uid: String
    private var position: Int = 0
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var lastVisibleItemPosition = -1
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var type = ""
    private var uname = ""
    private var ruid = ""
    private var rid = ""
    private var rPosition = 0
    private var r2rPosition = 0
    private lateinit var editText: ExtendEditText
    private var replyAndForward = "0"
    private var isPaste = false
    private var cursorBefore = -1

    companion object {
        fun newInstance(position: Int, uid: String, id: String): Reply2ReplyBottomSheetDialog {
            val args = Bundle()
            args.putString("UID", uid)
            args.putString("ID", id)
            args.putInt("POSITION", position)
            val fragment = Reply2ReplyBottomSheetDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private fun setData() {
        val args = arguments
        id = args!!.getString("ID", "")
        uid = args.getString("UID", "")
        position = args.getInt("POSITION")
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogReplyToReplyBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
        initView()
        initData()
        initScroll()

        viewModel.replyTotalLiveData.observe(viewLifecycleOwner) { result ->
            val data = result.getOrNull()
            if (!data.isNullOrEmpty()) {
                if (!viewModel.isLoadMore)
                    viewModel.replyTotalList.clear()
                for (element in data)
                    if (element.entityType == "feed_reply")
                        viewModel.replyTotalList.add(element)
                mAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                mAdapter.setLoadState(mAdapter.LOADING_END)
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.replyTotalList.size) {
                        if (!viewModel.isEnd) {
                            mAdapter.setLoadState(mAdapter.LOADING)
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getReplyTotal()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.replyTotalList.isNotEmpty())
                    lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }
        })
    }

    private fun initData() {
        viewModel.id = id
        if (viewModel.replyTotalList.isEmpty()) {
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.getReplyTotal()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)

        mAdapter =
            Reply2ReplyTotalAdapter(requireActivity(), uid, position, viewModel.replyTotalList)
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnReplyClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onStart() {
        super.onStart()
        val view: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
        view.layoutParams.height = -1
        view.layoutParams.width = -1
    }

    override fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
    ) {
        if (PrefManager.isLogin) {
            this.rPosition = rPosition
            r2rPosition?.let { this.r2rPosition = r2rPosition }
            this.rid = id
            this.ruid = uid
            this.uname = uname
            this.type = type
            initReply()
        }
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    private fun initReply() {

        bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = LayoutInflater.from(requireActivity())
            .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
        editText = view.findViewById(R.id.editText)
        val publish: TextView = view.findViewById(R.id.publish)
        val checkBox: MaterialCheckBox = view.findViewById(R.id.checkBox)
        val emojiPanel: ViewPager = view.findViewById(R.id.emojiPanel)
        val emotion: ImageButton = view.findViewById(R.id.emotion)
        val pageSize = 21
        val itemBeans = initEmoji(pageSize)
        val scrollAdapter = HorizontalScrollAdapter(requireActivity(), itemBeans)
        scrollAdapter.setIOnEmojiClickListener(this)
        emojiPanel.adapter = scrollAdapter

        fun checkAndPublish() {
            if (editText.text.toString().replace("\n", "").isEmpty()) {
                publish.isClickable = false
                publish.setTextColor(requireActivity().getColor(R.color.gray_bd))
            } else {
                publish.isClickable = true
                publish.setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        requireActivity(),
                        com.drakeet.about.R.attr.colorPrimary
                    )
                )
                publish.setOnClickListener {
                    publish(editText.text.toString())
                }
            }
        }

        editText.hint = "回复: $uname"
        viewModel.replyTextMap[rid + ruid]?.let {
            editText.text =
                SpannableStringBuilderUtil.setEmoji(
                    requireActivity(),
                    viewModel.replyTextMap[rid + ruid]!!,
                    ((editText.textSize) * 1.3).toInt()
                )
        }
        checkAndPublish()
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)

        emotion.setOnClickListener {
            if (emojiPanel.visibility != View.VISIBLE) {
                emojiPanel.visibility = View.VISIBLE
                val keyboard = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
                val drawableKeyboard = DrawableCompat.wrap(keyboard!!)
                DrawableCompat.setTint(
                    drawableKeyboard,
                    ContextCompat.getColor(requireContext(), R.color.gray_75)
                )
                emotion.setImageDrawable(drawableKeyboard)
            } else {
                emojiPanel.visibility = View.GONE
                val face = ContextCompat.getDrawable(requireContext(), R.drawable.ic_face)
                val drawableFace = DrawableCompat.wrap(face!!)
                DrawableCompat.setTint(
                    drawableFace,
                    ContextCompat.getColor(requireContext(), R.color.gray_75)
                )
                emotion.setImageDrawable(drawableFace)
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            replyAndForward = if (isChecked) "1"
            else "0"
        }

        bottomSheetDialog.apply {
            setContentView(view)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
            show()
            window?.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        editText.setOnPasteCallback(object : ExtendEditText.OnPasteCallback {
            override fun onPaste(text: String?, isPaste: Boolean) {
                this@Reply2ReplyBottomSheetDialog.isPaste = isPaste
                if (isPaste) {
                    cursorBefore = editText.selectionStart
                } else {
                    if (text == "") {//delete
                        editText.editableText.delete(editText.selectionStart, editText.selectionEnd)
                    } else {
                        val builder = SpannableStringBuilderUtil.setEmoji(
                            requireActivity(),
                            text!!,
                            ((editText.textSize) * 1.3).toInt()
                        )
                        editText.editableText.replace(
                            editText.selectionStart,
                            editText.selectionEnd,
                            builder
                        )
                    }
                }
            }
        })

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (isPaste) {
                    isPaste = false
                    val cursorNow = editText.selectionStart
                    val pasteText = editText.text.toString().substring(cursorBefore, cursorNow)
                    val builder = SpannableStringBuilderUtil.setEmoji(
                        requireActivity(),
                        pasteText,
                        ((editText.textSize) * 1.3).toInt()
                    )
                    editText.editableText.replace(
                        cursorBefore,
                        cursorNow,
                        builder
                    )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.replyTextMap[rid + ruid] = editText.text.toString()
                checkAndPublish()
            }
        })

    }

    private fun publish(content: String) {
        thread {
            try {
                val httpClient = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder()
                    .add("message", content)
                    .build()

                val getRequest: Request = Request.Builder()
                    .addHeader("User-Agent", Constants.USER_AGENT)
                    .addHeader("X-Requested-With", Constants.REQUEST_WIDTH)
                    .addHeader("X-Sdk-Int", "33")
                    .addHeader("X-Sdk-Locale", "zh-CN")
                    .addHeader("X-App-Id", Constants.APP_ID)
                    .addHeader("X-App-Token", CookieUtil.token)
                    .addHeader("X-App-Version", "13.3.1")
                    .addHeader("X-App-Code", "2307121")
                    .addHeader("X-Api-Version", "13")
                    .addHeader("X-App-Device", CookieUtil.deviceCode)
                    .addHeader("X-Dark-Mode", "0")
                    .addHeader("X-App-Channel", "coolapk")
                    .addHeader("X-App-Mode", "universal")
                    .addHeader(
                        "Cookie",
                        "uid=${PrefManager.uid}; username=${PrefManager.username}; token=${PrefManager.token}"
                    )
                    .url("https://api.coolapk.com/v6/feed/reply?id=$rid&type=$type")
                    .post(formBody)
                    .build()

                val call: Call = httpClient.newCall(getRequest)

                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Reply", "onFailure: ${e.message}")
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call, response: Response) {
                        val reply: CheckResponse = Gson().fromJson(
                            response.body!!.string(),
                            CheckResponse::class.java
                        )
                        if (reply.data?.messageStatus == 1) {
                            requireActivity().runOnUiThread {
                                viewModel.replyTotalList.add(
                                    r2rPosition + 1,
                                    TotalReplyResponse.Data(
                                        "feed_reply",
                                        id,
                                        ruid,
                                        PrefManager.uid,
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        uname,
                                        content,
                                        "",
                                        null,
                                        (System.currentTimeMillis() / 1000).toString(),
                                        "0",
                                        "0",
                                        PrefManager.userAvatar,
                                        ArrayList(),
                                        0
                                    )
                                )
                                mAdapter.notifyItemInserted(r2rPosition + 1)
                                Toast.makeText(activity, "回复成功", Toast.LENGTH_SHORT).show()
                                bottomSheetDialog.cancel()
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                Toast.makeText(activity, reply.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onShowEmoji(name: String) {
        val selectionStart: Int = editText.selectionStart
        val selectionEnd: Int = editText.selectionEnd
        if (name == "[c001apk]") { //delete
            if (selectionStart > 0) {
                val body: String = editText.text.toString()
                if (!TextUtils.isEmpty(body)) {
                    if (selectionStart == selectionEnd) {
                        val tempStr = body.substring(0, selectionStart)
                        val lastString = tempStr.substring(selectionStart - 1)
                        if ("]" == lastString) {
                            val i = tempStr.lastIndexOf("[")
                            if (i != -1) {
                                val cs = tempStr.substring(i, selectionStart)
                                if (EmojiUtil.getEmoji(cs) != -1) {
                                    editText.editableText.delete(i, selectionStart)
                                    return
                                } else {
                                    editText.editableText.delete(i, selectionStart)
                                }
                            } else {
                                editText.editableText.delete(tempStr.length - 1, selectionStart)
                            }
                        } else {
                            editText.editableText.delete(tempStr.length - 1, selectionStart)
                        }
                    } else { //括选
                        editText.editableText.delete(selectionStart, selectionEnd)
                    }
                }
            } else if (selectionStart == 0 && selectionEnd != 0) {
                editText.editableText.delete(selectionStart, selectionEnd)
            }
        } else {//insert
            val spannableStringBuilder = SpannableStringBuilder(name)
            val drawable: Drawable = requireActivity().getDrawable(EmojiUtil.getEmoji(name))!!
            val size = ((editText.textSize) * 1.3).toInt()
            drawable.setBounds(0, 0, size, size)
            val imageSpan = CenteredImageSpan(drawable, size)
            spannableStringBuilder.setSpan(
                imageSpan,
                0,
                name.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (selectionStart == selectionEnd) {
                editText.editableText.insert(selectionStart, spannableStringBuilder)
            } else { //括选
                editText.editableText.replace(selectionStart, selectionEnd, spannableStringBuilder)
            }
        }
    }


}
package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.c001apk.R
import com.example.c001apk.adapter.Reply2ReplyTotalAdapter
import com.example.c001apk.databinding.DialogReplyToReplyBottomSheetBinding
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.minterface.IOnEmojiClickListener
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.util.Emoji.initEmoji
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.ExtendEditText
import com.example.c001apk.view.HorizontalScrollAdapter
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback
import java.net.URLDecoder

class Reply2ReplyBottomSheetDialog : BottomSheetDialogFragment(), IOnReplyClickListener,
    IOnEmojiClickListener, IOnLikeClickListener, OnImageItemClickListener {

    private lateinit var binding: DialogReplyToReplyBottomSheetBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: Reply2ReplyTotalAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var editText: ExtendEditText

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
        viewModel.id = args!!.getString("ID", "")
        viewModel.uid = args.getString("UID", "")
        viewModel.position = args.getInt("POSITION")
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
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (!data.isNullOrEmpty()) {
                    if (!viewModel.isLoadMore)
                        viewModel.replyTotalList.clear()
                    for (element in data)
                        if (element.entityType == "feed_reply")
                            viewModel.replyTotalList.add(element)
                    mAdapter.notifyDataSetChanged()
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.likeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.replyTotalList[viewModel.likePosition].likenum = response.data
                        viewModel.replyTotalList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeReply) {
                viewModel.isPostUnLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.replyTotalList[viewModel.likePosition].likenum = response.data
                        viewModel.replyTotalList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostReply) {
                viewModel.isPostReply = false

                val response = result.getOrNull()
                response?.let {
                    if (response.data != null) {
                        if (response.data.messageStatus == 1 || response.data.messageStatus == 2) {
                            viewModel.replyTextMap[viewModel.rid + viewModel.ruid] = ""
                            if (response.data.messageStatus == 1)
                                Toast.makeText(activity, "回复成功", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.cancel()
                            viewModel.replyTotalList.add(
                                viewModel.r2rPosition + 1,
                                TotalReplyResponse.Data(
                                    "feed_reply",
                                    viewModel.id,
                                    viewModel.ruid,
                                    PrefManager.uid,
                                    URLDecoder.decode(PrefManager.username, "UTF-8"),
                                    viewModel.uname,
                                    editText.text.toString(),
                                    "",
                                    null,
                                    (System.currentTimeMillis() / 1000).toString(),
                                    "0",
                                    "0",
                                    PrefManager.userAvatar,
                                    ArrayList(),
                                    0,
                                    TotalReplyResponse.UserAction(0)
                                )
                            )
                            mAdapter.notifyItemInserted(viewModel.r2rPosition + 1)
                        }
                    } else {
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.replyTotalList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getReplyTotal()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.replyTotalList.isNotEmpty())
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }
        })
    }

    private fun initData() {
        if (viewModel.replyTotalList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            viewModel.isEnd = false
            viewModel.isLoadMore = false
            viewModel.isNew = true
            viewModel.getReplyTotal()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)

        mAdapter =
            Reply2ReplyTotalAdapter(
                requireContext(),
                viewModel.uid,
                viewModel.position,
                viewModel.replyTotalList
            )
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setIOnReplyClickListener(this)
        mAdapter.setOnImageItemClickListener(this)
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
        val behavior = BottomSheetBehavior.from(view)
        //behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = windowHeight
    }

    private val windowHeight: Int
        get() {
            val heightPixels = this.resources.displayMetrics.heightPixels
            return heightPixels - heightPixels / 4
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
            viewModel.rPosition = rPosition
            r2rPosition?.let { viewModel.r2rPosition = r2rPosition }
            viewModel.rid = id
            viewModel.ruid = uid
            viewModel.uname = uname
            viewModel.type = type
            initReply()
        }
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    private fun initReply() {

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
        editText = view.findViewById(R.id.editText)
        val publish: TextView = view.findViewById(R.id.publish)
        val checkBox: MaterialCheckBox = view.findViewById(R.id.checkBox)
        val emojiPanel: ViewPager = view.findViewById(R.id.emojiPanel)
        val emotion: ImageButton = view.findViewById(R.id.emotion)
        val itemBeans = initEmoji()
        val scrollAdapter = HorizontalScrollAdapter(requireContext(), itemBeans)
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
                    viewModel.replyData["message"] = editText.text.toString()
                    viewModel.replyData["replyAndForward"] = viewModel.replyAndForward
                    viewModel.isPostReply = true
                    viewModel.postReply()
                }
            }
        }

        editText.hint = "回复: ${viewModel.uname}"
        viewModel.replyTextMap[viewModel.rid + viewModel.ruid]?.let {
            editText.text =
                SpannableStringBuilderUtil.setEmoji(
                    requireActivity(),
                    viewModel.replyTextMap[viewModel.rid + viewModel.ruid]!!,
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
            viewModel.replyAndForward = if (isChecked) "1"
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
                viewModel.isPaste = isPaste
                if (isPaste) {
                    viewModel.cursorBefore = editText.selectionStart
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
                if (viewModel.isPaste) {
                    viewModel.isPaste = false
                    val cursorNow = editText.selectionStart
                    val pasteText =
                        editText.text.toString().substring(viewModel.cursorBefore, cursorNow)
                    val builder = SpannableStringBuilderUtil.setEmoji(
                        requireActivity(),
                        pasteText,
                        ((editText.textSize) * 1.3).toInt()
                    )
                    editText.editableText.replace(
                        viewModel.cursorBefore,
                        cursorNow,
                        builder
                    )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.replyTextMap[viewModel.rid + viewModel.ruid] = editText.text.toString()
                checkAndPublish()
            }
        })

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
            editText.editableText.replace(
                selectionStart,
                selectionEnd,
                SpannableStringBuilderUtil.setEmoji(
                    requireContext(),
                    name,
                    ((editText.textSize) * 1.3).toInt()
                )
            )
        }
    }

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {
        viewModel.likeReplyId = id
        viewModel.likePosition = position!!
        if (isLike) {
            viewModel.isPostUnLikeReply = true
            viewModel.postUnLikeReply()
        } else {
            viewModel.isPostLikeReply = true
            viewModel.postLikeReply()
        }
    }

    override fun onClick(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        val imgList: MutableList<String> = ArrayList()
        for (img in urlList) {
            if (img.substring(img.length - 6, img.length) == ".s.jpg")
                imgList.add(img.replace(".s.jpg", ""))
            else
                imgList.add(img)
        }
        Mojito.start(imageView.context) {
            urls(imgList)
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
            setIndicator(CircleIndexIndicator())
            views(nineGridView.getImageViews().toTypedArray())
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onStartAnim(position: Int) {
                    nineGridView.getImageViewAt(position)?.apply {
                        postDelayed(200) {
                            this.visibility = View.GONE
                        }
                    }
                }

                override fun onMojitoViewFinish(pagePosition: Int) {
                    nineGridView.getImageViews().forEach {
                        it.visibility = View.VISIBLE
                    }
                }

                override fun onViewPageSelected(position: Int) {
                    nineGridView.getImageViews().forEachIndexed { index, imageView ->
                        if (position == index) {
                            imageView.visibility = View.GONE
                        } else {
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

}
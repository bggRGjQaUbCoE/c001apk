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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.c001apk.R
import com.example.c001apk.adapter.FeedContentAdapter
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.fragment.minterface.IOnEmojiClickListener
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnListTypeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTotalReplyClickListener
import com.example.c001apk.util.Emoji.initEmoji
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.ExtendEditText
import com.example.c001apk.view.HorizontalScrollAdapter
import com.example.c001apk.view.StickyItemDecorator
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.checkbox.MaterialCheckBox
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback
import java.net.URLDecoder


class FeedFragment : Fragment(), IOnTotalReplyClickListener, IOnReplyClickListener,
    IOnEmojiClickListener, IOnLikeClickListener, OnImageItemClickListener,
    IOnListTypeClickListener {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var bottomSheetDialog: BottomSheetDialog


    //private var device: String? = null
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var editText: ExtendEditText
    private var isPaste = false


    companion object {
        @JvmStatic
        fun newInstance(id: String, uid: String, uname: String) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                    putString("UID", uid)
                    putString("UNAME", uname)
                    //putString("DEVICE", device)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.id = it.getString("ID")!!
            viewModel.uid = it.getString("UID")!!
            viewModel.uname = it.getString("UNAME")!!
            //device = it.getString("DEVICE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initView()
        initData()
        initButton()
        initRefresh()
        initScroll()

        binding.reply.setOnClickListener {
            viewModel.rid = viewModel.id
            viewModel.ruid = viewModel.uid
            viewModel.uname = viewModel.uname
            viewModel.type = "feed"
            initReply()
        }

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed != null) {
                    if (viewModel.isRefreshing) {
                        if (viewModel.uid == "") {
                            viewModel.uid = feed.data.uid
                            viewModel.uname = feed.data.username
                        }
                        viewModel.feedContentList.clear()
                        //viewModel.feedReplyList.clear()
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isNew = true
                        viewModel.getFeedReply()
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.feedContentList.add(feed)
                        //if (feed.data.topReplyRows.isNotEmpty()) {
                        //viewModel.haveTop = true
                        //viewModel.feedReplyList.addAll(feed.data.topReplyRows)
                        //}
                    }
                } else {
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val reply = result.getOrNull()
                if (!reply.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) {
                        viewModel.feedReplyList.clear()
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore)
                        for (element in reply) {
                            if (element.entityType == "feed_reply") {
                                viewModel.feedReplyList.add(element)
                            }
                        }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                } else {
                    viewModel.isEnd = true
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.replyCount.text = "共 ${viewModel.feedReplyList.size} 回复"
                mAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
                if (PrefManager.isLogin)
                    binding.reply.visibility = View.VISIBLE
                else
                    binding.reply.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.likeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedReplyList[viewModel.likeReplyPosition].likenum = response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition].userAction?.like = 1
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
                        viewModel.feedReplyList[viewModel.likeReplyPosition].likenum = response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition].userAction?.like = 0
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedContentList[0].data.likenum = response.data.count
                        viewModel.feedContentList[0].data.userAction?.like = 1
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeFeed) {
                viewModel.isPostUnLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedContentList[0].data.likenum = response.data.count
                        viewModel.feedContentList[0].data.userAction?.like = 0
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
                            if (viewModel.type == "feed") {
                                viewModel.feedReplyList.add(
                                    0, TotalReplyResponse.Data(
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
                                mAdapter.notifyItemInserted(1)
                                binding.recyclerView.scrollToPosition(1)
                            } else {
                                viewModel.feedReplyList[viewModel.rPosition - 1].replyRows.add(
                                    viewModel.feedReplyList[viewModel.rPosition - 1].replyRows.size,
                                    HomeFeedResponse.ReplyRows(
                                        viewModel.rid,
                                        PrefManager.uid,
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        editText.text.toString(),
                                        viewModel.ruid,
                                        viewModel.uname,
                                        null,
                                        ""
                                    )
                                )
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initButton() {
        binding.replyCount.text = "共 ${viewModel.feedReplyList.size} 回复"
        binding.lastUpdate.setOnClickListener {
            refreshReply("lastupdate_desc")
        }
        binding.dateLine.setOnClickListener {
            refreshReply("dateline_desc")
        }
        binding.popular.setOnClickListener {
            refreshReply("popular")
        }
        binding.author.setOnClickListener {
            refreshReply("")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshReply(listType: String) {
        if (listType == "")
            viewModel.fromFeedAuthor = 1
        else
            viewModel.fromFeedAuthor = 0
        mAdapter.setListType(listType)
        viewModel.listType = listType
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        binding.recyclerView.scrollToPosition(1)
        viewModel.feedReplyList.clear()
        mAdapter.notifyDataSetChanged()
        mAdapter.setLoadState(mAdapter.LOADING)
        viewModel.getFeedReply()
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    private fun initReply() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
        editText = view.findViewById(R.id.editText)
        val publish: TextView = view.findViewById(R.id.publish)
        val checkBox: MaterialCheckBox = view.findViewById(R.id.checkBox)
        val emotion: ImageButton = view.findViewById(R.id.emotion)
        val emojiPanel: ViewPager = view.findViewById(R.id.emojiPanel)
        val itemBeans = initEmoji()
        val scrollAdapter = HorizontalScrollAdapter(requireContext(), itemBeans)
        scrollAdapter.setIOnEmojiClickListener(this)
        emojiPanel.adapter = scrollAdapter

        fun checkAndPublish() {
            if (editText.text.toString().replace("\n", "").isEmpty()) {
                publish.isClickable = false
                publish.setTextColor(requireContext().getColor(R.color.gray_bd))
            } else {
                publish.isClickable = true
                publish.setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        requireContext(),
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
                    requireContext(),
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

        /*val decorView = requireActivity().window.decorView
        decorView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            decorView.getWindowVisibleDisplayFrame(rect)
            val height = decorView.height - rect.bottom
            if (realKeyboardHeight == 0 && height != 0) {
                realKeyboardHeight = height
                emojiPanel.layoutParams.height = realKeyboardHeight
            } else if (realKeyboardHeight == 0) {
                emojiPanel.layoutParams.height = -2
            }
        }*/

        emotion.setOnClickListener {
            if (emojiPanel.visibility != View.VISIBLE) {
                //requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                emojiPanel.visibility = View.VISIBLE
                val keyboard =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
                val drawableKeyboard = DrawableCompat.wrap(keyboard!!)
                DrawableCompat.setTint(
                    drawableKeyboard,
                    ContextCompat.getColor(requireContext(), R.color.gray_75)
                )
                emotion.setImageDrawable(drawableKeyboard)
                //imm.hideSoftInputFromWindow(view.windowToken, 0)
            } else {
                //requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                emojiPanel.visibility = View.GONE
                val face = ContextCompat.getDrawable(requireContext(), R.drawable.ic_face)
                val drawableFace = DrawableCompat.wrap(face!!)
                DrawableCompat.setTint(
                    drawableFace,
                    ContextCompat.getColor(requireContext(), R.color.gray_75)
                )
                emotion.setImageDrawable(drawableFace)
                //imm.showSoftInput(editText, 0)
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
                this@FeedFragment.isPaste = isPaste
                if (isPaste) {
                    viewModel.cursorBefore = editText.selectionStart
                } else {
                    if (text == "") {//delete
                        editText.editableText.delete(
                            editText.selectionStart,
                            editText.selectionEnd
                        )
                    } else {
                        val builder = SpannableStringBuilderUtil.setEmoji(
                            requireContext(),
                            text!!,
                            ((editText.textSize) * 1.3).toInt(),
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
                    val pasteText =
                        editText.text.toString().substring(viewModel.cursorBefore, cursorNow)
                    val builder = SpannableStringBuilderUtil.setEmoji(
                        requireContext(),
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

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.feedReplyList.size + 2
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getFeedReply()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.feedReplyList.isNotEmpty()) {
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }

                /*if (dy > 0 && binding.reply.visibility == View.VISIBLE) {
                    binding.reply.hide()
                } else if (dy < 0 && binding.reply.visibility != View.VISIBLE) {
                    binding.reply.show()
                }*/


            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.feedContentList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        } else {
            binding.contentLayout.visibility = View.VISIBLE
            if (PrefManager.isLogin)
                binding.reply.visibility = View.VISIBLE
            else
                binding.reply.visibility = View.GONE
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getFeed()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = FeedContentAdapter(
            requireContext(),
            viewModel.feedContentList,
            viewModel.feedReplyList
        )
        mAdapter.setIOnReplyClickListener(this)
        mAdapter.setIOnTotalReplyClickListener(this)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        mAdapter.setIOnListTypeClickListener(this)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(
                    StickyItemDecorator(
                        space,
                        object : StickyItemDecorator.SortShowListener {
                            override fun showSort(show: Boolean) {
                                binding.tabLayout.visibility = if (show) View.VISIBLE else View.GONE
                            }
                        })
                )
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = "动态"
            //title = uname
            //subtitle = device
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }
        }
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    override fun onShowTotalReply(position: Int, uid: String, id: String) {
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, uid, id)
        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
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
            viewModel.rid = id
            viewModel.ruid = uid
            viewModel.uname = uname
            viewModel.type = type
            initReply()
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
                                    editText.editableText.delete(
                                        tempStr.length - 1,
                                        selectionStart
                                    )
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
        if (type == "reply") {
            viewModel.likeReplyPosition = position!!
            viewModel.likeReplyId = id
            if (isLike) {
                viewModel.isPostUnLikeReply = true
                viewModel.postUnLikeReply()
            } else {
                viewModel.isPostLikeReply = true
                viewModel.postLikeReply()
            }
        } else {
            viewModel.likeFeedId = id
            if (isLike) {
                viewModel.isPostUnLikeFeed = true
                viewModel.postUnLikeFeed()
            } else {
                viewModel.isPostLikeFeed = true
                viewModel.postLikeFeed()
            }
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
            /*setActivityCoverLoader(ImageViewCoverLoader())
            fragmentCoverLoader {
                DefaultTargetFragmentCover()
            }*/
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

    override fun onRefreshReply(listType: String) {
        when (listType) {
            "lastupdate_desc" -> binding.buttonToggle.check(R.id.lastUpdate)
            "dateline_desc" -> binding.buttonToggle.check(R.id.dateLine)
            "popular" -> binding.buttonToggle.check(R.id.popular)
            "" -> binding.buttonToggle.check(R.id.author)
        }
        refreshReply(listType)
    }

}
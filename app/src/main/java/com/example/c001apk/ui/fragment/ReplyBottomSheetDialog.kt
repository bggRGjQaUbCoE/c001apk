package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager
import com.absinthe.libraries.utils.utils.UiUtils
import com.example.c001apk.R
import com.example.c001apk.adapter.HorizontalScrollAdapter
import com.example.c001apk.ui.fragment.minterface.IOnEmojiClickListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.util.Emoji
import com.example.c001apk.util.EmojiUtil
import com.example.c001apk.util.SpannableStringBuilderUtil
import com.example.c001apk.view.ExtendEditText
import com.example.c001apk.view.circleindicator.CircleIndicator
import com.google.android.material.bottomsheet.BottomSheetDialog

class ReplyBottomSheetDialog(mContext: Context, mView: View) : BottomSheetDialog(mContext),
    IOnEmojiClickListener {

    private var iOnPublishClickListener: IOnPublishClickListener? = null

    fun setIOnPublishClickListener(iOnPublishClickListener: IOnPublishClickListener) {
        this.iOnPublishClickListener = iOnPublishClickListener
    }

    private val context: Context = mContext
    private var isPaste = false
    private var cursorBefore = -1
    private val view = mView
    var type = ""
    var rid = ""
    var ruid = ""
    var uname = ""
    private var replyTextMap: MutableMap<String, String> = HashMap()
    private var replyAndForward = "0"
    var editText: ExtendEditText = view.findViewById(R.id.editText)

    fun setData() {
        editText.hint = "回复: $uname"
        if (replyTextMap[rid + ruid] == "" || replyTextMap[rid + ruid] == null)
            editText.text = null
        else {
            editText.text =
                SpannableStringBuilderUtil.setEmoji(
                    context,
                    replyTextMap[rid + ruid]!!,
                    ((editText.textSize) * 1.3).toInt()
                )
            editText.setSelection(editText.text.toString().length)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editText.highlightColor = ColorUtils.setAlphaComponent(
            ThemeUtils.getThemeAttrColor(
                context,
                rikka.preference.simplemenu.R.attr.colorPrimaryDark
            ), 128
        )
        val title: TextView = view.findViewById(R.id.title)
        val publish: TextView = view.findViewById(R.id.publish)
        val emotion: ImageButton = view.findViewById(R.id.emotion)
        val emojiPanel: ViewPager = view.findViewById(R.id.emojiPanel)
        val indicator: CircleIndicator = view.findViewById(R.id.indicator)
        val forwardLayout: LinearLayout = view.findViewById(R.id.forwardLayout)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        if (type == "publish") {
            title.text = "发动态"
            forwardLayout.visibility = View.GONE
        } else if (type == "reply") {
            title.text = "回复"
        }
        val itemBeans = Emoji.initEmoji()
        val scrollAdapter = HorizontalScrollAdapter(context, itemBeans)
        scrollAdapter.setIOnEmojiClickListener(this)
        emojiPanel.adapter = scrollAdapter
        indicator.setViewPager(emojiPanel)

        fun checkAndPublish() {
            if (editText.text.toString().replace("\n", "").isEmpty()) {
                publish.isClickable = false
                publish.setTextColor(context.getColor(android.R.color.darker_gray))
            } else {
                publish.isClickable = true
                publish.setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        context,
                        com.drakeet.about.R.attr.colorPrimary
                    )
                )
                publish.setOnClickListener {
                    iOnPublishClickListener?.onPublish(editText.text.toString(), replyAndForward)
                }
            }
        }


        checkAndPublish()
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)

        window?.let {
            it.attributes?.windowAnimations = com.absinthe.libraries.utils.R.style.DialogAnimation
            WindowCompat.setDecorFitsSystemWindows(it, false)
            UiUtils.setSystemBarStyle(it)
            WindowInsetsControllerCompat(it, it.decorView)
                .isAppearanceLightNavigationBars = !UiUtils.isDarkMode()
        }

        findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows = false
        findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows = false

        emotion.setOnClickListener {
            if (emojiPanel.visibility != View.VISIBLE) {
                emojiPanel.visibility = View.VISIBLE
                indicator.visibility = View.VISIBLE
                val keyboard =
                    ContextCompat.getDrawable(context, R.drawable.ic_arrow_down)
                val drawableKeyboard = DrawableCompat.wrap(keyboard!!)
                DrawableCompat.setTint(
                    drawableKeyboard,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                emotion.setImageDrawable(drawableKeyboard)
            } else {
                emojiPanel.visibility = View.GONE
                indicator.visibility = View.GONE
                val face = ContextCompat.getDrawable(context, R.drawable.ic_face)
                val drawableFace = DrawableCompat.wrap(face!!)
                DrawableCompat.setTint(
                    drawableFace,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                emotion.setImageDrawable(drawableFace)
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            replyAndForward = if (isChecked) "1"
            else "0"
        }

        editText.setOnPasteCallback(object : ExtendEditText.OnPasteCallback {
            override fun onPaste(text: String?, isPaste: Boolean) {
                this@ReplyBottomSheetDialog.isPaste = isPaste
                if (isPaste) {
                    cursorBefore = editText.selectionStart
                } else {
                    if (text == "") {//delete
                        editText.editableText.delete(
                            editText.selectionStart,
                            editText.selectionEnd
                        )
                    } else {
                        val builder = SpannableStringBuilderUtil.setEmoji(
                            context,
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
                        editText.text.toString().substring(cursorBefore, cursorNow)
                    val builder = SpannableStringBuilderUtil.setEmoji(
                        context,
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
                if (type == "reply")
                    replyTextMap[rid + ruid] = editText.text.toString()
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
                    context,
                    name,
                    ((editText.textSize) * 1.3).toInt()
                )
            )
        }
    }

}
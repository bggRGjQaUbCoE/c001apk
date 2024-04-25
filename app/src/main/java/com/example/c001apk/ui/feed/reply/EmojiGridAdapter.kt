package com.example.c001apk.ui.feed.reply

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R


class EmojiGridAdapter(
    private val emojiList: List<Pair<String, Int>>,
    private val onClickEmoji: (String) -> Unit
) : BaseAdapter() {

    override fun getCount() = 28

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder", "ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val emoji = emojiList.getOrNull(position)
        return if (emoji != null || position == 27) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_emoji, parent, false)
            val imageView: ImageView = view.findViewById(R.id.imageView)

            imageView.setImageResource(emoji?.second ?: R.drawable.ic_backspace)
            emoji?.first?.let {
                view.background = parent.context.getDrawable(R.drawable.selector_emoji)
            }
            view.setOnClickListener {
                onClickEmoji(emoji?.first ?: "[c001apk]")
            }
            if (emoji?.first.isNullOrEmpty()) {
                view.setOnLongClickListener {
                    countDownTimer.start()
                    false
                }
                view.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        countDownTimer.cancel()
                    }
                    false
                }
            }
            view
        } else View(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(48.dp, 48.dp)
        }.apply {
            isEnabled = false
        }
    }

    private val countDownTimer: CountDownTimer = object : CountDownTimer(100000, 50) {
        override fun onTick(millisUntilFinished: Long) {
            onClickEmoji("[c001apk]")
        }

        override fun onFinish() {}
    }

}
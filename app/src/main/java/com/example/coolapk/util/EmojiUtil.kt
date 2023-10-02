package com.example.coolapk.util

import com.example.coolapk.R

object EmojiUtil {

    fun getEmoji(emoji: String): Int {
        return when (emoji) {
            "[doge]" -> R.drawable.coolapk_emotion_37_doge
            "[流汗滑稽]" -> R.drawable.coolapk_emotion_63_liuhanhuaji
            "[受虐滑稽]" -> R.drawable.coolapk_emotion_64_shounuehuaji
            "[cos滑稽]" -> R.drawable.coolapk_emotion_65_coshuaji
            "[斗鸡眼滑稽]" -> R.drawable.coolapk_emotion_66_doujiyanhuaji
            "[墨镜滑稽]" -> R.drawable.coolapk_emotion_67_mojinghuaji
            "[呲牙]" -> R.drawable.coolapk_emotion_3_ciya
            "[嘿哈]" -> R.drawable.coolapk_emotion_32_heiha
            "[笑哭]" -> R.drawable.coolapk_emotion_31_xiaoku
            "[t耐克嘴]" -> R.drawable.coolapk_emotion_81_naikezui
            "[流泪]" -> R.drawable.coolapk_emotion_4_liulei
            "[喝酒]" -> R.drawable.coolapk_emotion_52_hejiu
            "[吃瓜]" -> R.drawable.coolapk_emotion_51_chigua
            "[强]" -> R.drawable.coolapk_emotion_27_qiang
            "[真不错]" -> R.drawable.coolapk_emotion_1022_zhenbuchuo
            "[喝茶]" -> R.drawable.coolapk_emotion_1016
            "[抱拳]" -> R.drawable.coolapk_emotion_29_baoquan
            "[托腮]" -> R.drawable.coolapk_emotion_16_tuosai
            "[哈哈哈]" -> R.drawable.coolapk_emotion_1_hahaha
            else -> R.drawable.coolapk_emotion_62_huaji
        }
    }

}
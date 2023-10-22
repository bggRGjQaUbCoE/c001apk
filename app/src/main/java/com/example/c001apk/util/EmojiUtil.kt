package com.example.c001apk.util

import com.example.c001apk.R

object EmojiUtil {

    fun getEmoji(emoji: String): Int {
        return when (emoji) {
            "[doge]" -> R.drawable.coolapk_emotion_37_doge
            "[doge原谅ta]" -> R.drawable.coolapk_emotion_58_dogeyuanliangta
            "[doge呵斥]" -> R.drawable.coolapk_emotion_57_dogehechi
            "[doge笑哭]" -> R.drawable.coolapk_emotion_56_dogexiaoku
            "[t耐克嘴]" -> R.drawable.coolapk_emotion_81_naikezui
            "[二哈]" -> R.drawable.coolapk_emotion_59_erha
            "[二哈盯]" -> R.drawable.coolapk_emotion_95_erhading
            "[亲亲]" -> R.drawable.coolapk_emotion_20_qinqin
            "[傲慢]" -> R.drawable.coolapk_emotion_10_aoman
            "[凋谢]" -> R.drawable.coolapk_emotion_42_diaoxie
            "[列文虎克]" -> R.drawable.coolapk_emotion_1023_liewenhuke
            "[发呆]" -> R.drawable.coolapk_emotion_102_fadai
            "[受虐滑稽]" -> R.drawable.coolapk_emotion_64_shounuehuaji
            "[可怜]" -> R.drawable.coolapk_emotion_26_kelian
            "[可爱]" -> R.drawable.coolapk_emotion_5_keai
            "[吃瓜]" -> R.drawable.coolapk_emotion_51_chigua
            "[吐舌]" -> R.drawable.coolapk_emotion_17_tushe
            "[呲牙]" -> R.drawable.coolapk_emotion_3_ciya
            "[哈哈哈]" -> R.drawable.coolapk_emotion_1_hahaha
            "[哦吼吼]" -> R.drawable.coolapk_emotion_1017
            "[哼唧]" -> R.drawable.coolapk_emotion_1011
            "[喝茶]" -> R.drawable.coolapk_emotion_1016
            "[喝酒]" -> R.drawable.coolapk_emotion_52_hejiu
            "[喷]" -> R.drawable.coolapk_emotion_44_pen
            "[喷血]" -> R.drawable.coolapk_emotion_21_penxue
            "[嘿哈]" -> R.drawable.coolapk_emotion_32_heiha
            "[坏笑]" -> R.drawable.coolapk_emotion_13_huaixiao
            "[墨镜滑稽]" -> R.drawable.coolapk_emotion_67_mojinghuaji
            "[委屈]" -> R.drawable.coolapk_emotion_47_weiqu
            "[害羞]" -> R.drawable.coolapk_emotion_97_haixiu
            "[强]" -> R.drawable.coolapk_emotion_27_qiang
            "[微微一笑]" -> R.drawable.coolapk_emotion_48_weiweiyixiao
            "[心碎]" -> R.drawable.coolapk_emotion_50_xinsui
            "[惊讶]" -> R.drawable.coolapk_emotion_2_jingya
            "[懒得理]" -> R.drawable.coolapk_emotion_107
            "[我最美]" -> R.drawable.coolapk_emotion_38_wozuimei
            "[托腮]" -> R.drawable.coolapk_emotion_16_tuosai
            "[抠鼻]" -> R.drawable.coolapk_emotion_19_koubi
            "[抱拳]" -> R.drawable.coolapk_emotion_29_baoquan
            "[挨打]" -> R.drawable.coolapk_emotion_1012
            "[捂脸]" -> R.drawable.coolapk_emotion_33_wulian
            "[撇嘴]" -> R.drawable.coolapk_emotion_8_piezui
            "[斗鸡眼滑稽]" -> R.drawable.coolapk_emotion_66_doujiyanhuaji
            "[无奈]" -> R.drawable.coolapk_emotion_98_wunai
            "[欢呼]" -> R.drawable.coolapk_emotion_49_huanhu
            "[汗]" -> R.drawable.coolapk_emotion_18_han
            "[流汗滑稽]" -> R.drawable.coolapk_emotion_63_liuhanhuaji
            "[流泪]" -> R.drawable.coolapk_emotion_4_liulei
            "[滑稽]" -> R.drawable.coolapk_emotion_62_huaji
            "[爱心]" -> R.drawable.coolapk_emotion_40_aixin
            "[牛啤]" -> R.drawable.coolapk_emotion_103_nb
            "[玫瑰]" -> R.drawable.coolapk_emotion_41_meigui
            "[疑问]" -> R.drawable.coolapk_emotion_11_yiwen
            "[皱眉]" -> R.drawable.coolapk_emotion_99_zhoumei
            "[笑哭]" -> R.drawable.coolapk_emotion_31_xiaoku
            "[红药丸]" -> R.drawable.coolapk_emotion_54_hongyaowan
            "[绿帽]" -> R.drawable.coolapk_emotion_96_kuanlvmao
            "[绿药丸]" -> R.drawable.coolapk_emotion_55_lvyaowan
            "[舒服]" -> R.drawable.coolapk_emotion_106
            "[色]" -> R.drawable.coolapk_emotion_9_se
            "[菜刀]" -> R.drawable.coolapk_emotion_39_caidao
            "[酷安]" -> R.drawable.coolapk_emotion_60_kuan
            "[酷安绿帽]" -> R.drawable.coolapk_emotion_96_kuanlvmao
            "[酷币100块]" -> R.drawable.c_oy
            "[酷币10块]" -> R.drawable.c_teny
            "[酷币2块]" -> R.drawable.c_twoy
            "[酷币5$]" -> R.drawable.c_fived
            "[酷币5€]" -> R.drawable.c_fiveo
            "[酷币]" -> R.drawable.c_coolb
            "[针不戳]" -> R.drawable.coolapk_emotion_1022_zhenbuchuo
            "[阴险]" -> R.drawable.coolapk_emotion_45_yinxian
            "[难过]" -> R.drawable.coolapk_emotion_46_nanguo
            "[黑线]" -> R.drawable.coolapk_emotion_43_heixian
            "[cos滑稽]" -> R.drawable.coolapk_emotion_65_coshuaji
            "[耶]" -> R.drawable.coolapk_emotion_35_ye
            "[偷看]" -> R.drawable.coolapk_emotion_1015
            "[噗]" -> R.drawable.coolapk_emotion_53_pu
            "[绿色酷币]" -> R.drawable.coolapk_emotion_69
            "[笑眼]" -> R.drawable.coolapk_emotion_22_xiaoyan
            "[小嘴滑稽]" -> R.drawable.coolapk_emotion_1013
            else -> -1
        }
    }

}
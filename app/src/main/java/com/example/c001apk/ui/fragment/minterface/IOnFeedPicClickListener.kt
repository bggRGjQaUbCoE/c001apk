package com.example.c001apk.ui.fragment.minterface

import cc.shinichi.library.bean.ImageInfo

interface IOnFeedPicClickListener {

    fun onShowPic(position: Int, urlList: MutableList<ImageInfo>)

}
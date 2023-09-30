package com.example.coolapk.ui.fragment.minterface

import com.example.coolapk.ui.fragment.minterface.IOnFeedPicClickListener

interface IOnFeedPicClickContainer {
    companion object {
        var controller: IOnFeedPicClickListener? = null
    }
}
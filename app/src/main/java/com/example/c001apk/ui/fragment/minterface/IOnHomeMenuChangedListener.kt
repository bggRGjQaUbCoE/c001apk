package com.example.c001apk.ui.fragment.minterface

import com.example.c001apk.logic.model.HomeMenu

interface IOnHomeMenuChangedListener {

    fun onMenuChanged(menuList:ArrayList<HomeMenu>)

}
package com.example.c001apk.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.FeedEntity
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val blackListRepo: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: String = savedStateHandle["type"] ?: "browse"

    val browseLiveData: LiveData<List<FeedEntity>> =
        if (type == "browse") {
            historyFavoriteRepo.loadAllHistoryListLive()
        } else {
            historyFavoriteRepo.loadAllFavoriteListLive()
        }


    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "browse" -> historyFavoriteRepo.deleteAllHistory()
                "favorite" -> historyFavoriteRepo.deleteAllFavorite()
                else -> {}
            }
        }
    }

    fun delete(fid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "browse" -> historyFavoriteRepo.deleteHistory(fid)
                "favorite" -> historyFavoriteRepo.deleteFavorite(fid)
                else -> {}
            }
        }
    }

    fun saveHistory(
        id: String,
        uid: String,
        username: String,
        userAvatar: String,
        deviceTitle: String,
        message: String,
        dateline: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            historyFavoriteRepo.saveHistory(
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
            )
        }
    }

}

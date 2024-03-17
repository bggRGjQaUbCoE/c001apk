package com.example.c001apk.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.FeedEntity
import com.example.c001apk.logic.repository.BlackListRepository
import com.example.c001apk.logic.repository.HistoryFavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val blackListRepository: BlackListRepository,
    private val historyFavoriteRepository: HistoryFavoriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: String = savedStateHandle["type"] ?: "browse"

    val browseLiveData: LiveData<List<FeedEntity>> =
        if (type == "browse") {
            historyFavoriteRepository.loadAllHistoryListLive()
        } else {
            historyFavoriteRepository.loadAllFavoriteListLive()
        }


    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepository.saveUid(uid)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "browse" -> historyFavoriteRepository.deleteAllHistory()
                "favorite" -> historyFavoriteRepository.deleteAllFavorite()
                else -> {}
            }
        }
    }

    fun delete(fid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "browse" -> historyFavoriteRepository.deleteHistory(fid)
                "favorite" -> historyFavoriteRepository.deleteFavorite(fid)
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
            historyFavoriteRepository.saveHistory(
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

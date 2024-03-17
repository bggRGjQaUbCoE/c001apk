package com.example.c001apk.ui.blacklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.repository.BlackListRepository
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlackListViewModel @Inject constructor(
    private val blackListRepository: BlackListRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: String = savedStateHandle["type"] ?: "user"
    val toastText = MutableLiveData<Event<String>>()

    val blackListLiveData: LiveData<List<StringEntity>> = when (type) {
        "user" -> blackListRepository.loadAllUserListLive()
        "topic" -> blackListRepository.loadAllTopicListLive()
        else -> throw IllegalArgumentException("invalid type: $type")
    }

    fun insertList(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> {
                    if (blackListRepository.checkUid(data))
                        toast()
                    else
                        blackListRepository.insertUid(StringEntity(data))
                }

                "topic" -> {
                    if (blackListRepository.checkTopic(data))
                        toast()
                    else
                        blackListRepository.insertTopic(StringEntity(data))
                }

                else -> {}
            }
        }
    }

    fun insertList(dataList: List<StringEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> blackListRepository.insertUidList(dataList)

                "topic" -> blackListRepository.insertTopicList(dataList)

                else -> {}
            }
        }
    }

    fun toast() {
        toastText.postValue(Event("已存在"))
    }

    fun deleteData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> blackListRepository.deleteUid(data)
                "topic" -> blackListRepository.deleteTopic(data)
                else -> {}
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> blackListRepository.deleteAllUser()
                "topic" -> blackListRepository.deleteAllTopic()
                else -> {}
            }
        }
    }

}
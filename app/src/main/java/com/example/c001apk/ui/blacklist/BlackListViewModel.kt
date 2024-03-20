package com.example.c001apk.ui.blacklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.util.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BlackListViewModel.Factory::class)
class BlackListViewModel @AssistedInject constructor(
    @Assisted val type: String,
    private val blackListRepo: BlackListRepo,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(type: String): BlackListViewModel
    }

    val toastText = MutableLiveData<Event<String>>()

    val blackListLiveData: LiveData<List<StringEntity>> = when (type) {
        "user" -> blackListRepo.loadAllUserListLive()
        "topic" -> blackListRepo.loadAllTopicListLive()
        else -> throw IllegalArgumentException("invalid type: $type")
    }

    fun insertList(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> {
                    if (blackListRepo.checkUid(data))
                        toast()
                    else
                        blackListRepo.insertUid(StringEntity(data))
                }

                "topic" -> {
                    if (blackListRepo.checkTopic(data))
                        toast()
                    else
                        blackListRepo.insertTopic(StringEntity(data))
                }

                else -> {}
            }
        }
    }

    fun insertList(dataList: List<StringEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> blackListRepo.insertUidList(dataList)

                "topic" -> blackListRepo.insertTopicList(dataList)

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
                "user" -> blackListRepo.deleteUid(data)
                "topic" -> blackListRepo.deleteTopic(data)
                else -> {}
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "user" -> blackListRepo.deleteAllUser()
                "topic" -> blackListRepo.deleteAllTopic()
                else -> {}
            }
        }
    }

}
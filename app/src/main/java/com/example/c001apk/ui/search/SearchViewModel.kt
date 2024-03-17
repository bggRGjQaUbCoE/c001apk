package com.example.c001apk.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchHistoryRepository
) : ViewModel() {

    var type: String? = null
    var title: String? = null
    var pageParam: String? = null
    var pageType: String? = null

    val blackListLiveData: LiveData<List<StringEntity>> = repository.loadAllListLive()

    fun insertData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!repository.checkHistory(data))
                repository.insertHistory(StringEntity(data))
        }
    }

    fun deleteData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistory(data)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUser()
        }
    }

}
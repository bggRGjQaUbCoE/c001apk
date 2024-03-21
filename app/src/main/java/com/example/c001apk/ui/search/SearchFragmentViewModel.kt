package com.example.c001apk.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.repository.SearchHistoryRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val repository: SearchHistoryRepo
) : ViewModel() {

    var type: String? = null
    var title: String? = null
    var pageParam: String? = null
    var pageType: String? = null

    val blackListLiveData: LiveData<List<StringEntity>> = repository.loadAllListLive()

    fun insertData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            with(StringEntity(data)) {
                if (repository.checkHistory(data)) {
                    val max = blackListLiveData.value?.maxOfOrNull { it.id } ?: -1
                    repository.updateHistory(data, max + 1)
                } else
                    repository.insertHistory(this)
            }

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
package com.example.c001apk.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.repository.SearchHistoryRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchFragmentViewModel @AssistedInject constructor(
    @Assisted("pageType") var pageType: String,
    @Assisted("pageParam") val pageParam: String,
    @Assisted("title") var title: String,
    private val historyRepo: SearchHistoryRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("pageType") pageType: String,
            @Assisted("pageParam") pageParam: String,
            @Assisted("title") title: String,
        ): SearchFragmentViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory, pageType: String, pageParam: String, title: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(pageType, pageParam, title) as T
            }
        }
    }

    var type: String? = null

    val blackListLiveData: LiveData<List<StringEntity>> = historyRepo.loadAllListLive()

    fun insertData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            with(StringEntity(data)) {
                if (historyRepo.checkHistory(data)) {
                    val max = blackListLiveData.value?.maxOfOrNull { it.id } ?: -1
                    historyRepo.updateHistory(data, max + 1)
                } else
                    historyRepo.insertHistory(this)
            }

        }
    }

    fun deleteData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepo.deleteHistory(data)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepo.deleteAllUser()
        }
    }

}
package org.eu.exodus_privacy.exodusprivacy.fragments.apps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.manager.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.manager.database.app.ExodusApplication
import javax.inject.Inject

enum class SortType {
    Name, Trackers, Permissions, CreatedAt, Default
}

@HiltViewModel
class AppsViewModel @Inject constructor(
    exodusDatabaseRepository: ExodusDatabaseRepository,
) : ViewModel() {

    private val _appList: LiveData<List<ExodusApplication>> = exodusDatabaseRepository.getAllApps()
    private val _sortedAppList = MediatorLiveData<List<ExodusApplication>>()

    val sortedAppList: LiveData<List<ExodusApplication>> = _sortedAppList

    private val _currentSortType = MutableLiveData(SortType.Default)
    val currentSortType: LiveData<SortType> = _currentSortType

    private var _currentSearchQuery = MutableLiveData<String>()
    val currentSearchQuery: LiveData<String> = _currentSearchQuery

    init {
        // By default, sorted list is the same as the original list,but
        // it allows us to apply sorting techniques in the future
        _sortedAppList.addSource(_appList) { apps ->
            _sortedAppList.value = apps
        }
    }

    fun sortApps(sortType: SortType) {
        val apps = _appList.value ?: return

        val sortedApps = when (sortType) {
            SortType.Name -> {
                _currentSortType.postValue(SortType.Name)
                apps.sortedBy { it.name }
            }

            SortType.Trackers -> {
                _currentSortType.postValue(SortType.Trackers)
                apps.sortedByDescending { it.exodusTrackers.size }
            }

            SortType.Permissions -> {
                _currentSortType.postValue(SortType.Permissions)
                apps.sortedByDescending { it.permissions.size }
            }

            SortType.CreatedAt -> {
                _currentSortType.postValue(SortType.CreatedAt)
                apps.sortedByDescending { it.created }
            }

            SortType.Default -> {
                _currentSortType.postValue(SortType.Default)
                apps
            }
        }
        _sortedAppList.value = sortedApps
    }

    fun searchApp(query: String) {
        _currentSearchQuery.value = query
        val apps = _appList.value ?: return

        if (query.isEmpty()) {
            sortApps(_currentSortType.value ?: SortType.Default)
            return
        }

        val filteredApps = apps.filter { it.name.contains(query, ignoreCase = true) }
        _sortedAppList.value = filteredApps
    }
}

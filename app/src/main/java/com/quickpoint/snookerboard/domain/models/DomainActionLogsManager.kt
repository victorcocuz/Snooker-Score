package com.quickpoint.snookerboard.domain.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

class DomainActionLogsManager @Inject constructor() {

    private var _actionLogs: MutableStateFlow<List<DomainActionLog>> = MutableStateFlow(emptyList())
    var actionLogs = _actionLogs.asStateFlow()

    fun addNewLog(newLog: DomainActionLog) {
        _actionLogs.value = _actionLogs.value.toMutableList().apply {
            add(newLog)
            Timber.e("Log: $newLog")
        }
    }
}
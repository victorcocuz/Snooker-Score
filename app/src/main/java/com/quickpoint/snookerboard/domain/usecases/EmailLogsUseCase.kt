package com.quickpoint.snookerboard.domain.usecases

import android.content.Context
import com.google.gson.Gson
import com.quickpoint.snookerboard.BuildConfig
import com.quickpoint.snookerboard.core.utils.Constants
import com.quickpoint.snookerboard.core.utils.sendEmail
import com.quickpoint.snookerboard.domain.repository.GameRepository
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import timber.log.Timber
import javax.inject.Inject

class EmailLogsUseCase @Inject constructor(
    private val matchConfig: MatchConfig,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(context: Context) {
        val logs = gameRepository.getDomainActionLogs().toString()
        val json = Gson().toJson(gameRepository.getDomainActionLogs())
        val body = "${matchConfig.getAsText()} \n\n $json \n\n $logs"
        Timber.e(json)
        context.sendEmail(arrayOf(BuildConfig.ADMIN_EMAIL), Constants.EMAIL_SUBJECT_LOGS, body)
    }
}
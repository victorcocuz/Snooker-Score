package com.quickpoint.snookerboard.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickpoint.snookerboard.domain.BallType
import com.quickpoint.snookerboard.domain.DomainActionLog
import com.quickpoint.snookerboard.domain.PotAction
import com.quickpoint.snookerboard.domain.PotType

@Entity(tableName = "match_debug_action_table")
data class DbActionLog(
    @PrimaryKey(autoGenerate = true)
    val actionId: Long = 0,
    val frameId: Long,
    val description: String,
    val potType: PotType? = null,
    val ballType: BallType? = null,
    val ballPoints: Int? = null,
    val potAction: PotAction? = null,
    val player: Int? = null,
    val breakCount: Int? = null,
    val ballStackLast: BallType? = null,
    val frameCount: Long? = null
)

// CONVERTER method from DATABASE debugFrameAction to string
fun List<DbActionLog>.asDomainActionLogs(): MutableList<DomainActionLog> {
    return map { dbActionLog ->
        DomainActionLog(
            description = dbActionLog.description,
            potType = dbActionLog.potType,
            ballType = dbActionLog.ballType,
            ballPoints = dbActionLog.ballPoints,
            potAction = dbActionLog.potAction,
            player = dbActionLog.player,
            breakCount = dbActionLog.breakCount,
            ballStackLast = dbActionLog.ballStackLast,
            frameCount = dbActionLog.frameCount
        )
    }.toMutableList()
}
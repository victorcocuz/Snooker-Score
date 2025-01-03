package com.quickpoint.snookerboard.data.database.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.quickpoint.snookerboard.data.database.SnookerDatabase.Companion.TABLE_MATCH_FRAMES
import com.quickpoint.snookerboard.domain.models.DomainFrame

@Entity(tableName = TABLE_MATCH_FRAMES)
data class DbFrame(
    @PrimaryKey(autoGenerate = false)
    val frameId: Long,
    var frameMax: Int
)

// Used to store all frame info within the DATABASE
data class DbFrameWithScoreAndBreakWithPotsAndBallStack(
    @Embedded val frame: DbFrame,
    @Relation(
        parentColumn = "frameId",
        entityColumn = "frameId",
    )
    val frameScore: List<DbScore>,
    @Relation(
        parentColumn = "frameId",
        entityColumn = "frameId",
        entity = DbBreak::class
    )
    val breaksList: List<DbBreakWithPots>,
    @Relation(
        parentColumn = "frameId",
        entityColumn = "frameId"
    )
    val ballStack: List<DbBall>,
    @Relation(
        parentColumn = "frameId",
        entityColumn = "frameId"
    )
    val debugFrameActions: List<DbActionLog>
)

fun DbFrameWithScoreAndBreakWithPotsAndBallStack.asDomain(): DomainFrame {
    return DomainFrame(
        frameId = frame.frameId,
        scoreList = frameScore.asDomain(),
        breaksList = breaksList.asDomain(),
        ballsList = ballStack.asDomain(),
        actionLogs = debugFrameActions.asDomain(),
        frameMax = frame.frameMax
    )
}
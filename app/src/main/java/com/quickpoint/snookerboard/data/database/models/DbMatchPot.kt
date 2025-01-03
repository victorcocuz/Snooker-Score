package com.quickpoint.snookerboard.data.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quickpoint.snookerboard.data.database.SnookerDatabase.Companion.TABLE_MATCH_POTS
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBall.BLACK
import com.quickpoint.snookerboard.domain.models.DomainBall.BLUE
import com.quickpoint.snookerboard.domain.models.DomainBall.BROWN
import com.quickpoint.snookerboard.domain.models.DomainBall.COLOR
import com.quickpoint.snookerboard.domain.models.DomainBall.FREEBALL
import com.quickpoint.snookerboard.domain.models.DomainBall.FREEBALLAVAILABLE
import com.quickpoint.snookerboard.domain.models.DomainBall.FREEBALLTOGGLE
import com.quickpoint.snookerboard.domain.models.DomainBall.GREEN
import com.quickpoint.snookerboard.domain.models.DomainBall.NOBALL
import com.quickpoint.snookerboard.domain.models.DomainBall.PINK
import com.quickpoint.snookerboard.domain.models.DomainBall.RED
import com.quickpoint.snookerboard.domain.models.DomainBall.WHITE
import com.quickpoint.snookerboard.domain.models.DomainBall.YELLOW
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.DomainPot.ADDRED
import com.quickpoint.snookerboard.domain.models.DomainPot.FOUL
import com.quickpoint.snookerboard.domain.models.DomainPot.FOULATTEMPT
import com.quickpoint.snookerboard.domain.models.DomainPot.FREEACTIVE
import com.quickpoint.snookerboard.domain.models.DomainPot.HIT
import com.quickpoint.snookerboard.domain.models.DomainPot.LASTBLACKFOULED
import com.quickpoint.snookerboard.domain.models.DomainPot.MISS
import com.quickpoint.snookerboard.domain.models.DomainPot.REMOVECOLOR
import com.quickpoint.snookerboard.domain.models.DomainPot.REMOVERED
import com.quickpoint.snookerboard.domain.models.DomainPot.RESPOTBLACK
import com.quickpoint.snookerboard.domain.models.DomainPot.SAFE
import com.quickpoint.snookerboard.domain.models.DomainPot.SAFEMISS
import com.quickpoint.snookerboard.domain.models.DomainPot.SNOOKER
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_ADDRED
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FOUL
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FOUL_ATTEMPT
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FREE
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FREE_ACTIVE
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_HIT
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_LAST_BLACK_FOULED
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_MISS
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_REMOVE_COLOR
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_REMOVE_RED
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_RESPOT_BLACK
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SAFE
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SAFE_MISS
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SNOOKER
import com.quickpoint.snookerboard.domain.models.PotType.entries
import com.quickpoint.snookerboard.domain.models.ShotType

@Entity(tableName = TABLE_MATCH_POTS)
data class DbPot(
    @PrimaryKey(autoGenerate = false)
    val potId: Long,
    val breakId: Long,
    val ballId: Long,
    val ballOrdinal: Int,
    val ballPoints: Int,
    val ballFoul: Int,
    val potType: Int,
    val potAction: Int,
    val shotType: Int
)

fun List<DbPot>.asDomain(): MutableList<DomainPot> {
    return map { pot ->
        when (entries[pot.potType]) {
            TYPE_HIT -> HIT(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_FOUL -> FOUL(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId,  pot.ballPoints, pot.ballFoul), PotAction.entries[pot.potAction], ShotType.entries[pot.shotType])
            TYPE_FOUL_ATTEMPT -> FOULATTEMPT(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_FREE_ACTIVE -> FREEACTIVE(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_FREE -> FREEACTIVE(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_SNOOKER -> SNOOKER(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_SAFE -> SAFE(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_SAFE_MISS -> SAFEMISS(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_MISS -> MISS(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul), ShotType.entries[pot.shotType])
            TYPE_REMOVE_RED -> REMOVERED(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_REMOVE_COLOR -> REMOVECOLOR(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_ADDRED -> ADDRED(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_LAST_BLACK_FOULED -> LASTBLACKFOULED(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
            TYPE_RESPOT_BLACK -> RESPOTBLACK(pot.potId, getBallFromValues(pot.ballOrdinal, pot.ballId, pot.ballPoints, pot.ballFoul))
        }
    }.toMutableList()
}

fun getBallFromValues(position: Int, id: Long, points: Int, foul: Int): DomainBall { // Return a DOMAIN ball from a list of values
    return when (position) {
        0 -> NOBALL(id, points, foul)
        1 -> WHITE(id, points, foul)
        2 -> RED(id, points, foul)
        3 -> YELLOW(id, points, foul)
        4 -> GREEN(id, points, foul)
        5 -> BROWN(id, points, foul)
        6 -> BLUE(id, points, foul)
        7 -> PINK(id, points, foul)
        8 -> BLACK(id, points, foul)
        9 -> COLOR(id, points, foul)
        10 -> FREEBALL(id, points, foul)
        11 -> FREEBALLAVAILABLE()
        else -> FREEBALLTOGGLE()
    }
}
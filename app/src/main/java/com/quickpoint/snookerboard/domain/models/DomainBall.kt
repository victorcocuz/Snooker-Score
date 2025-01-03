package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BLACK
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BLUE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BROWN
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_COLOR
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_FREEBALL
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_FREEBALLAVAILABLE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_FREEBALLTOGGLE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_GREEN
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_NOBALL
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_PINK
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_RED
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_WHITE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_YELLOW

enum class BallType { TYPE_NOBALL, TYPE_WHITE, TYPE_RED, TYPE_YELLOW, TYPE_GREEN, TYPE_BROWN, TYPE_BLUE, TYPE_PINK, TYPE_BLACK, TYPE_COLOR, TYPE_FREEBALL, TYPE_FREEBALLTOGGLE, TYPE_FREEBALLAVAILABLE }

sealed class DomainBall(
    var ballId: Long,
    var ballType: BallType,
    var points: Int,
    var foul: Int,
) {
    class NOBALL(ballId: Long, points: Int = 0, foul: Int = 0) : DomainBall(ballId, TYPE_NOBALL, points, foul)
    class WHITE(ballId: Long, points: Int = 0, foul: Int = 4) : DomainBall(ballId, TYPE_WHITE, points, foul)

    class RED(ballId: Long, points: Int = 1, foul: Int = 4) : DomainBall(ballId, TYPE_RED, points, foul)

    class YELLOW(ballId: Long, points: Int = 2, foul: Int = 4) : DomainBall(ballId, TYPE_YELLOW, points, foul)

    class GREEN(ballId: Long, points: Int = 3, foul: Int = 4) : DomainBall(ballId, TYPE_GREEN, points, foul)

    class BROWN(ballId: Long, points: Int = 4, foul: Int = 4) : DomainBall(ballId, TYPE_BROWN, points, foul)

    class BLUE(ballId: Long, points: Int = 5, foul: Int = 5) : DomainBall(ballId, TYPE_BLUE, points, foul)

    class PINK(ballId: Long, points: Int = 6, foul: Int = 6) : DomainBall(ballId, TYPE_PINK, points, foul)

    class BLACK(ballId: Long, points: Int = 7, foul: Int = 7) : DomainBall(ballId, TYPE_BLACK, points, foul)

    class COLOR(ballId: Long, points: Int = 1, foul: Int = 4) : DomainBall(ballId, TYPE_COLOR, points, foul)

    class FREEBALL(ballId: Long = 0, points: Int = 1, foul: Int = 4) : DomainBall(ballId, TYPE_FREEBALL, points, foul)

    class FREEBALLAVAILABLE(ballId: Long = 0, points: Int = 1, foul: Int = 4) : DomainBall(ballId, TYPE_FREEBALLAVAILABLE, points, foul)

    class FREEBALLTOGGLE(ballId: Long = 0, points: Int = 0, foul: Int = 0) : DomainBall(ballId, TYPE_FREEBALLTOGGLE, points, foul)
}

fun DomainBall.getBallOrdinal(): Int { // Get a numeric correspondent for each ball to store in database
    return when (ballType) {
        TYPE_NOBALL -> 0
        TYPE_WHITE -> 1
        TYPE_RED -> 2
        TYPE_YELLOW -> 3
        TYPE_GREEN -> 4
        TYPE_BROWN -> 5
        TYPE_BLUE -> 6
        TYPE_PINK -> 7
        TYPE_BLACK -> 8
        TYPE_COLOR -> 9
        TYPE_FREEBALL -> 10
        TYPE_FREEBALLAVAILABLE -> 11
        TYPE_FREEBALLTOGGLE -> 12
    }
}

fun List<DomainBall>.isLastBall() = size == 1
fun List<DomainBall>.isLastBlack() = size == 2
fun List<DomainBall>.isInColorsWithFreeBall() = size <= 8
fun List<DomainBall>.wasPreviousBallColor() = size in (7..37).filter { it % 2 == 1 }
fun List<DomainBall>.isThisBallColorAndNotLast() = size in (10..38).filter { it % 2 == 0 }
fun List<DomainBall>.redsRemaining() = (size - 7) / 2
fun List<DomainBall>.redsOnTheTable() = count { it.ballType == TYPE_RED }
fun List<DomainBall>.maxRemoveReds() = minOf(redsOnTheTable(), 3)
fun List<DomainBall>.foulValue() = if (size > 4) 4 else (7 - 2 * (size - 1))
fun List<DomainBall>.filterBallsForFoulDialog() = filter { it.ballType !in listOf(TYPE_FREEBALL, TYPE_NOBALL, TYPE_COLOR) }.reversed()

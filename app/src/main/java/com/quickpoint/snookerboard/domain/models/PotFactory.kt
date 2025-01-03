package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.repository.DataStoreRepository
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import javax.inject.Inject

class PotFactory @Inject constructor(
    private val matchConfig: MatchConfig,
    private val ballFactory: BallFactory,
    private val dataStoreRepository: DataStoreRepository,
) {
    private fun createPot(
        ball: DomainBall? = null,
        potType: PotType,
        shotType: ShotType,
        shotAction: PotAction = PotAction.CONTINUE,
    ): DomainPot =
        when (potType) {
            PotType.TYPE_HIT -> DomainPot.HIT(
                matchConfig.generateUniqueId(),
                ball!!,
                shotType
            )

            PotType.TYPE_FOUL -> DomainPot.FOUL(
                matchConfig.generateUniqueId(),
                ball!!,
                shotAction,
                shotType
            )

            PotType.TYPE_FOUL_ATTEMPT -> DomainPot.FOULATTEMPT(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_FREEBALLTOGGLE),
                shotType
            )

            PotType.TYPE_FREE_ACTIVE -> DomainPot.FREEACTIVE(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL)
            )

            PotType.TYPE_FREE -> DomainPot.FREE(
                matchConfig.generateUniqueId(),
                ball!!,
                shotType
            )

            PotType.TYPE_SNOOKER -> DomainPot.SNOOKER(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL),
                shotType
            )

            PotType.TYPE_SAFE -> DomainPot.SAFE(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL),
                shotType
            )

            PotType.TYPE_SAFE_MISS -> DomainPot.SAFEMISS(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL),
                shotType
            )

            PotType.TYPE_MISS -> DomainPot.MISS(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL),
                shotType
            )

            PotType.TYPE_REMOVE_RED -> DomainPot.REMOVERED(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_RED)
            )

            PotType.TYPE_REMOVE_COLOR -> DomainPot.REMOVECOLOR(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_COLOR)
            )

            PotType.TYPE_ADDRED -> DomainPot.ADDRED(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_RED)
            )

            PotType.TYPE_LAST_BLACK_FOULED -> DomainPot.LASTBLACKFOULED(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL)
            )

            PotType.TYPE_RESPOT_BLACK -> DomainPot.RESPOTBLACK(
                matchConfig.generateUniqueId(),
                ballFactory.createBall(BallType.TYPE_NOBALL)
            )
        }

    suspend fun createPotWithFreeBallHandling(potType: PotType, ball: DomainBall? = null, action: PotAction): DomainPot {
        val shotType = dataStoreRepository.getShotType()
        return if (ball is DomainBall.FREEBALL) {
            createPot(
                ball = DomainBall.FREEBALL(points = ball.points),
                potType = PotType.TYPE_FREE,
                shotType = shotType
            )
        } else {
            createPot(ball, potType, shotType, action)
        }
    }

}
package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {

    init {
        require(point >= 0L) {
            throw IllegalStateException("포인트는 0원보다 많아야 합니다.")
        }

        require(point <= 1_000_000L) {
            throw IllegalStateException("point는 1,000,000 이상일 수 없습니다.")
        }
    }

    fun charge(amount: Long): UserPoint {
        require(amount > 0L) {throw IllegalStateException("포인트 충전 금액은 0원 보다 많아야 합니다.")}
        val chargePoint = point + amount
        return UserPoint(id,chargePoint,updateMillis)
    }

    fun use(amount: Long): UserPoint {
        require(amount <= 1_000_000L) {throw IllegalStateException("포인트 최대 사용 금액은 1,000,000 초과 일 수 없습니다.")}
        val usePoint = point - amount
        return UserPoint(id,usePoint,updateMillis)
    }

}

package io.hhplus.tdd.point

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


object UserPointLocker {

    private val concurrentMap : MutableMap<Long, ReentrantLock> = ConcurrentHashMap()

    fun <T> withUserPointLock(id: Long, action: () -> T): T {
        val lock = concurrentMap.computeIfAbsent(id) { ReentrantLock() }
        return lock.withLock {
            action()
        }
    }

}
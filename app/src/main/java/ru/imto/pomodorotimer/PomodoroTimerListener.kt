package ru.imto.pomodorotimer

interface PomodoroTimerListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)
}
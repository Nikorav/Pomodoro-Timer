package ru.imto.pomodorotimer.recyclerView

import java.io.Serializable

data class PomodoroTimer(
    val id: Int,
    var currentMs: Long,
    var startMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean = false
) : Serializable
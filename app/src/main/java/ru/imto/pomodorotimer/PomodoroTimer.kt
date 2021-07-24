package ru.imto.pomodorotimer

import java.io.Serializable

data class PomodoroTimer (
    val id: Int,
    var startMs: Long,
    var currentMs: Long,
    var isStarted: Boolean
) : Serializable
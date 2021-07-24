package ru.imto.pomodorotimer

import java.io.Serializable

data class PomodoroTimer (
    val id: Int,
    var currentMs: Long,
    var startMs: Long,
    var isStarted: Boolean
) : Serializable
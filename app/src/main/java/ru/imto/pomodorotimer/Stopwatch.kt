package ru.imto.pomodorotimer

data class Stopwatch (
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)
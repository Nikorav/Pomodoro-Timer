package ru.imto.pomodorotimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import ru.imto.pomodorotimer.recyclerView.PomodoroTimer
import ru.imto.pomodorotimer.recyclerView.PomodoroTimerAdapter
import ru.imto.pomodorotimer.recyclerView.PomodoroTimerListener
import ru.imto.pomodorotimer.service.ForegroundService
import ru.imto.pomodorotimer.databinding.ActivityMainBinding
import ru.imto.pomodorotimer.constants.COMMAND_ID
import ru.imto.pomodorotimer.constants.COMMAND_START
import ru.imto.pomodorotimer.constants.COMMAND_STOP
import ru.imto.pomodorotimer.constants.STARTED_TIMER_TIME_MS

class MainActivity : AppCompatActivity(), PomodoroTimerListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private var minutesInput = ""
    private var minutesToMs = 0L
    private val pomodoroTimers = mutableListOf<PomodoroTimer>()
    private val pomodoroTimerAdapter = PomodoroTimerAdapter(this)
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pomodoroTimerAdapter
        }

        binding.addButton.setOnClickListener {
            minutesInput = binding.textInput.text.toString()
            if (minutesInput.isEmpty() || minutesInput.toInt() == 0 || minutesInput.toInt() > 999) {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
            } else {
                minutesToMs = (minutesInput.toInt() * 60 * 1000).toLong()
                pomodoroTimers.add(PomodoroTimer(nextId++, minutesToMs, minutesToMs, false))
                pomodoroTimerAdapter.submitList(pomodoroTimers.toList())
            }
        }
    }

    override fun start(id: Int) {
        val stopTimers = mutableListOf<PomodoroTimer>()

        pomodoroTimers.forEach {
            stopTimers.add(PomodoroTimer(it.id, it.currentMs, it.startMs, false, it.isFinished))
        }
        pomodoroTimerAdapter.submitList(stopTimers)
        pomodoroTimers.clear()
        pomodoroTimers.addAll(stopTimers)

        shiftPomodoroTimerState(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        shiftPomodoroTimerState(id, currentMs, false)
    }

    override fun delete(id: Int) {
        pomodoroTimers.remove(pomodoroTimers.find { it.id == id })
        pomodoroTimerAdapter.submitList(pomodoroTimers.toList())
    }

    private fun shiftPomodoroTimerState(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<PomodoroTimer>()

        pomodoroTimers.forEach {
            if (it.id == id) {
                newTimers.add(
                    PomodoroTimer(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.startMs,
                        isStarted,
                        it.isFinished
                    )
                )
            } else newTimers.add(it)
        }

        pomodoroTimerAdapter.submitList(newTimers)
        pomodoroTimers.clear()
        pomodoroTimers.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        pomodoroTimers.forEach {
            if (it.isStarted) {
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(STARTED_TIMER_TIME_MS, it)
                startService(startIntent)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}

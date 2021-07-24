package ru.imto.pomodorotimer

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.imto.pomodorotimer.databinding.PomodoroTimerViewBinding

class PomodoroTimerViewHolder(
    private val binding: PomodoroTimerViewBinding,
    private val listener: PomodoroTimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null

    fun bind(pomodoroTimer: PomodoroTimer) {
        binding.textTimer.text = pomodoroTimer.currentMs.displayTime()
        binding.startPauseButton.text = "Start"
        binding.customTimer.setPeriod(pomodoroTimer.startMs)
        setIsRecyclable(true)

        if (pomodoroTimer.currentMs >= pomodoroTimer.startMs) binding.customTimer.setCurrent(0)
        else binding.customTimer.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)


        if (pomodoroTimer.isStarted) startTimer(pomodoroTimer)
        else stopTimer(pomodoroTimer)
        initButtonsListeners(pomodoroTimer)
    }

    private fun initButtonsListeners(pomodoroTimer: PomodoroTimer) {

        binding.startPauseButton.setOnClickListener {
            if (pomodoroTimer.isStarted) {
                listener.stop(pomodoroTimer.id, pomodoroTimer.currentMs)
                binding.startPauseButton.text = resources.getText(R.string.text_button)
            } else {
                listener.start(pomodoroTimer.id)
                binding.startPauseButton.text = "Stop"
            }
        }

        binding.deleteButton.setOnClickListener {
            setIsRecyclable(true)
            stopTimer(pomodoroTimer)
            pomodoroTimer.isStarted = false
            binding.pomodoroTimerItem.setCardBackgroundColor(resources.getColor(R.color.white))
            listener.delete(pomodoroTimer.id)
        }
    }

    private fun startTimer(pomodoroTimer: PomodoroTimer) {
        setIsRecyclable(false)
        binding.startPauseButton.text = resources.getText(R.string.text_button)
        binding.pomodoroTimerItem.setCardBackgroundColor(resources.getColor(R.color.white))

        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(pomodoroTimer)
        countDownTimer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(pomodoroTimer: PomodoroTimer) {

        countDownTimer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(pomodoroTimer: PomodoroTimer): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                pomodoroTimer.currentMs = millisUntilFinished
                binding.textTimer.text = pomodoroTimer.currentMs.displayTime()
                binding.customTimer.setCurrent(pomodoroTimer.startMs-pomodoroTimer.currentMs)
            }

            override fun onFinish() {
                binding.apply {
                    pomodoroTimerItem.setCardBackgroundColor(resources.getColor(R.color.purple_700))
                    customTimer.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)
                    blinkingIndicator.isInvisible = true
                    startPauseButton.isClickable = false
                    startPauseButton.text = "Finished"
                    setIsRecyclable(true)
                    (blinkingIndicator.background as? AnimationDrawable)?.stop()
                }
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L
    }
}

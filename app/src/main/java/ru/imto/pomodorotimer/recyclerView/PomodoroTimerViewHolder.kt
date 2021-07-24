package ru.imto.pomodorotimer.recyclerView

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import ru.imto.pomodorotimer.R
import ru.imto.pomodorotimer.constants.UNIT_TEN_MS
import ru.imto.pomodorotimer.databinding.PomodoroTimerViewBinding
import ru.imto.pomodorotimer.constants.displayTime

class PomodoroTimerViewHolder(
    private val binding: PomodoroTimerViewBinding,
    private val listener: PomodoroTimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    private var countDownTimer: CountDownTimer? = null

    fun bind(pomodoroTimer: PomodoroTimer) {
        setIsRecyclable(true)
        if (pomodoroTimer.isFinished) {
            binding.apply {
                textPomodoroTimer.text = resources.getText(R.string.text_timer)

                pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.teal_700))

                defaultActivityButton.isClickable = false
                defaultActivityButton.text = resources.getText(R.string.text_complete)

                customProgressBar.setPeriod(pomodoroTimer.startMs)
                customProgressBar.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)

                deleteButton.setOnClickListener {
                    stopTimer(pomodoroTimer)
                    pomodoroTimer.isStarted = false

                    pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.white))

                    listener.delete(pomodoroTimer.id)

                }
            }
        } else {
            binding.pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.white))

            binding.textPomodoroTimer.text = pomodoroTimer.currentMs.displayTime()

            binding.defaultActivityButton.text = resources.getText(R.string.text_start)

            binding.customProgressBar.setPeriod(pomodoroTimer.startMs)

            if (pomodoroTimer.isStarted) {
                startTimer(pomodoroTimer)

                binding.defaultActivityButton.text = resources.getText(R.string.text_stop)
            } else {
                stopTimer(pomodoroTimer)

                binding.defaultActivityButton.text = resources.getText(R.string.text_start)
            }

            if (pomodoroTimer.currentMs >= pomodoroTimer.startMs) binding.customProgressBar.setCurrent(0)
            else binding.customProgressBar.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)

            initButtonsListeners(pomodoroTimer)
        }

    }

    private fun initButtonsListeners(pomodoroTimer: PomodoroTimer) {
        binding.defaultActivityButton.setOnClickListener {
            if (pomodoroTimer.isStarted) listener.stop(pomodoroTimer.id, pomodoroTimer.currentMs)
            else listener.start(pomodoroTimer.id)
        }

        binding.deleteButton.setOnClickListener {
            setIsRecyclable(true)
            stopTimer(pomodoroTimer)
            pomodoroTimer.isStarted = false

            binding.pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.white))
            listener.delete(pomodoroTimer.id)
        }
    }

    private fun startTimer(pomodoroTimer: PomodoroTimer) {
        setIsRecyclable(false)

        binding.defaultActivityButton.text = resources.getText(R.string.text_stop)
        binding.pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.white))

        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(pomodoroTimer)
        countDownTimer?.start()

        binding.circleIndicator.isInvisible = false
        (binding.circleIndicator.background as? AnimationDrawable)?.start()

    }

    private fun stopTimer(pomodoroTimer: PomodoroTimer) {
        countDownTimer?.cancel()
        binding.circleIndicator.isInvisible = true
        (binding.circleIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(pomodoroTimer: PomodoroTimer): CountDownTimer {

        return object : CountDownTimer(pomodoroTimer.currentMs, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                pomodoroTimer.currentMs = millisUntilFinished

                binding.textPomodoroTimer.text = millisUntilFinished.displayTime()
                binding.customProgressBar.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)
            }

            override fun onFinish() {
                binding.apply {
                    pomodoroTimerView.setCardBackgroundColor(resources.getColor(R.color.teal_700))

                    customProgressBar.setCurrent(pomodoroTimer.startMs - pomodoroTimer.currentMs)

                    circleIndicator.isInvisible = true

                    defaultActivityButton.isClickable = false
                    defaultActivityButton.text = resources.getText(R.string.text_complete)

                    pomodoroTimer.isStarted = false
                    pomodoroTimer.isFinished = true
                    setIsRecyclable(false)
                    (circleIndicator.background as? AnimationDrawable)?.stop()
                }
            }
        }
    }
}

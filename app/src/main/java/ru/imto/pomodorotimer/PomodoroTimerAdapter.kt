package ru.imto.pomodorotimer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.imto.pomodorotimer.databinding.PomodoroTimerViewBinding

class PomodoroTimerAdapter(
    private val listener: PomodoroTimerListener
) : ListAdapter<PomodoroTimer, PomodoroTimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PomodoroTimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PomodoroTimerViewBinding.inflate(layoutInflater, parent, false)
        return PomodoroTimerViewHolder(binding, listener, binding.root.context.resources)
    }

    override fun onBindViewHolder(holder: PomodoroTimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<PomodoroTimer>() {

            override fun areItemsTheSame(oldItem: PomodoroTimer, newItem: PomodoroTimer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PomodoroTimer, newItem: PomodoroTimer): Boolean {
                return oldItem.currentMs == newItem.currentMs &&
                        oldItem.isStarted == newItem.isStarted
            }

            override fun getChangePayload(oldItem: PomodoroTimer, newItem: PomodoroTimer) = Any()
        }
    }
}
package com.example.vb_weatherapp.fragments.homepage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vb_weatherapp.data.DailyForecast
import coil.load
import com.example.vb_weatherapp.databinding.ItemContainerForecastDailyBinding

class WeatherDataAdapterHomepage(
    private val onItemClick: (DailyForecast) -> Unit
) : ListAdapter<DailyForecast, WeatherDataAdapterHomepage.ViewHolder>(DailyForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContainerForecastDailyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContainerForecastDailyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: DailyForecast) {
            binding.apply {
                textDate.text = forecast.dayOfWeek
                textTemperature.text = "${forecast.minTemp.toInt()}°C / ${forecast.maxTemp.toInt()}°C"
                imageIcon.load("https:${forecast.icon}") {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
                root.setOnClickListener { onItemClick(forecast) }
            }
        }
    }

    class DailyForecastDiffCallback : DiffUtil.ItemCallback<DailyForecast>() {
        override fun areItemsTheSame(oldItem: DailyForecast, newItem: DailyForecast): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyForecast, newItem: DailyForecast): Boolean {
            return oldItem == newItem
        }
    }
}
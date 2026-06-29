package com.lumio.app.weather
import java.util.UUID
object WeatherAnalyzer {
    fun analyze(weather: WeatherInfo): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()
        if (weather.isStormy) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\u26C8\uFE0F","Storm Warning!","Heavy thunderstorm. Stay indoors if possible.",AlertPriority.HIGH,true))
        else if (weather.isRaining) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\u2614","Carry Umbrella!","Rain expected today. Take your umbrella!",AlertPriority.HIGH,true))
        if (weather.isHot) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\uD83C\uDF21\uFE0F","Stay Hydrated!","Temperature is ${weather.temperatureCelsius}C. Drink extra water!",AlertPriority.HIGH,true))
        if (weather.isCold) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\uD83E\uDDE5","Wear Warm Clothes","Cold at ${weather.temperatureCelsius}C. Wear a jacket!",AlertPriority.MEDIUM,true))
        if (weather.isFoggy) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\uD83C\uDF2B\uFE0F","Drive Carefully","Dense fog. Drive slowly and use fog lights.",AlertPriority.HIGH,true))
        if (weather.isWindy) alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\uD83D\uDCA8","Strong Winds","Wind ${weather.windSpeed.toInt()} km/h. Secure loose items.",AlertPriority.MEDIUM,false))
        if (!weather.isRaining && !weather.isStormy && !weather.isHot && !weather.isCold && weather.temperatureCelsius in 20..30)
            alerts.add(WeatherAlert(UUID.randomUUID().toString(),"\uD83C\uDF1F","Perfect Weather!","Beautiful ${weather.temperatureCelsius}C. Great for outdoor activities!",AlertPriority.LOW,false))
        return alerts
    }
    fun fromResponse(response: WeatherResponse): WeatherInfo {
        val desc = response.weather.firstOrNull()
        val weatherId = desc?.id ?: 800
        val windKmh = response.wind.speed * 3.6
        val tempC = response.main.temp - 273.15
        return WeatherInfo(
            cityName=response.name, temperature=tempC,
            feelsLike=response.main.feels_like-273.15,
            humidity=response.main.humidity,
            description=desc?.description?:"clear sky",
            icon=desc?.icon?:"01d", windSpeed=windKmh,
            visibility=response.visibility,
            isRaining=weatherId in 200..622,
            isStormy=weatherId in 200..232,
            isCold=tempC<15, isHot=tempC>35,
            isFoggy=weatherId in 701..781,
            isWindy=windKmh>40, timestamp=response.dt*1000L
        )
    }
}

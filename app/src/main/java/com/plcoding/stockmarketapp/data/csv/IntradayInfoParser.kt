package com.plcoding.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.plcoding.stockmarketapp.data.mapper.toIntradayInfo
import com.plcoding.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayInfoParser @Inject constructor() : CSVParser<IntradayInfo> {
    override suspend fun parse(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(firstColumn) ?: return@mapNotNull null
                    val close = line.getOrNull(fourthColumn) ?: return@mapNotNull null
                    val dto = IntradayInfoDto(timestamp, close.toDouble())
                    dto.toIntradayInfo()
                }
                .filter {
                    when (LocalDateTime.now().dayOfWeek) {
                        DayOfWeek.SUNDAY -> it.date.dayOfMonth == LocalDateTime.now()
                            .minusDays(2).dayOfMonth
                        DayOfWeek.MONDAY -> it.date.dayOfMonth == LocalDateTime.now()
                            .minusDays(3).dayOfMonth
                        else ->
                            it.date.dayOfMonth == LocalDateTime.now().minusDays(1).dayOfMonth
                    }
                }.also {
                    csvReader.close()
                }
        }
    }

    companion object {
        const val firstColumn = 0
        const val fourthColumn = 4
    }
}

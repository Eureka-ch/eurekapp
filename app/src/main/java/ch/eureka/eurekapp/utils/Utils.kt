package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    /**
     * @param millis a Unix timestamp to convert to a date
     * @return a date formatted in the dd/MM/yyyy pattern
     * **/
    fun convertMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }
}
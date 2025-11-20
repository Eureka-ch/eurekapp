/* Portions of the code in this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.map

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a location.
 *
 * A location is attached to meetings that are in-person.
 *
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 * @property name The name of the location.
 */
@Parcelize
data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = "Null Island",
) : Parcelable

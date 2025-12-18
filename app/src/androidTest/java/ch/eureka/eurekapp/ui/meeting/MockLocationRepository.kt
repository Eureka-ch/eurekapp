/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.model.map.LocationRepository

class MockLocationRepository : LocationRepository {
  var searchResults: List<Location> = emptyList()
  var shouldThrow: Boolean = false

  override suspend fun search(query: String): List<Location> {
    if (shouldThrow) {
      throw Exception("Mock location error")
    }
    return searchResults
  }
}

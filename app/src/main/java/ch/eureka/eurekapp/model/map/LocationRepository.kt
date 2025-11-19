/*
This file is copy-pasted from the Bootcamp provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.map

/**
 * Interface that represents the repository from which to get a list of location
 * (longitude-latitude) from a location name.
 */
interface LocationRepository {
  /**
   * Search for locations matching the following [query]
   *
   * @param query The name of the location to search for latitude-longitude coordinate for.
   * @return The list of locations matching the [query]
   */
  suspend fun search(query: String): List<Location>
}

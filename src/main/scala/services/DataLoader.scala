package services

import scala.io.Source
import model.{Country, Airport, Runway}

object DataLoader:
  // Loads CSV lines (skipping header) and deserializes each into a Country instance.
  def loadCountries(filePath: String): List[Country] =
    Source.fromFile(filePath)
      .getLines
      .drop(1)
      .toList
      .flatMap(Country.fromCsvLine)

  // Loads airports from the CSV file.
  def loadAirports(filePath: String): List[Airport] =
    Source.fromFile(filePath)
      .getLines
      .drop(1)
      .toList
      .flatMap(Airport.fromCsvLine)

  // Loads runways from the CSV file.
  def loadRunways(filePath: String): List[Runway] =
    Source.fromFile(filePath)
      .getLines
      .drop(1)
      .toList
      .flatMap(Runway.fromCsvLine)
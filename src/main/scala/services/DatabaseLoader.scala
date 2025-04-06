package services

import model.{Country, Airport, Runway}
import slick.jdbc.H2Profile.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object DatabaseLoader {

  // Table definition for Countries
  class CountriesTable(tag: Tag) extends Table[Country](tag, "COUNTRIES") {
    def id = column[Int]("ID", O.PrimaryKey)
    def code = column[String]("CODE")
    def name = column[String]("NAME")
    def continent = column[Option[String]]("CONTINENT")
    def wikipediaLink = column[Option[String]]("WIKIPEDIA_LINK")
    def keywords = column[Option[String]]("KEYWORDS")

    // Use .mapTo to map columns directly to the case class
    def * = (id, code, name, continent, wikipediaLink, keywords).mapTo[Country]
  }
  val countries = TableQuery[CountriesTable]

  // Table definition for Airports
  class AirportsTable(tag: Tag) extends Table[Airport](tag, "AIRPORTS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def ident = column[String]("IDENT")
    def airportType = column[String]("TYPE")
    def name = column[String]("NAME")
    def latitude_deg = column[Option[Double]]("LATITUDE_DEG")
    def longitude_deg = column[Option[Double]]("LONGITUDE_DEG")
    def elevation_ft = column[Option[Int]]("ELEVATION_FT")
    def continent = column[Option[String]]("CONTINENT")
    def iso_country = column[Option[String]]("ISO_COUNTRY")
    def iso_region = column[Option[String]]("ISO_REGION")
    def municipality = column[Option[String]]("MUNICIPALITY")
    def scheduled_service = column[Option[String]]("SCHEDULED_SERVICE")
    def gps_code = column[Option[String]]("GPS_CODE")
    def iata_code = column[Option[String]]("IATA_CODE")
    def local_code = column[Option[String]]("LOCAL_CODE")
    def home_link = column[Option[String]]("HOME_LINK")
    def wikipedia_link = column[Option[String]]("WIKIPEDIA_LINK")
    def keywords = column[Option[String]]("KEYWORDS")

    def * = (
      id,
      ident,
      airportType,
      name,
      latitude_deg,
      longitude_deg,
      elevation_ft,
      continent,
      iso_country,
      iso_region,
      municipality,
      scheduled_service,
      gps_code,
      iata_code,
      local_code,
      home_link,
      wikipedia_link,
      keywords
    ).mapTo[Airport]
  }
  val airports = TableQuery[AirportsTable]

  // Table definition for Runways
  class RunwaysTable(tag: Tag) extends Table[Runway](tag, "RUNWAYS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def airport_ref = column[Int]("AIRPORT_REF")
    def airport_ident = column[String]("AIRPORT_IDENT")
    def length_ft = column[Option[Int]]("LENGTH_FT")
    def width_ft = column[Option[Int]]("WIDTH_FT")
    def surface = column[Option[String]]("SURFACE")
    def lighted = column[Option[Int]]("LIGHTED")
    def closed = column[Option[Int]]("CLOSED")
    def le_ident = column[Option[String]]("LE_IDENT")

    def * = (
      id,
      airport_ref,
      airport_ident,
      length_ft,
      width_ft,
      surface,
      lighted,
      closed,
      le_ident
    ).mapTo[Runway]
  }
  val runways = TableQuery[RunwaysTable]

  // Create an in-memory H2 database using the configuration key "h2mem1"
  val db = Database.forConfig("h2mem1")

  // Set up the schema (creates tables if they don't exist)
  def setupSchema(): Unit = {
    val schema = countries.schema ++ airports.schema ++ runways.schema
    val setupAction = schema.createIfNotExists
    Await.result(db.run(setupAction), Duration.Inf)
  }

  // Load data from CSV files into the database using DataLoader
  def loadData(countriesPath: String, airportsPath: String, runwaysPath: String): Unit = {
    val countryData = DataLoader.loadCountries(countriesPath)
    val airportData = DataLoader.loadAirports(airportsPath)
    val runwayData = DataLoader.loadRunways(runwaysPath)

    val actions = DBIO.seq(
      countries ++= countryData,
      airports  ++= airportData,
      runways   ++= runwayData
    )
    Await.result(db.run(actions.transactionally), Duration.Inf)
  }

  // Example: fuzzy matching of country name or exact country code using a simple LIKE query
  def findCountryByNameOrCode(query: String): Seq[Country] = {
    // If you see warnings about .like, make sure you have the right Slick imports
    val q = countries.filter { c =>
      c.name.toLowerCase.like(s"%${query.toLowerCase}%") ||
        c.code.toLowerCase === query.toLowerCase
    }
    Await.result(db.run(q.result), Duration.Inf)
  }

  // Example: Get airports and their runways for a given country code
  def getAirportsAndRunwaysForCountry(countryCode: String): Seq[(Airport, Seq[Runway])] = {
    val airportQuery = airports.filter(_.iso_country === countryCode.toUpperCase)
    val airportList  = Await.result(db.run(airportQuery.result), Duration.Inf)

    airportList.map { airport =>
      val runwaysList = Await.result(db.run(runways.filter(_.airport_ref === airport.id).result), Duration.Inf)
      (airport, runwaysList)
    }
  }

  def getAllCountries(): Seq[Country] =
    Await.result(db.run(countries.result), Duration.Inf)
}
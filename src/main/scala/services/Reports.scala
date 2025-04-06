package services

import slick.jdbc.H2Profile.api._
import scala.concurrent.Await
import scala.concurrent.duration._
import model.{Country, Airport, Runway}
import DatabaseLoader.{db, countries, airports, runways}

object Reports {

  // Report 1: Top 10 countries with the highest number of airports.
  def top10CountriesByAirports(): Seq[(String, Int)] = {
    val query = for {
      (a, c) <- airports join countries on (_.iso_country === _.code)
    } yield (c.name, a.id)
    val groupedQuery = query.groupBy(_._1).map {
      case (name, group) => (name, group.length)
    }
    val sortedQuery = groupedQuery.sortBy(_._2.desc).take(10)
    Await.result(db.run(sortedQuery.result), Duration.Inf)
  }

  // Report 2: Bottom 10 countries with the lowest number of airports.
  def bottom10CountriesByAirports(): Seq[(String, Int)] = {
    val query = for {
      (a, c) <- airports join countries on (_.iso_country === _.code)
    } yield (c.name, a.id)
    val groupedQuery = query.groupBy(_._1).map {
      case (name, group) => (name, group.length)
    }
    val sortedQuery = groupedQuery.sortBy(_._2.asc).take(10)
    Await.result(db.run(sortedQuery.result), Duration.Inf)
  }

  // Report 3: Runway surfaces per country.
  // Returns a sequence of (country name, distinct list of runway surfaces)
  def runwaySurfacesPerCountry(): Seq[(String, Seq[String])] = {
    val joinQuery = for {
      ((a, r), c) <- airports join runways on (_.id === _.airport_ref) join countries on (_._1.iso_country === _.code)
    } yield (c.name, r.surface)
    val result: Seq[(String, Option[String])] = Await.result(db.run(joinQuery.result), Duration.Inf)
    result.groupBy(_._1).map { case (country, list) =>
      val surfaces = list.flatMap(_._2).distinct
      (country, surfaces)
    }.toSeq
  }

  // Report 4: Top 10 most common runway le_ident values.
  def top10RunwayLeIdent(): Seq[(String, Int)] = {
    // Fetch all non-null runway le_ident values into memory
    val leIdents: Seq[String] = Await.result(
      db.run(runways.filter(_.le_ident.isDefined).map(_.le_ident).result),
      Duration.Inf
    ).flatMap(identity)

    // Group by the identifier, count occurrences, sort descending and take top 10
    leIdents.groupBy(identity).map { case (id, list) => (id, list.size) }
      .toSeq.sortBy(-_._2).take(10)
  }

  // Report 5: Average Runway Length per Country.
  def averageRunwayLengthPerCountry(): Seq[(String, Double)] = {
    // Join runways with airports and countries, considering only runways with defined length
    val query = for {
      ((a, r), c) <- airports join runways on (_.id === _.airport_ref) join countries on (_._1.iso_country === _.code)
      if r.length_ft.isDefined
    } yield (c.name, r.length_ft.get)
    val grouped = query.groupBy(_._1).map {
      case (country, group) => (country, group.map(_._2).avg)
    }
    Await.result(db.run(grouped.result), Duration.Inf).flatMap {
      case (country, Some(avg)) => Some((country, avg))
      case _ => None
    }
  }

  // Report 6: Top 10 Airports with Most Runways.
  def top10AirportsByRunways(): Seq[(String, String, Int)] = {
    val query = for {
      (a, r) <- airports join runways on (_.id === _.airport_ref)
    } yield (a.name, a.ident, r.id)
    val grouped = query.groupBy { case (name, ident, _) => (name, ident) }
      .map { case ((name, ident), group) => (name, ident, group.length) }
      .sortBy(_._3.desc)
      .take(10)
    Await.result(db.run(grouped.result), Duration.Inf)
  }


  // Report 7: Number of Airports by Airport Type.
  def airportsCountByType(): Seq[(String, Int)] = {
    val query = airports.groupBy(_.airportType).map { case (atype, group) => (atype, group.length) }
    Await.result(db.run(query.result), Duration.Inf)
  }

  // Report 8: Countries with No Runways.
  def countriesWithNoRunways(): Seq[(String, String)] = {
    val countriesWithRunwaysQuery = for {
      (a, r) <- airports join runways on (_.id === _.airport_ref)
      c <- countries if a.iso_country === c.code
    } yield c
    val countriesWithRunways: Set[String] = Await.result(
      db.run(countriesWithRunwaysQuery.map(_.code).distinct.result),
      Duration.Inf
    ).toSet
    val allCountries: Seq[Country] = Await.result(db.run(countries.result), Duration.Inf)
    allCountries.filterNot(c => countriesWithRunways.contains(c.code)).map(c => (c.name, c.code))
  }

  // Report 9: Top 10 Countries by Total Number of Runways.
  def top10CountriesByRunways(): Seq[(String, Int)] = {
    val joinQuery = for {
      (a, r) <- airports join runways on (_.id === _.airport_ref)
      c <- countries if a.iso_country === c.code
    } yield (c.name, r.id)
    val grouped = joinQuery.groupBy(_._1).map { case (country, group) => (country, group.length) }
    val sorted = grouped.sortBy(_._2.desc).take(10)
    Await.result(db.run(sorted.result), Duration.Inf)
  }
}
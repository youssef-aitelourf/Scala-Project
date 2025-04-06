error id: `<none>`.
file://<WORKSPACE>/src/main/scala/ui/ConsoleUI.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
|empty definition using fallback
non-local guesses:
	 -services/DatabaseLoader.getAirportsAndRunwaysForCountry.
	 -services/DatabaseLoader.getAirportsAndRunwaysForCountry#
	 -services/DatabaseLoader.getAirportsAndRunwaysForCountry().
	 -DatabaseLoader.getAirportsAndRunwaysForCountry.
	 -DatabaseLoader.getAirportsAndRunwaysForCountry#
	 -DatabaseLoader.getAirportsAndRunwaysForCountry().
	 -scala/Predef.DatabaseLoader.getAirportsAndRunwaysForCountry.
	 -scala/Predef.DatabaseLoader.getAirportsAndRunwaysForCountry#
	 -scala/Predef.DatabaseLoader.getAirportsAndRunwaysForCountry().

Document text:

```scala
package ui

import services.DatabaseLoader
import services.Reports
import scala.io.StdIn

@main def runConsoleUI(): Unit =
  // 1. Set up schema and load data into the database
  println("Setting up database schema and loading data...")
  DatabaseLoader.setupSchema()
  DatabaseLoader.loadData("data/countries.csv", "data/airports.csv", "data/runways.csv")
  println("Data loaded.")

  // 2. Present main menu
  println("Select option: 1. Query, 2. Reports")
  StdIn.readLine() match {
    case "1" => {
      println("Enter country name or code:")
      val input = StdIn.readLine().trim
      val found = DatabaseLoader.findCountryByNameOrCode(input)
      if found.isEmpty then
        println(s"No match found for '$input'")
      else
        found.foreach { country =>
          println(s"Found country: ${country.name} [${country.code}]")
          val airportsRunways = DatabaseLoader.getAirportsAndRunwaysForCountry(country.code)
          if airportsRunways.isEmpty then
            println("  No airports found for this country.")
          else
            airportsRunways.foreach { case (airport, runways) =>
              println(s"  Airport: ${airport.name} [${airport.ident}]")
              if runways.isEmpty then
                println("    No runways found for this airport.")
              else
                runways.foreach { runway =>
                  println(s"    Runway ID: ${runway.id}, Surface: ${runway.surface.getOrElse("Unknown")}, LE Ident: ${runway.le_ident.getOrElse("Unknown")}")
                }
            }
        }
    }
    case "2" =>
      println("Select report:")
      println("1. Top 10 Countries by Airports")
      println("2. Bottom 10 Countries by Airports")
      println("3. Runway Surfaces per Country")
      println("4. Top 10 Most Common Runway LE Identifiers")
      println("5. Average Runway Length per Country")
      println("6. Top 10 Airports with Most Runways")
      println("7. Number of Airports by Airport Type")
      println("8. Countries with No Runways")
      println("9. Top 10 Countries by Total Number of Runways")

      StdIn.readLine() match {
        case "1" =>
          println("Top 10 Countries by Airports:")
          Reports.top10CountriesByAirports().foreach { case (name, count) =>
            println(s"$name: $count airports")
          }
        case "2" =>
          println("Bottom 10 Countries by Airports:")
          Reports.bottom10CountriesByAirports().foreach { case (name, count) =>
            println(s"$name: $count airports")
          }
        case "3" =>
          println("Runway Surfaces per Country:")
          Reports.runwaySurfacesPerCountry().foreach { case (country, surfaces) =>
            println(s"$country: ${surfaces.mkString(", ")}")
          }
        case "4" =>
          println("Top 10 Most Common Runway LE Identifiers:")
          Reports.top10RunwayLeIdent().foreach { case (leIdent, count) =>
            println(s"$leIdent: $count")
          }
        case "5" =>
          println("Average Runway Length per Country:")
          Reports.averageRunwayLengthPerCountry().foreach { case (country, avg) =>
            println(s"$country: $avg ft")
          }
        case "6" =>
          println("Top 10 Airports with Most Runways:")
          Reports.top10AirportsByRunways().foreach { case (airportName, ident, count) =>
            println(s"$airportName [$ident]: $count runways")
          }
        case "7" =>
          println("Number of Airports by Airport Type:")
          Reports.airportsCountByType().foreach { case (atype, count) =>
            println(s"$atype: $count airports")
          }
        case "8" =>
          println("Countries with No Runways:")
          Reports.countriesWithNoRunways().foreach { case (name, code) =>
            println(s"$name [$code]")
          }
        case "9" =>
          println("Top 10 Countries by Total Number of Runways:")
          Reports.top10CountriesByRunways().foreach { case (country, count) =>
            println(s"$country: $count runways")
          }
        case other =>
          println(s"Invalid report option: $other")
      }
    case other =>
      println(s"Invalid option: $other")
  }
```

#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.
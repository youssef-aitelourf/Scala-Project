package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, Button, ComboBox, Label, Tab, TabPane, TextArea, TextField}
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import scalafx.application.Platform
import services.DatabaseLoader
import services.Reports
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object GUIApp extends JFXApp3 {

  override def start(): Unit = {
    DatabaseLoader.setupSchema()
    DatabaseLoader.loadData("data/countries.csv", "data/airports.csv", "data/runways.csv")

    stage = new PrimaryStage {
      title = "Airport & Runway Explorer"
      scene = new Scene(900, 600) {

        val queryTab = new Tab {
          text = "Query"
          content = new VBox {
            spacing = 10
            padding = Insets(10)
            val input = new TextField { promptText = "Country name or code" }
            val resultArea = new TextArea { editable = false; prefHeight = 400; wrapText = true }
            val searchButton = new Button("Search") {
              onAction = _ =>
                val query = input.text.value.trim
                if query.isEmpty then resultArea.text = "Please enter a country name or code."
                else Future {
                  val matches = DatabaseLoader.findCountryByNameOrCode(query)
                  if matches.isEmpty then s"No match found for '$query'"
                  else matches.map { country =>
                    val header = s"Found country: ${country.name} [${country.code}]"
                    val details = DatabaseLoader.getAirportsAndRunwaysForCountry(country.code)
                      .map { case (airport, runways) =>
                        val headerA = s"  Airport: ${airport.name} [${airport.ident}]"
                        val runwaysInfo =
                          if runways.isEmpty then "    No runways found"
                          else runways.map(r => s"    Runway ID: ${r.id}, Surface: ${r.surface.getOrElse("Unknown")}, LE Ident: ${r.le_ident.getOrElse("Unknown")}").mkString("\n")
                        s"$headerA\n$runwaysInfo"
                      }.mkString("\n")
                    s"$header\n$details"
                  }.mkString("\n\n")
                }.onComplete(result => Platform.runLater { resultArea.text = result.getOrElse("Error executing query") })
            }
            children = Seq(new Label("Country:"), input, searchButton, resultArea)
          }
        }

        val reportsTab = new Tab {
          text = "Reports"
          content = new VBox {
            spacing = 10
            padding = Insets(10)
            val combo = new ComboBox[String] {
              id = "reportCombo"
              items = scalafx.collections.ObservableBuffer(
                "Top 10 Countries by Airports",
                "Bottom 10 Countries by Airports",
                "Runway Surfaces per Country",
                "Top 10 Most Common Runway LE Identifiers",
                "Average Runway Length per Country",
                "Top 10 Airports with Most Runways",
                "Number of Airports by Airport Type",
                "Countries with No Runways",
                "Top 10 Countries by Total Number of Runways"
              )
              promptText = "Choose a report"
            }
            val resultArea = new TextArea { editable = false; prefHeight = 400; wrapText = true }
            val showButton = new Button("Show Report") {
              onAction = _ =>
                val selected = combo.value.value
                if selected == null then resultArea.text = "Please select a report."
                else Future {
                  selected match
                    case "Top 10 Countries by Airports"           => Reports.top10CountriesByAirports().map((n,c) => s"$n: $c airports").mkString("\n")
                    case "Bottom 10 Countries by Airports"        => Reports.bottom10CountriesByAirports().map((n,c) => s"$n: $c airports").mkString("\n")
                    case "Runway Surfaces per Country"           => Reports.runwaySurfacesPerCountry().map((c,s) => s"$c: ${s.mkString(", ")}").mkString("\n")
                    case "Top 10 Most Common Runway LE Identifiers"=> Reports.top10RunwayLeIdent().map((i,c) => s"$i: $c").mkString("\n")
                    case "Average Runway Length per Country"      => Reports.averageRunwayLengthPerCountry().map((c,avg) => s"$c: $avg ft").mkString("\n")
                    case "Top 10 Airports with Most Runways"      => Reports.top10AirportsByRunways().map((n,id,cnt) => s"$n [$id]: $cnt runways").mkString("\n")
                    case "Number of Airports by Airport Type"     => Reports.airportsCountByType().map((t,cnt) => s"$t: $cnt airports").mkString("\n")
                    case "Countries with No Runways"              => Reports.countriesWithNoRunways().map((n,code) => s"$n [$code]").mkString("\n")
                    case "Top 10 Countries by Total Number of Runways" => Reports.top10CountriesByRunways().map((c,cnt) => s"$c: $cnt runways").mkString("\n")
                    case _                                       => "Unknown report."
                }.onComplete(r => Platform.runLater { resultArea.text = r.getOrElse("Error generating report") })
            }
            children = Seq(new Label("Report:"), combo, showButton, resultArea)
          }
        }

        root = new TabPane { tabs = Seq(queryTab, reportsTab) }
      }
    }
  }
}
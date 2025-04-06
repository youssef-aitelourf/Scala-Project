package csv

object CsvParser:
  // A simple CSV splitter that splits on commas.
  // Note: This does not handle quoted commas or escaping.
  def splitLine(line: CharSequence): List[String] = {
    val pattern = java.util.regex.Pattern.compile(
      """,(?=(?:[^"]*"[^"]*")*[^"]*$)"""
    )
    pattern.split(line.toString, -1).toList.map(field => field.trim.stripPrefix("\"").stripSuffix("\""))
  }
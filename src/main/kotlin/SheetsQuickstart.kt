import services.SheetsService

class SheetsQuickstart {

    private val sheetsService = SheetsService()

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    fun run() {
        val range = "A1:G"
            val values = sheetsService.getValues(range)
            if (values == null || values.isEmpty()) {
                println("No data found.")
            } else {
                println("Name, Major")
                for (row in values) {
                    // Print columns A and E, which correspond to indices 0 and 4.
                    println("Row: $row")
    //                System.out.printf("%s, %s\n", row[0], row[4])
                }
            }
    }
}

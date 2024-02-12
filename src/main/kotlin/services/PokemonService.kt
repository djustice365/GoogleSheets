package services

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.model.ValueRange
import model.CardData

class PokemonService {
    private val sheetsService: SheetsService = SheetsService()

    fun insertValues(data: CardData) {
        val range = "${data.base}!A${data.number+1}:H"

        // create sheet if it doesn't exist
        createIfMissing(range)

        val values = ValueRange()
        values.setValues(
            listOf(
                listOf(data.number,
                    data.name,
                    data.productionNumber,
                    data.variant,
                    data.condition.condition,
                    data.lowPrice,
                    data.highPrice,
                    data.quantity)
            )
        )
        sheetsService.insertValues(range, values)
    }

    private fun createIfMissing(range: String) {
        try {
            sheetsService.getValues(range)
        } catch (e: GoogleJsonResponseException) {
            if (e.message!!.contains("Unable to parse range:")) {
                sheetsService.createSheet(range)
            }
        }
    }
}
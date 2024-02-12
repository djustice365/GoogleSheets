import enums.Condition
import model.CardData
import services.PokemonService
import services.SheetsService

fun main(args: Array<String>) {
    val test = CardData("Pikachu", "0025".toInt(), "58/102", "Pokemon Base set", Condition.NOT_RATED, "1st Edition Red Cheeks", "$69.50", "$1,163.50", 5)
    PokemonService().insertValues(test)
    println(SheetsService().getValues("Pokemon Base Set!A1:H"))
}
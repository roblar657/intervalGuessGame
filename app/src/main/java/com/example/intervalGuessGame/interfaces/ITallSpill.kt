import com.example.intervalGuessGame.ResultType

/**
 * Et spill hvor en kan gjette tall,
 * uendelig antall ganger (antall forsøk bestemmes av ekstern URL)
 *
 * Riktig tall, og korrigering av gjette-interval (for mye /for lite),
 * håndteres av en ekstern URL.
 */
interface ITallSpill {

    /**
     * Starter spillet med gitt spillerens navn og kortnummer.
     *
     * @param navn navnet på spilleren.
     * @param kortnummer kortnummeret for utbetaling av beløp
     * @return Par av status (om noe er error,success, notCorrect...) og respons
     */
    suspend fun start(navn: String, kortnummer: String): Pair<ResultType, String>

    /**
     * Gjetter et tall.
     *
     * @param tall Gjetter følgende tall.
     * @param onResult callback som forteller hva en gjør med retur-verdi.
     */
    suspend fun next(tall: Int, onResult: (type: ResultType, message: String) -> Unit)
}
package com.example.intervalGuessGame

import ITallSpill
import android.util.Log
import com.example.intervalGuessGame.interfaces.IHttpWrapper
import com.example.intervalGuessGame.interfaces.IInterval
import kotlinx.coroutines.CompletableDeferred

class TallSpill(
    private val httpWrapper: IHttpWrapper
) : ITallSpill {

    // Intervallet til tallet en skal gjette.
    private var interval: IInterval? = null




    override suspend fun start(navn: String, kortnummer: String): Pair<ResultType, String> {
        val job = CompletableDeferred<Pair<ResultType, String>>() // Brukes for å vente på asynkrone resultater

        val parameters = mutableMapOf<String, String>() // Parametere som skal sendes til serveren

        // Legger kun til parametere som ikke er blanke
        if (navn.isNotBlank()) parameters.put("navn", navn)
        if (kortnummer.isNotBlank()) parameters.put("kortnummer", kortnummer)


        httpWrapper.getAsync(
            parameters = parameters,
            includeHeaders = true,
            okCodes = listOf(200),
            onSuccess = { res, _ ->
                val response = res.trim()

                // Sjekker for logiske feil før intervallet behandles
                when {
                    response.contains("kortnummer er ikke oppgitt", ignoreCase = true) -> {

                        val responseText = "Kortnummer er ikke oppgitt"
                        Log.e("Tallspill", responseText)
                        job.complete(Pair(ResultType.ERROR, responseText))
                        //Må avslutte med en gang en job er ferdig, for å unngå concurrency problemer
                        return@getAsync
                    }
                    response.contains("navn og kortnummer", ignoreCase = true) -> {

                        val responseText = "Du har glemt å støtte cookies, eller du har ikke oppgitt navn og kortnummer i første forespørsel!"
                        Log.e("Tallspill", "Feil ved start: $responseText")
                        job.complete(Pair(ResultType.ERROR, responseText))
                        //Må avslutte med en gang en job er ferdig, for å unngå concurrency problemer
                        return@getAsync
                    }


                }

                // Prøver å hente intervallet fra responsen
                val regex = Regex("""mellom (\d+) og (\d+)""")
                val match = regex.find(response)

                // Hvis intervallet finnes i responsen
                if (match != null) {
                    val min = match.groupValues[1].toInt()
                    val max = match.groupValues[2].toInt()
                    interval = Interval(min, max)

                    val responseText = "Tenk på et tall mellom $min og $max"
                    Log.i("Tallspill", "Startet spill: $min - $max")
                    job.complete(Pair(ResultType.SUCCESS, responseText))
                } else {

                    val responseText = "Kunne ikke hente intervallet fra responsen."
                    Log.e("Tallspill", "Kunne ikke hente intervallet fra respons")
                    job.complete(Pair(ResultType.ERROR, responseText))
                }
            },
            onError = { e, _ ->

                val responseText = e?.message ?: "Problemer med forbindelsen..."
                Log.e("Tallspill", responseText, e)
                job.complete(Pair(ResultType.ERROR, responseText))
            }
        )

        return job.await()
    }


    override suspend fun next(tall: Int, onResult: (type: ResultType, message: String) -> Unit) {
        var ferdig = false
        // Hvis spillet allerede er ferdig, send melding
        if (ferdig) {
            onResult(ResultType.SUCCESS, "Spillet er ferdig. Trykk på 'nytt spill' om du likte spillet")
            return
        }

        // Sjekk at intervallet finnes
        val interval = interval ?: run {
            ferdig = true
            onResult(ResultType.ERROR, "Noe galt skjedde, prøv å restart appen")
            return
        }

        // Sjekk at intervallet er gyldig
        if (!interval.isValid()) {
            ferdig = true
            onResult(ResultType.ERROR, "Ugyldig interval: ${interval.min} - ${interval.max}")
            return
        }

        // Hvis tallet ikke er i intervallet som riktig tall er i,
        // fortell brukeren at en har gitt feilaktig bruker input
        if (!interval.isInsideInterval(tall)) {
            onResult(ResultType.WARNING, "Tallet $tall er utenfor gyldig intervalet (${interval.min} - ${interval.max})")
            return
        }

        val job = CompletableDeferred<Unit>()

        httpWrapper.getAsync(
            parameters = mapOf("tall" to tall.toString()),
            okCodes = listOf(200),
            onSuccess = { res, _ ->
                val response = res.trim()
                Log.i("Tallspill", "Tippet $tall: $response")

                // Håndterer ulike typer respons fra serveren
                when {
                    //Bruker gjettet riktig tall
                    response.contains("du har vunnet", ignoreCase = true) -> {
                        ferdig = true
                        onResult(ResultType.SUCCESS, response)
                    }
                    //Ingen flere forsøk, opp mot gjetting av tall
                    response.contains("Beklager ingen flere sjanser", ignoreCase = true) -> {
                        ferdig = true
                        onResult(ResultType.NOTCORRECT, response)
                    }
                    //Gjettet for lite tall
                    response.contains("for lite", ignoreCase = true) -> {
                        interval.tooLow(tall)
                        val message = "$response  Nytt intervall (${interval.min} - ${interval.max})"
                        onResult(ResultType.NOTCORRECT, message)
                    }
                    //Gjettet for stort tall
                    response.contains("for stort", ignoreCase = true) -> {
                        interval.tooHigh(tall)
                        val message = "$response Nytt intervall (${interval.min} - ${interval.max})"
                        onResult(ResultType.NOTCORRECT, message)
                    }
                    //Enten er cookie avslått, eller så har bruker
                    //levert navn og kortnummer i et format som
                    //server ikke forstår
                    response.contains("navn og kortnummer", ignoreCase = true) ||
                    response.contains("Feil:", ignoreCase = true) -> {
                                ferdig = true
                                onResult(ResultType.ERROR, response)
                    }
                    //Alle andre respons antas å være en error respons
                    else -> onResult(ResultType.ERROR, response)
                }

                job.complete(Unit)
            },
            onError = { e, _ ->
                ferdig = true
                onResult(ResultType.ERROR, "Feil ved sending av tall: ${e?.message}")
                job.complete(Unit)
            }
        )
        job.await()
    }
}

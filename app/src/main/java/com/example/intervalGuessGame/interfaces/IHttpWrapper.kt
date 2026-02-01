package com.example.intervalGuessGame.interfaces

enum class HTTP { GET, POST }

/**
 * Inneholder metoder for å håndtere HTTP-trafikk til
 * en spesifikk URL.
 *
 */
interface IHttpWrapper {

    /**
     * Synkron GET med callbacks.
     * @param parameters Query-parametere.
     * @param includeHeaders Hvis true, inkluderes headers i responsen.
     * @param okCodes Liste over HTTP-koder som anses som OK.
     * @param onSuccess Kalles når responskoden er i okCodes.
     * @param onError Kalles når responskoden ikke er i okCodes eller ved exception.
     */
    fun get(
        parameters: Map<String, String> = emptyMap(),
        includeHeaders: Boolean = false,
        okCodes: List<Int> = listOf(200),
        onSuccess: (response: String, responseCode: Int) -> Unit,
        onError: (exception: Exception?, responseCode: Int?) -> Unit
    )

    /**
     * Synkron POST med callbacks.
     * @param parameters POST-parametere.
     * @param okCodes Liste over HTTP-koder som anses som OK.
     * @param onSuccess Kalles når responskoden er i okCodes.
     * @param onError Kalles når responskoden ikke er i okCodes eller ved exception.
     */
    fun post(
        parameters: Map<String, String> = emptyMap(),
        okCodes: List<Int> = listOf(200),
        onSuccess: (response: String, responseCode: Int) -> Unit,
        onError: (exception: Exception?, responseCode: Int?) -> Unit
    )

    /**
     * Suspend GET med callbacks (async).
     * @param parameters Query-parametere.
     * @param includeHeaders Hvis true, inkluderes headers i responsen.
     * @param okCodes Liste over HTTP-koder som anses som OK.
     * @param onSuccess Kalles når responskoden er i okCodes.
     * @param onError Kalles når responskoden ikke er i okCodes eller ved exception.
     */
    suspend fun getAsync(
        parameters: Map<String, String> = emptyMap(),
        includeHeaders: Boolean = false,
        okCodes: List<Int> = listOf(200),
        onSuccess: (response: String, responseCode: Int) -> Unit,
        onError: (exception: Exception?, responseCode: Int?) -> Unit
    )

    /**
     * Suspend POST med callbacks (async).
     * @param parameters POST-parametere.
     * @param okCodes Liste over HTTP-koder som anses som OK.
     * @param onSuccess Kalles når responskoden er i okCodes.
     * @param onError Kalles når responskoden ikke er i okCodes eller ved exception.
     */
    suspend fun postAsync(
        parameters: Map<String, String> = emptyMap(),
        okCodes: List<Int> = listOf(200),
        onSuccess: (response: String, responseCode: Int) -> Unit,
        onError: (exception: Exception?, responseCode: Int?) -> Unit
    )
}

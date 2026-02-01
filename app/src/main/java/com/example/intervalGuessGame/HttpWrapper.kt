package com.example.intervalGuessGame

import android.util.Log
import com.example.intervalGuessGame.interfaces.HTTP
import com.example.intervalGuessGame.interfaces.IHttpWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.*
import java.nio.charset.Charset

const val ENCODING = "UTF-8"

const val DEFAULT_TIMEOUT = 5000L

const val TIME_BETWEEN_RETRIES = 200L
const val MAX_RETRIES = 3

class HttpWrapper(private val url: String) : IHttpWrapper {

	init {
		CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
	}

	private fun openConnection(url: String): HttpURLConnection {
		val connection = URL(url).openConnection() as HttpURLConnection
		connection.setRequestProperty("Accept-Charset", ENCODING)
		connection.connectTimeout = DEFAULT_TIMEOUT.toInt()
		connection.readTimeout = DEFAULT_TIMEOUT.toInt()
		return connection
	}

	/**
	 * Utfører en HTTP forespørsel.
	 *
	 * @param type HTTP-metoden som skal brukes (GET eller POST).
	 * @param parameters parametre som skal sendes i request ( '?key1=value1&key2=value2')
	 * @param includeHeaders hvis true, tas HTTP-headerne med i responsen (Kan ikke brukes opp mot POST).
	 * @param okCodes liste over gyldige HTTP-statuskoder.
	 * @param onSuccess callback som kalles ved vellykket forespørsel, med respons og statuskode.
	 * @param onError callback som kalles ved feil, med tilhørende unntak og  statuskode.
	 */
	private fun request(
		type: HTTP,
		parameters: Map<String, String> = emptyMap(),
		includeHeaders: Boolean = false,
		okCodes: List<Int> = listOf(200),
		onSuccess: (response: String, responseCode: Int) -> Unit,
		onError: (exception: Exception?, responseCode: Int?) -> Unit
	) {
		if (includeHeaders && type != HTTP.GET) {
			onError(Exception("includeHeaders kan kun brukes med GET"), null)
			return
		}

		val connection: HttpURLConnection = try {
			when (type) {
				HTTP.GET -> openConnection(url + encodeParameters(parameters))
				HTTP.POST -> openConnection(url)
			}
		} catch (e: Exception) {
			onError(e, null)
			return
		}

		try {
			if (type == HTTP.POST) {
				connection.requestMethod = "POST"
				connection.doOutput = true
				connection.setRequestProperty(
					"Content-Type",
					"application/x-www-form-urlencoded; charset=$ENCODING"
				)
				connection.outputStream.use { output ->
					val postString = encodeParameters(parameters).removePrefix("?")
					output.write(postString.toByteArray(Charsets.UTF_8))
				}
			}

			val responseCode = connection.responseCode

			if (!okCodes.contains(responseCode)) {
				val msg = connection.responseMessage ?: "HTTP error code $responseCode"
				onError(Exception(msg), responseCode)
				return
			}

			var response = ""

			if (type == HTTP.GET && includeHeaders) {
				response += connection.headerFields.entries.joinToString("\n", postfix = "\n") { "${it.key}=${it.value}" }
			}

			val stream: InputStream? = try { connection.inputStream } catch (_: Exception) { null }

			response += if (stream != null)
				readResponseBody(stream, getCharSet(connection))
			else ""

			if (response.isBlank())
				response = "Ingen respons fra server"

			onSuccess(response, responseCode)

		} catch (e: Exception) {
			onError(e, null)
		} finally {
			connection.disconnect()
		}
	}

	override fun get(
		parameters: Map<String, String>,
		includeHeaders: Boolean,
		okCodes: List<Int>,
		onSuccess: (response: String, responseCode: Int) -> Unit,
		onError: (exception: Exception?, responseCode: Int?) -> Unit
	) {
		//Hvilket forsøk en er på, opp mot request
		var attempt = 0

		//Nåværende exception opp mot forsøk, opp mot request
		var lastException: Exception? = null

		//Nåværende respons kode opp mot forsøk, opp mot request
		var lastResponseCode: Int? = null

		var response: String? = null

		var responseCode: Int? = null

		var succeeded = false

        //Forsk å gjøre request, maksimalt MAX_RETRIES
		//ganger, inntill en programmet
		//returnerer succsess (som også kan inneholde
		//feil, men som blir håndtert utenfor HttpWrapper)
		while (attempt < MAX_RETRIES && !succeeded) {
			request(
				HTTP.GET, parameters, includeHeaders, okCodes,
				onSuccess = { resp, code ->
					response = resp
					responseCode = code
					succeeded = true
				},
				onError = { exception, code ->
					lastException = exception
					lastResponseCode = code
				}
			)
			if (!succeeded) {
				attempt++
				if (attempt < MAX_RETRIES) Log.w("HttpWrapper", "GET forsøk $attempt feilet, prøver igjen…")
			}
		}

		if (succeeded) onSuccess(response!!, responseCode!!)
		else onError(lastException, lastResponseCode)
	}

	override fun post(
		parameters: Map<String, String>,
		okCodes: List<Int>,
		onSuccess: (response: String, responseCode: Int) -> Unit,
		onError: (exception: Exception?, responseCode: Int?) -> Unit
	) {
		//Hvilket forsøk en er på, opp mot request
		var attempt = 0

		//Nåværende exception opp mot forsøk, opp mot request
		var lastException: Exception? = null

		//Nåværende respons kode opp mot forsøk, opp mot request
		var lastResponseCode: Int? = null

		var response: String? = null

		var responseCode: Int? = null

		var succeeded = false

		//Forsk å gjøre request, maksimalt MAX_RETRIES
		//ganger, inntill en programmet
		//returnerer succsess (som også kan inneholde
		//feil, men som blir håndtert utenfor HttpWrapper)
		while (attempt < MAX_RETRIES && !succeeded) {
			request(
				HTTP.POST, parameters, false, okCodes,
				onSuccess = { resp, code ->
					response = resp
					responseCode = code
					succeeded = true
				},
				onError = { exception, code ->
					lastException = exception
					lastResponseCode = code
				}
			)
			if (!succeeded) {
				attempt++
				if (attempt < MAX_RETRIES)
					Log.w("HttpWrapper", "POST forsøk $attempt feilet, prøver igjen…")
			}
		}

		if (succeeded) onSuccess(response!!, responseCode!!)
		else onError(lastException, lastResponseCode)
	}

	override suspend fun getAsync(
		parameters: Map<String, String>,
		includeHeaders: Boolean,
		okCodes: List<Int>,
		onSuccess: (response: String, responseCode: Int) -> Unit,
		onError: (exception: Exception?, responseCode: Int?) -> Unit
	) {
		//Hvilket forsøk en er på, opp mot request
		var attempt = 0

		//Nåværende exception opp mot forsøk, opp mot request
		var lastException: Exception? = null

		//Nåværende respons kode opp mot forsøk, opp mot request
		var lastResponseCode: Int? = null

		var response: String? = null

		var responseCode: Int? = null

		var succeeded = false

		//Forsk å gjøre request, maksimalt MAX_RETRIES
		//ganger, inntill en programmet
		//returnerer succsess (som også kan inneholde
		//feil, men som blir håndtert utenfor HttpWrapper)
		while (attempt < MAX_RETRIES && !succeeded) {
			try {
				withTimeout(DEFAULT_TIMEOUT) {
					request(
						HTTP.GET, parameters, includeHeaders, okCodes,
						onSuccess = { resp, code ->
							response = resp
							responseCode = code
							succeeded = true
						},
						onError = { exception, code ->
							lastException = exception
							lastResponseCode = code
						}
					)
				}
			} catch (e: Exception) {
				lastException = e
			}

			if (!succeeded) {
				attempt++
				if (attempt < MAX_RETRIES) {
					Log.w("HttpWrapper", "Async GET forsøk $attempt feilet, prøver igjen…")

					//Skaper pause mellom forsøk, opp mot request
					delay(TIME_BETWEEN_RETRIES)
				}
			}
		}

		if (succeeded) onSuccess(response!!, responseCode!!)
		else onError(lastException, lastResponseCode)
	}

	override suspend fun postAsync(
		parameters: Map<String, String>,
		okCodes: List<Int>,
		onSuccess: (response: String, responseCode: Int) -> Unit,
		onError: (exception: Exception?, responseCode: Int?) -> Unit
	) {
		//Hvilket forsøk en er på, opp mot request
		var attempt = 0

		//Nåværende exception opp mot forsøk, opp mot request
		var lastException: Exception? = null

		//Nåværende respons kode opp mot forsøk, opp mot request
		var lastResponseCode: Int? = null

		var response: String? = null

		var responseCode: Int? = null

		var succeeded = false

		//Forsk å gjøre request, maksimalt MAX_RETRIES
		//ganger, inntill en programmet
		//returnerer succsess (som også kan inneholde
		//feil, men som blir håndtert utenfor HttpWrapper)
		while (attempt < MAX_RETRIES && !succeeded) {
			try {
				withTimeout(DEFAULT_TIMEOUT) {
					request(
						HTTP.POST, parameters, false, okCodes,
						onSuccess = { resp, code ->
							response = resp
							responseCode = code
							succeeded = true
						},
						onError = { exception, code ->
							lastException = exception
							lastResponseCode = code
						}
					)
				}
			} catch (e: Exception) {
				lastException = e
			}

			if (!succeeded) {
				attempt++
				if (attempt < MAX_RETRIES) {
					Log.w("HttpWrapper", "Async POST forsøk $attempt feilet, prøver igjen…")
					//Skaper pause mellom forsøk, opp mot request
					delay(TIME_BETWEEN_RETRIES)
				}
			}
		}

		if (succeeded) onSuccess(response!!, responseCode!!)
		else onError(lastException, lastResponseCode)
	}

	/**
	 * Koder parametre til  URL-enkodet format.
	 *
	 * @param parameterList map av parametre som skal kodes.
	 * @return streng på formatet '?key1=value1&key2=value2'
	 */
	private fun encodeParameters(parameterList: Map<String, String>): String {
		if (parameterList.isEmpty()) return ""
		return parameterList.entries.joinToString("&", prefix = "?") { (key, value) ->
			try {
				listOf(
					URLEncoder.encode(key, ENCODING),
					URLEncoder.encode(value, ENCODING)
				).joinToString("=")
			} catch (e: UnsupportedEncodingException) {
				Log.e("encodeParameters", e.toString())
				""
			}
		}
	}


/**
 * Leser inputstream.
 *
 * @param inputStream strømmen av data.
 * @param charset Tegnsett (f.eks UTF-8).
 * @return respons som en string.
 */
	private fun readResponseBody(inputStream: InputStream, charset: String?): String {
		return try {
			val charsetToUse = if (!charset.isNullOrBlank() && Charset.isSupported(charset)) charset else ENCODING
			inputStream.bufferedReader(Charset.forName(charsetToUse)).use { it.readText() }
		} catch (e: Exception) {
			Log.e("readResponseBody", e.toString())
			"Ingen respons fra serveren"
		}
	}

	/**
	 * Henter charset fra Content-Type-headeren i en HTTP-tilkobling.
	 *
	 * @param connection aktiv URL kobling.
	 * @return charset spesifisert i headeren, eller standardverdien UTF-8.
	 */
	private fun getCharSet(connection: URLConnection): String? {
		val contentType = connection.contentType ?: return ENCODING
		return contentType.substringAfter("charset=", ENCODING).substringBefore(";")
	}
}

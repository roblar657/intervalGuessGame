package com.example.intervalGuessGame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.intervalGuessGame.compose.TallspillCompose
import com.example.intervalGuessGame.ui.theme.Oving5Theme
import kotlinx.coroutines.*
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
class MainActivity : ComponentActivity() {

    private lateinit var cookieManager: CookieManager
    private val URL_TO_CONNECT = "https://bigdata.idi.ntnu.no/mobil/tallspill.jsp"

    //SupervisorJob betyr å gjøre seg uavhengig av fragmenter
    //, slik at hvis noe krasjer, så vil ikke denne krasje.
    private val activityScope = CoroutineScope(SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)

        enableEdgeToEdge()

        setContent {
            Oving5Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainCompose(
                        modifier = Modifier.padding(innerPadding),
                        urlToConnect = URL_TO_CONNECT,
                        onClearCookies = {
                            cookieManager.cookieStore.removeAll()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}

@Composable
fun MainCompose(
    modifier: Modifier = Modifier,
    urlToConnect: String,
    onClearCookies: () -> Unit
) {
    var tallspill by remember { mutableStateOf<TallSpill?>(null) }

    if (tallspill == null) {
        val httpWrapper = HttpWrapper(urlToConnect)
        tallspill = TallSpill(httpWrapper)
    }

    TallspillCompose(
        modifier = modifier,
        tallspill = tallspill!!,
        onStartNyttSpill = {
            onClearCookies()
            val httpWrapper = HttpWrapper(urlToConnect)
            tallspill = TallSpill(httpWrapper)
        }
    )
}

package com.example.intervalGuessGame.compose

import ITallSpill
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.intervalGuessGame.ResultType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun SpillFragmentCompose(
    tallspill: ITallSpill,
    navn: String,
    kortnummer: String
) {
    var respons by remember { mutableStateOf("") }
    var responsType by remember { mutableStateOf(ResultType.SUCCESS) }
    var brukerTall by remember { mutableStateOf("") }
    var knappAktiv by remember { mutableStateOf(false) }

    //Husker på corutiner
    val scope = rememberCoroutineScope()

    //Kjøres bare i starten, ikke påvirket
    //av eventuelle endringer i keys i launchedEffect(...)
    LaunchedEffect(Unit) {

        //Tallspill inneholder indirekte IO operasjoner,
        //så må avlaste med coroutinen vår, og
        //fortelle at vi har en IO operasjon
        scope.launch(Dispatchers.IO) {
            val (type, message) = tallspill.start(navn, kortnummer)
            respons = message
            responsType = type
            knappAktiv = type == ResultType.SUCCESS
            brukerTall = ""

        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!knappAktiv) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF2471A3),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Sender forespørsel...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }
        }
        else {
            Text(
                text = respons,
                style = MaterialTheme.typography.bodyLarge,
                color = when (responsType) {
                    ResultType.ERROR -> Color.Red
                    ResultType.NOTCORRECT -> Color(0xFF2471A3)
                    ResultType.SUCCESS -> Color.Unspecified
                    ResultType.WARNING -> Color(0xFFB35C00)
                }
            )
        }

        OutlinedTextField(
            value = brukerTall,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    brukerTall = newValue
                }
            },
            label = { Text("Ditt tall") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                when (val tall = brukerTall.toIntOrNull()) {
                    null -> {
                        // Hvis konvertering feiler (ikke gyldig tall)
                        respons = "Ugyldig tall — prøv igjen. ${respons.replace("Ugyldig tall — prøv igjen. ","")}"
                        responsType = ResultType.ERROR
                    }

                    else -> {
                        // Deaktiver knapp for å unngå dobbel klikk
                        knappAktiv = false
                        respons = "Sender forespørsel..."
                        responsType = ResultType.SUCCESS

                        // Tallspill inneholder indirekte IO-operasjoner,
                        // så vi avlaster dette i coroutine med IO-dispatcher
                        scope.launch(Dispatchers.IO) {
                            // Gjetter ett nytt tall
                            tallspill.next(tall) { type, message ->
                                respons = message
                                responsType = type
                                knappAktiv = true
                            }
                        }
                    }
                }

            },
            enabled = knappAktiv,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2471A3),
                contentColor = Color.White,
                disabledContainerColor = Color.LightGray,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gjett et tall")
        }
    }
}

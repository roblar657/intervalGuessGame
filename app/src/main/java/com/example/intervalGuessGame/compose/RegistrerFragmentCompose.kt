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
fun RegistrerFragmentCompose(
    navn: String,
    kortnummer: String,
    onNavnChange: (String) -> Unit,
    onKortnummerChange: (String) -> Unit,
    tallspill: ITallSpill,
    respons: String,
    onResponsChange: (String) -> Unit,
    onSuccess: () -> Unit
) {
    var knappAktiv by remember { mutableStateOf(true) }

    //Husker på corutiner
    val scope = rememberCoroutineScope()

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

        if (respons.isNotEmpty()) {

            Text(
                text = respons,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red
            )
        }

        OutlinedTextField(
            value = navn,
            onValueChange = onNavnChange,
            label = { Text("Navn") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = kortnummer,
            onValueChange = onKortnummerChange,
            label = { Text("Kortnummer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                knappAktiv = false
                //Tallspill inneholder indirekte IO operasjoner,
                //så må avlaste med coroutinen vår, og
                //fortelle at vi har en IO operasjon
                scope.launch(Dispatchers.IO) {
                    val (type, message) = tallspill.start(navn, kortnummer)

                    knappAktiv = true
                    if (type == ResultType.SUCCESS) {
                        onSuccess()
                    } else {
                        //Beskjeden vises ikke i denne compose,
                        // sender den derfor oppover
                        onResponsChange(message)

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
            Text("Start spill")
        }
    }
}

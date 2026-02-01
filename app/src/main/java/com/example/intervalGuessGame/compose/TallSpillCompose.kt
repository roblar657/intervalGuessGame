package com.example.intervalGuessGame.compose

import ITallSpill
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TallspillCompose(
    modifier: Modifier = Modifier,
    tallspill: ITallSpill,
    onStartNyttSpill: () -> Unit
) {
    var spillStartet by remember { mutableStateOf(false) }
    var navn by remember { mutableStateOf("") }
    var kortnummer by remember { mutableStateOf("") }
    var respons by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gjett riktig tall",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        spillStartet = false
                        navn = ""
                        kortnummer = ""
                        respons = ""
                        onStartNyttSpill()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray  ,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Start nytt spill")
                }
            }


        }

        //Hvilket compose-fragment som skal vises
        if (!spillStartet) {
            RegistrerFragmentCompose(
                navn = navn,
                kortnummer = kortnummer,
                onNavnChange = { navn = it },
                onKortnummerChange = { kortnummer = it },
                tallspill = tallspill,
                respons = respons,
                onResponsChange = { respons = it },
                onSuccess = { spillStartet = true }
            )
        } else {
            SpillFragmentCompose(
                tallspill = tallspill,
                navn = navn,
                kortnummer = kortnummer
            )
        }
    }
}

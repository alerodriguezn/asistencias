package com.example.asistencias.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


@Composable
fun HomeScreen() {


    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    val uid = user?.uid ?: return
    var loading by remember { mutableStateOf(true) }

    var userData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(uid) {
        val doc = db.collection("usuarios").document(uid).get().await()
        userData = doc.data?.mapValues { it.value.toString() } ?: emptyMap()
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),


        ) {

        Text(
            text = "Bienvenido, ${userData["nombre"]}",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(
                bottom = 5.dp
            )
        )

        Text(
            text = "Gestiona las asistencias fácilmente",
            color = Color.Gray
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()

            ) {
                Column(
                    Modifier.padding(
                        end = 10.dp
                    )
                ) {
                    Text(
                        text = "Jornada Activa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Jornada 2025, II Semestre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Periodo Activo Actual",
                        color = Color.Gray
                    )

                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    tint = Color("#3c82f5".toColorInt()),
                    contentDescription = "Jornada Activa",
                    modifier = Modifier
                        .size(35.dp)

                )

            }

        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()

            ) {
                Column(
                    Modifier.padding(
                        end = 10.dp
                    )
                ) {
                    Text(
                        text = "Estadísticas",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp),
                        fontSize = 18.sp,
                    )

                    Text(
                        text = "Resumen de Asistentes",
                        color = Color.Gray
                    )

                    Text(
                        text = "128",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )

                    Text(
                        text = "Asistentes en esta jornada",
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Person,
                    tint = Color("#3c82f5".toColorInt()),
                    contentDescription = "Jornada Activa",
                    modifier = Modifier
                        .size(55.dp)

                )

            }

        }

        Column(

        ){


        }


    }


}
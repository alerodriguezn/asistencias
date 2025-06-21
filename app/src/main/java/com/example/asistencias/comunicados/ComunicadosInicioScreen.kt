package com.example.asistencias.comunicados

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asistencias.data.Comunicado
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ComunicadosInicioScreen() {
    val db = FirebaseFirestore.getInstance()
    var comunicados by remember { mutableStateOf<List<Comunicado>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("comunicados")
                .whereEqualTo("estado", "activo")
                .get().await()

            comunicados = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comunicado::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            // Podés mostrar un mensaje de error si querés
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Comunicados Activos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else if (comunicados.isEmpty()) {
            Text("No hay comunicados activos.")
        } else {
            LazyColumn {
                items(comunicados) { comunicado ->
                    ComunicadoCardPublico(comunicado)
                }
            }
        }
    }
}

@Composable
fun ComunicadoCardPublico(comunicado: Comunicado) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(comunicado.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(comunicado.descripcion)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Publicado: ${comunicado.fechaPublicacion}")
            Text("Disponible hasta: ${comunicado.fechaCierre}")
        }
    }
}
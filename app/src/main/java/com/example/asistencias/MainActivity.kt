package com.example.asistencias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.asistencias.auth.AuthManager
import com.example.asistencias.auth.PreferencesManager
import com.example.asistencias.core.navigation.NavigationWrapper
import com.example.asistencias.core.navigation.Routes
import com.example.asistencias.ui.theme.AsistenciasTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsistenciasTheme {
                NavigationDrawerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val user = remember { mutableStateOf(Firebase.auth.currentUser) }

    Firebase.auth.addAuthStateListener { auth ->
        user.value = auth.currentUser

    }

    //create state to check if the user is logged in
    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(navController, drawerState, user)
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                if (user.value != null) {
                    TopAppBar(
                        title = { Text(text = "Asistencias") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationWrapper(navController)
            }
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    user :  MutableState<FirebaseUser?>

) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))

        val scope = rememberCoroutineScope()
        val correosAdmin = listOf(
            "nesa14@estudiantec.cr",
            "jos-rodriguez@estudiantec.cr"
        )

        val esAdmin = user.value?.email in correosAdmin

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = "Logo TEC",
                    modifier = Modifier.height(60.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (esAdmin) {
                                navController.navigate(Routes.ComunicadosAdmin.route)
                            } else {
                                navController.navigate(Routes.ComunicadosInicio.route)
                            }
                            scope.launch { drawerState.close() }
                        },
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        tint = Color.Black,
                        contentDescription = "Comunicados"
                    )
                    Text(
                        text = if (esAdmin) "Gestión de Comunicados" else "Comunicados Activos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
                listOf(
                    Routes.Home,
                    Routes.AssistanceTypes,
                    Routes.Courses,
                    Routes.Jornadas,
                    Routes.UserManagement,
                    Routes.Profile,
                ).forEach { route ->

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                navController.navigate(route.route)
                                scope.launch { drawerState.close() }
                            }
                        ,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically

                    ){
                        route.imageVector?.let {
                            Icon(
                                imageVector = it,
                                tint = Color.Black,
                                contentDescription = "Add"
                            )
                        }
                        Text(
                            text = route.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()

                                .padding(12.dp)
                        )

                    }

                }


            }



            if (user.value != null) {
                Column {
                    Text(
                        text = "Sesión iniciada con: ${user.value!!.email}",
                        modifier = Modifier.padding(12.dp),
                        fontStyle = FontStyle.Normal
                    )
                    Button(
                        onClick = {
                            com.google.firebase.ktx.Firebase.auth.signOut()
                            prefs.setRememberMeState(false)
                            navController.navigate(Routes.Login.route) {
                                popUpTo(Routes.Profile.route) { inclusive = true }
                            }

                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Text("Cerrar sesión", color = Color.White)
                    }

                }

            }
        }
    }
}
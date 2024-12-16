package com.example.fallout_replica

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fallout_replica.ui.theme.Fallout_ReplicaTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import android.media.MediaPlayer
import android.widget.ImageView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sqrt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar la barra de estado
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        actionBar?.hide()

        setContent {
            Fallout_ReplicaTheme {
                val currentScreen = remember { mutableStateOf("home") }
                when (currentScreen.value) {
                    "home" -> HomeScreen(
                        onNavigateToCharacter = { currentScreen.value = "selectCharacter" },
                        onNavigateToLogin = { currentScreen.value = "login" },
                        onNavigateToOptions = { currentScreen.value = "options" },
                        onExitApp = { finish() },
                        onNavigateToGameMap = { currentScreen.value = "gameMap" } // Nuevo
                    )
                    "selectCharacter" -> SelectCharacterScreen(
                        onChooseCharacter = { currentScreen.value = "gameMap" }, // Navega al mapa del juego
                        onCreateNewCharacter = { currentScreen.value = "character" },
                        onBackToMainMenu = { currentScreen.value = "home" }
                    )
                    "login" -> OverlayImagesWithLoginScreen(onBackToHome = { currentScreen.value = "home" })
                    "options" -> OptionsScreen(onBackToMainMenu = { currentScreen.value = "home" })
                    "character" -> CharacterScreen(onBack = { currentScreen.value = "home" })
                    "gameMap" -> GameMapScreen(onBackToMainMenu = { currentScreen.value = "home" }) // Nueva pantalla
                }
            }
        }
    }
}
@Composable
fun SelectCharacterScreen(
    onChooseCharacter: () -> Unit,
    onCreateNewCharacter: () -> Unit,
    onBackToMainMenu: () -> Unit
) {
    val impactFont = FontFamily(Font(R.font.impact))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.select_character),
            contentDescription = "Seleccionar Personaje",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(id = R.drawable.char_1),
            contentDescription = "Personaje 1",
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopStart)
                .padding(start = 200.dp)
                .offset(y = -70.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                Text(
                    text = "Elegir este personaje",
                    fontFamily = impactFont,
                    fontSize = 24.sp,
                    color = Color.Yellow,
                    modifier = Modifier.clickable { onChooseCharacter() }
                )
                Text(
                    text = "Crear uno nuevo",
                    fontFamily = impactFont,
                    fontSize = 24.sp,
                    color = Color.Yellow,
                    modifier = Modifier.clickable { onCreateNewCharacter() }
                )
            }
            Text(
                text = "Volver al menú principal",
                fontFamily = impactFont,
                fontSize = 24.sp,
                color = Color.Yellow,
                modifier = Modifier.clickable { onBackToMainMenu() }
            )
        }
    }
}

@Composable
fun GameMapScreen(onBackToMainMenu: () -> Unit) {
    val gridSize = 6
    val items = listOf("💣", "🔧", "🍾", "⚛")
    var grid by remember { mutableStateOf(generateGrid(gridSize, items)) }
    var selectedTile by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var movesLeft by remember { mutableStateOf(30) }
    var bombsLeft by remember { mutableStateOf(30) }
    var score by remember { mutableStateOf(0) } // Puntuación
    var showGameOver by remember { mutableStateOf(false) }
    var showPauseMenu by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0.5f) } // Control del volumen

    // Música de fondo
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.musica_fondo) }

    LaunchedEffect(Unit) {
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(volume, volume)
        mediaPlayer.start()
    }
    LaunchedEffect(volume) { mediaPlayer.setVolume(volume, volume) }
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // Hace que las fichas caigan después de eliminar coincidencias
    fun dropTiles() {
        for (x in 0 until gridSize) {
            val column = mutableListOf<String>()
            for (y in gridSize - 1 downTo 0) {
                if (grid[y][x] != "") column.add(grid[y][x])
            }
            for (y in gridSize - 1 downTo 0) {
                grid[y][x] = if (y >= gridSize - column.size) column[gridSize - 1 - y] else ""
            }
        }
    }

    // Rellena las posiciones vacías con nuevas fichas
    fun refillGrid() {
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                if (grid[y][x] == "") grid[y][x] = items.random()
            }
        }
    }

    // Elimina coincidencias de 3 o más y actualiza la puntuación y el objetivo
    fun checkAndRemoveMatches() {
        val matches = mutableSetOf<Pair<Int, Int>>()

        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val current = grid[y][x]
                if (x <= gridSize - 3 && current != "" && current == grid[y][x + 1] && current == grid[y][x + 2]) {
                    matches.addAll(listOf(Pair(x, y), Pair(x + 1, y), Pair(x + 2, y)))
                }
                if (y <= gridSize - 3 && current != "" && current == grid[y + 1][x] && current == grid[y + 2][x]) {
                    matches.addAll(listOf(Pair(x, y), Pair(x, y + 1), Pair(x, y + 2)))
                }
            }
        }

        if (matches.isNotEmpty()) {
            score += matches.size * 100 // Incrementar puntuación por cada ficha eliminada
            matches.forEach { (x, y) ->
                if (grid[y][x] == "💣") bombsLeft--
                grid[y][x] = ""
            }
            dropTiles()
            refillGrid()
        }

        if (bombsLeft <= 0) showGameOver = true
    }

    // Reiniciar el juego
    fun resetGame() {
        grid = generateGrid(gridSize, items)
        movesLeft = 30
        bombsLeft = 30
        score = 0
        showGameOver = false
    }

    // UI Principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo principal
        Image(
            painter = painterResource(id = R.drawable.fallout_mapa_fond),
            contentDescription = "Fondo Fallout",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.8f
        )

        // Panel superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(8.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Puntuación: $score", color = Color.Yellow, fontSize = 18.sp)
                Text("Movimientos: $movesLeft", color = Color.Yellow, fontSize = 18.sp)
                Text("Objetivo: $bombsLeft 💣", color = Color.Yellow, fontSize = 18.sp)
            }
        }

        // Botón de pausa
        IconButton(
            onClick = { showPauseMenu = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
        ) {
            Icon(Icons.Default.Pause, contentDescription = "Pausar", tint = Color.White)
        }

        // Tablero de juego
        Column(
            modifier = Modifier
                .padding(top = 60.dp, start = 200.dp, end = 200.dp, bottom = 16.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(8.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Refugio Tapón Roto",
                color = Color.Yellow,
                fontSize = 26.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                modifier = Modifier.fillMaxWidth().weight(1.1f)
            ) {
                items(grid.flatten().size) { index ->
                    val x = index % gridSize
                    val y = index / gridSize

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(38.dp)
                            .clickable {
                                if (selectedTile == null) {
                                    selectedTile = Pair(x, y)
                                } else {
                                    val (prevX, prevY) = selectedTile!!
                                    if (isValidMove(prevX, prevY, x, y)) {
                                        grid = swapTiles(grid, prevX, prevY, x, y)
                                        movesLeft--
                                        checkAndRemoveMatches()
                                    }
                                    selectedTile = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = grid[y][x],
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Menú de pausa
        if (showPauseMenu) {
            AlertDialog(
                onDismissRequest = { showPauseMenu = false },
                title = { Text("Menú de Pausa") },
                text = {
                    Column {
                        Text("Opciones del juego", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Volumen: ")
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                valueRange = 0f..1f
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showPauseMenu = false }) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    Button(onClick = { onBackToMainMenu() }) {
                        Text("Salir al Menú Principal")
                    }
                }
            )
        }

        // Mensaje de Game Over
        if (showGameOver) {
            AlertDialog(
                onDismissRequest = { resetGame() },
                title = { Text("¡Juego Terminado!") },
                text = { Text("¡Has completado el objetivo o te quedaste sin movimientos!") },
                confirmButton = {
                    Button(onClick = { resetGame() }) {
                        Text("Reintentar")
                    }
                }
            )
        }
    }
}


// Movimiento válido: solo arriba, abajo, izquierda, derecha
fun isValidMove(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
    return (x1 == x2 && (y1 - y2).absoluteValue == 1) || (y1 == y2 && (x1 - x2).absoluteValue == 1)
}

// Genera un grid inicial con ítems aleatorios
fun generateGrid(size: Int, items: List<String>): Array<Array<String>> {
    return Array(size) { Array(size) { items.random() } }
}

// Intercambia dos tiles en el grid
fun swapTiles(grid: Array<Array<String>>, x1: Int, y1: Int, x2: Int, y2: Int): Array<Array<String>> {
    val newGrid = grid.map { it.copyOf() }.toTypedArray()
    val temp = newGrid[y1][x1]
    newGrid[y1][x1] = newGrid[y2][x2]
    newGrid[y2][x2] = temp
    return newGrid
}









// Funciones para renderizar los distintos tipos de tiles
@Composable
fun TilePath() {
    Box(modifier = Modifier.size(64.dp).background(Color.Gray))
}

@Composable
fun TileObstacle() {
    Box(modifier = Modifier.size(64.dp).background(Color.DarkGray))
}

@Composable
fun PlayerTile() {
    Box(modifier = Modifier.size(64.dp).background(Color.Blue))
}







@Composable
fun Tile3() {
    Image(
        painter = painterResource(id = R.drawable.tile3),
        contentDescription = "Tile 3",
        modifier = Modifier.size(64.dp)
    )
}

@Composable
fun Tile4() {
    Image(
        painter = painterResource(id = R.drawable.tile4),
        contentDescription = "Tile 4",
        modifier = Modifier.size(64.dp)
    )
}

@Composable
fun Tile5() {
    Image(
        painter = painterResource(id = R.drawable.tile5),
        contentDescription = "Tile 5",
        modifier = Modifier.size(64.dp)
    )
}

@Composable
fun Flag1() {
    Image(
        painter = painterResource(id = R.drawable.flag1),
        contentDescription = "Flag 1",
        modifier = Modifier.size(64.dp)
    )
}




    @Composable
    fun HomeScreen(
        onNavigateToCharacter: () -> Unit,
        onNavigateToLogin: () -> Unit,
        onNavigateToOptions: () -> Unit,
        onExitApp: () -> Unit,
        onNavigateToGameMap: () -> Unit
    ) {
    // Define la fuente Impact
    val impactFont = FontFamily(
        Font(R.font.impact)
    )


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_fallout),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Utiliza columnas para organizar los elementos en pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),  // Ajusta los márgenes generales
            verticalArrangement = Arrangement.Center,  // Centra los elementos verticalmente
            horizontalAlignment = Alignment.CenterHorizontally  // Alinea los elementos horizontalmente en el centro
        ) {
            // "Nuevo Juego" botón
            Text(
                text = "Nuevo Juego",
                fontFamily = impactFont,
                fontSize = 48.sp,
                color = Color.Yellow,
                modifier = Modifier
                    .fillMaxWidth()  // Hacer que ocupe todo el ancho disponible
                    .offset(x = 430.dp)
                    .padding(8.dp)
                    .clickable { onNavigateToCharacter() }  // Navega a la pantalla de creación de personaje
            )

            // "Continuar" botón
            Text(
                text = "Iniciar Sesion",
                fontFamily = impactFont,
                fontSize = 48.sp,
                color = Color.Yellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 430.dp)
                    .padding(8.dp)
                    .clickable { onNavigateToLogin() }
            )

            // "Cargar Partida" botón
            Text(
                text = "Opciones",
                fontFamily = impactFont,
                fontSize = 48.sp,
                color = Color.Yellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 430.dp)
                    .padding(8.dp)
                    .clickable { onNavigateToOptions() }
            )

            // "Salir" botón
            Text(
                text = "Salir",
                fontFamily = impactFont,
                fontSize = 48.sp,
                color = Color.Yellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 430.dp)
                    .padding(bottom = 0.dp, start = 8.dp, end = 8.dp)
                    .clickable { onExitApp() }
            )
        }
    }
}
@Composable
fun BlurredImage(
    imageResId: Int,
    blurRadius: Float = 10f,
    alphaValue: Float = 0.1f // Parámetro para ajustar la opacidad
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                setImageResource(imageResId)
                // Aplicar efecto de desenfoque
                setRenderEffect(
                    RenderEffect.createBlurEffect(
                        blurRadius,
                        blurRadius,
                        Shader.TileMode.CLAMP
                    )
                )
                // Ajustar la opacidad
                alpha = alphaValue
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .width(500.dp)
                .background(Color.Black.copy(alpha = 0.7f)) // Fondo semitransparente para destacar
                .padding(24.dp)
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.Green) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Green,
                    unfocusedBorderColor = Color.Green,
                    textColor = Color.Green,
                    cursorColor = Color.Green
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para la contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Green) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.Green
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Green,
                    unfocusedBorderColor = Color.Green,
                    textColor = Color.Green,
                    cursorColor = Color.Green
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de login
            Button(
                onClick = {
                    onLogin(username, password)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", color = Color.Black)
            }
        }
    }
}



@Composable
fun OverlayImagesWithLoginScreen(onBackToHome: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen del fondo (terminal 1)
        Image(
            painter = painterResource(id = R.drawable.terminal),
            contentDescription = "Fondo Terminal",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Imagen desenfocada que estará encima (terminal 2)
        BlurredImage(imageResId = R.drawable.terminal2, blurRadius = 10f)

        // Pantalla de login
        LoginScreen { username, password ->
            // Aquí puedes manejar el inicio de sesión
            println("Username: $username, Password: $password")
            onBackToHome()
        }
    }
}


@Composable
fun PreviewOptionsScreen() {
    OptionsScreen(onBackToMainMenu = { /* Navegación al menú principal */ })
}

@Composable
fun OptionsScreen(onBackToMainMenu: () -> Unit) {
    val impactFont = FontFamily(Font(R.font.impact))
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo (Opciones.png)
        Image(
            painter = painterResource(id = R.drawable.opciones), // Usa la imagen de fondo proporcionada
            contentDescription = "Opciones Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenedor de opciones
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f) // Ajusta el ancho del contenedor
                .padding(1.dp)
                .align(Alignment.Center) // Centrar el contenedor en la pantalla
                .background(Color(0xAA000000)) // Fondo semi-transparente para las opciones
                .padding(16.dp) // Padding interno
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título "Opciones"
                Text(
                    text = "Opciones",
                    fontFamily = impactFont,
                    fontSize = 34.sp,
                    color = Color.Green
                )

                // Slider para el Sonido
                Text(
                    text = "Sonido",
                    fontFamily = impactFont,
                    fontSize = 26.sp,
                    color = Color.Green
                )
                var soundValue by remember { mutableStateOf(0.5f) }
                Slider(
                    value = soundValue,
                    onValueChange = { soundValue = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Green,
                        activeTrackColor = Color.Green,
                        inactiveTrackColor = Color.DarkGray
                    )
                )

                Text(
                    text = "Música",
                    fontFamily = impactFont,
                    fontSize = 26.sp,
                    color = Color.Green
                )
                var musicValue by remember { mutableStateOf(0.5f) }
                Slider(
                    value = musicValue,
                    onValueChange = { musicValue = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Green,
                        activeTrackColor = Color.Green,
                        inactiveTrackColor = Color.DarkGray
                    )
                )
                Button(
                    onClick = { onBackToMainMenu() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Main Menu", fontFamily = impactFont, fontSize = 26.sp, color = Color.Black)
                }

                Button(
                    onClick = { /* Aquí puedes agregar la lógica de cerrar las opciones */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", fontFamily = impactFont, fontSize = 26.sp, color = Color.Black)
                }
            }
        }
    }
}




@Composable
fun CharacterScreen(onBack: () -> Unit) {
    val impactFont = FontFamily(
        Font(R.font.impact)
    )

    // Manejar el botón de retroceso
    BackHandler {
        onBack()  // Regresar a la pantalla principal
    }
    // Cambiar el fondo del Box a negro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo negro
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_character),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit // Ajuste para evitar que la imagen se recorte
        )

        // Texto Posicionado manualmente con offset, manteniendo su posición original
        Text(
            text = "Destreza",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -510.dp, y = 302.dp)
                .padding(16.dp)
        )
        Text(
            text = "Capacitado",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -495.dp, y = 328.dp)
                .padding(16.dp)
        )
        Text(
            text = "Buena Persona",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -472.dp, y = 355.dp)
                .padding(16.dp)
        )
        Text(
            text = "Gafe",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -542.dp, y = 381.dp)
                .padding(16.dp)
        )
        Text(
            text = "Medio",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -407.dp, y = -400.dp)
                .padding(16.dp)
        )
        Text(
            text = "PS                          20/20",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -140.dp, y = -400.dp)
                .padding(16.dp)
        )
        Text(
            text = "Envenenado",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -190.dp, y = -370.dp)
                .padding(16.dp)
        )
        Text(
            text = "Radioactivo",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -190.dp, y = -340.dp)
                .padding(16.dp)
        )
        Text(
            text = "Daño Ocular",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -190.dp, y = -310.dp)
                .padding(16.dp)
        )
        Text(
            text = "Pts. Accion",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -190.dp, y = -120.dp)
                .padding(16.dp)
        )
        Text(
            text = "Peso Max.",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -200.dp, y = -80.dp)
                .padding(16.dp)
        )
        Text(
            text = "Resist. Daño",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -188.dp, y = -40.dp)
                .padding(16.dp)
        )
        Text(
            text = "Resist. Radiacion",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -160.dp, y = -100.dp)
                .padding(16.dp)
        )
        Text(
            text = "Daño Critico",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = -188.dp, y = 25.dp)
                .padding(16.dp)
        )
        Text(
            text = "Nombre",
            fontFamily = impactFont,
            fontSize = 20.sp,
            color = Color.Yellow,
            modifier = Modifier
                .offset(x = 180.dp, y = -20.dp)
                .padding(16.dp)
        )
        Text(
            text = "Edad",
            fontFamily = impactFont,
            fontSize = 20.sp,
            color = Color.Yellow,
            modifier = Modifier
                .offset(x = 330.dp, y = -20.dp)
                .padding(16.dp)
        )
        Text(
            text = "Male",
            fontFamily = impactFont,
            fontSize = 20.sp,
            color = Color.Yellow,
            modifier = Modifier
                .offset(x = 260.dp, y = -20.dp)
                .padding(16.dp)
        )
        Text(
            text = "TÉCNICAS",
            fontFamily = impactFont,
            fontSize = 39.sp,
            color = Color.Yellow,
            modifier = Modifier
                .offset(x = 100.dp, y = -400.dp)
                .padding(16.dp)
        )
        Text(
            text = "TÉCNICAS DES",
            fontFamily = impactFont,
            fontSize = 20.sp,
            color = Color.Yellow,
            modifier = Modifier
                .offset(x = 420.dp, y = 165.dp)
                .padding(16.dp)
        )
        Text(
            text = "Sigilo",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 145.dp, y = -415.dp)
                .padding(16.dp)
        )
        Text(
            text = "Ganzúa",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 155.dp, y = -380.dp)
                .padding(16.dp)
        )
        Text(
            text = "Robar",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 145.dp, y = -345.dp)
                .padding(16.dp)
        )
        Text(
            text = "Conversacion",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 195.dp, y = -310.dp)
                .padding(16.dp)
        )
        Text(
            text = "Treque",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 150.dp, y = -280.dp)
                .padding(16.dp)
        )
        Text(
            text = "Juego",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 145.dp, y = -250.dp)
                .padding(16.dp)
        )
        Text(
            text = "Armas Pequeñas",
            fontFamily = impactFont,
            fontSize = 30.sp,
            color = Color.Green,
            modifier = Modifier
                .offset(x = 215.dp, y = -220.dp)
                .padding(16.dp)
        )
    }
    @Composable
    fun OverlayImagesScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Imagen del fondo (terminal 1)
            Image(
                painter = painterResource(id = R.drawable.terminal),
                contentDescription = "Fondo Terminal",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Imagen que estará encima (terminal 2)
            Image(
                painter = painterResource(id = R.drawable.terminal2),
                contentDescription = "Superposición Terminal",
                modifier = Modifier
                    .align(Alignment.Center) // Ajusta la posición
                    .fillMaxSize(), // Puedes ajustar el tamaño si es necesario
                contentScale = ContentScale.Fit // O ajusta el ContentScale que prefieras
            )
        }
    }



}


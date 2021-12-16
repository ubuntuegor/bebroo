package to.bnt.draw.app.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import to.bnt.draw.app.R
import to.bnt.draw.app.data.Brush
import to.bnt.draw.app.data.defaultBrushes
import to.bnt.draw.app.theme.Coral
import to.bnt.draw.app.theme.DarkGray

@Composable
fun BoardScreen(navController: NavController, boardID: String) {
    val currentBrush = remember { mutableStateOf(defaultBrushes.first()) }
    Scaffold(
        topBar = { BoardTopBar(navController, boardID) },
        bottomBar = { DrawingBoardBottomBar(currentBrush) },
    ) { innerPadding ->
        Desk(currentBrush, innerPadding)
    }
}

@Composable
fun BoardTopBar(
    navController: NavController, boardName: String
) {
    TopAppBar(title = {
        Text(
            text = boardName, fontSize = 20.sp, overflow = TextOverflow.Ellipsis, softWrap = true, maxLines = 1
        )
    }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "arrow back sign",
            )
        }
    }, actions = {
        IconButton(onClick = { print("Bebra") }) {
            Icon(
                Icons.Default.Share,
                contentDescription = "share sign",
            )
        }
        IconButton(onClick = { print("Bebra") }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "more vert icon",
            )
        }

    }, backgroundColor = MaterialTheme.colors.background
    )
}

@Composable
fun DrawingBoardBottomBar(currentBrush: MutableState<Brush>) {
    Surface(elevation = 7.dp) {
        Column(modifier = Modifier.height(95.dp).fillMaxWidth().padding(1.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(start = 53.dp, end = 47.dp).fillMaxWidth()) {
                    Box(
                        modifier = Modifier.clip(CircleShape).background(color = DarkGray).size(8.dp)
                            .align(Alignment.CenterStart)
                    )
                    Box(
                        modifier = Modifier.clip(CircleShape).background(color = DarkGray).size(20.dp)
                            .align(Alignment.CenterEnd)
                    )
                }
                var sliderPosition by remember { mutableStateOf(0f) }
                Row(modifier = Modifier.padding(start = 57.dp, end = 57.dp).padding(top = 11.dp)) {
                    Slider(
                        value = sliderPosition, onValueChange = {
                            sliderPosition = it
                            currentBrush.value = currentBrush.value.copy(stroke = it.toInt())
                        }, colors = SliderDefaults.colors(
                            thumbColor = DarkGray,
                            activeTrackColor = DarkGray,
                            inactiveTrackColor = Color.LightGray,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.LightGray,
                        ), steps = 4, valueRange = 1f..6f
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                var selectedOption by remember { mutableStateOf(defaultBrushes.first()) }
                val onSelectionChange = { brush: Brush -> selectedOption = brush }
                for (brush in defaultBrushes) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape)
                            .border(width = 1.dp, color = Color.LightGray, shape = CircleShape).clickable {
                                currentBrush.value = brush
                                onSelectionChange(brush)
                            }, contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(
                                if (brush == selectedOption) {
                                    18.dp
                                } else {
                                    brush.stroke.dp
                                }
                            ).clip(CircleShape).background(brush.color)
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape)
                        .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                        .clickable { Log.i("BoardScreen", "Hello! Im a color picker. I am still not working :(") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "edit color",
                        modifier = Modifier.size(17.dp),
                        tint = Color.LightGray
                    )
                }
                val eraserBrush = Brush(color = Color.Unspecified)
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape)
                        .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                        .clickable { onSelectionChange(eraserBrush) }, contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.eraser_icon),
                        contentDescription = "eraser",
                        modifier = Modifier.size(17.dp),
                        tint = if (selectedOption == eraserBrush) {
                            Coral
                        } else {
                            Color.LightGray
                        }
                    )
                }
            }
        }
    }
}
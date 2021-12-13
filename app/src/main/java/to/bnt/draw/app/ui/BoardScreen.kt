package to.bnt.draw.app.ui

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
import to.bnt.draw.app.theme.DarkGray

@Composable
fun BoardScreen(navController: NavController, boardID: String) {
    Scaffold(
        topBar = { BoardTopBar(navController, boardID) },
        bottomBar = { DrawingBoardBottomBar() },
    ) { innerPadding ->
        Desk(innerPadding)
    }
}

//TODO Move to Models
data class Brush(val color: Color, val stroke: Int)

@Composable
fun BoardTopBar(
    navController: NavController,
    boardName: String
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
fun DrawingBoardBottomBar() {
    Surface(elevation = 7.dp)
    {
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
                        value = sliderPosition, onValueChange = { sliderPosition = it }, colors = SliderDefaults.colors(
                            thumbColor = DarkGray,
                            activeTrackColor = DarkGray,
                            inactiveTrackColor = Color.LightGray,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.LightGray,
                        ), steps = 4, valueRange = 1f..6f
                    )
                }
            }
            //TODO Move colors for brushes to Colors
            val brushes = listOf(
                Brush(Color(0xFFF85353), 8),
                Brush(Color(0xFF38CA46), 9),
                Brush(Color(0xFF388CCA), 10),
                Brush(Color(0xFF9238CA), 11),
                Brush(Color(0xFFFA78C6), 12),
                Brush(DarkGray, 13),
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (brush in brushes) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape)
                            .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                            .clickable { print("Bebra") }, contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(brush.stroke.dp).clip(CircleShape).background(brush.color))
                    }
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape)
                        .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                        .clickable { print("Bebra") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "change color",
                        modifier = Modifier.size(17.dp),
                        tint = Color.LightGray
                    )
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape)
                        .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
                        .clickable { print("Bebra") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.eraser_icon),
                        contentDescription = "Eraser",
                        modifier = Modifier.size(17.dp),
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}
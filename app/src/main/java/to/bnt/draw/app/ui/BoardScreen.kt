package to.bnt.draw.app.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import to.bnt.draw.app.R
import to.bnt.draw.app.controller.BebrooController
import to.bnt.draw.app.data.Brush
import to.bnt.draw.app.data.Config
import to.bnt.draw.app.data.defaultBrushes
import to.bnt.draw.app.theme.Coral
import to.bnt.draw.app.theme.DarkGray
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import java.lang.StringBuilder

@Composable
fun BoardScreen(navController: NavController, boardID: String) {
    val boardInfo = remember { mutableStateOf<Board?>(null) }
    MainScope().launch { boardInfo.value = BebrooController.client.getBoard(boardID, true) }
    val currentBrush = remember { mutableStateOf(defaultBrushes.first()) }
    Scaffold(
        topBar = { BoardTopBar(navController, boardInfo) },
        bottomBar = { DrawingBoardBottomBar(currentBrush) },
    ) { innerPadding -> Desk(currentBrush, innerPadding) }
}

@Composable
fun BoardTopBar(navController: NavController, boardInfo: MutableState<Board?>) {
    val isBoardSettingsExpanded = remember { mutableStateOf(false) }
    val isSharingSettingsExpanded = remember { mutableStateOf(false) }
    TopAppBar(title = {
        boardInfo.value?.let {
            Text(
                text = it.name, fontSize = 20.sp, overflow = TextOverflow.Ellipsis, softWrap = true, maxLines = 1
            )
        }
    }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "arrow back sign",
            )
        }
    }, actions = {
        IconButton(onClick = { isSharingSettingsExpanded.value = !isSharingSettingsExpanded.value }) {
            Icon(
                Icons.Default.Share,
                contentDescription = "share sign",
            )
        }
        IconButton(onClick = { isBoardSettingsExpanded.value = !isBoardSettingsExpanded.value }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "more vert icon",
            )
        }

    }, backgroundColor = MaterialTheme.colors.background
    )
    SharingMenu(isSharingSettingsExpanded, boardInfo)
    BoardSettingsMenu(isBoardSettingsExpanded, boardInfo)
}

@Composable
fun UserCard(user: User, modifier: Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        if (user.avatarUrl != null) {
            Image(
                painter = rememberImagePainter(user.avatarUrl),
                contentDescription = "Board owner avatar",
                modifier = Modifier.padding(start = 17.dp).size(20.dp).clip(CircleShape)
            )
        } else Box(
            modifier = Modifier.padding(start = 17.dp).size(20.dp).clip(CircleShape).background(color = Color.LightGray)
        )
        Text(
            text = user.displayName,
            modifier = Modifier.padding(start = 6.dp, end = 19.dp),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
            fontSize = 14.sp,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            maxLines = 1
        )
    }
}

@Composable
fun SharingMenu(isSharingSettingsExpanded: MutableState<Boolean>, boardInfo: MutableState<Board?>) {
    var meInfo by remember { mutableStateOf<User?>(null) }
    MainScope().launch { meInfo = BebrooController.client.getMe() }

    if (isSharingSettingsExpanded.value) {
        Dialog(onDismissRequest = { isSharingSettingsExpanded.value = !isSharingSettingsExpanded.value }) {
            Card(elevation = 7.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.share_board),
                        modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    var changePublicError by remember { mutableStateOf<String?>(null) }
                    boardInfo.value?.let { board ->
                        if (meInfo?.id == board.creator.id) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp).padding(top = 15.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(R.string.make_public), color = Color.Gray)
                                var isPublic by remember { mutableStateOf(board.isPublic) }
                                Switch(
                                    checked = isPublic, onCheckedChange = {
                                        try {
                                            isPublic = !isPublic
                                            MainScope().launch {
                                                BebrooController.client.modifyBoard(board.uuid, isPublic = isPublic)
                                            }
                                        } catch (e: ApiException) {
                                            isPublic = !isPublic
                                            changePublicError = e.message
                                        }
                                    }, colors = SwitchDefaults.colors(checkedThumbColor = Coral)
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.board_owner) + ":",
                        modifier = Modifier.padding(horizontal = 12.dp).padding(top = 10.dp),
                        color = Color.Gray
                    )
                    boardInfo.value?.let {
                        UserCard(
                            it.creator, Modifier.padding(horizontal = 12.dp).padding(top = 5.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.collaborators) + ":",
                        modifier = Modifier.padding(horizontal = 12.dp).padding(top = 10.dp),
                        color = Color.Gray
                    )
                    boardInfo.value?.contributors?.let {
                        val scrollState = rememberLazyListState()
                        LazyColumn(modifier = Modifier.fillMaxWidth(), state = scrollState) {
                            items(it) { collaborator ->
                                UserCard(
                                    collaborator, Modifier.padding(horizontal = 12.dp).padding(top = 10.dp)
                                )
                            }
                        }
                    }
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val sharingIntent = Intent(Intent.ACTION_SEND)
                            sharingIntent.type = "text/plain"
                            val sharingURLBuilder = StringBuilder().append(Config.APP_URL)
                            boardInfo.value?.let { sharingURLBuilder.append("board/").append(it.uuid) }
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingURLBuilder.toString())
                            startActivity(context, Intent.createChooser(sharingIntent, "Sharing using"), null)
                        },
                        modifier = Modifier.padding(top = 25.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally).height(37.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = stringResource(R.string.share))
                    }
                    Row(
                        modifier = Modifier.height(24.dp).fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        changePublicError?.let {
                            Text(
                                text = it,
                                color = Coral,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun BoardSettingsMenu(isBoardSettingsExpanded: MutableState<Boolean>, boardInfo: MutableState<Board?>) {
    var isChangeBoardNameButtonCLicked by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd).padding(top = 58.dp, end = 12.dp)) {
        DropdownMenu(
            expanded = isBoardSettingsExpanded.value,
            onDismissRequest = { isBoardSettingsExpanded.value = !isBoardSettingsExpanded.value },
        ) {
            DropdownMenuItem(onClick = {
                isChangeBoardNameButtonCLicked = !isChangeBoardNameButtonCLicked
                isBoardSettingsExpanded.value = !isBoardSettingsExpanded.value
            }) {
                Text(stringResource(R.string.rename_board))
            }
        }
    }
    var newBoardName by remember { mutableStateOf("") }
    var changeBoardNameError by remember { mutableStateOf<String?>(null) }
    if (isChangeBoardNameButtonCLicked) {
        Dialog(onDismissRequest = { isChangeBoardNameButtonCLicked = !isChangeBoardNameButtonCLicked }) {
            Card(elevation = 7.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.rename_board),
                        modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(top = 15.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally),
                        value = newBoardName,
                        onValueChange = {
                            if (it.length < 200) {
                                newBoardName = it
                            }
                        },
                        label = { Text(stringResource(R.string.board_name)) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    var isChangeButtonClicked by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            MainScope().launch {
                                try {
                                    isChangeButtonClicked = !isChangeButtonClicked
                                    boardInfo.value?.let { BebrooController.client.modifyBoard(it.uuid, newBoardName) }
                                    boardInfo.value = boardInfo.value?.copy(name = newBoardName)
                                    isChangeBoardNameButtonCLicked = !isChangeBoardNameButtonCLicked
                                } catch (e: ApiException) {
                                    isChangeButtonClicked = !isChangeButtonClicked
                                    changeBoardNameError = e.message
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 20.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally).height(37.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isChangeButtonClicked) {
                            CircularProgressIndicator(modifier = Modifier.size(25.dp), color = Color.White)
                        } else {
                            Text(text = stringResource(R.string.rename))
                        }
                    }
                    Row(
                        modifier = Modifier.height(24.dp).fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        changeBoardNameError?.let {
                            Text(
                                text = it,
                                color = Coral,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun DrawingBoardBottomBar(currentBrush: MutableState<Brush>) {
    Surface(elevation = 7.dp) {
        Column(modifier = Modifier.height(95.dp).fillMaxWidth().padding(top = 1.dp)) {
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
                Row(modifier = Modifier.padding(top = 11.dp, start = 57.dp, end = 57.dp)) {
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

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
                                    21.dp
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
package to.bnt.draw.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import to.bnt.draw.app.R
import to.bnt.draw.app.controller.BebrooController
import to.bnt.draw.app.controller.UserPreferencesManager
import to.bnt.draw.app.theme.Coral
import to.bnt.draw.app.theme.SuperLightGray
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import to.bnt.draw.shared.util.formatTimestamp

@Composable
fun MenuScreen(navController: NavController) {
    var meInfo by remember { mutableStateOf<User?>(null) }
    MainScope().launch { meInfo = BebrooController.client.getMe() }
    Scaffold(topBar = { MenuTopBar(navController, meInfo) }) {
        var listOfBoard by remember { mutableStateOf<List<Board>>(listOf()) }
        MainScope().launch { listOfBoard = BebrooController.client.listBoards() }

        var isRefreshing by remember { mutableStateOf(true) }
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
        LaunchedEffect(isRefreshing) {
            if (isRefreshing) {
                delay(1000L)
                isRefreshing = !isRefreshing
            }
        }
        SwipeRefresh(state = swipeRefreshState, onRefresh = {
            isRefreshing = !isRefreshing
            MainScope().launch { listOfBoard = BebrooController.client.listBoards() }
        }) {
            val scrollState = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
                items(listOfBoard) { board ->
                    BoardPreviewCard(
                        navController,
                        meInfo,
                        board.uuid,
                        board.name,
                        board.creator,
                        board.timestamp,
                    )
                }
            }
        }
    }
}

@Composable
fun BoardPreviewCard(
    navController: NavController,
    meInfo: User?,
    boardID: String,
    boardName: String,
    boardCreator: User,
    timeStamp: Long?,
) {
    Column(
        modifier = Modifier.height(67.dp).fillMaxWidth().clickable {
            navController.navigate("board/${boardID}")
        }, verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = boardName,
                modifier = Modifier.padding(start = 17.dp).weight(1f, fill = false),
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                maxLines = 1
            )
            timeStamp?.let {
                Text(
                    text = formatTimestamp(it),
                    modifier = Modifier.padding(top = 3.dp, end = 17.dp),
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            boardCreator.avatarUrl?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Board owner avatar",
                    modifier = Modifier.padding(start = 17.dp).size(width = 20.dp, height = 20.dp).clip(CircleShape)
                )
            }
            Text(
                if (meInfo?.displayName == boardCreator.displayName) {
                    stringResource(R.string.your_board)
                } else {
                    boardCreator.displayName
                },
                modifier = Modifier.padding(start = (if (boardCreator.avatarUrl == null) 16.dp else 6.dp), end = 19.dp),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                maxLines = 1
            )
        }
    }
    Divider(color = SuperLightGray)
}

@Composable
fun MenuTopBar(navController: NavController, meInfo: User?) {
    var isCreateButtonClicked by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    var newBoardName by remember { mutableStateOf("") }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    if (isCreateButtonClicked) {
        Dialog(onDismissRequest = { isCreateButtonClicked = !isCreateButtonClicked }) {
            Card(elevation = 7.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.new_board),
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
                    var isBoardCreateButtonClicked by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            MainScope().launch {
                                try {
                                    isBoardCreateButtonClicked = true
                                    val boardID = BebrooController.client.createBoard(newBoardName)
                                    navController.navigate("board/${boardID}")
                                } catch (e: ApiException) {
                                    isBoardCreateButtonClicked = false
                                    createError = e.message
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 20.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally).height(37.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isBoardCreateButtonClicked) {
                            CircularProgressIndicator(modifier = Modifier.size(25.dp), color = Color.White)
                        } else {
                            Text(text = stringResource(R.string.create))
                        }
                    }
                    Row(
                        modifier = Modifier.height(24.dp).fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        createError?.let {
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

    var isChangeNameButtonCLicked by remember { mutableStateOf(false) }
    var changeNameError by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }

    if (isChangeNameButtonCLicked) {
        Dialog(onDismissRequest = { isChangeNameButtonCLicked = !isChangeNameButtonCLicked }) {
            Card(elevation = 7.dp, shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.new_name),
                        modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(top = 15.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally),
                        value = newName,
                        onValueChange = {
                            if (it.length < 100) {
                                newName = it
                            }
                        },
                        label = { Text(stringResource(R.string.name)) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    var isChangeButtonClicked by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            MainScope().launch {
                                try {
                                    isChangeButtonClicked = !isChangeButtonClicked
                                    BebrooController.client.modifyMe(newName)
                                    isChangeNameButtonCLicked = !isChangeNameButtonCLicked
                                    isSettingsExpanded = !isSettingsExpanded
                                } catch (e: ApiException) {
                                    isChangeButtonClicked = !isChangeButtonClicked
                                    changeNameError = e.message
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
                            Text(text = stringResource(R.string.change))
                        }
                    }
                    Row(
                        modifier = Modifier.height(24.dp).fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        changeNameError?.let {
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

    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd).padding(top = 55.dp, end = 12.dp)) {
        val userPreferencesManager = UserPreferencesManager(LocalContext.current)
        DropdownMenu(
            expanded = isSettingsExpanded,
            onDismissRequest = { isSettingsExpanded = !isSettingsExpanded },
        ) {
            DropdownMenuItem(onClick = { isChangeNameButtonCLicked = !isChangeNameButtonCLicked }) {
                Text(stringResource(R.string.change_name))
            }
            Divider()
            DropdownMenuItem(onClick = {
                MainScope().launch { userPreferencesManager.cleanUserPreferences() }
                navController.popBackStack()
            }) { Text(stringResource(R.string.exit)) }
        }
    }
    TopAppBar(title = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.height(24.dp),
            )
        }
    }, navigationIcon = {
        IconButton(
            onClick = { isCreateButtonClicked = !isCreateButtonClicked },
        ) { Icon(Icons.Default.Add, contentDescription = "plus sign") }
    }, actions = {
        IconButton(onClick = { isSettingsExpanded = !isSettingsExpanded }) {
            if (meInfo?.avatarUrl != null) Image(
                painter = rememberImagePainter(meInfo.avatarUrl),
                contentDescription = "user avatar",
                modifier = Modifier.size(width = 32.dp, height = 32.dp).clip(CircleShape)
            ) else Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(color = Color.LightGray)
            ) {
                meInfo?.let {
                    Text(
                        text = it.displayName.first().toString(), modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }, backgroundColor = MaterialTheme.colors.background, elevation = 0.dp
    )
}

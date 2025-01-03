package com.quickpoint.snookerboard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.domain.models.isFrameAndMatchEqual
import com.quickpoint.snookerboard.domain.models.isFrameEqual
import com.quickpoint.snookerboard.domain.models.isFrameInProgress
import com.quickpoint.snookerboard.ui.screens.game.GameViewModel

class MenuItem(
    val id: String,
    val title: String,
    val contentDescription: String,
    val imageVector: ImageVector,
    var isActive: Boolean = true,
)

object MenuItemIds {
    const val ID_MENU_ITEM_UNDO = "id_menu_item_undo"
    const val ID_MENU_ITEM_MORE = "id_menu_item_more"
    const val ID_MENU_ITEM_RERACK = "id_menu_item_rerack"
    const val ID_MENU_ITEM_CONCEDE_FRAME = "id_menu_item_concede_frame"
    const val ID_MENU_ITEM_CONCEDE_MATCH = "id_menu_item_concede_match"
    const val ID_MENU_ITEM_CANCEL_MATCH = "id_menu_item_cancel_match"
    const val ID_MENU_ITEM_LOG = "id_menu_item_log"
}

@Composable
fun getMenuItems() = listOf(
    MenuItem(
        id = Screen.DrawerRules.route,
        title = stringResource(R.string.menu_drawer_rules),
        contentDescription = stringResource(R.string.menu_drawer_rules),
        imageVector = Icons.AutoMirrored.Filled.Rule
    ),
    MenuItem(
        id = Screen.DrawerImprove.route,
        title = stringResource(R.string.menu_drawer_improve),
        contentDescription = stringResource(R.string.menu_drawer_improve),
        imageVector = Icons.AutoMirrored.Filled.ContactSupport
    ),
    MenuItem(
        id = Screen.DrawerSupport.route,
        title = stringResource(R.string.menu_drawer_support),
        contentDescription = stringResource(R.string.menu_drawer_support),
        imageVector = Icons.Default.Savings
    ),
    MenuItem(
        id = Screen.DrawerSettings.route,
        title = stringResource(R.string.menu_drawer_settings),
        contentDescription = stringResource(R.string.menu_drawer_settings),
        imageVector = Icons.Default.Settings
    ),
    MenuItem(
        id = Screen.DrawerAbout.route,
        title = stringResource(R.string.menu_drawer_about),
        contentDescription = stringResource(R.string.menu_drawer_about),
        imageVector = Icons.Default.Info
    )
)

@Composable
fun GameViewModel.getActionItems(): List<MenuItem> {
    val frameState by frameState.collectAsState(initial = null)

    return listOf(
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_UNDO,
            title = stringResource(R.string.menu_item_undo),
            contentDescription = stringResource(R.string.menu_item_undo),
            imageVector = Icons.AutoMirrored.Filled.Undo,
            isActive = frameState?.breaksList?.isFrameInProgress() ?: false
        ),
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_MORE,
            title = stringResource(R.string.menu_item_more),
            contentDescription = stringResource(R.string.menu_item_more),
            imageVector = Icons.Default.MoreVert
        )
    )
}

@Composable
fun GameViewModel.getActionItemsOverflow(): List<MenuItem> {
    val frameState by frameState.collectAsState(initial = null)

    return listOf(
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_RERACK,
            title = stringResource(R.string.menu_item_rerack),
            contentDescription = stringResource(R.string.menu_item_rerack),
            imageVector = Icons.Default.RestartAlt,
            isActive = frameState?.breaksList?.isFrameInProgress() ?: false
        ),
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_CONCEDE_FRAME,
            title = stringResource(R.string.menu_item_concede_frame),
            contentDescription = stringResource(R.string.menu_item_concede_frame),
            imageVector = Icons.Default.Done,
            isActive = frameState?.let { !it.scoreList.isFrameEqual() } ?: false
        ),
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_CONCEDE_MATCH,
            title = stringResource(R.string.menu_item_concede_match),
            contentDescription = stringResource(R.string.menu_item_concede_match),
            imageVector = Icons.Default.DoneAll,
            isActive = frameState?.let { !it.scoreList.isFrameAndMatchEqual() } ?: false
        ),
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_CANCEL_MATCH,
            title = stringResource(R.string.menu_item_cancel_match),
            contentDescription = stringResource(R.string.menu_item_cancel_match),
            imageVector = Icons.Default.Cancel
        ),
        MenuItem(
            id = MenuItemIds.ID_MENU_ITEM_LOG,
            title = stringResource(R.string.menu_item_log),
            contentDescription = stringResource(R.string.menu_item_log),
            imageVector = Icons.Default.BugReport
        )
    )
}

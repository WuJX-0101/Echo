package com.echo.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.app.R
import com.echo.app.data.model.ECHO_EMOJIS
import com.echo.app.data.model.EchoEmoji
import com.echo.app.ui.calendar.CalendarScreen
import com.echo.app.ui.calendar.CalendarViewModel
import com.echo.app.ui.trend.TrendScreen
import com.echo.app.ui.trend.TrendViewModel
import com.echo.app.widget.EchoMediumWidget
import com.echo.app.widget.EchoSmallWidget
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val calendarViewModel: CalendarViewModel = koinViewModel()
    val trendViewModel: TrendViewModel = koinViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.isTodayRecorded) {
        if (state.isTodayRecorded) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(EchoSmallWidget::class.java).forEach { id ->
                EchoSmallWidget().update(context, id)
            }
            manager.getGlanceIds(EchoMediumWidget::class.java).forEach { id ->
                EchoMediumWidget().update(context, id)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopBar(onNavigateToSettings = onNavigateToSettings)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> CalendarScreen(
                    viewModel = calendarViewModel,
                    showTopBar = false
                )
                1 -> HomePage(
                    state = state,
                    viewModel = viewModel
                )
                2 -> TrendScreen(
                    viewModel = trendViewModel,
                    showTopBar = false,
                    onShare = {
                        val ds = trendViewModel.uiState.value.distributions
                        val streak = trendViewModel.uiState.value.streak
                        val total = trendViewModel.uiState.value.totalCount
                        com.echo.app.util.ShareCardHelper.shareAsImage(context, ds, streak, total)
                    }
                )
            }
        }

        BottomBar(
            currentPage = pagerState.currentPage,
            onPageSelected = { scope.launch { pagerState.animateScrollToPage(it) } }
        )
    }

    if (state.showYesterdayPrompt) {
        YesterdayPrompt(
            onDismiss = viewModel::dismissYesterdayPrompt,
            onRecord = viewModel::saveRecord
        )
    }
}

@Composable
private fun TopBar(onNavigateToSettings: () -> Unit) {
    val titleFont = FontFamily(Font(R.font.zcool_qlkh_yellow_you))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = titleFont,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onNavigateToSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentPage: Int,
    onPageSelected: (Int) -> Unit
) {
    val labels = listOf("日历", "首页", "趋势")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val isActive = currentPage == index
            val textColor by animateColorAsState(
                targetValue = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "bottomBarColor"
            )
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.1f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "bottomBarScale"
            )
            val indicatorAlpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "indicatorAlpha"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .clickable(onClick = { onPageSelected(index) })
                    .padding(horizontal = 28.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(20.dp)
                        .height(3.dp)
                        .alpha(indicatorAlpha)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun HomePage(
    state: HomeUiState,
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.dateText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (state.isTodayRecorded) "今天的心情" else "今天感觉怎么样？",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isTodayRecorded) {
            TodayRecorded(state.todayRecord!!)
        } else {
            EmojiGrid(
                selectedEmoji = state.selectedEmoji,
                onEmojiSelected = viewModel::selectEmoji
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.selectedEmoji?.let { emoji ->
                Text(
                    text = emoji.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveRecord,
                enabled = state.selectedEmoji != null && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    text = if (state.isSaving) "记录中..." else "记 录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (state.recentWeekRecords.isNotEmpty()) {
            RecentWeekPreview(records = state.recentWeekRecords)
        }
    }
}

@Composable
private fun EmojiGrid(
    selectedEmoji: EchoEmoji?,
    onEmojiSelected: (EchoEmoji) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ECHO_EMOJIS) { emoji ->
            val isSelected = emoji == selectedEmoji
            val bgColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface,
                label = "emojiBg"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "emojiScale"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .scale(scale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onEmojiSelected(emoji)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = emoji.emoji,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayRecorded(record: com.echo.app.data.db.EchoRecord) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = record.emoji,
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = record.label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "今日已记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        ) {
            Text(
                text = "今日已记录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentWeekPreview(records: List<Pair<String?, String?>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "最近 7 天",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            records.forEach { (emoji, _) ->
                Text(
                    text = emoji ?: "·",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            records.forEach { (_, dayLabel) ->
                Text(
                    text = dayLabel ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}

@Composable
private fun YesterdayPrompt(
    onDismiss: () -> Unit,
    onRecord: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "昨天忘了吗？",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "要不要补一个？",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRecord,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("补一个")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text("算了")
                    }
                }
            }
        }
    }
}

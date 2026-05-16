package com.example.eatwise.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft

enum class ResultTab {
    Advice,
    Nutrition,
}

private const val SwipeTabThreshold = 48f

fun resolvedResultTab(
    selected: ResultTab,
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
): ResultTab = when {
    selected == ResultTab.Nutrition && nutritionAvailable -> ResultTab.Nutrition
    selected == ResultTab.Advice && adviceAvailable -> ResultTab.Advice
    nutritionAvailable -> ResultTab.Nutrition
    else -> ResultTab.Advice
}

fun swipedResultTab(
    selected: ResultTab,
    dragDistance: Float,
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
    threshold: Float = SwipeTabThreshold,
): ResultTab {
    val target = when {
        dragDistance <= -threshold -> ResultTab.Nutrition
        dragDistance >= threshold -> ResultTab.Advice
        else -> selected
    }
    return resolvedResultTab(target, adviceAvailable, nutritionAvailable)
}

fun resultTabPages(
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
): List<ResultTab> = buildList {
    if (adviceAvailable) add(ResultTab.Advice)
    if (nutritionAvailable) add(ResultTab.Nutrition)
}

fun resultTabPageIndex(
    selected: ResultTab,
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
): Int {
    val pages = resultTabPages(adviceAvailable, nutritionAvailable)
    if (pages.isEmpty()) return 0
    val resolved = resolvedResultTab(selected, adviceAvailable, nutritionAvailable)
    return pages.indexOf(resolved).takeIf { it >= 0 } ?: 0
}

fun resultTabIndicatorIndex(
    selected: ResultTab,
    adviceEnabled: Boolean,
    nutritionEnabled: Boolean,
): Int = when (resolvedResultTab(selected, adviceEnabled, nutritionEnabled)) {
    ResultTab.Advice -> 0
    ResultTab.Nutrition -> 1
}

fun resultTabForPage(
    page: Int,
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
): ResultTab {
    val pages = resultTabPages(adviceAvailable, nutritionAvailable)
    return pages.getOrElse(page.coerceAtLeast(0)) { pages.firstOrNull() ?: ResultTab.Advice }
}

@Composable
fun ResultTabSwitcher(
    selectedTab: ResultTab,
    onSelectedTab: (ResultTab) -> Unit,
    adviceLabel: String,
    nutritionLabel: String,
    modifier: Modifier = Modifier,
    adviceEnabled: Boolean = true,
    nutritionEnabled: Boolean = true,
) {
    val selectedIndex = resultTabIndicatorIndex(selectedTab, adviceEnabled, nutritionEnabled)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, LineSoft.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        val indicatorWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = indicatorWidth * selectedIndex,
            animationSpec = tween(durationMillis = 180),
            label = "resultTabIndicator",
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(GreenSoft),
        )
        Row(Modifier.fillMaxSize()) {
            ResultTabButton(
                text = adviceLabel,
                selected = selectedTab == ResultTab.Advice,
                enabled = adviceEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onSelectedTab(ResultTab.Advice) },
            )
            ResultTabButton(
                text = nutritionLabel,
                selected = selectedTab == ResultTab.Nutrition,
                enabled = nutritionEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onSelectedTab(ResultTab.Nutrition) },
            )
        }
    }
}

@Composable
fun SwipeableResultPane(
    selectedTab: ResultTab,
    adviceAvailable: Boolean,
    nutritionAvailable: Boolean,
    onSelectedTab: (ResultTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ResultTab) -> Unit,
) {
    val pages = remember(adviceAvailable, nutritionAvailable) {
        resultTabPages(adviceAvailable, nutritionAvailable)
    }
    if (pages.isEmpty()) return

    val selectedPage = resultTabPageIndex(selectedTab, adviceAvailable, nutritionAvailable)
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { pages.size },
    )
    val currentSelectedTab by rememberUpdatedState(selectedTab)
    val currentOnSelectedTab by rememberUpdatedState(onSelectedTab)
    var programmaticTargetPage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedPage, pages.size) {
        if (pagerState.currentPage != selectedPage) {
            programmaticTargetPage = selectedPage
            try {
                pagerState.animateScrollToPage(selectedPage)
            } finally {
                programmaticTargetPage = null
            }
        }
    }

    LaunchedEffect(pagerState, pages) {
        snapshotFlow { Triple(pagerState.settledPage, pagerState.targetPage, pagerState.isScrollInProgress) }.collect { (page, targetPage, isScrollInProgress) ->
            if (isScrollInProgress || page != targetPage) return@collect
            val pendingTarget = programmaticTargetPage
            if (pendingTarget != null && page != pendingTarget) return@collect
            val settledTab = pages.getOrNull(page) ?: return@collect
            if (settledTab != currentSelectedTab) currentOnSelectedTab(settledTab)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        userScrollEnabled = pages.size > 1,
        beyondViewportPageCount = 1,
        pageSpacing = 0.dp,
        verticalAlignment = Alignment.Top,
        key = { page -> pages[page] },
    ) { page ->
        Box(Modifier.fillMaxSize()) {
            content(pages[page])
        }
    }
}

@Composable
private fun ResultTabButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selectedText = if (selected) GreenDeep else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (enabled) selectedText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

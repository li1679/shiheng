package com.example.nutritiontracker.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

object NutritionMotion {
    const val pageMillis = 210
    const val modalMillis = 160
    const val fadeMillis = 90
}

@Composable
fun <T> NutritionAnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    label: String = "nutrition-animated-content",
    isForward: (from: T, to: T) -> Boolean = { _, _ -> true },
    content: @Composable AnimatedContentScope.(T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        label = label,
        transitionSpec = {
            val movingForward = isForward(initialState, targetState)
            val direction = if (movingForward) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            nutritionPageTransform(direction)
        },
        content = content,
    )
}

@Composable
fun NutritionAnimatedDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val configuration = LocalConfiguration.current
        val backgroundInteractionSource = remember { MutableInteractionSource() }
        val surfaceInteractionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .width(configuration.screenWidthDp.dp)
                .height(configuration.screenHeightDp.dp)
                .padding(horizontal = 16.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = backgroundInteractionSource,
                        indication = null,
                        onClick = onDismissRequest,
                    )
                    .semantics {
                        testTag = "nutrition-dialog-scrim"
                        onClick {
                            onDismissRequest()
                            true
                        }
                    },
            )
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(NutritionMotion.fadeMillis)) +
                    scaleIn(
                        animationSpec = tween(NutritionMotion.modalMillis),
                        initialScale = 0.96f,
                    ),
                exit = fadeOut(tween(NutritionMotion.fadeMillis)) +
                    scaleOut(
                        animationSpec = tween(NutritionMotion.modalMillis),
                        targetScale = 0.96f,
                    ),
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = surfaceInteractionSource,
                            indication = null,
                            onClick = {},
                        ),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = containerColor,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        content = content,
                    )
                }
            }
        }
    }
}

fun Modifier.nutritionEdgeSwipeBack(
    enabled: Boolean,
    onBack: () -> Unit,
): Modifier {
    if (!enabled) return this
    return this.pointerInput(onBack) {
        val edgeWidthPx = 72.dp.toPx()
        val triggerDistancePx = 72.dp.toPx()
        var startX = Float.MAX_VALUE
        var totalDragX = 0f
        var triggered = false
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                startX = offset.x
                totalDragX = 0f
                triggered = false
            },
            onHorizontalDrag = { change, dragAmount ->
                if (startX <= edgeWidthPx && !triggered) {
                    totalDragX += dragAmount
                    if (totalDragX > triggerDistancePx) {
                        triggered = true
                        change.consume()
                        onBack()
                    }
                }
            },
        )
    }
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
private fun <T> AnimatedContentTransitionScope<T>.nutritionPageTransform(
    direction: AnimatedContentTransitionScope.SlideDirection,
): ContentTransform {
    val movingForward = direction == AnimatedContentTransitionScope.SlideDirection.Left
    val enter = slideInHorizontally(
        initialOffsetX = { fullWidth ->
            if (movingForward) fullWidth / 5 else -fullWidth / 5
        },
        animationSpec = tween(durationMillis = NutritionMotion.pageMillis, easing = FastOutSlowInEasing),
    ) + fadeIn(animationSpec = tween(durationMillis = NutritionMotion.fadeMillis, easing = FastOutSlowInEasing))
    val exit = slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            if (movingForward) -fullWidth / 6 else fullWidth / 6
        },
        animationSpec = tween(durationMillis = NutritionMotion.pageMillis, easing = FastOutSlowInEasing),
    ) + fadeOut(animationSpec = tween(durationMillis = NutritionMotion.fadeMillis, easing = FastOutSlowInEasing))
    return enter togetherWith exit
}

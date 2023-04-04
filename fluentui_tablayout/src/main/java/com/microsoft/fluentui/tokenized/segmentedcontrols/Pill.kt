package com.microsoft.fluentui.tokenized.segmentedcontrols

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.microsoft.fluentui.tablayout.R
import com.microsoft.fluentui.theme.FluentTheme
import com.microsoft.fluentui.theme.token.ControlTokens
import com.microsoft.fluentui.theme.token.FluentStyle
import com.microsoft.fluentui.theme.token.GlobalTokens
import com.microsoft.fluentui.theme.token.controlTokens.PillBarInfo
import com.microsoft.fluentui.theme.token.controlTokens.PillBarTokens
import com.microsoft.fluentui.theme.token.controlTokens.PillButtonInfo
import com.microsoft.fluentui.theme.token.controlTokens.PillButtonTokens
import com.microsoft.fluentui.util.dpToPx
import kotlinx.coroutines.launch
import kotlin.math.max

data class PillMetaData(
    var text: String,
    var onClick: (() -> Unit),
    var icon: ImageVector? = null,
    var enabled: Boolean = true,
    var selected: Boolean = false,
    var notificationDot: Boolean = false,
)

val LocalPillButtonTokens = compositionLocalOf { PillButtonTokens() }
val LocalPillButtonInfo = compositionLocalOf { PillButtonInfo() }
val LocalPillBarTokens = compositionLocalOf { PillBarTokens() }
val LocalPillBarInfo = compositionLocalOf { PillBarInfo() }

/**
 * API to create Pill shaped Button which will further be used in tabs and bars.
 *
 * @param pillMetaData Metadata for a single pill. Type: [PillMetaData]
 * @param modifier Optional Modifier to customize the design and behaviour of pill button
 * @param style Color Scheme of pill shaped button. Default: [FluentStyle.Neutral]
 * @param interactionSource Interaction Source Object to handle gestures.
 * @param pillButtonTokens Tokens to customize the design of pill button.
 */
@Composable
fun PillButton(
    pillMetaData: PillMetaData,
    modifier: Modifier = Modifier,
    style: FluentStyle = FluentStyle.Neutral,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pillButtonTokens: PillButtonTokens? = null
) {
    val token = pillButtonTokens
        ?: FluentTheme.controlTokens.tokens[ControlTokens.ControlType.PillButton] as PillButtonTokens

    CompositionLocalProvider(
        LocalPillButtonTokens provides token,
        LocalPillButtonInfo provides PillButtonInfo(
            style,
            pillMetaData.enabled,
            pillMetaData.selected
        )

    ) {
        val shape = RoundedCornerShape(50)
        val scaleBox = remember { Animatable(1.0F) }

        LaunchedEffect(key1 = pillMetaData.selected) {
            if (pillMetaData.selected) {
                launch {
                    scaleBox.animateTo(
                        targetValue = 0.95F,
                        animationSpec = tween(
                            durationMillis = 50
                        )
                    )
                    scaleBox.animateTo(
                        targetValue = 1.0F,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
        }

        val backgroundColor by animateColorAsState(
            targetValue = getPillButtonTokens().backgroundColor(pillButtonInfo = getPillButtonInfo())
                .getColorByState(
                    enabled = pillMetaData.enabled,
                    selected = pillMetaData.selected,
                    interactionSource = interactionSource
                ),
            animationSpec = tween(200)
        )
        val iconColor =
            getPillButtonTokens().iconColor(pillButtonInfo = getPillButtonInfo()).getColorByState(
                enabled = pillMetaData.enabled,
                selected = pillMetaData.selected,
                interactionSource = interactionSource
            )
        val textColor =
            getPillButtonTokens().textColor(pillButtonInfo = getPillButtonInfo()).getColorByState(
                enabled = pillMetaData.enabled,
                selected = pillMetaData.selected,
                interactionSource = interactionSource
            )

        val fontStyle = getPillButtonTokens().fontStyle(getPillButtonInfo())

        val focusStroke = getPillButtonTokens().focusStroke(getPillButtonInfo())
        var focusedBorderModifier: Modifier = Modifier
        for (borderStroke in focusStroke) {
            focusedBorderModifier =
                focusedBorderModifier.border(borderStroke, shape)
        }

        val clickAndSemanticsModifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            enabled = pillMetaData.enabled,
            onClickLabel = null,
            role = Role.Button,
            onClick = pillMetaData.onClick
        )

        val selectedString = if (pillMetaData.selected)
            LocalContext.current.resources.getString(R.string.fluentui_selected)
        else
            LocalContext.current.resources.getString(R.string.fluentui_not_selected)

        val enabledString = if (pillMetaData.enabled)
            LocalContext.current.resources.getString(R.string.fluentui_enabled)
        else
            LocalContext.current.resources.getString(R.string.fluentui_disabled)

        Box(
            modifier
                .scale(scaleBox.value)
                .defaultMinSize(minHeight = getPillButtonTokens().minHeight(getPillButtonInfo()))
                .clip(shape)
                .background(backgroundColor, shape)
                .padding(vertical = getPillButtonTokens().verticalPadding(getPillButtonInfo()))
                .then(clickAndSemanticsModifier)
                .then(if (interactionSource.collectIsFocusedAsState().value || interactionSource.collectIsHoveredAsState().value) focusedBorderModifier else Modifier)
                .semantics(true) {
                    contentDescription =
                        "${pillMetaData.text} $selectedString $enabledString"
                },
            contentAlignment = Alignment.Center
        ) {
            Row(Modifier.width(IntrinsicSize.Max)) {
                if (pillMetaData.icon != null) {
                    Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size180)))
                    Icon(
                        pillMetaData.icon!!,
                        pillMetaData.text,
                        modifier = Modifier
                            .size(getPillButtonTokens().iconSize(getPillButtonInfo()))
                            .clearAndSetSemantics { },
                        tint = iconColor
                    )
                } else {
                    Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size160)))
                    Text(
                        pillMetaData.text,
                        modifier = Modifier
                            .weight(1F)
                            .clearAndSetSemantics { },
                        color = textColor,
                        style = fontStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (pillMetaData.notificationDot) {
                    val notificationDotColor: Color =
                        getPillButtonTokens().notificationDotColor(getPillButtonInfo())
                            .getColorByState(
                                enabled = pillMetaData.enabled,
                                selected = pillMetaData.selected,
                                interactionSource = interactionSource
                            )
                    Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size20)))
                    Canvas(
                        modifier = Modifier
                            .padding(top = 2.dp, bottom = 12.dp)
                            .sizeIn(minWidth = 6.dp, minHeight = 6.dp)
                    ) {
                        drawCircle(
                            color = notificationDotColor, style = Fill, radius = dpToPx(3.dp)
                        )
                    }
                    if (pillMetaData.icon != null)
                        Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size100)))
                    else
                        Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size80)))
                } else {
                    Spacer(Modifier.requiredWidth(GlobalTokens.size(GlobalTokens.SizeTokens.Size160)))
                }
            }
        }
    }
}

/**
 * API to create Bar of Pill button. The PillBar control is a linear set of two or more PillButton, each of which functions as a mutually exclusive button.
 * PillBar are commonly used as filter for search results.
 *
 * @param metadataList
 * @param modifier
 * @param style
 * @param showBackground
 * @param pillButtonTokens
 * @param pillBarTokens
 */
@Composable
fun PillBar(
    metadataList: MutableList<PillMetaData>,
    modifier: Modifier = Modifier,
    style: FluentStyle = FluentStyle.Neutral,
    showBackground: Boolean = false,
    pillButtonTokens: PillButtonTokens? = null,
    pillBarTokens: PillBarTokens? = null
) {
    if (metadataList.size == 0)
        return

    val token = pillBarTokens
        ?: FluentTheme.controlTokens.tokens[ControlTokens.ControlType.PillBar] as PillBarTokens

    CompositionLocalProvider(
        LocalPillBarTokens provides token,
        LocalPillBarInfo provides PillBarInfo(style)
    ) {
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .background(if (showBackground) getPillBarTokens().background(getPillBarInfo()) else Color.Unspecified)
                .focusable(enabled = false),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            state = lazyListState
        ) {
            metadataList.forEachIndexed { index, pillMetadata ->
                item(index.toString()) {
                    PillButton(
                        pillMetadata,
                        modifier = Modifier.onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch {
                                    lazyListState.animateScrollToItem(
                                        max(0, index - 2)
                                    )
                                }
                            }
                        }, style = style, pillButtonTokens = pillButtonTokens
                    )
                }
            }
        }
    }
}

@Composable
fun getPillButtonTokens(): PillButtonTokens {
    return LocalPillButtonTokens.current
}

@Composable
fun getPillButtonInfo(): PillButtonInfo {
    return LocalPillButtonInfo.current
}

@Composable
fun getPillBarTokens(): PillBarTokens {
    return LocalPillBarTokens.current
}

@Composable
fun getPillBarInfo(): PillBarInfo {
    return LocalPillBarInfo.current
}

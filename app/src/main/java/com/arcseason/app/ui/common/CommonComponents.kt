package com.arcseason.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcSurface
import com.arcseason.app.ui.theme.NeonEmerald
import com.arcseason.app.ui.theme.NeonEmeraldDim

/** Section heading used at the top of every tab ("Today", "This Week", ...). Optional trailing slot for a header-level action. */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ArcOnSurfaceMuted)
            }
        }
        trailing?.invoke()
    }
}

/** Standard dark card shell every list row / stat block in the app sits inside. */
@Composable
fun ArcCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

/** A labeled horizontal progress bar, e.g. "Protein 96 / 140 g". */
@Composable
fun LabeledProgressBar(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    accent: Color = NeonEmerald,
    modifier: Modifier = Modifier
) {
    val fraction = if (target <= 0) 0f else (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$current / $target $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = ArcOnSurfaceMuted
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = accent,
            trackColor = ArcSurface,
            strokeCap = StrokeCap.Round
        )
    }
}

/** Small rounded numeric/text badge, e.g. a streak count or a "4x/wk" tag. */
@Composable
fun ArcBadge(text: String, accent: Color = NeonEmerald, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.16f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Content padding that clears the bottom navigation bar consistently across tabs. */
fun contentPadding(inner: PaddingValues) = PaddingValues(
    top = inner.calculateTopPadding(),
    bottom = inner.calculateBottomPadding() + 12.dp
)

/** Circular "X / Y this week" progress ring, shared by the Rules and Workout tabs. */
@Composable
fun ArcWeeklyRing(current: Int, target: Int, modifier: Modifier = Modifier, sizeDp: Int = 84) {
    val fraction = if (target <= 0) 0f else (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Box(modifier = modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            drawArc(
                color = NeonEmeraldDim.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
            drawArc(
                color = NeonEmerald,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }
        Text("$current/$target", style = MaterialTheme.typography.titleMedium, fontSize = 16.sp)
    }
}

/** Ring + label + "X / Y this week" caption, stacked vertically. */
@Composable
fun ArcWeeklyRingCard(label: String, current: Int, target: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        ArcWeeklyRing(current = current, target = target)
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$current / $target this week", style = MaterialTheme.typography.bodySmall, color = ArcOnSurfaceMuted)
    }
}

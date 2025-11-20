package com.example.dataloft.feature.workout

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dataloft.AppViewModelFactory
import com.example.dataloft.R
import com.example.dataloft.feature.workout.WorkoutViewModel.WorkoutIntervalUi
import com.example.dataloft.feature.workout.WorkoutViewModel.WorkoutUiState
import com.example.dataloft.workout.WorkoutStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.squareup.picasso.Picasso
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WorkoutRoute(
    timerId: Int?,
    viewModel: WorkoutViewModel = viewModel(factory = AppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutScreen(
        state = uiState,
        onStartStop = viewModel::onStartStopRequested,
        requestedTimerId = timerId
    )
}

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onStartStop: () -> Unit,
    modifier: Modifier = Modifier,
    requestedTimerId: Int? = null
) {
    val headerTitle = when {
        state.timerTitle.isNotBlank() -> state.timerTitle
        requestedTimerId != null -> "Тренировка #$requestedTimerId"
        else -> "Выберите тренировку"
    }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WorkoutHeader(imageUrl = HEADER_IMAGE_URL, title = headerTitle)
        Spacer(modifier = Modifier.height(12.dp))
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text(
                    text = "Таймер",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text(
                    text = "Карта",
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
        when (selectedTab) {
            0 -> TimerTab(state = state, onStartStop = onStartStop)
            1 -> MapTab(track = state.track)
        }
    }
}

@Composable
private fun TimerTab(state: WorkoutUiState, onStartStop: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        val statusLabel = when (state.status) {
            WorkoutStatus.Running -> "Идёт тренировка"
            WorkoutStatus.Completed -> "Тренировка завершена"
            WorkoutStatus.Stopped -> "Остановлено"
            WorkoutStatus.Idle -> "Готовы к старту"
        }
        Text(
            text = "Общая длительность: ${state.totalDurationLabel}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Прошло: ${state.totalElapsedLabel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = statusLabel,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Осталось в интервале: ${state.currentIntervalRemainingLabel}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        IntervalStrip(intervals = state.intervals)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Интервалы",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.intervals.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузите тренировку, чтобы увидеть интервалы")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(state.intervals) { index, interval ->
                    IntervalRow(index = index, interval = interval)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onStartStop,
            enabled = state.isStartEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = state.buttonLabel)
        }
    }
}

@Composable
private fun MapTab(track: List<LatLng>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(track.firstOrNull() ?: LatLng(55.752220, 37.615560), 14f)
        }
        LaunchedEffect(track.lastOrNull()) {
            track.lastOrNull()?.let { last ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(last, 15f)
                    )
                )
            }
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            if (track.size > 1) {
                Polyline(
                    points = track,
                    color = MaterialTheme.colorScheme.primary,
                    width = 12f
                )
            }
        }
    }
}

@Composable
private fun IntervalStrip(intervals: List<WorkoutIntervalUi>) {
    if (intervals.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "Добавьте тренировку", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        intervals.forEach { interval ->
            val background = if (interval.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
            Box(
                modifier = Modifier
                    .weight(interval.durationSeconds.coerceAtLeast(1).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(background)
            ) {
                if (interval.progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(interval.progress.coerceIn(0f, 1f))
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntervalRow(index: Int, interval: WorkoutIntervalUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "${index + 1}. ${interval.title}", fontWeight = if (interval.isActive) FontWeight.Bold else FontWeight.Normal)
            Text(text = interval.durationLabel)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(interval.progress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun WorkoutHeader(imageUrl: String, title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        RemoteBanner(imageUrl = imageUrl)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (title.isNotBlank()) title else "Выберите тренировку",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RemoteBanner(imageUrl: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "Фото тренировки"
            }
        },
        update = { imageView ->
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.banner_placeholder)
                .error(R.drawable.banner_placeholder)
                .fit()
                .centerCrop()
                .into(imageView)
        }
    )
}

private const val HEADER_IMAGE_URL = "https://images.unsplash.com/photo-1556817411-31ae72fa3ea0"

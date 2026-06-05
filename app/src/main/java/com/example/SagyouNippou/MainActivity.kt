package com.example.SagyouNippou

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var isCurrentlyTracking by mutableStateOf(false)
    private var selectedCategory by mutableStateOf("Work")
    private var startTimeMillis by mutableStateOf(0L)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrackerDashboard(
                        isTracking = isCurrentlyTracking,
                        selectedCategory = selectedCategory,
                        startTime = startTimeMillis,
                        onCategorySelect = { selectedCategory = it },
                        onStartClicked = { startTrackingEngine(selectedCategory) },
                        onStopClicked = { stopTrackingEngine() }
                    )
                }
            }
        }
    }

    private fun startTrackingEngine(category: String) {
        startTimeMillis = System.currentTimeMillis()
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_CLASSIFICATION_NAME, category)
        }
        startService(intent)
        isCurrentlyTracking = true
    }

    private fun stopTrackingEngine() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        startService(intent)
        isCurrentlyTracking = false
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun TrackerDashboard(
    isTracking: Boolean,
    selectedCategory: String,
    startTime: Long,
    onCategorySelect: (String) -> Unit,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit
) {
    // Local ticker state that calculates elapsed time on-screen every second
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isTracking, startTime) {
        if (isTracking) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Activity Logger",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 40.dp)
        )

        // 1. Interactive Digital Stopwatch Display Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    color = if (isTracking) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatSecondsToTimeString(elapsedSeconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (isTracking) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isTracking) {
                    Text(
                        text = "Recording: $selectedCategory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 2. Classification Selection Segment
        Text(
            text = "Select Classification",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val standardClassifications = listOf("Work", "Study", "Break", "Exercise")
            standardClassifications.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category) },
                    enabled = !isTracking, // Lock choices while clock runs
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. Bottom Action Buttons Panel
        if (!isTracking) {
            Button(
                onClick = onStartClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onStopClicked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Stop & Record", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 4. Temporary Offline CSV Export Button Placeholder
        OutlinedButton(
            onClick = { /* We will hook this up to the CSV engine next */ },
            enabled = !isTracking,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Export Data to PC (.csv)", fontSize = 14.sp)
        }
    }
}

fun formatSecondsToTimeString(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
package tn.esprit.dam.features.scan.presentation.detail
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.features.scan.presentation.model.TrackerUi
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetectedTrackersSection(app: AppDetailUi) {
    var showAllTrackers by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = AppColors.orange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.detected_trackers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Surface(
                color = if (app.trackers.isEmpty()) AppColors.success.copy(alpha = 0.2f) else AppColors.error.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${app.trackers.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (app.trackers.isEmpty()) AppColors.success else AppColors.error
                )
            }
        }

        if (app.trackers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.successContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, AppColors.success.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AppColors.success.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AppColors.success
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.no_trackers_detected),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.success
                        )
                        Text(
                            text = stringResource(R.string.this_app_doesn_t_contain_known_trackers),
                            fontSize = 13.sp,
                            color = AppColors.successLight
                        )
                    }
                }
            }
        } else {
            val trackersToShow = if (showAllTrackers) app.trackers else app.trackers.take(3)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    trackersToShow.forEach { tracker ->
                        EnhancedTrackerCard(tracker)
                    }

                    if (app.trackers.size > 3) {
                        TextButton(
                            onClick = { showAllTrackers = !showAllTrackers },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (showAllTrackers) "Show Less" else "View all ${app.trackers.size} trackers",
                                color = AppColors.orange,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (showAllTrackers) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.orange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedTrackerCard(tracker: TrackerUi) {
    val trackerColor = when (tracker.riskLevel?.lowercase()) {
        "critical", "high" -> AppColors.error
        "medium" -> AppColors.orange
        "low" -> AppColors.warning
        else -> AppColors.textTertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = trackerColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, trackerColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = trackerColor.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = trackerColor
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = tracker.riskLevel?.uppercase() ?: "UNKNOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = trackerColor
                )
            }
            Surface(
                color = trackerColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.tracking),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = trackerColor
                )
            }
        }
    }
}

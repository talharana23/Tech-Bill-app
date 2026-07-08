package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaaSViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: SaaSViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val showWarning by viewModel.showSubscriptionWarning.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    val lowStockLoading by viewModel.lowStockLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgStart)
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Sticky Warning Banner
        StickyWarningBanner(isVisible = showWarning)

        // Application Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tech With Moiz",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Owner Console",
                    fontSize = 13.sp,
                    color = DarkTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stylized Notification Bell with Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /* Read notifications action */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
                // Badge
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentAmber)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                )
            }
        }

        // AI Insights Component
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            AiInsightsCard()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metrics Section Header
        Text(
            text = "PERFORMANCE METRICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Metrics Grid (2-column layout implemented with FlowRows or custom column pairs for vertical scrolling optimization)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Today's Revenue",
                    value = "Rs 25,000",
                    accentColor = AccentCyan,
                    subtitle = "+12% from yesterday",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Gross Profit",
                    value = "Rs 22,000",
                    accentColor = AccentGreen,
                    subtitle = "88% profit margin",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Sales",
                    value = "2",
                    accentColor = Color.White,
                    subtitle = "2 orders completed",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Items Sold",
                    value = "2",
                    accentColor = Color.White,
                    subtitle = "Average 1 item/order",
                    modifier = Modifier.weight(1f)
                )
            }

            // Single Row card spanning full width for Discounts
            MetricCard(
                title = "Discounts Given",
                value = "Rs 0",
                accentColor = DarkTextSecondary,
                subtitle = "No promotional markdowns",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Secondary Breakdown Section
        Text(
            text = "CHANNELS BREAKDOWN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Horizontal scrolling channel summaries
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChannelBreakdownCard(
                title = "Pending Online",
                value = "0",
                tint = AccentAmber,
                modifier = Modifier.weight(1f)
            )
            ChannelBreakdownCard(
                title = "Online Sales",
                value = "Rs 0",
                tint = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            ChannelBreakdownCard(
                title = "Offline Sales",
                value = "Rs 25,000",
                tint = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Canvas Chart Section
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            RevenueChart()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Low Stock Alerts Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CRITICAL LOW STOCK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextSecondary,
                letterSpacing = 1.sp
            )
            if (lowStockLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp, color = AccentCyan)
            }
        }

        if (lowStockItems.isEmpty() && !lowStockLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("All stock items fully supplied.", color = DarkTextSecondary, fontSize = 13.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lowStockItems.take(4).forEach { item ->
                    LowStockItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = DarkTextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun ChannelBreakdownCard(
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = DarkTextSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = tint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LowStockItemCard(
    item: com.example.data.model.InventoryItem,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "SKU: ${item.sku ?: "N/A"}",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Soft amber badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentAmber.copy(alpha = 0.15f))
                    .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${item.quantity} left",
                    color = AccentAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

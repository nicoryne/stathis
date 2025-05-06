package citu.edu.stathis.mobile.features.progress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavHostController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Achievements", "Badges", "Leaderboard")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show progress info */ }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress summary
            ProgressSummary()

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandColors.Purple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> AchievementsTab()
                1 -> BadgesTab()
                2 -> LeaderboardTab()
            }
        }
    }
}

@Composable
fun ProgressSummary() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Level 5",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.Purple
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Computer Science Student",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3,500 XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "5,000 XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.7f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandColors.Purple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1,500 XP to Level 6",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.Purple
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStatItem(
                    count = "15",
                    label = "Achievements",
                    icon = Icons.Default.EmojiEvents
                )

                ProgressStatItem(
                    count = "8",
                    label = "Badges",
                    icon = Icons.Default.Star
                )

                ProgressStatItem(
                    count = "#3",
                    label = "Rank",
                    icon = Icons.Default.School
                )
            }
        }
    }
}

@Composable
fun ProgressStatItem(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandColors.Purple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BrandColors.Purple,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementsTab() {
    val achievements = remember {
        listOf(
            Achievement(
                id = "1",
                title = "Posture Pro",
                description = "Maintain good posture for 3 hours in a day",
                points = 50,
                icon = Icons.Default.Accessibility,
                isUnlocked = true,
                category = "Posture",
                date = "Today"
            ),
            Achievement(
                id = "2",
                title = "Task Master",
                description = "Complete 10 tasks in a week",
                points = 30,
                icon = Icons.Default.Assignment,
                isUnlocked = true,
                category = "Tasks",
                date = "Yesterday"
            ),
            Achievement(
                id = "3",
                title = "Health Guru",
                description = "Monitor your health metrics for 5 consecutive days",
                points = 25,
                icon = Icons.Default.Favorite,
                isUnlocked = true,
                category = "Health",
                date = "May 5"
            ),
            Achievement(
                id = "4",
                title = "Perfect Posture",
                description = "Achieve a perfect posture score",
                points = 100,
                icon = Icons.Default.Accessibility,
                isUnlocked = false,
                category = "Posture",
                date = ""
            ),
            Achievement(
                id = "5",
                title = "Academic Excellence",
                description = "Complete all academic tasks for a week",
                points = 75,
                icon = Icons.Default.School,
                isUnlocked = false,
                category = "Academic",
                date = ""
            ),
            Achievement(
                id = "6",
                title = "Consistency King",
                description = "Use the app for 30 consecutive days",
                points = 150,
                icon = Icons.Default.EmojiEvents,
                isUnlocked = false,
                category = "General",
                date = ""
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(achievements.filter { it.isUnlocked }) { achievement ->
            AchievementItem(achievement = achievement)
        }

        item {
            Text(
                text = "Locked Achievements",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(achievements.filter { !it.isUnlocked }) { achievement ->
            AchievementItem(achievement = achievement)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked)
                            when (achievement.category) {
                                "Posture" -> BrandColors.Purple.copy(alpha = 0.1f)
                                "Health" -> Color(0xFFF44336).copy(alpha = 0.1f) // Red
                                "Tasks" -> BrandColors.Teal.copy(alpha = 0.1f)
                                "Academic" -> Color(0xFF2196F3).copy(alpha = 0.1f) // Blue
                                else -> Color(0xFFFFA726).copy(alpha = 0.1f) // Orange
                            }
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = achievement.title,
                    tint = if (achievement.isUnlocked)
                        when (achievement.category) {
                            "Posture" -> BrandColors.Purple
                            "Health" -> Color(0xFFF44336) // Red
                            "Tasks" -> BrandColors.Teal
                            "Academic" -> Color(0xFF2196F3) // Blue
                            else -> Color(0xFFFFA726) // Orange
                        }
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (achievement.isUnlocked)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (achievement.isUnlocked) 0.8f else 0.6f
                    )
                )

                if (achievement.isUnlocked && achievement.date.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Unlocked: ${achievement.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (achievement.isUnlocked)
                                BrandColors.Purple.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${achievement.points} XP",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (achievement.isUnlocked)
                            BrandColors.Purple
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesTab() {
    val badges = remember {
        listOf(
            Badge(
                id = "1",
                title = "Posture Expert",
                description = "Awarded for exceptional posture habits",
                icon = Icons.Default.Accessibility,
                isUnlocked = true,
                rarity = BadgeRarity.GOLD,
                category = "Posture"
            ),
            Badge(
                id = "2",
                title = "Task Champion",
                description = "Completed 50 tasks",
                icon = Icons.Default.Assignment,
                isUnlocked = true,
                rarity = BadgeRarity.SILVER,
                category = "Tasks"
            ),
            Badge(
                id = "3",
                title = "Health Monitor",
                description = "Tracked health metrics for 30 days",
                icon = Icons.Default.Favorite,
                isUnlocked = true,
                rarity = BadgeRarity.BRONZE,
                category = "Health"
            ),
            Badge(
                id = "4",
                title = "Academic Star",
                description = "Completed all academic tasks for a month",
                icon = Icons.Default.School,
                isUnlocked = false,
                rarity = BadgeRarity.GOLD,
                category = "Academic"
            ),
            Badge(
                id = "5",
                title = "Perfect Attendance",
                description = "Used the app every day for 3 months",
                icon = Icons.Default.EmojiEvents,
                isUnlocked = false,
                rarity = BadgeRarity.PLATINUM,
                category = "General"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Your Badges",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Badge categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BadgeCategoryItem(
                    category = "All",
                    count = badges.count { it.isUnlocked },
                    isSelected = true
                )
            }

            item {
                BadgeCategoryItem(
                    category = "Posture",
                    count = badges.count { it.isUnlocked && it.category == "Posture" },
                    isSelected = false
                )
            }

            item {
                BadgeCategoryItem(
                    category = "Tasks",
                    count = badges.count { it.isUnlocked && it.category == "Tasks" },
                    isSelected = false
                )
            }

            item {
                BadgeCategoryItem(
                    category = "Health",
                    count = badges.count { it.isUnlocked && it.category == "Health" },
                    isSelected = false
                )
            }

            item {
                BadgeCategoryItem(
                    category = "Academic",
                    count = badges.count { it.isUnlocked && it.category == "Academic" },
                    isSelected = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unlocked Badges",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Unlocked badges grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            badges.filter { it.isUnlocked }.forEach { badge ->
                BadgeItem(badge = badge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Locked Badges",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Locked badges grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            badges.filter { !it.isUnlocked }.forEach { badge ->
                BadgeItem(badge = badge)
            }
        }
    }
}

@Composable
fun BadgeCategoryItem(
    category: String,
    count: Int,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) BrandColors.Purple
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$category ($count)",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (badge.isUnlocked)
                        when (badge.rarity) {
                            BadgeRarity.PLATINUM -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE5E4E2),
                                    Color(0xFFB9B8B5)
                                )
                            )
                            BadgeRarity.GOLD -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFB8860B)
                                )
                            )
                            BadgeRarity.SILVER -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFC0C0C0),
                                    Color(0xFF808080)
                                )
                            )
                            BadgeRarity.BRONZE -> Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFCD7F32),
                                    Color(0xFF8B4513)
                                )
                            )
                        }
                    else
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = badge.title,
                tint = if (badge.isUnlocked) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = badge.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Text(
            text = badge.rarity.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                !badge.isUnlocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                badge.rarity == BadgeRarity.PLATINUM -> Color(0xFFE5E4E2)
                badge.rarity == BadgeRarity.GOLD -> Color(0xFFFFD700)
                badge.rarity == BadgeRarity.SILVER -> Color(0xFFC0C0C0)
                badge.rarity == BadgeRarity.BRONZE -> Color(0xFFCD7F32)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun LeaderboardTab() {
    val leaderboardEntries = remember {
        listOf(
            LeaderboardEntry(
                id = "1",
                name = "Sarah Johnson",
                points = 5200,
                rank = 1,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "2",
                name = "Michael Chen",
                points = 4800,
                rank = 2,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "3",
                name = "John Doe",
                points = 3500,
                rank = 3,
                avatar = null,
                isCurrentUser = true
            ),
            LeaderboardEntry(
                id = "4",
                name = "Emily Wilson",
                points = 3200,
                rank = 4,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "5",
                name = "David Kim",
                points = 2900,
                rank = 5,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "6",
                name = "Jessica Martinez",
                points = 2700,
                rank = 6,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "7",
                name = "Ryan Taylor",
                points = 2500,
                rank = 7,
                avatar = null,
                isCurrentUser = false
            ),
            LeaderboardEntry(
                id = "8",
                name = "Sophia Lee",
                points = 2300,
                rank = 8,
                avatar = null,
                isCurrentUser = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Top 3 users
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd place
            LeaderboardTopItem(
                entry = leaderboardEntries[1],
                height = 120.dp,
                showMedal = true
            )

            // 1st place
            LeaderboardTopItem(
                entry = leaderboardEntries[0],
                height = 150.dp,
                showMedal = true
            )

            // 3rd place
            LeaderboardTopItem(
                entry = leaderboardEntries[2],
                height = 100.dp,
                showMedal = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rest of the leaderboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rank",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(50.dp)
                    )

                    Text(
                        text = "User",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of users
                leaderboardEntries.forEach { entry ->
                    LeaderboardItem(entry = entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardTopItem(
    entry: LeaderboardEntry,
    height: androidx.compose.ui.unit.Dp,
    showMedal: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (entry.isCurrentUser)
                        BrandColors.Purple
                    else when (entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.name.first().toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        if (showMedal) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (entry.rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = entry.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (entry.isCurrentUser)
                BrandColors.Purple
            else
                MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${entry.points} XP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    if (entry.isCurrentUser)
                        BrandColors.Purple.copy(alpha = 0.7f)
                    else when (entry.rank) {
                        1 -> Color(0xFFFFD700).copy(alpha = 0.7f) // Gold
                        2 -> Color(0xFFC0C0C0).copy(alpha = 0.7f) // Silver
                        3 -> Color(0xFFCD7F32).copy(alpha = 0.7f) // Bronze
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
        )
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                if (entry.isCurrentUser)
                    BrandColors.Purple.copy(alpha = 0.1f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (entry.isCurrentUser)
                        BrandColors.Purple
                    else when (entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (entry.rank <= 3 || entry.isCurrentUser)
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // User info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (entry.isCurrentUser)
                    BrandColors.Purple
                else
                    MaterialTheme.colorScheme.onSurface
            )

            if (entry.isCurrentUser) {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandColors.Purple
                )
            }
        }

        // Points
        Text(
            text = "${entry.points} XP",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (entry.isCurrentUser)
                BrandColors.Purple
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val category: String,
    val date: String
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val rarity: BadgeRarity,
    val category: String
)

enum class BadgeRarity {
    BRONZE, SILVER, GOLD, PLATINUM;

    override fun toString(): String {
        return when (this) {
            BRONZE -> "Bronze"
            SILVER -> "Silver"
            GOLD -> "Gold"
            PLATINUM -> "Platinum"
        }
    }
}

data class LeaderboardEntry(
    val id: String,
    val name: String,
    val points: Int,
    val rank: Int,
    val avatar: String?,
    val isCurrentUser: Boolean
)
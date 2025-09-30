package citu.edu.stathis.mobile.features.onboarding.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import cit.edu.stathis.mobile.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
    onLogin: () -> Unit
) {
    val pages = listOf(
        OnboardPage(
            title = "Learn to move",
            body = "Master safe, guided routines with your AI coach and build confidence.",
            imageRes = R.drawable.mascot_teacher
        ),
        OnboardPage(
            title = "Maintain consistency",
            body = "Small steps, big wins. Stay consistent with friendly nudges.",
            imageRes = R.drawable.mascot_cheer
        ),
        OnboardPage(
            title = "Change your routine",
            body = "Fix posture, reduce pain, and feel your best every day.",
            imageRes = R.drawable.mascot_celebrate
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars.only(sides = WindowInsetsSides.Bottom)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val item = pages[page]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp),
                    contentScale = ContentScale.Fit
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.8f)

                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        DotsIndicator(
            count = pages.size,
            selectedIndex = pagerState.currentPage,
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {
                scope.launch {
                    if (pagerState.currentPage < pages.lastIndex) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        onNext()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
                focusedElevation = 8.dp
            )
        ) {
            Text(
                text = if (pagerState.currentPage < pages.lastIndex) "CONTINUE" else "GET STARTED",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "I ALREADY HAVE AN ACCOUNT",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { onLogin() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class OnboardPage(
    val title: String,
    val body: String,
    val imageRes: Int
)

@Composable
private fun DotsIndicator(
    count: Int,
    selectedIndex: Int,
    activeColor: Color,
    inactiveColor: Color,
    size: Dp = 6.dp,
    spacing: Dp = 6.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == selectedIndex) size + 2.dp else size)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) activeColor else inactiveColor)
            )
        }
    }
}



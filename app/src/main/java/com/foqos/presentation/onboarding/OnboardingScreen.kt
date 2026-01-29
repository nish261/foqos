package com.foqos.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.Lock,
            title = "Welcome to Foqos",
            description = "The ultimate focus app that helps you stay productive by blocking distracting apps and websites.",
            iconTint = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            icon = Icons.Filled.Block,
            title = "Block Distractions",
            description = "Choose apps and websites to block during focus sessions. Once started, you can't access them until the session ends.",
            iconTint = MaterialTheme.colorScheme.error
        ),
        OnboardingPage(
            icon = Icons.Filled.TouchApp,
            title = "Physical Unlock",
            description = "Use NFC tags or QR codes as physical keys. Place them somewhere inconvenient to create friction before breaking focus.",
            iconTint = MaterialTheme.colorScheme.tertiary
        ),
        OnboardingPage(
            icon = Icons.Filled.BarChart,
            title = "Track Progress",
            description = "Build streaks, view statistics, and see your focus time grow. Every session counts towards your goals.",
            iconTint = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            icon = Icons.Filled.Security,
            title = "Privacy First",
            description = "All data stays on your device. No analytics, no ads, no cloud storage. Your focus journey is yours alone.",
            iconTint = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            icon = Icons.Filled.Settings,
            title = "Enable Permissions",
            description = "To block apps, Foqos needs Accessibility and Usage Access permissions. We'll guide you through setup next.",
            iconTint = MaterialTheme.colorScheme.tertiary
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Page indicator
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.small,
                            color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {}
                    }
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                if (pagerState.currentPage < pages.size - 1) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.ArrowForward, "Next")
                    }
                } else {
                    Button(onClick = onComplete) {
                        Text("Get Started")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Check, "Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = page.iconTint.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = page.iconTint
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val iconTint: androidx.compose.ui.graphics.Color
)

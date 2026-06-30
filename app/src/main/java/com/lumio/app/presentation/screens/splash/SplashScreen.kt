package com.lumio.app.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumio.app.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter            = painterResource(R.drawable.ic_lumio_logo),
                contentDescription = "LUMIO",
                modifier           = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text          = "LUMIO",
                color         = Color.White,
                fontSize      = 28.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

package com.lumio.app.presentation.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lumio.app.domain.model.Category
import com.lumio.app.presentation.components.LumioBottomNavBar

@Composable
fun CategoriesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Category")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { LumioBottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Category.defaults) { cat ->
                val color = Color(android.graphics.Color.parseColor(cat.colorHex))
                Card(
                    onClick = {},
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat.emoji, fontSize = 22.sp)
                        }
                        Text(cat.name, fontWeight = FontWeight.SemiBold, color = color)
                        Text("0 reminders", fontSize = 11.sp, color = color.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

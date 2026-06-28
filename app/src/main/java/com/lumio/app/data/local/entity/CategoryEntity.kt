package com.lumio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumio.app.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Category(
        id        = id,
        name      = name,
        emoji     = emoji,
        colorHex  = colorHex,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(category: Category) = CategoryEntity(
            id        = category.id,
            name      = category.name,
            emoji     = category.emoji,
            colorHex  = category.colorHex,
            isDefault = category.isDefault
        )
    }
}

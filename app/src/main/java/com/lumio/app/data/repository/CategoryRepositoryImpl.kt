package com.lumio.app.data.repository

import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.entity.CategoryEntity
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(CategoryEntity.fromDomain(category))

    override suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(CategoryEntity.fromDomain(category))

    override suspend fun deleteCategory(id: Long) =
        categoryDao.deleteCategoryById(id)

    override suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    override suspend fun insertDefaultCategories() {
        val count = categoryDao.getCategoryCount()
        if (count == 0) {
            categoryDao.insertCategories(
                Category.defaults.map { CategoryEntity.fromDomain(it) }
            )
        }
    }
}

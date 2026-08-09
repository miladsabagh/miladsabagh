package com.goldjewelry.app.data.repository

import com.goldjewelry.app.data.database.ProductDao
import com.goldjewelry.app.data.model.Product
import com.goldjewelry.app.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    fun getAllActive(): Flow<List<Product>> = productDao.getAllActive()

    fun getByCategory(category: ProductCategory): Flow<List<Product>> =
        productDao.getByCategory(category)

    suspend fun getById(id: Long): Product? = productDao.getById(id)

    suspend fun insert(product: Product): Long = productDao.insert(product)

    suspend fun update(product: Product) = productDao.update(product)

    suspend fun softDelete(id: Long) = productDao.softDelete(id)

    fun countActive(): Flow<Int> = productDao.countActive()
}

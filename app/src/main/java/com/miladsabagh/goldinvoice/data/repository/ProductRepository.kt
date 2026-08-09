package com.miladsabagh.goldinvoice.data.repository

import com.miladsabagh.goldinvoice.data.dao.ProductDao
import com.miladsabagh.goldinvoice.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    fun observeAll(): Flow<List<Product>> = dao.observeAll()
    fun observeCount(): Flow<Int> = dao.observeCount()
    fun search(query: String): Flow<List<Product>> = dao.search(query)
    suspend fun getById(id: Long): Product? = dao.getById(id)
    suspend fun save(product: Product): Long = dao.upsert(product)
    suspend fun delete(product: Product) = dao.delete(product)
}

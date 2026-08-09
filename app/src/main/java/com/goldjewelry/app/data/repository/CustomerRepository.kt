package com.goldjewelry.app.data.repository

import com.goldjewelry.app.data.database.CustomerDao
import com.goldjewelry.app.data.model.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {

    fun getAll(): Flow<List<Customer>> = customerDao.getAll()

    fun search(query: String): Flow<List<Customer>> = customerDao.search(query)

    suspend fun getById(id: Long): Customer? = customerDao.getById(id)

    suspend fun insert(customer: Customer): Long = customerDao.insert(customer)

    suspend fun update(customer: Customer) = customerDao.update(customer)

    suspend fun delete(id: Long) = customerDao.delete(id)

    fun count(): Flow<Int> = customerDao.count()
}

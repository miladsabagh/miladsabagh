package com.miladsabagh.goldinvoice.data.repository

import com.miladsabagh.goldinvoice.data.dao.CustomerDao
import com.miladsabagh.goldinvoice.data.entity.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val dao: CustomerDao) {
    fun observeAll(): Flow<List<Customer>> = dao.observeAll()
    fun observeCount(): Flow<Int> = dao.observeCount()
    fun search(query: String): Flow<List<Customer>> = dao.search(query)
    suspend fun getById(id: Long): Customer? = dao.getById(id)
    suspend fun save(customer: Customer): Long = dao.upsert(customer)
    suspend fun delete(customer: Customer) = dao.delete(customer)
}

package com.ma.ikea.data.local

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.ma.ikea.core.Result
import com.ma.ikea.data.Product
import com.ma.ikea.data.remote.ProductApi
import com.ma.ikea.data.remote.ProductDataSource
import com.ma.ikea.logd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class EventData(val event: String, val payload: Product)

class ProductsRepository(private val productDao: ProductDao) {
    val products = productDao.getAll()
    private var isActive = false

    suspend fun refresh(): Result<Boolean> {
        return try {
            logd("fetching product list")
            val fetchedProducts = ProductApi.service.find()
            logd("fetching successful with ${fetchedProducts.size} items")
            CoroutineScope(Dispatchers.IO).launch {
                for (id in productDao.getAllIds()) {
                    if (fetchedProducts.indexOfFirst { p -> p._id == id } == -1) {
                        productDao.delete(id)
                    }
                }
            }
            for (product in fetchedProducts) {
                productDao.insert(product)
            }
            Result.Success(true)
        } catch(e: Exception) {
            logd("fetching failed with exception ${e.message}")
            Result.Error(e)
        }
    }

    fun getById(id: String): LiveData<Product> {
        logd("get product by id = $id")
        return productDao.getById(id)
    }

    suspend fun save(product: Product): Result<Product> {
        return try {
            logd("saving product")
            val createdProduct = ProductApi.service.create(product)
            logd("saving successful with id = ${product._id}")
            productDao.insert(createdProduct)
            Result.Success(createdProduct)
        } catch (e: Exception) {
            logd("saving failed with exception ${e.message}")
            saveLocally(product)
        }
    }

    private suspend fun saveLocally(product: Product): Result<Product> {
        return try {
            logd("saving product locally")
            val count = productDao.getLocalCount()
            product._id = "temp_${count}"
            product.local = true
            productDao.insert(product)
            Result.Success(product)
        } catch (e: Exception) {
            logd("saving locally failed with exception ${e.message}")
            Result.Error(e)
        }
    }

    suspend fun update(product: Product): Result<Product> {
        return try {
            logd("updating product with id ${product._id}")
            val updatedProduct = ProductApi.service.update(product._id, product)
            logd("updating successful")
            productDao.update(updatedProduct)
            Result.Success(updatedProduct)
        } catch (e: Exception) {
            logd("updating failed with exception ${e.message}")
            Result.Error(e)
        }
    }

    suspend fun remove(id: String): Result<Int> {
        return try {
            logd("removing product with id $id")
            ProductApi.service.delete(id)
            logd("removing successful")
            val row = productDao.delete(id)
            Result.Success(row)
        } catch (e: Exception) {
            logd("removing failed with exception ${e.message}")
            Result.Error(e)
        }
    }

    private suspend fun collectEvents() {
        logd("start collecting events")
        while (isActive) {
            val event = ProductDataSource.eventChannel.receive()
            val eventObject = Gson().fromJson(event, EventData::class.java)
            val product = eventObject.payload
            val index = products.value?.indexOfFirst { it._id == product._id }
            if (eventObject.event == "created" || eventObject.event == "updated") {
                product.local = false
                if (index != null && index != -1) {
                    productDao.update(product)
                } else {
                    productDao.insert(product)
                }
            }
            else if (eventObject.event == "deleted") {
                if (index != null && index != -1) {
                    productDao.delete(product._id)
                }
            }
            logd("received ${eventObject.event} event with payload ${eventObject.payload}")
        }
    }

    fun startListen() {
        logd("start listening - WebSocket")
        ProductDataSource.createWebSocket()
        isActive = true
        CoroutineScope(Dispatchers.Default).launch { collectEvents() }
    }

    fun stopListen() {
        logd("stop listening - WebSocket")
        isActive = false;
        ProductDataSource.destroyWebSocket()
    }

    fun syncData() {
        logd("sync data")
        CoroutineScope(Dispatchers.IO).launch {
            val products = productDao.getAllLocal()
            for (product in products) {
                try {
                    val createdProduct = ProductApi.service.create(product)
                    logd("saving successful with id = ${createdProduct._id}")
                    productDao.delete(product._id)
                }
                catch (e: Exception) {
                    logd("saving failed with exception ${e.message}")
                }
            }
            refresh()
        }
    }
}

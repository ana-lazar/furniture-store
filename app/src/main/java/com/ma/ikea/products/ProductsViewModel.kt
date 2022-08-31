package com.ma.ikea.products

import android.app.Application
import androidx.lifecycle.*
import com.ma.ikea.data.local.ProductsDatabase
import com.ma.ikea.data.Product
import com.ma.ikea.data.local.ProductsRepository
import com.ma.ikea.logd
import com.ma.ikea.core.Result
import kotlinx.coroutines.launch
import java.net.ConnectException

class ProductsViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableErrorMessage = MutableLiveData<String>().apply { value = "" }

    var products: LiveData<List<Product>>
    var loading: LiveData<Boolean> = mutableLoading
    var errorMessage: LiveData<String> = mutableErrorMessage

    private val productsRepository: ProductsRepository

    init {
        val productDao = ProductsDatabase.getDatabase(application).productDao()
        productsRepository = ProductsRepository(productDao)
        products = productsRepository.products
    }

    fun refresh() {
        logd("starting refresh")
        viewModelScope.launch {
            mutableLoading.value = true
            mutableErrorMessage.value = null
            when (val result = productsRepository.refresh()) {
                is Result.Success -> {
                    logd("refresh succeeded")
                }
                is Result.Error -> {
                    logd("refresh failed")
                    val message: String = when (result.exception) {
                        is ConnectException -> {
                            "Failed to connect to server"
                        }
                        else -> {
                            "Refreshing products failed"
                        }
                    }

                    mutableErrorMessage.value = message
                }
            }
            mutableLoading.value = false
        }
    }

    fun startListen() {
        productsRepository.startListen()
    }

    fun stopListen() {
        productsRepository.stopListen()
    }

    fun syncData() {
        productsRepository.syncData()
    }
}

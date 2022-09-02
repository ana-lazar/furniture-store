package com.ma.ikea.product

import android.app.Application
import androidx.lifecycle.*
import com.ma.ikea.R
import com.ma.ikea.core.Result
import com.ma.ikea.data.local.ProductsDatabase
import com.ma.ikea.data.Product
import com.ma.ikea.data.local.ProductsRepository
import com.ma.ikea.isStringValid
import com.ma.ikea.logd
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableProductFormState = MutableLiveData<ProductFormState>()
    private val mutableErrorMessage = MutableLiveData<String>().apply { value = "" }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }

    val productFormState: LiveData<ProductFormState> = mutableProductFormState
    var errorMessage: LiveData<String> = mutableErrorMessage
    var loading: LiveData<Boolean> = mutableLoading
    var completed: LiveData<Boolean> = mutableCompleted

    private val productsRepository: ProductsRepository

    init {
        val productDao = ProductsDatabase.getDatabase(application).productDao()
        productsRepository = ProductsRepository(productDao)
    }

    fun getById(id: String): LiveData<Product> {
        logd("get product by id = $id")
        return productsRepository.getById(id)
    }

    fun saveOrUpdate(product: Product) {
        logd("save or update product")
        viewModelScope.launch {
            val result: Result<Product>
            mutableErrorMessage.value = null
            productDataCheck(product)
            if (productFormState.value?.isDataValid == true) {
                mutableLoading.value = true
                logd("save / update started")
                result = if (product._id === "") {
                    productsRepository.save(product)
                } else {
                    productsRepository.update(product)
                }
                logd("save / update done")
                when (result) {
                    is Result.Success -> {
                        logd("save or update successful")
                        if (result.data.local) {
                            mutableErrorMessage.value = "Product saved locally"
                        }
                        mutableCompleted.value = true
                    }
                    is Result.Error -> {
                        logd("save or update failed")
                        var message = ""
                        if (result.exception is ConnectException) {
                            message = "Failed to connect to server"
                        }
                        else if (result.exception is HttpException) {
                            if (result.exception.code() == 400) {
                                message = "Product is not valid"
                            }
                            else if (result.exception.code() == 409) {
                                message = "Product version conflict"
                            }
                        }
                        else {
                            message = "Saving product to server failed"
                        }

                        mutableErrorMessage.value = message
                    }
                }
                mutableLoading.value = false
            }
        }
    }

    fun remove(id: String) {
        logd("remove product with id = $id")
        viewModelScope.launch {
            mutableErrorMessage.value = null
            mutableLoading.value = true
            when (val result: Result<Int> = productsRepository.remove(id)) {
                is Result.Success -> {
                    logd("remove successful")
                    mutableCompleted.value = true
                }
                is Result.Error -> {
                    logd("remove failed")
                    val message: String = if (result.exception is ConnectException) {
                        "Failed to connect to server"
                    } else {
                        "Removing product on server failed"
                    }

                    mutableErrorMessage.value = message
                }
            }
            mutableLoading.value = false
        }
    }

    private fun productDataCheck(product: Product) {
        var nameError: Int? = null
        var descriptionError: Int? = null
        var companyError: Int? = null
        var categoryError: Int? = null
        var isleError: Int? = null
        if (!isStringValid(product.name)) {
            nameError = R.string.invalid_name
        }
        if (!isStringValid(product.description)) {
            descriptionError = R.string.invalid_description
        }
        if (!isStringValid(product.company)) {
            companyError = R.string.invalid_description
        }
        if (!isStringValid(product.category)) {
            categoryError = R.string.invalid_category
        }
        if (!isStringValid(product.isle)) {
            isleError = R.string.invalid_isle
        }
        val valid = nameError === null && descriptionError === null && companyError === null && categoryError === null && isleError === null
        mutableProductFormState.value = ProductFormState(nameError, descriptionError, companyError, categoryError, isleError, valid)
    }
}
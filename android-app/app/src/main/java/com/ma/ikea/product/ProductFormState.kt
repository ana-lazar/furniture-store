package com.ma.ikea.product

data class ProductFormState(
    val nameError: Int? = null,
    val descriptionError: Int? = null,
    val companyError: Int? = null,
    val categoryError: Int? = null,
    val isleError: Int? = null,
    val isDataValid: Boolean = false
)

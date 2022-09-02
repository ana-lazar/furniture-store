package com.ma.ikea.data.mock

import com.ma.ikea.data.Product

object ProductsRepository {
    private var products = mutableListOf<Product>()

    private val count = 10

    init {
        for (i in 1..count) {
            save(create(i))
        }
    }

    private fun create(position: Int): Product {
        return Product(position.toString(), "ScaunNumber" + position, "Description " + position, "Company " + position, position, "Category " + position, position.toString(), false)
    }

    fun save(product: Product) {
        if (product._id == "") {
//            manually generate product id
//            product._id = generateId()
        }

        products.add(product)
    }

    fun update(product: Product) {
        val index = products.indexOfFirst{ p-> p._id == product._id }
        products[index] = product
    }

    fun remove(id: String) {
        products.removeIf { p -> p._id == id }
    }

    fun get(): List<Product> {
        return products
    }

    private fun generateId(): Int {
        var id = 0
        while (products.indexOfFirst{ p -> p._id == id.toString() } != -1) {
            id += 1
        }
        return id
    }
}

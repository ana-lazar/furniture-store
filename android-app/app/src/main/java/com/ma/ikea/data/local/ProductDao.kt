package com.ma.ikea.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ma.ikea.data.Product

@Dao
interface ProductDao {
    @Query("SELECT * from products ORDER BY name ASC")
    fun getAll(): LiveData<List<Product>>

    @Query("SELECT DISTINCT _id from products ORDER BY _id ASC")
    fun getAllIds(): List<String>

    @Query("SELECT * from products WHERE local=1 ORDER BY name ASC")
    fun getAllLocal(): List<Product>

    @Query("SELECT * FROM products WHERE _id=:id ")
    fun getById(id: String): LiveData<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Query("SELECT COUNT(*) FROM products WHERE local=1")
    suspend fun getLocalCount(): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(product: Product)

    @Query("DELETE FROM products WHERE _id=:id")
    suspend fun delete(id: String): Int
}

package com.ma.ikea.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    var _id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "company") var company: String,
    @ColumnInfo(name = "quantity") var quantity: Int,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "isle") var isle: String,
    @ColumnInfo(name = "local") var local: Boolean
): Serializable {
    constructor():this("","","", "", 0, "", "", false)
}

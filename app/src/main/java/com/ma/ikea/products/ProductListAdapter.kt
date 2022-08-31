package com.ma.ikea.products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ma.ikea.R
import com.ma.ikea.data.Product
import com.ma.ikea.product.ProductEditActivity

class ProductListAdapter(
    private val activity: ProductListActivity
) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {

    var products = emptyList<Product>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onProductClick: View.OnClickListener = View.OnClickListener { view ->
        val product = view.tag as Product
        val intent = Intent(activity, ProductEditActivity::class.java)
        intent.putExtras(Bundle().apply {
            putString("PRODUCT_ID", product._id)
        })
        activity.productEditActivityLauncher.launch(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.itemView.setOnClickListener(onProductClick)
        holder.itemView.tag = product
        holder.nameTextView.text = product.name
        holder.companyTextView.text = product.company
        holder.quantityTextView.text = product.quantity.toString()
        if (!product.local) {
            holder.networkIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_network))
        }
        else {
            holder.networkIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_network_not))
        }
    }

    override fun getItemCount() = products.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.name)
        val companyTextView: TextView = view.findViewById(R.id.company)
        val quantityTextView: TextView = view.findViewById(R.id.quantity)
        val networkIcon: ImageView = view.findViewById(R.id.network_icon)
    }
}

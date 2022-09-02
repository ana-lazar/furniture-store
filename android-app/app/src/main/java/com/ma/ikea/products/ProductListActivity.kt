package com.ma.ikea.products

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ma.ikea.R
import com.ma.ikea.core.ConnectivityLiveData
import com.ma.ikea.databinding.ActivityProductListBinding
import com.ma.ikea.logd
import com.ma.ikea.product.ProductEditActivity

class ProductListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductListBinding
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var productsViewModel: ProductsViewModel

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logd("onCreate called")

        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityManager = getSystemService(android.net.ConnectivityManager::class.java)
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(this, {
            if (it) {
                productsViewModel.startListen()
                productsViewModel.syncData()
                binding.networkIcon.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_network))
            }
            else {
                productsViewModel.stopListen()
                binding.networkIcon.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_network_not))
            }
        })

        productListAdapter = ProductListAdapter(this)
        productsViewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        binding.productList.adapter = productListAdapter
        binding.productList.layoutManager = LinearLayoutManager(this)

        productsViewModel.products.observe(this, { value ->
            if (value.isEmpty()) {
                binding.listMessage.visibility = View.VISIBLE
            }
            else {
                binding.listMessage.visibility = View.INVISIBLE
            }
            logd(value.size)
            productListAdapter.products = value
        })
        productsViewModel.loading.observe(this, { loading ->
            logd("update loading")
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        })
        productsViewModel.errorMessage.observe(this, { errorMessage ->
            if (errorMessage != null && errorMessage != "") {
                logd("update loading error")
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
        productsViewModel.refresh()

        binding.addButton.setOnClickListener {
            val intent = Intent(this@ProductListActivity, ProductEditActivity::class.java)
            productEditActivityLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logd("onDestroy")
        productsViewModel.stopListen()
    }

    val productEditActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val message = data.getStringExtra("MESSAGE")
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
        }
}

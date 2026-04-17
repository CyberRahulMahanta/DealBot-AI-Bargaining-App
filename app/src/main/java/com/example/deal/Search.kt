package com.example.deal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deal.adapter.SearchProductAdapter
import com.example.deal.model.Product
import com.example.deal.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductSearchActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvResultCount: TextView
    private lateinit var emptyLayout: LinearLayout

    private lateinit var adapter: SearchProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_search)

        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        rvProducts = findViewById(R.id.rvProducts)
        tvResultCount = findViewById(R.id.tvResultCount)
        emptyLayout = findViewById(R.id.emptyLayout)

        btnBack.setOnClickListener { finish() }

        adapter = SearchProductAdapter(emptyList())

        rvProducts.layoutManager = GridLayoutManager(this, 2)
        rvProducts.adapter = adapter
        rvProducts.setHasFixedSize(false)

        fetchProducts()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
                rvProducts.post { updateResultState() }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchProducts() {
        RetrofitClient.api.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(
                call: Call<List<Product>>,
                response: Response<List<Product>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body() ?: emptyList()

                    productList.clear()
                    productList.addAll(products)
                    adapter.updateData(products)
                } else {
                    productList.clear()
                    adapter.updateData(emptyList())

                    Toast.makeText(
                        this@ProductSearchActivity,
                        "No products found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                updateResultState()
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                productList.clear()
                adapter.updateData(emptyList())
                updateResultState()

                Toast.makeText(
                    this@ProductSearchActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateResultState() {
        val count = adapter.getFilteredCount()
        tvResultCount.text = "$count products found"

        if (count == 0) {
            emptyLayout.visibility = View.VISIBLE
            rvProducts.visibility = View.GONE
        } else {
            emptyLayout.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
        }
    }
}
package io.github.rosariopfernandes.firepesasampleapp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.github.rosariopfernandes.firepesasampleapp.model.Product
import kotlinx.android.synthetic.main.activity_shopping_cart.*
import kotlinx.android.synthetic.main.content_shopping_cart.*

class ShoppingCartActivity : AppCompatActivity() {
    var adapter: FirebaseRecyclerAdapter<Product, ProductHolder>? = null

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Check Out", Snackbar.LENGTH_LONG).show()
        }
        val user = FirebaseAuth.getInstance().currentUser

        val productsRef = FirebaseDatabase.getInstance()
                .getReference("shoppingCart").child(user!!.uid)
        val options = FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(productsRef, Product::class.java)
                .build()
        adapter = object: FirebaseRecyclerAdapter<Product, ProductHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                    ProductHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_shopping_cart, parent, false)
                return ProductHolder(view)
            }

            override fun onBindViewHolder(holder: ProductHolder,
                                          position: Int, product: Product) {
                holder.txtProduct.text = product.name
                val total = product.price * product.qty
                holder.txtQty.text = "${product.qty} x ${product.price} MT"
                holder.txtPrice.text = "${total} MT"
            }
        }
        rvShoppingCart.layoutManager = LinearLayoutManager(this)
        rvShoppingCart.adapter = adapter

    }

    class ProductHolder(v: View) : RecyclerView.ViewHolder(v) {
        var cardView: CardView = v.findViewById(R.id.cardView)
        var txtProduct: TextView = v.findViewById(R.id.txtProductName)
        var txtQty: TextView = v.findViewById(R.id.txtQty)
        var txtPrice: TextView = v.findViewById(R.id.txtTotal)
    }

}

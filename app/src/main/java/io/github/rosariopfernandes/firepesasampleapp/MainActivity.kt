/*
 *MIT License
 *
 *Copyright (c) 2018 Rosário Pereira Fernandes
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in all
 *copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *SOFTWARE.
 */

package io.github.rosariopfernandes.firepesasampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import io.github.rosariopfernandes.firepesa.Transaction
import io.github.rosariopfernandes.firepesa.ktx.catch
import io.github.rosariopfernandes.firepesa.ktx.then
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import io.github.rosariopfernandes.firepesasampleapp.model.Product
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    123)
        }
        else{
            /*fab.setOnClickListener { view ->
            val tx = Transaction()
            //tx.addNotification("You did it!", "Yey")
            tx.payment("844471329",25.0f,
                    "ref","3rdRef")
                    .then {
                        Log.e("Response", "${it.code}, ${it.transactionStatus}")
                    }.catch {
                        it.printStackTrace()
                    }
            }*/
            /*val tx = Transaction(this)
            tx.payment("844471329",25.0f,
                    "ref","3rdRef")
                    .then {
                        Log.e("Response", "description = "+it?.description)
                    }*/

            val productsRef = FirebaseDatabase.getInstance().getReference("products")
            val options = FirebaseRecyclerOptions.Builder<Product>()
                    .setQuery(productsRef, Product::class.java)
                    .build()
            adapter = object:FirebaseRecyclerAdapter<Product, ProductHolder>(options){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                        ProductHolder {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.card_product, parent, false)
                    return ProductHolder(view)
                }

                override fun onBindViewHolder(holder: ProductHolder,
                                                        position: Int, product:Product) {
                    holder.txtProduct.text = product.name
                    holder.txtPrice.text = "${product.price}"
                    holder.cardView.setOnClickListener {
                        val intent = Intent(this@MainActivity,
                                ProductActivity::class.java)
                        intent.putExtra("productId", product.id)
                        startActivity(intent)
                    }
                }
            }
            rvProducts.layoutManager = GridLayoutManager(this, 2)
            rvProducts.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_cart -> {
                val intent = Intent(this, ShoppingCartActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Log.d("onActivityResult","Logged in")
            val token = FirebaseInstanceId.getInstance().token
            val user = FirebaseAuth.getInstance().currentUser
            user.let {
                FirebaseDatabase.getInstance().getReference("firePesa/consumers")
                        .child(user!!.uid).child("tokens/android")
                        .setValue(token)
            }
        }
    }

    class ProductHolder(v: View) : RecyclerView.ViewHolder(v) {
        var cardView:CardView = v.findViewById(R.id.cardView)
        var imgProduct: ImageView = v.findViewById(R.id.imgProduct)
        var txtProduct: TextView = v.findViewById(R.id.txtProduct)
        var txtPrice: TextView = v.findViewById(R.id.txtPrice)
    }
}

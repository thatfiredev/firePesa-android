package io.github.rosariopfernandes.firepesasampleapp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.github.rosariopfernandes.firepesasampleapp.model.Product
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.content_product.*

class ProductActivity : AppCompatActivity() {
    val rootRef = FirebaseDatabase.getInstance().reference
    val shoppingCartRef = rootRef.child("shoppingCart")
    val productsRef = rootRef.child("products")
    val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val productId = intent.extras.getString("productId")
        productId.let {

            Log.e("ProductActivity","productId = ${productId}")
            productsRef.child("product${productId}").addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val product = dataSnapshot.getValue(Product::class.java)
                    supportActionBar?.title = product?.name

                    txtDescription.setText(product?.description)

                    fab.setOnClickListener { view ->
                        shoppingCartRef.child(user!!.uid).child(productId)
                                .setValue(product)
                        Snackbar.make(view, "Adicionado ao carrinho",
                                Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        }
    }
}

import com.example.practice.repository.ProductRepo
import com.example.travium.Model.ProfileModel

class ProductRepoImpl : ProductRepo {

    private val ref =
        FirebaseDatabase.getInstance().getReference("products")

    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val productId = ref.push().key ?: return
        model.id = productId

        ref.child(productId).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product added successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    override fun updateProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.id).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product updated successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    override fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Product deleted successfully")
                } else {
                    callback(false, it.exception?.message ?: "Error")
                }
            }
    }

    override fun getAllProduct(
        callback: (Boolean, String, List<ProductModel>) -> Unit
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children
                    .mapNotNull { it.getValue(ProductModel::class.java) }

                callback(true, "Fetched", products)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getProductById(
        productID: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        ref.child(productID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product =
                        snapshot.getValue(ProductModel::class.java)
                    callback(true, "Fetched", product)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getProductByCategory(
        categoryId: String,
        callback: (Boolean, String, List<ProductModel>) -> Unit
    ) {
        ref.orderByChild("categoryId")
            .equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = snapshot.children
                        .mapNotNull { it.getValue(ProductModel::class.java) }

                    callback(true, "Fetched", products)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        // same as before – logic unchanged
    }
}

package com.xrandolphx.elegantcommerce.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xrandolphx.elegantcommerce.data.CartProduct
import com.xrandolphx.elegantcommerce.databinding.ItemCartBinding
import com.xrandolphx.elegantcommerce.helper.getProductPrice
import java.text.NumberFormat
import java.util.Locale

class CartProductAdapter :
    RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imageCartProduct)
                tvProductCartTitle.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                val priceAfterPercentage =
                    cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
                    .format(priceAfterPercentage)
                tvPriceEachItem.text = formattedPrice

                imageCartProductColor.setImageDrawable(
                    ColorDrawable(
                        cartProduct.selectedColor ?: Color.TRANSPARENT
                    )
                )

                if (cartProduct.selectedSize != null) {
                    tvCartProductSize.text = cartProduct.selectedSize
                } else {
                    tvCartProductSize.text = ""
                    imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                }

            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartProductsViewHolder {
        return CartProductsViewHolder(
            ItemCartBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)
        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }
        holder.binding.plusCartBtn.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }
        holder.binding.minusCartBtn.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
}
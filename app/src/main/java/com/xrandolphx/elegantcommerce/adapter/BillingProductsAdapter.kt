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
import com.xrandolphx.elegantcommerce.databinding.BillingProductsRvItemBinding
import com.xrandolphx.elegantcommerce.helper.getProductPrice
import java.text.NumberFormat
import java.util.Locale

class BillingProductsAdapter :
    RecyclerView.Adapter<BillingProductsAdapter.BillingProductsViewHolder>() {

    inner class BillingProductsViewHolder(val binding: BillingProductsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(billingProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = billingProduct.product.name
                tvBillingProductQuantity.text = billingProduct.quantity.toString()

                val priceAfterPercentage =
                    billingProduct.product.offerPercentage.getProductPrice(billingProduct.product.price)
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
                    .format(priceAfterPercentage)
                tvProductCartPrice.text = formattedPrice

                imageCartProductColor.setImageDrawable(
                    ColorDrawable(
                        billingProduct.selectedColor ?: Color.TRANSPARENT
                    )
                )
                if (billingProduct.selectedSize != null) {
                    tvCartProductSize.text = billingProduct.selectedSize
                } else {
                    tvCartProductSize.text = ""
                    imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                }

            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductsViewHolder {
        return BillingProductsViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BillingProductsViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]

        holder.bind(billingProduct)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
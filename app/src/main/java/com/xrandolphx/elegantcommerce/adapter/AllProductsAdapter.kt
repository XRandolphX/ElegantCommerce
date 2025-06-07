package com.xrandolphx.elegantcommerce.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.xrandolphx.elegantcommerce.data.Product
import com.xrandolphx.elegantcommerce.databinding.ProductRvItemBinding
import java.text.NumberFormat
import java.util.Locale

class AllProductsAdapter : RecyclerView.Adapter<AllProductsAdapter.AllProductsViewHolder>() {

    inner class AllProductsViewHolder(private val binding: ProductRvItemBinding) :
        ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                product.offerPercentage?.let {
                    val remainingPricePercentage = 1f - it
                    val priceAfterOffer = remainingPricePercentage * product.price
                    val formattedPrice =
                        NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(priceAfterOffer)
                    tvNewPrice.text = formattedPrice
                    tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                if (product.offerPercentage != null) {
                    tvNewPrice.visibility = View.VISIBLE
                    tvOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    val discount = 1f - product.offerPercentage
                    val newPrice = discount * product.price
                    val formattedPrice =
                        NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(newPrice)
                    tvNewPrice.text = formattedPrice
                } else {
                    tvNewPrice.visibility = View.INVISIBLE
                    tvOldPrice.paintFlags = 0
                }
                val formattedPrice =
                    NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(product.price)
                tvOldPrice.text = formattedPrice
                tvName.text = product.name
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProductsViewHolder {
        return AllProductsViewHolder(
            ProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null

}
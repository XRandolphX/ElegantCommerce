package com.xrandolphx.elegantcommerce.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.data.order.Order
import com.xrandolphx.elegantcommerce.data.order.OrderStatus
import com.xrandolphx.elegantcommerce.data.order.getOrderStatus
import com.xrandolphx.elegantcommerce.databinding.OrderItemBinding

class AllOrdersAdapter : RecyclerView.Adapter<AllOrdersAdapter.OrdersViewHolder>() {

    inner class OrdersViewHolder(private val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = order.orderId.toString()
                tvOrderDate.text = order.date
                val resources = itemView.resources

                val colorDrawable = when (getOrderStatus(order.orderStatus)) {
                    is OrderStatus.Ordered -> {
                        ColorDrawable(
                            resources.getColor(
                                R.color.g_orange_yellow,
                                itemView.context.theme
                            )
                        )
                    }

                    is OrderStatus.Confirmed -> {
                        ColorDrawable(resources.getColor(R.color.c_blue, itemView.context.theme))
                    }

                    is OrderStatus.Delivered -> {
                        ColorDrawable(resources.getColor(R.color.g_green, itemView.context.theme))
                    }

                    is OrderStatus.Shipped -> {
                        ColorDrawable(resources.getColor(R.color.c_orange, itemView.context.theme))
                    }

                    is OrderStatus.Canceled -> {
                        ColorDrawable(resources.getColor(R.color.g_red, itemView.context.theme))
                    }

                    is OrderStatus.Returned -> {
                        ColorDrawable(resources.getColor(R.color.c_yellow, itemView.context.theme))
                    }
                }

                imageOrderState.setImageDrawable(colorDrawable)

            }
        }
    }


    private val diffUtil = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.products == newItem.products
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(
            OrderItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

        holder.itemView.setOnClickListener {
            onClick?.invoke(order)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Order) -> Unit)? = null
}
package com.spendly.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spendly.app.data.Transaction
import com.spendly.app.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    private var items: List<Transaction> = emptyList()
    private val dateFmt = SimpleDateFormat("d MMM, HH:mm", Locale.UK)

    fun submit(list: List<Transaction>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tx: Transaction) {
            binding.merchant.text = tx.merchant
            binding.amount.text = String.format(Locale.UK, "-£%.2f", tx.amount)
            binding.category.text = tx.category ?: "Tap to categorise"
            binding.category.setTextColor(
                if (tx.category == null)
                    binding.root.context.getColor(android.R.color.holo_red_light)
                else
                    binding.root.context.getColor(android.R.color.darker_gray)
            )
            binding.date.text = dateFmt.format(tx.timestamp)
            binding.root.setOnClickListener { onClick(tx) }
        }
    }
}

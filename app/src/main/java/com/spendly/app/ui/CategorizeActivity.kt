package com.spendly.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.R as MaterialR
import com.spendly.app.data.AppDatabase
import com.spendly.app.data.Categories
import com.spendly.app.databinding.ActivityCategorizeBinding
import kotlinx.coroutines.launch
import java.util.Locale

class CategorizeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TX_ID = "tx_id"
        const val EXTRA_MERCHANT = "merchant"
        const val EXTRA_AMOUNT = "amount"
    }

    private lateinit var binding: ActivityCategorizeBinding
    private var selectedCategory: String? = null
    private var txId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txId = intent.getLongExtra(EXTRA_TX_ID, -1L)
        val merchant = intent.getStringExtra(EXTRA_MERCHANT) ?: "Unknown"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)

        binding.sheetAmount.text = String.format(Locale.UK, "£%.2f — %s", amount, merchant)

        if (amount != 0.0) {
            binding.amountInput.setText(String.format(Locale.UK, "%.2f", amount))
        }

        Categories.ALL.forEach { category ->
            val chip = Chip(this, null, MaterialR.attr.chipStyle).apply {
                text = category
                isCheckable = true
                setOnClickListener {
                    selectedCategory = category
                    binding.saveBtn.isEnabled = true
                }
            }
            binding.chipGroup.addView(chip)
        }

        binding.saveBtn.isEnabled = false
        binding.saveBtn.setOnClickListener { save() }
        binding.dismissArea.setOnClickListener { finish() }
    }

    private fun save() {
        val category = selectedCategory ?: return
        val note = binding.noteInput.text?.toString()?.trim().orEmpty()

        val amountText = binding.amountInput.text?.toString()?.trim().orEmpty()
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            binding.amountInput.error = "Enter a valid amount"
            return
        }

        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).transactionDao()
            val existing = dao.getById(txId) ?: return@launch
            dao.update(existing.copy(amount = amount, category = category, note = note.ifBlank { null }))
            finish()
        }
    }
}

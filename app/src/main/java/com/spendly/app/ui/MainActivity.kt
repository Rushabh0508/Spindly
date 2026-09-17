package com.spendly.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spendly.app.data.AppDatabase
import com.spendly.app.data.Transaction
import com.spendly.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TransactionAdapter { tx -> openCategorize(tx) }
        binding.txList.layoutManager = LinearLayoutManager(this)
        binding.txList.adapter = adapter

        binding.grantAccessBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.addManualBtn.setOnClickListener {
            openCategorize(
                Transaction(merchant = "Manual entry", amount = 0.0, category = null, note = null, timestamp = System.currentTimeMillis())
            )
        }

        observeThisMonth()
    }

    private fun openCategorize(tx: Transaction) {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).transactionDao()
            val id = if (tx.id == 0L) dao.insert(tx) else tx.id
            startActivity(
                Intent(this@MainActivity, CategorizeActivity::class.java).apply {
                    putExtra(CategorizeActivity.EXTRA_TX_ID, id)
                    putExtra(CategorizeActivity.EXTRA_MERCHANT, tx.merchant)
                    putExtra(CategorizeActivity.EXTRA_AMOUNT, tx.amount)
                }
            )
        }
    }

    private fun observeThisMonth() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        lifecycleScope.launch {
            AppDatabase.getInstance(applicationContext).transactionDao()
                .observeForMonth(monthStart, monthEnd)
                .collectLatest { list -> render(list) }
        }
    }

    private fun render(list: List<Transaction>) {
        adapter.submit(list)

        val total = list.sumOf { it.amount }
        binding.totalSpend.text = String.format(Locale.UK, "£%.2f", total)

        val uncategorised = list.count { it.category == null }
        binding.uncategorisedNote.text = if (uncategorised > 0)
            "⚠️ $uncategorised transaction${if (uncategorised == 1) "" else "s"} unlabeled"
        else
            "✅ Everything categorised"

        val byCategory = list
            .groupBy { it.category ?: "Uncategorised" }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
        binding.pieChart.setData(byCategory)
    }
}

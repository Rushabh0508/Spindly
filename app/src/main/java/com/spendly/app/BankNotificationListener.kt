package com.spendly.app

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spendly.app.data.AppDatabase
import com.spendly.app.data.Transaction
import com.spendly.app.ui.CategorizeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for push notifications and, when one comes from a banking app in
 * [BANK_PACKAGES], tries to pull an amount + merchant out of the text and
 * pop the categorise screen.
 *
 * Tuned first for Monzo (co.uk.getmondo). Add more package names to
 * BANK_PACKAGES as you add other banks — each bank phrases its alerts
 * slightly differently, so you may need to tweak parseTransaction() too.
 */
class BankNotificationListener : NotificationListenerService() {

    companion object {
        val BANK_PACKAGES = setOf(
            "co.uk.getmondo" // Monzo
            // "com.barclays.android.barclaysmobilebanking", // Barclays
            // "com.starlingbank.android",                    // Starling
        )

        // Monzo phrases alerts like:
        //   "£4.20 at Pret A Manger"
        //   "You spent £4.20 at PRET A MANGER"
        //   "£12.00 to Uber"
        private val AMOUNT_REGEX = Regex("""£\s?(\d+(?:\.\d{1,2})?)""")
        private val MERCHANT_REGEX = Regex(
            """(?:at|to|@)\s+([A-Za-z0-9 .*&'’\-]{2,40})""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        if (sbn.packageName !in BANK_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val combined = "$title $text".trim()

        // Ignore non-spend notifications (balance alerts, top-ups, chat messages, etc.)
        if (AMOUNT_REGEX.find(combined) == null) return

        val parsed = parseTransaction(combined) ?: return

        val tx = Transaction(
            merchant = parsed.merchant,
            amount = parsed.amount,
            category = null,
            note = null,
            timestamp = System.currentTimeMillis(),
            source = "monzo"
        )

        CoroutineScope(Dispatchers.IO).launch {
            val id = AppDatabase.getInstance(applicationContext).transactionDao().insert(tx)
            launchCategorizeScreen(id, parsed.merchant, parsed.amount)
        }
    }

    private fun launchCategorizeScreen(id: Long, merchant: String, amount: Double) {
        val intent = Intent(this, CategorizeActivity::class.java).apply {
            putExtra(CategorizeActivity.EXTRA_TX_ID, id)
            putExtra(CategorizeActivity.EXTRA_MERCHANT, merchant)
            putExtra(CategorizeActivity.EXTRA_AMOUNT, amount)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private data class ParsedTx(val merchant: String, val amount: Double)

    private fun parseTransaction(text: String): ParsedTx? {
        val amount = AMOUNT_REGEX.find(text)?.groupValues?.get(1)?.toDoubleOrNull() ?: return null
        val merchant = MERCHANT_REGEX.find(text)?.groupValues?.get(1)?.trim()
            ?.replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "") // strip stray emoji
            ?.trim()
            ?.ifBlank { null }
            ?: "Unknown merchant"
        return ParsedTx(merchant, amount)
    }
}

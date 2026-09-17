package com.spendly.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val amount: Double,
    val category: String?,   // null until the user categorises it
    val note: String?,
    val timestamp: Long,
    val source: String = "manual" // "monzo" or "manual"
)

/** The fixed set of quick-pick categories shown in the bottom sheet. */
object Categories {
    val ALL = listOf(
        "Groceries", "Coffee & Eating out", "Transport",
        "Shopping", "Bills", "Other"
    )
}

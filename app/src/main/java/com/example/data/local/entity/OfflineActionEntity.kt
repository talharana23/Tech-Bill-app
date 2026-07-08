package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_actions")
data class OfflineActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String, // e.g. "CREATE_SALE"
    val payloadJson: String, // The JSON body
    val endpoint: String,
    val createdAt: Long = System.currentTimeMillis()
)

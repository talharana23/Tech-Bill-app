package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.TokenManager
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.OfflineActionEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.model.*
import com.example.data.remote.ApiService
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class SaaSDataRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val database: AppDatabase
) {

    private val inventoryDao = database.inventoryDao()
    private val saleDao = database.saleDao()
    private val offlineActionDao = database.offlineActionDao()
    private val moshi = RetrofitClient.moshi

    // --- State Flows from TokenManager ---
    fun getAccessTokenFlow(): Flow<String?> = tokenManager.accessToken
    fun getUserNameFlow(): Flow<String?> = tokenManager.userName
    fun getUserEmailFlow(): Flow<String?> = tokenManager.userEmail
    fun getOnlineSellingEnabledFlow(): Flow<Boolean> = tokenManager.onlineSellingEnabled
    fun getAppAccessEnabledFlow(): Flow<Boolean> = tokenManager.appAccessEnabled
    fun getCurrentPeriodEndFlow(): Flow<String?> = tokenManager.currentPeriodEnd
    fun getPushNotificationsEnabledFlow(): Flow<Boolean> = tokenManager.pushNotificationsEnabled

    // --- Offline-First Data Flows (from Room DB) ---
    fun getInventoryFlow(): Flow<List<InventoryItem>> {
        return inventoryDao.getAllInventory().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getSalesFlow(): Flow<List<SaleItem>> {
        val adapter = moshi.adapter<List<WarrantyDetails>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, WarrantyDetails::class.java)
        )
        return saleDao.getAllSales().map { list ->
            list.map { entity ->
                SaleItem(
                    id = entity.id,
                    createdAt = entity.createdAt,
                    customerName = entity.customerName,
                    customerType = entity.customerType,
                    paymentType = entity.paymentType,
                    totalAmount = entity.totalAmount,
                    courierStatus = entity.courierStatus,
                    payoutStatus = entity.payoutStatus,
                    codValue = entity.codValue,
                    type = entity.type,
                    items = entity.itemsJson?.let { adapter.fromJson(it) }
                )
            }
        }
    }

    fun getOnlineSalesFlow(): Flow<List<SaleItem>> {
        val adapter = moshi.adapter<List<WarrantyDetails>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, WarrantyDetails::class.java)
        )
        return saleDao.getOnlineSales().map { list ->
            list.map { entity ->
                SaleItem(
                    id = entity.id,
                    createdAt = entity.createdAt,
                    customerName = entity.customerName,
                    customerType = entity.customerType,
                    paymentType = entity.paymentType,
                    totalAmount = entity.totalAmount,
                    courierStatus = entity.courierStatus,
                    payoutStatus = entity.payoutStatus,
                    codValue = entity.codValue,
                    type = entity.type,
                    items = entity.itemsJson?.let { adapter.fromJson(it) }
                )
            }
        }
    }

    // --- Network Sync Methods ---
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            syncProfile()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProfile(): Result<UserInfo> {
        return try {
            val userInfo = apiService.getProfile()
            if (!userInfo.appAccessEnabled || userInfo.status.lowercase(Locale.ROOT) != "active") {
                logout()
                return Result.failure(IllegalStateException("App Access disabled or subscription inactive."))
            }

            tokenManager.saveUser(
                email = userInfo.email,
                name = userInfo.name,
                onlineSellingEnabled = userInfo.tenant?.onlineSellingEnabled ?: false,
                appAccessEnabled = userInfo.appAccessEnabled,
                currentPeriodEnd = userInfo.currentPeriodEnd
            )
            Result.success(userInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchInventoryFromServer(): Result<Unit> {
        return try {
            val items = apiService.getInventory(lowStock = false)
            val entities = items.map { InventoryEntity.fromDomainModel(it) }
            inventoryDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSalesFromServer(): Result<Unit> {
        return try {
            val sales = apiService.getSales()
            val adapter = moshi.adapter<List<WarrantyDetails>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, WarrantyDetails::class.java)
            )
            val entities = sales.map { sale ->
                SaleEntity(
                    id = sale.id,
                    createdAt = sale.createdAt,
                    customerName = sale.customerName,
                    customerType = sale.customerType,
                    paymentType = sale.paymentType,
                    totalAmount = sale.totalAmount,
                    courierStatus = sale.courierStatus,
                    payoutStatus = sale.payoutStatus,
                    codValue = sale.codValue,
                    type = sale.type,
                    itemsJson = sale.items?.let { adapter.toJson(it) }
                )
            }
            saleDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalesSummary(): Result<SalesSummaryResponse> {
        return try {
            val summary = apiService.getSalesSummary()
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Offline-First Write Actions ---
    suspend fun createSale(sale: SaleItem) {
        val adapter = moshi.adapter<List<WarrantyDetails>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, WarrantyDetails::class.java)
        )
        // 1. Save to local DB instantly
        val entity = SaleEntity(
            id = sale.id,
            createdAt = sale.createdAt,
            customerName = sale.customerName,
            customerType = sale.customerType,
            paymentType = sale.paymentType,
            totalAmount = sale.totalAmount,
            courierStatus = sale.courierStatus,
            payoutStatus = sale.payoutStatus,
            codValue = sale.codValue,
            type = sale.type,
            itemsJson = sale.items?.let { adapter.toJson(it) }
        )
        saleDao.insert(entity)

        // 2. Queue for background sync
        val saleJson = moshi.adapter(SaleItem::class.java).toJson(sale)
        offlineActionDao.insertAction(
            OfflineActionEntity(
                actionType = "CREATE_SALE",
                payloadJson = saleJson,
                endpoint = "sales"
            )
        )
        // WorkManager queueing will be handled by UI or an observer
    }

    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        tokenManager.setPushNotificationsEnabled(enabled)
    }

    suspend fun logout() {
        tokenManager.clearSession()
        inventoryDao.clearAll()
        saleDao.clearAll()
    }
}

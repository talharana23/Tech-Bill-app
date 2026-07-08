package com.example.data.repository

import com.example.data.local.TokenManager
import com.example.data.model.*
import com.example.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class SaaSDataRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    fun getAccessTokenFlow(): Flow<String?> = tokenManager.accessToken
    fun getUserNameFlow(): Flow<String?> = tokenManager.userName
    fun getUserEmailFlow(): Flow<String?> = tokenManager.userEmail
    fun getOnlineSellingEnabledFlow(): Flow<Boolean> = tokenManager.onlineSellingEnabled
    fun getAppAccessEnabledFlow(): Flow<Boolean> = tokenManager.appAccessEnabled
    fun getCurrentPeriodEndFlow(): Flow<String?> = tokenManager.currentPeriodEnd
    fun getPushNotificationsEnabledFlow(): Flow<Boolean> = tokenManager.pushNotificationsEnabled

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            // Perform profile sync immediately after login to populate user context
            syncProfile()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProfile(): Result<UserInfo> {
        return try {
            val userInfo = apiService.getProfile()
            
            // Authorization Guard Check:
            // If appAccessEnabled is false or status is not 'active', immediately clear session.
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

    suspend fun getLowStockItems(): Result<List<InventoryItem>> {
        return try {
            val items = apiService.getInventory(lowStock = true)
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentSales(limit: Int = 10): Result<List<SaleItem>> {
        return try {
            val sales = apiService.getSales(limit = limit)
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOnlineSales(): Result<List<SaleItem>> {
        return try {
            val sales = apiService.getSales(type = "online")
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        tokenManager.setPushNotificationsEnabled(enabled)
    }

    suspend fun logout() {
        tokenManager.clearSession()
    }
}

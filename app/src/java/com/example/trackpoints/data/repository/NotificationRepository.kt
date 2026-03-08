package com.example.trackpoints.data.repository

import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.utils.Utils.dbResponseHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface NotificationRepository {
    suspend fun getNotifications(): ApiResult<List<Notification>>
    suspend fun markCurrentNotificationsAsRead(latestNotification: Notification?): ApiResult<Unit>
}

class NotificationRepositoryImpl(
    private val auth: Auth,
    supabase: SupabaseClient
) : NotificationRepository {
    private val notificationsTable = supabase.from("notifications")

    override suspend fun getNotifications(): ApiResult<List<Notification>> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        val currentUserId = auth.currentUserOrNull()!!.id
        return dbResponseHandler {
            notificationsTable
                .select { filter { eq("user_id", currentUserId) } }
                .decodeList<Notification>()
        }
    }

    override suspend fun markCurrentNotificationsAsRead(latestNotification: Notification?): ApiResult<Unit> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        val currentUserId = auth.currentUserOrNull()!!.id

        return dbResponseHandler {
            val latestTimestamp = latestNotification?.createdAt ?: return@dbResponseHandler

            notificationsTable.update(
                buildJsonObject {
                    put("is_read", true)
                }
            ) {
                filter {
                    eq("user_id", currentUserId)
                    eq("is_read", false)
                    lte("created_at", latestTimestamp)
                }
            }
        }
    }
}
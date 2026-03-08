package com.example.trackpoints.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.model.Stats
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.utils.Utils.dbResponseHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.time.Instant
import java.util.UUID

interface UserRepository {
    suspend fun getCurrentUserProfile(): ApiResult<User>
    suspend fun getAdminStats(): ApiResult<Stats>
    suspend fun getClientFreelancerStats(): ApiResult<Stats>
    suspend fun approveOrRejectUserRequest(shouldAccept: Boolean, userId: String): ApiResult<Unit>
    suspend fun updateLastSeen(): ApiResult<Unit>
    suspend fun getUsers(): ApiResult<List<User>>
    suspend fun getFreelancers(): ApiResult<List<User>>
    suspend fun getUserById(id: String): ApiResult<User>
    suspend fun updateUserProfile(id: String, updates: JsonObject): ApiResult<User>
    suspend fun updateProfilePic(
        id: String,
        uri: Uri,
        context: Context
    ): ApiResult<Unit>

    suspend fun updatePortfolioPics(
        id: String,
        uris: List<Uri>,
        context: Context
    ): ApiResult<Unit>
}

class UserRepositoryImpl(
    private val auth: Auth,
    private val supabase: SupabaseClient
) : UserRepository {
    private val usersTable = supabase.from("users")

    override suspend fun getCurrentUserProfile(): ApiResult<User> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        val currentUserId = auth.currentUserOrNull()?.id
            ?: return ApiResult.Error("Not authenticated")

        return getUserById(currentUserId)
    }

    override suspend fun getAdminStats(): ApiResult<Stats> {
        return dbResponseHandler {
            supabase.postgrest.rpc(
                function = "get_admin_stats"
            ).decodeAs<Stats>()
        }
    }

    override suspend fun getClientFreelancerStats(): ApiResult<Stats> {
        return dbResponseHandler {
            val currentUserId = auth.currentUserOrNull()!!.id
            supabase.postgrest.rpc(
                function = "get_user_commission_stats",
                parameters = buildJsonObject {
                    put("input_user_id", currentUserId)
                }
            ).decodeAs<Stats>()
        }
    }

    override suspend fun approveOrRejectUserRequest(
        shouldAccept: Boolean,
        userId: String
    ): ApiResult<Unit> {
        return dbResponseHandler {
            usersTable.update(
                buildJsonObject {
                    put(if (shouldAccept) "is_approved" else "is_rejected", true)
                }
            ) {
                filter { eq("id", userId) }
            }
        }
    }

    override suspend fun updateLastSeen(): ApiResult<Unit> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        return dbResponseHandler {
            val currentUserId = auth.currentUserOrNull()!!.id

            usersTable.update(
                buildJsonObject {
                    put("last_seen", Instant.now().toString())
                }
            ) {
                filter { eq("id", currentUserId) }
            }
        }
    }

    override suspend fun getUsers(): ApiResult<List<User>> {
        return dbResponseHandler {
            usersTable
                .select {
                    filter {
                        neq("role", "null")
                    }
                }
                .decodeList<User>()
        }
    }

    override suspend fun getFreelancers(): ApiResult<List<User>> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        return dbResponseHandler {
            val currentUserId = auth.currentUserOrNull()!!.id
            supabase.postgrest.rpc(
                function = "get_freelancers_with_client_stats",
                parameters = buildJsonObject {
                    put("p_client_id", currentUserId)
                }
            ).decodeList<User>()
        }
    }

    override suspend fun getUserById(id: String): ApiResult<User> {
        return dbResponseHandler {
            usersTable
                .select { filter { eq("id", id) } }
                .decodeSingle<User>()
        }
    }

    override suspend fun updateUserProfile(
        id: String,
        updates: JsonObject
    ): ApiResult<User> {
        return dbResponseHandler {
            usersTable
                .update(updates)
                {
                    filter { eq("id", id) }
                    select()
                }
                .decodeSingle<User>()
        }
    }

    override suspend fun updateProfilePic(
        id: String,
        uri: Uri,
        context: Context
    ): ApiResult<Unit> {
        return dbResponseHandler {
            val user =
                supabase.from("users")
                    .select { filter { eq("id", id) } }.decodeSingle<User>()
            val mimeType = context.contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val path = "$id/profile_avatar/${UUID.randomUUID()}.$extension"
            val bucket = supabase.storage.from("user_attachments")

            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: throw Exception("Empty file")

            bucket.upload(
                path = path,
                data = bytes
            ) { upsert = true }

            val publicUrl = bucket.publicUrl(path)
            usersTable.update(
                buildJsonObject { put("photo", publicUrl) }
            ) {
                filter { eq("id", id) }
            }

            user.photo?.let { url ->
                try {
                    val pathToDelete = url.substringAfter("user_attachments/")

                    if (pathToDelete.isNotEmpty()) {
                        bucket.delete(pathToDelete)
                        Log.d("DELETE", "Successfully deleted $pathToDelete")
                    }
                } catch (e: Exception) {
                    Log.e("DELETE", "Failed to delete files: ${e.message}")
                }
            }
        }
    }

    override suspend fun updatePortfolioPics(
        id: String,
        uris: List<Uri>,
        context: Context
    ): ApiResult<Unit> {
        return dbResponseHandler {
            val user =
                supabase.from("users")
                    .select { filter { eq("id", id) } }.decodeSingle<User>()
            val bucket = supabase.storage.from("user_attachments")

            val uploadedUrls = coroutineScope {
                uris.map { uri ->
                    async {
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val extension =
                            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
                        val fileName = "portfolio/${UUID.randomUUID()}.$extension"
                        val path = "$id/$fileName"

                        val bytes =
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }

                        if (bytes != null) {
                            bucket.upload(path, bytes)
                            bucket.publicUrl(path)
                        } else null
                    }
                }.awaitAll().filterNotNull()
            }

            if (uploadedUrls.isNotEmpty()) {
                supabase.from("users").update(
                    buildJsonObject {
                        putJsonArray("portfolio") {
                            uploadedUrls.forEach { add(it) }
                        }
                    }
                ) {
                    filter { eq("id", id) }
                }
            }

            user.portfolio?.let { urls ->
                try {
                    val pathsToDelete = urls.map { url ->
                        url.substringAfter("user_attachments/")
                    }

                    if (pathsToDelete.isNotEmpty()) {
                        bucket.delete(pathsToDelete)
                        Log.d("DELETE", "Successfully deleted ${pathsToDelete.size} files")
                    }
                } catch (e: Exception) {
                    Log.e("DELETE", "Failed to delete files: ${e.message}")
                }
            }
        }
    }
}
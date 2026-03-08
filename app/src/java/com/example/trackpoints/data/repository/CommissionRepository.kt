package com.example.trackpoints.data.repository

import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionRequest
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.utils.Utils.dbResponseHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

interface CommissionRepository {
    suspend fun checkDeadlines(): ApiResult<Unit>
    suspend fun createCommission(newCommission: CommissionRequest): ApiResult<Unit>
    suspend fun getCommissions(currentUserId: String): ApiResult<List<Commission>>
    suspend fun updateCommission(commissionId: String, rating: Int): ApiResult<Unit>
    suspend fun markCommissionRequest(commissionId: String, shouldAccept: Boolean): ApiResult<Unit>
}

class CommissionRepositoryImpl(
    private val auth: Auth,
    private val supabase: SupabaseClient
) : CommissionRepository {
    private val commissionsTable = supabase.from("commissions")
    override suspend fun checkDeadlines(): ApiResult<Unit> {
        return dbResponseHandler {
            supabase.postgrest.rpc(
                function = "check_upcoming_deadlines",
            )
        }
    }

    override suspend fun createCommission(newCommission: CommissionRequest): ApiResult<Unit> {
        return dbResponseHandler {
            commissionsTable.insert(newCommission)
        }
    }

    override suspend fun getCommissions(currentUserId: String): ApiResult<List<Commission>> {
        return dbResponseHandler {
            supabase.postgrest.rpc(
                function = "get_commissions_with_details",
                parameters = buildJsonObject {
                    put("p_user_id", currentUserId)
                }
            ).decodeAs<List<Commission>>()
        }
    }

    override suspend fun updateCommission(commissionId: String, rating: Int): ApiResult<Unit> {
        return dbResponseHandler {
            commissionsTable.update(
                buildJsonObject {
                    put("status", "DONE")
                    put("date_paid", Instant.now().toString())
                    put("rating", rating)
                }
            ) {
                filter { eq("id", commissionId) }
            }
        }
    }

    override suspend fun markCommissionRequest(
        commissionId: String,
        shouldAccept: Boolean
    ): ApiResult<Unit> {
        return dbResponseHandler {
            commissionsTable.update(
                buildJsonObject {
                    put("status", if (shouldAccept) "IN_PROGRESS" else "REJECTED")
                }
            ) {
                filter { eq("id", commissionId) }
            }
        }
    }

}
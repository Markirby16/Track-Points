package com.example.trackpoints.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.trackpoints.data.model.InboxItem
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.utils.Utils.dbResponseHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.UUID

interface MessageRepository {
    suspend fun markMessagesAsRead(commissionId: String): ApiResult<Unit>
    suspend fun getInbox(): ApiResult<List<InboxItem>>
    suspend fun getChatHistory(commissionId: String): ApiResult<List<Message>>
    suspend fun getRealtimeMessages(otherId: String): ApiResult<Flow<Message>>
    suspend fun sendMessage(newMessage: Message): ApiResult<Unit>
    suspend fun uploadImage(
        commissionId: String,
        uri: Uri,
        context: Context
    ): ApiResult<String>
}

class MessageRepositoryImpl(
    private val auth: Auth,
    private val supabase: SupabaseClient
) : MessageRepository {
    private val messagesTable = supabase.from("messages")

    override suspend fun markMessagesAsRead(commissionId: String): ApiResult<Unit> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }

        return dbResponseHandler {
            val currentUserId = auth.currentUserOrNull()?.id!!

            messagesTable.update(
                update = {
                    set("read_at", true)
                }
            ) {
                filter {
                    eq("commission_id", commissionId)
                    eq("receiver_id", currentUserId)
                    eq("read_at", false)
                }
            }
        }
    }

    override suspend fun getInbox(): ApiResult<List<InboxItem>> {
        if (auth.currentUserOrNull() == null) {
            return ApiResult.Error("User not authenticated")
        }
        return dbResponseHandler {
            val currentId = auth.currentUserOrNull()!!.id
            messagesTable.postgrest.rpc(
                function = "get_my_inbox",
                parameters = mapOf("my_id" to currentId)
            ).decodeList<InboxItem>()
        }
    }

    override suspend fun getChatHistory(commissionId: String): ApiResult<List<Message>> {
        return dbResponseHandler {
            messagesTable
                .select {
                    filter {
                        eq("commission_id", commissionId)
                    }
                    order("created_at", order = Order.ASCENDING)
                }.decodeList<Message>()
        }
    }

    override suspend fun getRealtimeMessages(otherId: String): ApiResult<Flow<Message>> {
        return dbResponseHandler {
            val currentId = auth.currentUserOrNull()!!.id
            val channel = supabase.realtime.channel("chat_$otherId")

            val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "messages"
            }

            channel.subscribe()

            changeFlow.map { it.decodeRecord<Message>() }
                .filter { msg ->
                    (msg.senderId == otherId && msg.receiverId == currentId) ||
                            (msg.senderId == currentId && msg.receiverId == otherId)
                }
        }
    }

    override suspend fun sendMessage(newMessage: Message): ApiResult<Unit> {
        return dbResponseHandler {
            messagesTable.insert(newMessage)
        }
    }

    override suspend fun uploadImage(
        commissionId: String,
        uri: Uri,
        context: Context
    ): ApiResult<String> {
        return dbResponseHandler {
            val mimeType = context.contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val fileName = "${UUID.randomUUID()}.$extension"

            val path = "$commissionId/$fileName"

            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: throw Exception("Empty file")

            val bucket = supabase.storage.from("commission_attachments")
            bucket.upload(
                path = path,
                data = bytes
            )

            bucket.publicUrl(path)
        }
    }

}
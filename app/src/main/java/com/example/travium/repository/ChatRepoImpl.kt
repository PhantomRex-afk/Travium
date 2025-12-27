package com.example.travium.repository

import com.example.travium.model.ChatListItemModel
import com.example.travium.model.ChatMessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatRepoImpl : ChatRepo {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun getChatList(
        currentUserId: String,
        callback: (List<ChatListItemModel>) -> Unit
    ) {
        db.child("chatList").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ChatListItemModel>()

                    for (child in snapshot.children) {
                        val item = child.getValue(ChatListItemModel::class.java)
                        item?.let { list.add(it) }
                    }

                    // Latest chat first
                    callback(list.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getMessages(
        chatId: String,
        callback: (List<ChatMessageModel>) -> Unit
    ) {
        db.child("chats")
            .child(chatId)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<ChatMessageModel>()

                    for (child in snapshot.children) {
                        val msg = child.getValue(ChatMessageModel::class.java)
                        msg?.let { messages.add(it) }
                    }

                    // Oldest → newest
                    callback(messages.sortedBy { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        message: String
    ) {
        val msgId = db
            .child("chats")
            .child(chatId)
            .child("messages")
            .push()
            .key ?: return

        val msg = ChatMessageModel(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        db
            .child("chats")
            .child(chatId)
            .child("messages")
            .child(msgId)
            .setValue(msg)
    }
}
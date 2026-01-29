package com.example.travium.repository

import com.example.travium.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")
    private val bannedDb = FirebaseDatabase.getInstance().getReference("banned_users")
    private val followersRef = FirebaseDatabase.getInstance().getReference("followers")
    private val followingRef = FirebaseDatabase.getInstance().getReference("following")

    override fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    callback(true, userId, "Registration Successful")
                } else {
                    callback(false, "", task.exception?.message ?: "Registration Failed")
                }
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    
                    // Essential Check: Verify if user is banned immediately after auth success
                    bannedDb.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                auth.signOut() // Kill the session
                                callback(false, "ACCESS DENIED: Your account has been banned.")
                            } else {
                                // Double Check: Ensure they actually exist in the active users node
                                db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (userSnapshot.exists()) {
                                            callback(true, "Login Successfully")
                                        } else {
                                            auth.signOut()
                                            callback(false, "Account error. Please register again.")
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        callback(false, error.message)
                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(false, error.message)
                        }
                    })
                } else {
                    callback(false, task.exception?.message ?: "Login Failed")
                }
            }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Link sent $email")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun changePassword(newPassword: String, callback: (Boolean, String) -> Unit) {
        auth.currentUser?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password changed successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to change password")
                }
            } ?: callback(false, "User not logged in")
    }

    override fun followUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit) {
        followingRef.child(currentUserId).child(targetUserId).setValue(true)
            .addOnSuccessListener {
                followersRef.child(targetUserId).child(currentUserId).setValue(true)
                    .addOnSuccessListener { callback(true, "Followed successfully") }
                    .addOnFailureListener { callback(false, it.message ?: "Failed to update followers") }
            }
            .addOnFailureListener { callback(false, it.message ?: "Failed to update following") }
    }

    override fun unfollowUser(currentUserId: String, targetUserId: String, callback: (Boolean, String) -> Unit) {
        followingRef.child(currentUserId).child(targetUserId).removeValue()
            .addOnSuccessListener {
                followersRef.child(targetUserId).child(currentUserId).removeValue()
                    .addOnSuccessListener { callback(true, "Unfollowed successfully") }
                    .addOnFailureListener { callback(false, it.message ?: "Failed to update followers") }
            }
            .addOnFailureListener { callback(false, it.message ?: "Failed to update following") }
    }

    override fun getFollowersCount(userId: String, callback: (Long) -> Unit) {
        followersRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }

    override fun getFollowingCount(userId: String, callback: (Long) -> Unit) {
        followingRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }

    override fun isFollowing(currentUserId: String, targetUserId: String, callback: (Boolean) -> Unit) {
        followingRef.child(currentUserId).child(targetUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        db.child(userId).setValue(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User data stored successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to store user data")
                }
            }
    }

    override fun getUserById(userId: String, callback: (UserModel?) -> Unit) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    override fun getAllUsers(callback: (List<UserModel>) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserModel>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)
                    user?.let { userList.add(it) }
                }
                callback(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    override fun banUser(userId: String, callback: (Boolean, String) -> Unit) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    bannedDb.child(userId).setValue(user).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            db.child(userId).removeValue().addOnCompleteListener { removeTask ->
                                if (removeTask.isSuccessful) {
                                    callback(true, "User banned successfully")
                                } else {
                                    callback(false, "Failed to remove user from active list")
                                }
                            }
                        } else {
                            callback(false, "Failed to add user to banned list")
                        }
                    }
                } else {
                    callback(false, "User not found")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun unbanUser(userId: String, callback: (Boolean, String) -> Unit) {
        bannedDb.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    db.child(userId).setValue(user).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            bannedDb.child(userId).removeValue().addOnCompleteListener { removeTask ->
                                if (removeTask.isSuccessful) {
                                    callback(true, "User unbanned successfully")
                                } else {
                                    callback(false, "Failed to remove user from banned list")
                                }
                            }
                        } else {
                            callback(false, "Failed to restore user to active list")
                        }
                    }
                } else {
                    callback(false, "Banned user not found")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun getBannedUsers(callback: (List<UserModel>) -> Unit) {
        bannedDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserModel>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)
                    user?.let { userList.add(it) }
                }
                callback(userList)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
    override fun searchUsers(query: String, callback: (List<UserModel>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.orderByChild("username")
            .startAt(query.lowercase())
            .endAt(query.lowercase() + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = mutableListOf<UserModel>()
                for (child in snapshot.children) {
                    val user = child.getValue(UserModel::class.java)
                    user?.let {
                        // Also check fullName if you want to search by both username and fullName
                        if (it.username.contains(query, ignoreCase = true) ||
                            it.fullName.contains(query, ignoreCase = true)) {
                            users.add(it)
                        }
                    }
                }
                callback(users)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}

package com.example.travium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travium.model.UserModel
import com.example.travium.repository.GroupChatRepo
import com.example.travium.repository.UserRepo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreateGroupChatViewModel : ViewModel() {

    // Repositories (to be initialized externally)
    private var groupChatRepo: GroupChatRepo? = null
    private var userRepo: UserRepo? = null

    // UI State
    private val _uiState = MutableStateFlow<CreateGroupUiState>(CreateGroupUiState.Idle)
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _availableContacts = MutableStateFlow<List<UserModel>>(emptyList())
    val availableContacts: StateFlow<List<UserModel>> = _availableContacts.asStateFlow()

    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts.asStateFlow()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _groupImage = MutableStateFlow<String?>(null)
    val groupImage: StateFlow<String?> = _groupImage.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0.0)
    val uploadProgress: StateFlow<Double> = _uploadProgress.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentUserInfo = MutableStateFlow<UserModel?>(null)

    // Initialize repositories
    fun initRepositories(groupChatRepo: GroupChatRepo, userRepo: UserRepo) {
        this.groupChatRepo = groupChatRepo
        this.userRepo = userRepo
    }

    // Check if repositories are initialized
    private fun areRepositoriesInitialized(): Boolean {
        return groupChatRepo != null && userRepo != null
    }

    // Load current user information
    fun loadCurrentUser(userId: String) {
        if (!areRepositoriesInitialized()) {
            _uiState.value = CreateGroupUiState.Error("Repositories not initialized")
            return
        }

        _currentUserId.value = userId

        userRepo?.getUserById(userId) { user ->
            _currentUserInfo.value = user
        }
    }

    // Load users for group creation
    fun loadAvailableContacts() {
        if (!areRepositoriesInitialized()) {
            _uiState.value = CreateGroupUiState.Error("Repositories not initialized")
            return
        }

        val currentUserId = _currentUserId.value
        if (currentUserId == null) {
            _uiState.value = CreateGroupUiState.Error("User not logged in. Please set current user ID first.")
            return
        }

        _uiState.value = CreateGroupUiState.Loading

        userRepo?.getAllUsers { success, message, users ->
            if (success && users != null) {
                // Filter out current user
                val otherUsers = users.filter { it.userId != currentUserId }
                _availableContacts.value = otherUsers
                _uiState.value = CreateGroupUiState.Success
            } else {
                _uiState.value = CreateGroupUiState.Error("Failed to load users: ${message ?: "Unknown error"}")
                _availableContacts.value = emptyList()
            }
        }
    }

    // Alternative: Load only users with follow relationships
    fun loadFollowContacts() {
        if (!areRepositoriesInitialized()) {
            _uiState.value = CreateGroupUiState.Error("Repositories not initialized")
            return
        }

        val currentUserId = _currentUserId.value
        if (currentUserId == null) {
            _uiState.value = CreateGroupUiState.Error("User not logged in")
            return
        }

        _uiState.value = CreateGroupUiState.Loading

        userRepo?.getAllUsers { success, message, allUsers ->
            if (!success || allUsers == null) {
                _uiState.value = CreateGroupUiState.Error("Failed to load users: ${message ?: "Unknown error"}")
                return@getAllUsers
            }

            val otherUsers = allUsers.filter { it.userId != currentUserId }
            if (otherUsers.isEmpty()) {
                _availableContacts.value = emptyList()
                _uiState.value = CreateGroupUiState.Success
                return@getAllUsers
            }

            val filteredContacts = mutableListOf<UserModel>()
            val checksCompleted = mutableListOf<Boolean>()

            otherUsers.forEach { user ->
                // Check if there's any follow relationship
                userRepo?.isFollowing(currentUserId, user.userId) { isFollowing ->
                    userRepo?.isFollowing(user.userId, currentUserId) { isFollower ->
                        if (isFollowing || isFollower) {
                            filteredContacts.add(user)
                        }

                        checksCompleted.add(true)

                        if (checksCompleted.size == otherUsers.size) {
                            _availableContacts.value = filteredContacts
                            _uiState.value = CreateGroupUiState.Success
                        }
                    }
                }
            }
        }
    }

    fun toggleContactSelection(userId: String) {
        val currentSelected = _selectedContacts.value.toMutableSet()
        if (currentSelected.contains(userId)) {
            currentSelected.remove(userId)
        } else {
            currentSelected.add(userId)
        }
        _selectedContacts.value = currentSelected
    }

    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun setGroupImage(imageUrl: String?) {
        _groupImage.value = imageUrl
    }

    fun createGroup() {
        if (!areRepositoriesInitialized()) {
            _uiState.value = CreateGroupUiState.Error("Repositories not initialized")
            return
        }

        val currentUserId = _currentUserId.value
        val currentUserInfo = _currentUserInfo.value

        if (currentUserId == null) {
            _uiState.value = CreateGroupUiState.Error("User information not available")
            return
        }

        if (_groupName.value.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Group name cannot be empty")
            return
        }

        if (_selectedContacts.value.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Select at least one contact")
            return
        }

        // Get selected user details
        val selectedUsers = _availableContacts.value.filter { _selectedContacts.value.contains(it.userId) }
        if (selectedUsers.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("No contacts selected")
            return
        }

        // Prepare data for group creation
        val members = selectedUsers.map { it.userId } + currentUserId
        val memberNames = selectedUsers.map { it.fullName.ifEmpty { "User" } } +
                (currentUserInfo?.fullName?.takeIf { it.isNotEmpty() } ?: "User")
        val memberPhotos = selectedUsers.mapNotNull { it.profileImageUrl } +
                (_groupImage.value ?: currentUserInfo?.profileImageUrl ?: "")

        _uiState.value = CreateGroupUiState.Loading

        groupChatRepo?.createGroup(
            groupName = _groupName.value,
            groupImage = _groupImage.value ?: "",
            createdBy = currentUserId,
            createdByName = currentUserInfo?.fullName?.takeIf { it.isNotEmpty() } ?: "User",
            members = members,
            memberNames = memberNames,
            memberPhotos = memberPhotos,
            callback = { result ->
                result.fold(
                    onSuccess = { groupId ->
                        _uiState.value = CreateGroupUiState.GroupCreated(groupId)
                        resetForm()
                    },
                    onFailure = { error ->
                        _uiState.value = CreateGroupUiState.Error(
                            error.message ?: "Failed to create group"
                        )
                    }
                )
            }
        )
    }

    fun createGroupWithSelectedUsers(
        groupName: String,
        groupImage: String?,
        selectedUserIds: List<String>,
        onComplete: (Result<String>) -> Unit
    ) {
        if (!areRepositoriesInitialized()) {
            onComplete(Result.failure(Exception("Repositories not initialized")))
            return
        }

        val currentUserId = _currentUserId.value
        val currentUserInfo = _currentUserInfo.value

        if (currentUserId == null) {
            onComplete(Result.failure(Exception("User not logged in")))
            return
        }

        // Get selected user details
        val selectedUsers = _availableContacts.value.filter { selectedUserIds.contains(it.userId) }
        if (selectedUsers.isEmpty()) {
            onComplete(Result.failure(Exception("No users selected")))
            return
        }

        // Prepare data
        val members = selectedUsers.map { it.userId } + currentUserId
        val memberNames = selectedUsers.map { it.fullName.ifEmpty { "User" } } +
                (currentUserInfo?.fullName?.takeIf { it.isNotEmpty() } ?: "User")
        val memberPhotos = selectedUsers.mapNotNull { it.profileImageUrl } +
                (groupImage ?: currentUserInfo?.profileImageUrl ?: "")

        groupChatRepo?.createGroup(
            groupName = groupName,
            groupImage = groupImage ?: "",
            createdBy = currentUserId,
            createdByName = currentUserInfo?.fullName?.takeIf { it.isNotEmpty() } ?: "User",
            members = members,
            memberNames = memberNames,
            memberPhotos = memberPhotos,
            callback = { result ->
                onComplete(result)
            }
        )
    }

    fun validateGroupCreation(): ValidationResult {
        if (!areRepositoriesInitialized()) {
            return ValidationResult.Error("Repositories not initialized")
        }

        val currentUserId = _currentUserId.value

        return when {
            currentUserId == null -> ValidationResult.Error("User not logged in")
            _groupName.value.isEmpty() -> ValidationResult.Error("Group name cannot be empty")
            _selectedContacts.value.isEmpty() -> ValidationResult.Error("Select at least one contact")
            else -> ValidationResult.Valid
        }
    }

    fun clearError() {
        if (_uiState.value is CreateGroupUiState.Error) {
            _uiState.value = CreateGroupUiState.Idle
        }
    }

    fun clearSelection() {
        _selectedContacts.value = emptySet()
    }

    fun resetForm() {
        _groupName.value = ""
        _groupImage.value = null
        _selectedContacts.value = emptySet()
    }

    fun getSelectedUsersCount(): Int {
        return _selectedContacts.value.size
    }

    fun getSelectedUsers(): List<UserModel> {
        return _availableContacts.value.filter { _selectedContacts.value.contains(it.userId) }
    }

    // Helper method to check if a user is in follow relationship with current user
    fun checkFollowRelationship(targetUserId: String, callback: (Boolean) -> Unit) {
        if (!areRepositoriesInitialized()) {
            callback(false)
            return
        }

        val currentUserId = _currentUserId.value
        if (currentUserId == null) {
            callback(false)
            return
        }

        userRepo?.isFollowing(currentUserId, targetUserId, callback)
    }

    // Helper method to follow/unfollow users
    fun followUser(targetUserId: String, callback: (Boolean, String) -> Unit) {
        if (!areRepositoriesInitialized()) {
            callback(false, "Repositories not initialized")
            return
        }

        val currentUserId = _currentUserId.value
        if (currentUserId == null) {
            callback(false, "User not logged in")
            return
        }

        userRepo?.followUser(currentUserId, targetUserId, callback)
    }

    fun unfollowUser(targetUserId: String, callback: (Boolean, String) -> Unit) {
        if (!areRepositoriesInitialized()) {
            callback(false, "Repositories not initialized")
            return
        }

        val currentUserId = _currentUserId.value
        if (currentUserId == null) {
            callback(false, "User not logged in")
            return
        }

        userRepo?.unfollowUser(currentUserId, targetUserId, callback)
    }
}

sealed class CreateGroupUiState {
    object Idle : CreateGroupUiState()
    object Loading : CreateGroupUiState()
    object Uploading : CreateGroupUiState()
    object Success : CreateGroupUiState()
    data class Error(val message: String) : CreateGroupUiState()
    data class GroupCreated(val groupId: String) : CreateGroupUiState()
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
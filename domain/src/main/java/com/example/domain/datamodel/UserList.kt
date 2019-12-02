package com.example.domain.datamodel

import com.example.domain.constants.RemoteRepositoryConstants

/**
 * ATTENTION!
 * Field names need to match the constants in [RemoteRepositoryConstants].
 *
 * @param id Generated by Firebase,
 * thus it is not known when a UserList is first created.
 */
data class UserList(val title: String,
                    val position: Int,
                    val id: String = "") {
    /**
     * Required by Firebase Firestore.
     */
    constructor() : this("", 0)
}
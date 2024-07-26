package org.apps.flexmed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = " ",
    val email: String = " ",
    val displayName: String = " ",
    val username: String = " ",
    val password: String = " ",
    val image: String? = null,
    val bio: String? = null
): Parcelable

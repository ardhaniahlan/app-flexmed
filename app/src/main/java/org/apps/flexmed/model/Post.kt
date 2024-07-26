package org.apps.flexmed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post (
    val id: String? = null,
    val image: String? = null,
    val caption: String? = null,
    val userId: String? = null,
    val imgUser: String? = null,
    val displayName: String? = null,
    var isLiked: Boolean = false,
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
): Parcelable
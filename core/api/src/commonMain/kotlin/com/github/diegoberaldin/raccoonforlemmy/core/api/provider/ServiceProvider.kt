package com.github.diegoberaldin.raccoonforlemmy.core.api.provider

import com.github.diegoberaldin.raccoonforlemmy.core.api.service.AuthService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.CommentService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.CommunityService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.ModlogService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.PostService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.PrivateMessageService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.SearchService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.SiteService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.UserService

interface ServiceProvider {
    val currentInstance: String
    val auth: AuthService
    val post: PostService
    val community: CommunityService
    val user: UserService
    val site: SiteService
    val comment: CommentService
    val search: SearchService
    val privateMessages: PrivateMessageService
    val modLog: ModlogService

    fun changeInstance(value: String)
}

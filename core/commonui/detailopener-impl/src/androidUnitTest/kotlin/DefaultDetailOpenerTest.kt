import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.DetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.impl.DefaultDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailScreen
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultDetailOpenerTest {
    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val communityRepository = mockk<CommunityRepository>()

    private val identityRepository = mockk<IdentityRepository>()

    private val lemmyItemCache = mockk<LemmyItemCache>(relaxed = true)

    private val navigationCoordinator = mockk<NavigationCoordinator>(relaxed = true)

    private val sut: DetailOpener =
        DefaultDetailOpener(
            navigationCoordinator = navigationCoordinator,
            itemCache = lemmyItemCache,
            identityRepository = identityRepository,
            communityRepository = communityRepository,
        )

    @Test
    fun whenOpenCommunityDetailOnSameInstance_thenNavigatesAccordingly() =
        runTest {
            val community = CommunityModel(name = "test", id = 1)

            sut.openCommunityDetail(community)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putCommunity(community)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<CommunityDetailScreen>(it)
                    },
                )
                identityRepository wasNot Called
                communityRepository wasNot Called
            }
        }

    @Test
    fun whenOpenCommunityDetailOnDifferentInstance_thenNavigatesAccordingly() =
        runTest {
            val token = "token"
            val communityName = "test"
            val otherInstance = "otherInstance"
            val community = CommunityModel(name = communityName, id = 1, host = otherInstance)
            every { identityRepository.authToken } returns MutableStateFlow(token)
            coEvery {
                communityRepository.search(
                    query = any(),
                    auth = any(),
                    page = any(),
                    limit = any(),
                    listingType = any(),
                    sortType = any(),
                    resultType = any(),
                )
            } returns listOf(SearchResult.Community(community))

            sut.openCommunityDetail(community, otherInstance)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putCommunity(community)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<CommunityDetailScreen>(it)
                    },
                )
                communityRepository.search(
                    query = communityName,
                    auth = token,
                    page = any(),
                    limit = any(),
                    listingType = ListingType.All,
                    sortType = any(),
                    resultType = SearchResultType.Communities,
                )
            }
        }

    @Test
    fun whenOpenUserDetail_thenNavigatesAccordingly() =
        runTest {
            val user = UserModel(name = "test", id = 1)

            sut.openUserDetail(user)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putUser(user)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<UserDetailScreen>(it)
                    },
                )
            }
        }

    @Test
    fun whenOpenPostDetail_thenNavigatesAccordingly() =
        runTest {
            val post = PostModel(title = "test", id = 1)

            sut.openPostDetail(post)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putPost(post)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<PostDetailScreen>(it)
                    },
                )
            }
        }

    @Test
    fun whenOpenReplyToPost_thenNavigatesAccordingly() =
        runTest {
            val post = PostModel(title = "test", id = 1)

            sut.openReply(originalPost = post)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putPost(post)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<CreateCommentScreen>(it)
                    },
                )
            }
        }

    @Test
    fun whenOpenReplyToComment_thenNavigatesAccordingly() =
        runTest {
            val comment = CommentModel(text = "test", id = 1)

            sut.openReply(originalComment = comment)
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                lemmyItemCache.putComment(comment)
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<CreateCommentScreen>(it)
                    },
                )
            }
        }

    @Test
    fun whenOpenCreatePost_thenNavigatesAccordingly() =
        runTest {
            sut.openCreatePost()
            advanceTimeBy(OPEN_DELAY)

            coVerify {
                navigationCoordinator.pushScreen(
                    withArg {
                        assertIs<CreatePostScreen>(it)
                    },
                )
            }
        }

    companion object {
        private val OPEN_DELAY = 1.seconds
    }
}

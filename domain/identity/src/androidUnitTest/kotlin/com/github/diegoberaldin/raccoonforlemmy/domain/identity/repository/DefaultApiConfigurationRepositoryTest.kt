package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultApiConfigurationRepositoryTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val serviceProvider = mockk<ServiceProvider>(relaxUnitFun = true) {
        every { currentInstance } returns "lemmy.world"
    }
    private val keyStore = mockk<TemporaryKeyStore>(relaxUnitFun = true) {
        every { get(any(), any<String>()) } returns ""
    }
    private val sut = DefaultApiConfigurationRepository(
        serviceProvider = serviceProvider,
        keyStore = keyStore,
    )

    @Test
    fun whenChangeInstance_thenValueIsUpdated() {
        val resBefore = sut.instance.value
        assertEquals("", resBefore)

        sut.changeInstance("feddit.it")

        verify {
            serviceProvider.changeInstance("feddit.it")
            keyStore.save(any(), "feddit.it")
        }
    }
}

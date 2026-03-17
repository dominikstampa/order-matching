package de.ordermatching.model

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class NodeTest {

    @MockK
    lateinit var serviceProvider: LogisticsServiceProvider

    val node = Node(
        position = GeoPosition(1.0, 1.0),
        transferPoint = null,
        lspOwner = null,
        type = NodeType.END,
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `test node not equal with null`() {
        assertFalse(node.equals(null))
    }

    @Test
    fun `test transfer points not equal with other position`() {
        val other = Node(
            position = GeoPosition(1.1, 1.0),
            lspOwner = null,
            transferPoint = null,
            type = NodeType.END,
        )
        assertNotEquals(node, other)
    }

    @Test
    fun `test transfer points not equal with other type`() {
        val other = Node(
            position = GeoPosition(1.0, 1.0),
            lspOwner = null,
            transferPoint = null,
            type = NodeType.NEUTRAL
        )
        assertNotEquals(node, other)
    }

    @Test
    fun `test transfer points not equal with other lsp owner`() {
        every { serviceProvider.name } returns "test"
        val other = Node(
            position = GeoPosition(1.0, 1.0),
            lspOwner = serviceProvider,
            transferPoint = null,
            type = NodeType.END
        )
        assertNotEquals(node, other)
    }

    @Test
    fun `test transfer points equal same object`() {
        assertEquals(node, node)
    }

    @Test
    fun `test transfer points equal`() {
        val other = Node(
            position = GeoPosition(1.0, 1.0),
            transferPoint = null,
            lspOwner = null,
            type = NodeType.END,
        )
        assertEquals(node, other)
    }
}
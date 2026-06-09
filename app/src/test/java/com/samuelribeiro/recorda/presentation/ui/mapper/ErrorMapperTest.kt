package com.samuelribeiro.recorda.presentation.ui.mapper

import android.content.Context
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.network.NetworkError
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ErrorMapperTest {

    private val context: Context = mockk()

    @Before
    fun setUp() {
        every { context.getString(any()) } returns "fallback"
        every { context.getString(any(), any()) } returns "fallback"
    }

    @Test
    fun `NoInternet maps to error_no_internet resource`() {
        every { context.getString(R.string.error_no_internet) } returns "Sem conexão com a internet."

        val result = NetworkError.NoInternet().asUserMessage(context)

        assertEquals("Sem conexão com a internet.", result)
        verify { context.getString(R.string.error_no_internet) }
    }

    @Test
    fun `Timeout maps to error_timeout resource`() {
        every { context.getString(R.string.error_timeout) } returns "A requisição expirou."

        val result = NetworkError.Timeout().asUserMessage(context)

        assertEquals("A requisição expirou.", result)
        verify { context.getString(R.string.error_timeout) }
    }

    @Test
    fun `EmptyResponse maps to error_empty_response resource`() {
        every { context.getString(R.string.error_empty_response) } returns "Resposta vazia."

        val result = NetworkError.EmptyResponse().asUserMessage(context)

        assertEquals("Resposta vazia.", result)
        verify { context.getString(R.string.error_empty_response) }
    }

    @Test
    fun `HttpError maps to error_http_generic resource with code`() {
        every { context.getString(R.string.error_http_generic, 404) } returns "Erro HTTP 404."

        val result = NetworkError.HttpError(404, "Not Found").asUserMessage(context)

        assertEquals("Erro HTTP 404.", result)
        verify { context.getString(R.string.error_http_generic, 404) }
    }

    @Test
    fun `unknown error maps to error_unknown resource`() {
        every { context.getString(R.string.error_unknown) } returns "Erro desconhecido."

        val result = RuntimeException("unexpected").asUserMessage(context)

        assertEquals("Erro desconhecido.", result)
        verify { context.getString(R.string.error_unknown) }
    }
}

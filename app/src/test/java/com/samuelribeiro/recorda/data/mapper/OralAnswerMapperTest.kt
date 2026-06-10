package com.samuelribeiro.recorda.data.mapper

import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import org.junit.Test
import kotlin.test.assertEquals

class OralAnswerMapperTest {

    private val mapper = OralAnswerMapper()

    @Test
    fun `parses exact format with CORRECT verdict`() {
        val result = mapper.toEvaluation("VEREDITO: CORRECT | FEEDBACK: Muito bem, resposta correta!")

        assertEquals(OralAnswerVerdict.CORRECT, result.verdict)
        assertEquals("Muito bem, resposta correta!", result.feedback)
    }

    @Test
    fun `parses exact format with PARTIAL verdict`() {
        val result = mapper.toEvaluation("VEREDITO: PARTIAL | FEEDBACK: Quase lá, faltou um detalhe.")

        assertEquals(OralAnswerVerdict.PARTIAL, result.verdict)
        assertEquals("Quase lá, faltou um detalhe.", result.feedback)
    }

    @Test
    fun `parses exact format with INCORRECT verdict`() {
        val result = mapper.toEvaluation("VEREDITO: INCORRECT | FEEDBACK: Não foi dessa vez.")

        assertEquals(OralAnswerVerdict.INCORRECT, result.verdict)
        assertEquals("Não foi dessa vez.", result.feedback)
    }

    @Test
    fun `unrecognized verdict defaults to PARTIAL`() {
        val result = mapper.toEvaluation("VEREDITO: MAYBE | FEEDBACK: Resposta ambígua.")

        assertEquals(OralAnswerVerdict.PARTIAL, result.verdict)
        assertEquals("Resposta ambígua.", result.feedback)
    }

    @Test
    fun `text without VEREDITO defaults to PARTIAL and uses raw text as feedback`() {
        val raw = "Desculpe, não consegui avaliar essa resposta."

        val result = mapper.toEvaluation(raw)

        assertEquals(OralAnswerVerdict.PARTIAL, result.verdict)
        assertEquals(raw, result.feedback)
    }

    @Test
    fun `extracts verdict line when surrounded by extra text`() {
        val raw = """
            Aqui está minha avaliação:
            VEREDITO: CORRECT | FEEDBACK: Perfeito!
            Obrigado por responder.
        """.trimIndent()

        val result = mapper.toEvaluation(raw)

        assertEquals(OralAnswerVerdict.CORRECT, result.verdict)
        assertEquals("Perfeito!", result.feedback)
    }
}

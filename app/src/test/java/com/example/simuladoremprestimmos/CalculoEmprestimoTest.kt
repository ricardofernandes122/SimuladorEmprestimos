package com.example.simuladoremprestimmos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculoEmprestimoTest {

    @Test
    fun `taxa zero devolve prestacao igual a montante dividido por meses`() {
        val montante = 1200.0
        val meses = 12

        val res = CalculoEmprestimo.calcular(
            montante = montante,
            taxaAnual = 0.0,
            meses = meses
        )

        assertEquals(100.0, res.prestacaoMensal, 0.0001)
        assertEquals(1200.0, res.totalPago, 0.0001)
        assertEquals(0.0, res.totalJuros, 0.0001)
    }

    @Test
    fun `taxa positiva gera prestacao maior do que montante dividido por meses`() {
        val montante = 10_000.0
        val meses = 60

        val res = CalculoEmprestimo.calcular(
            montante = montante,
            taxaAnual = 9.0,
            meses = meses
        )

        val semJuros = montante / meses
        assertTrue(res.prestacaoMensal > semJuros)

        assertEquals(res.prestacaoMensal * meses, res.totalPago, 0.01)
        assertEquals(res.totalPago - montante, res.totalJuros, 0.01)
    }

    @Test
    fun `prazo de 1 mes com taxa zero devolve prestacao igual ao montante`() {
        val montante = 5000.0
        val meses = 1

        val res = CalculoEmprestimo.calcular(
            montante = montante,
            taxaAnual = 0.0,
            meses = meses
        )

        assertEquals(montante, res.prestacaoMensal, 0.0001)
        assertEquals(montante, res.totalPago, 0.0001)
        assertEquals(0.0, res.totalJuros, 0.0001)
    }

    @Test
    fun `taxa muito pequena nao gera NaN nem Infinity`() {
        val montante = 20_000.0
        val meses = 48

        val res = CalculoEmprestimo.calcular(
            montante = montante,
            taxaAnual = 0.0001,
            meses = meses
        )

        assertTrue(!res.prestacaoMensal.isNaN())
        assertTrue(!res.prestacaoMensal.isInfinite())
        assertTrue(res.prestacaoMensal > 0)

        // quase igual a montante/meses
        val semJuros = montante / meses
        assertEquals(semJuros, res.prestacaoMensal, 0.5)
    }

    @Test
    fun `aumentar a taxa aumenta a prestacao`() {
        val montante = 15_000.0
        val meses = 60

        val resBaixa = CalculoEmprestimo.calcular(montante, taxaAnual = 5.0, meses = meses)
        val resAlta = CalculoEmprestimo.calcular(montante, taxaAnual = 10.0, meses = meses)

        assertTrue(resAlta.prestacaoMensal > resBaixa.prestacaoMensal)
    }

    @Test
    fun `aumentar o prazo diminui a prestacao e aumenta os juros totais`() {
        val montante = 10_000.0
        val taxa = 10.0

        val resCurto = CalculoEmprestimo.calcular(montante, taxaAnual = taxa, meses = 12)
        val resLongo = CalculoEmprestimo.calcular(montante, taxaAnual = taxa, meses = 24)

        assertTrue(resLongo.prestacaoMensal < resCurto.prestacaoMensal)
        assertTrue(resLongo.totalJuros > resCurto.totalJuros)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `montante zero lanca excecao`() {
        CalculoEmprestimo.calcular(montante = 0.0, taxaAnual = 10.0, meses = 12)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `montante negativo lanca excecao`() {
        CalculoEmprestimo.calcular(montante = -1.0, taxaAnual = 10.0, meses = 12)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `meses zero lanca excecao`() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = 10.0, meses = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `meses negativo lanca excecao`() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = 10.0, meses = -5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `taxa negativa lanca excecao`() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = -1.0, meses = 12)
    }

    @Test
    fun `caso maximo do dominio nao explode`() {
        val res = CalculoEmprestimo.calcular(
            montante = 75_000.0,
            taxaAnual = 20.0,
            meses = 84
        )

        assertTrue(!res.prestacaoMensal.isNaN())
        assertTrue(!res.prestacaoMensal.isInfinite())
        assertTrue(res.totalPago > 0)
        assertTrue(res.totalJuros >= 0)
    }
}

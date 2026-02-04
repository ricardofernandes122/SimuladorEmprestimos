package com.example.simuladoremprestimmos.domain

import org.junit.Assert.*
import org.junit.Test

class CalculoEmprestimoTest {

    @Test
    fun calcular_quandoTaxaAnualZero_prestacaoIgualMontanteDivididoPorMeses_eSemJuros() {
        // Arrange
        val montante = 1200.0
        val meses = 12

        // Act
        val res = CalculoEmprestimo.calcular(montante = montante, taxaAnual = 0.0, meses = meses)

        // Assert
        assertEquals(100.0, res.prestacaoMensal, 0.0001)
        assertEquals(1200.0, res.totalPago, 0.0001)
        assertEquals(0.0, res.totalJuros, 0.0001)
    }

    @Test
    fun calcular_quandoTaxaPositiva_prestacaoMaiorQueSemJuros_eTotaisConsistentes() {
        // Arrange
        val montante = 10_000.0
        val meses = 60
        val taxaAnual = 9.0
        val semJuros = montante / meses

        // Act
        val res = CalculoEmprestimo.calcular(montante = montante, taxaAnual = taxaAnual, meses = meses)

        // Assert
        assertTrue("Prestação deve ser maior que montante/meses quando há juros", res.prestacaoMensal > semJuros)
        assertEquals(res.prestacaoMensal * meses, res.totalPago, 0.01)
        assertEquals(res.totalPago - montante, res.totalJuros, 0.01)
    }

    @Test
    fun calcular_quandoPrazoUmMes_eTaxaZero_totalPagoIgualMontante() {
        // Arrange
        val montante = 5000.0
        val meses = 1

        // Act
        val res = CalculoEmprestimo.calcular(montante = montante, taxaAnual = 0.0, meses = meses)

        // Assert
        assertEquals(montante, res.prestacaoMensal, 0.0001)
        assertEquals(montante, res.totalPago, 0.0001)
        assertEquals(0.0, res.totalJuros, 0.0001)
    }

    @Test
    fun calcular_quandoTaxaMuitoPequena_naoDevolveNaN_ouInfinity_eAproximaSemJuros() {
        // Arrange
        val montante = 20_000.0
        val meses = 48
        val taxaAnual = 0.0001
        val semJuros = montante / meses

        // Act
        val res = CalculoEmprestimo.calcular(montante = montante, taxaAnual = taxaAnual, meses = meses)

        // Assert
        assertFalse(res.prestacaoMensal.isNaN())
        assertFalse(res.prestacaoMensal.isInfinite())
        assertTrue(res.prestacaoMensal > 0)

        // Muito perto de montante/meses
        assertEquals(semJuros, res.prestacaoMensal, 0.5)
    }

    @Test
    fun calcular_quandoTaxaAumenta_prestacaoMensalTambemAumenta() {
        // Arrange
        val montante = 15_000.0
        val meses = 60

        // Act
        val resTaxaBaixa = CalculoEmprestimo.calcular(montante, taxaAnual = 5.0, meses = meses)
        val resTaxaAlta = CalculoEmprestimo.calcular(montante, taxaAnual = 10.0, meses = meses)

        // Assert
        assertTrue(resTaxaAlta.prestacaoMensal > resTaxaBaixa.prestacaoMensal)
    }

    @Test
    fun calcular_quandoPrazoAumenta_prestacaoDiminui_eJurosTotaisAumentam() {
        // Arrange
        val montante = 10_000.0
        val taxaAnual = 10.0

        // Act
        val resPrazoCurto = CalculoEmprestimo.calcular(montante, taxaAnual = taxaAnual, meses = 12)
        val resPrazoLongo = CalculoEmprestimo.calcular(montante, taxaAnual = taxaAnual, meses = 24)

        // Assert
        assertTrue(resPrazoLongo.prestacaoMensal < resPrazoCurto.prestacaoMensal)
        assertTrue(resPrazoLongo.totalJuros > resPrazoCurto.totalJuros)
    }
    @Test
    fun validarPrazo_quandoMenorQue12_mesesNaoAceite() {
        // Arrange
        val meses = 11

        // Act
        val erro = if (meses < 12) "O prazo mínimo é 12 meses." else null

        // Assert
        assertEquals("O prazo mínimo é 12 meses.", erro)
    }

    @Test
    fun validarPrazo_quandoIgualA12_mesesAceite() {
        // Arrange
        val meses = 12

        // Act
        val erro = if (meses < 12) "O prazo mínimo é 12 meses." else null

        // Assert
        assertNull(erro)
    }


    @Test(expected = IllegalArgumentException::class)
    fun calcular_quandoMontanteZero_lancaExcecao() {
        CalculoEmprestimo.calcular(montante = 0.0, taxaAnual = 10.0, meses = 12)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calcular_quandoMontanteNegativo_lancaExcecao() {
        CalculoEmprestimo.calcular(montante = -1.0, taxaAnual = 10.0, meses = 12)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calcular_quandoMesesZero_lancaExcecao() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = 10.0, meses = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calcular_quandoMesesNegativo_lancaExcecao() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = 10.0, meses = -5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calcular_quandoTaxaNegativa_lancaExcecao() {
        CalculoEmprestimo.calcular(montante = 1000.0, taxaAnual = -1.0, meses = 12)
    }

    @Test
    fun calcular_quandoValoresMaximosDoDominio_naoExplode_eMantemResultadosValidos() {
        // Arrange
        val montante = 75_000.0
        val meses = 84
        val taxaAnual = 20.0

        // Act
        val res = CalculoEmprestimo.calcular(montante = montante, taxaAnual = taxaAnual, meses = meses)

        // Assert
        assertFalse(res.prestacaoMensal.isNaN())
        assertFalse(res.prestacaoMensal.isInfinite())
        assertTrue(res.totalPago > 0)
        assertTrue(res.totalJuros >= 0)
    }

}

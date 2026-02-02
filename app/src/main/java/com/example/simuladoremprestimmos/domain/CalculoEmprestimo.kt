package com.example.simuladoremprestimmos.domain


import kotlin.math.pow

object CalculoEmprestimo {

    fun calcular(montante: Double, taxaAnual: Double, meses: Int): ResultadoEmprestimo {
        require(montante > 0) { "O montante tem de ser maior que 0." }
        require(taxaAnual >= 0) { "A taxa não pode ser negativa." }
        require(meses > 0) { "O prazo tem de ser maior que 0." }

        val i = (taxaAnual / 100.0) / 12.0

        val prestacaoMensal = if (i == 0.0) {
            montante / meses
        } else {
            val fator = (1 + i).pow(meses)
            montante * (i * fator) / (fator - 1)
        }

        val totalPago = prestacaoMensal * meses
        val totalJuros = totalPago - montante

        return ResultadoEmprestimo(
            prestacaoMensal = prestacaoMensal,
            totalPago = totalPago,
            totalJuros = totalJuros
        )
    }
}

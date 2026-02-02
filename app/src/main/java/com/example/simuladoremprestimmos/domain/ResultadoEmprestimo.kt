package com.example.simuladoremprestimmos.domain


data class ResultadoEmprestimo(
    val prestacaoMensal: Double,
    val totalPago: Double,
    val totalJuros: Double
)

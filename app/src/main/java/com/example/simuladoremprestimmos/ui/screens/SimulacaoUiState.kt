package com.example.simuladoremprestimmos.ui.screens

import com.example.simuladoremprestimmos.domain.ResultadoEmprestimo

data class SimulacaoUiState(



    val montanteText: String = "",
    val mesesText: String = "",

    val montanteErro: String? = null,
    val mesesErro: String? = null,

    // taxa calculada automaticamente (para mostrar no resultado)
    val taxaCalculada: Double? = null,
    val mostrarDetalheTaxa: Boolean = false,
    val detalheTaxa: String? = null,

    val resultado: ResultadoEmprestimo? = null


) {
    val podeSimular: Boolean
        get() = montanteErro == null &&
                mesesErro == null &&
                montanteText.isNotBlank() &&
                mesesText.isNotBlank()
}

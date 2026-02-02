package com.example.simuladoremprestimmos.ui.screens

import com.example.simuladoremprestimmos.domain.ResultadoEmprestimo

data class SimulacaoUiState(
    val montanteText: String = "",
    val taxaText: String = "",
    val mesesText: String = "",

    val montanteErro: String? = null,
    val taxaErro: String? = null,
    val mesesErro: String? = null,

    val resultado: ResultadoEmprestimo? = null
) {
    val podeSimular: Boolean
        get() = montanteErro == null &&
                taxaErro == null &&
                mesesErro == null &&
                montanteText.isNotBlank() &&
                taxaText.isNotBlank() &&
                mesesText.isNotBlank()
}

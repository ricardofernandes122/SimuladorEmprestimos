package com.example.simuladoremprestimmos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.simuladoremprestimmos.domain.CalculoEmprestimo
import com.example.simuladoremprestimmos.ui.screens.SimulacaoUiState

class SimulacaoViewModel : ViewModel() {

    var uiState by mutableStateOf(SimulacaoUiState())
        private set

    // Centraliza updates (menos repetição e mais limpo)
    private fun updateState(transform: (SimulacaoUiState) -> SimulacaoUiState) {
        uiState = transform(uiState)
    }

    fun onMontanteChange(novo: String) {
        val texto = novo.replace(',', '.')
        updateState { s ->
            s.copy(
                montanteText = texto,
                montanteErro = validarMontante(texto),
                resultado = null
            )
        }
    }

    fun onTaxaChange(novo: String) {
        val texto = novo.replace(',', '.')
        updateState { s ->
            s.copy(
                taxaText = texto,
                taxaErro = validarTaxa(texto),
                resultado = null
            )
        }
    }

    fun onMesesChange(novo: String) {
        val texto = novo.filter { it.isDigit() }
        updateState { s ->
            s.copy(
                mesesText = texto,
                mesesErro = validarMeses(texto),
                resultado = null
            )
        }
    }

    fun limpar() {
        uiState = SimulacaoUiState()
    }

    fun simular() {
        // Revalidar tudo antes de simular (garante consistência mesmo que chamem simular() direto)
        val montanteErro = validarMontante(uiState.montanteText)
        val taxaErro = validarTaxa(uiState.taxaText)
        val mesesErro = validarMeses(uiState.mesesText)

        updateState { s ->
            s.copy(
                montanteErro = montanteErro,
                taxaErro = taxaErro,
                mesesErro = mesesErro,
                resultado = null
            )
        }

        if (montanteErro != null || taxaErro != null || mesesErro != null) return
        if (!uiState.podeSimular) return

        val montante = uiState.montanteText.toDoubleOrNull() ?: return
        val taxa = uiState.taxaText.toDoubleOrNull() ?: return
        val meses = uiState.mesesText.toIntOrNull() ?: return

        try {
            val resultado = CalculoEmprestimo.calcular(
                montante = montante,
                taxaAnual = taxa,
                meses = meses
            )

            updateState { s -> s.copy(resultado = resultado) }
        } catch (e: IllegalArgumentException) {
            // Se quiseres ser ainda mais profissional:
            // adiciona "val erroGeral: String? = null" ao SimulacaoUiState
            // e aqui faz s.copy(erroGeral = e.message ?: "Não foi possível simular.")
            // Por agora, só garantimos que não crasha e que o resultado fica limpo.
            updateState { s -> s.copy(resultado = null) }
        }
    }

    private fun validarMontante(texto: String): String? {
        if (texto.isBlank()) return "Obrigatório."
        val v = texto.toDoubleOrNull() ?: return "Valor inválido."
        if (v <= 0.0) return "Tem de ser maior que 0."
        return null
    }

    private fun validarTaxa(texto: String): String? {
        if (texto.isBlank()) return "Obrigatório."
        val v = texto.toDoubleOrNull() ?: return "Valor inválido."
        if (v <= 0.0) return "Tem de ser maior que 0."
        if (v > 200.0) return "Taxa demasiado alta."
        return null
    }

    private fun validarMeses(texto: String): String? {
        if (texto.isBlank()) return "Obrigatório."
        val v = texto.toIntOrNull() ?: return "Valor inválido."
        if (v <= 0) return "Tem de ser pelo menos 1."
        if (v > 600) return "Prazo demasiado longo."
        return null
    }
}

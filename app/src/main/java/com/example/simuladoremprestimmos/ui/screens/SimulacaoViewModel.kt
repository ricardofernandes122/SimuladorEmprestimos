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
                resultado = null,
                taxaCalculada = null
            )
        }
    }

    fun onMesesChange(novo: String) {
        val texto = novo.filter { it.isDigit() }
        updateState { s ->
            s.copy(
                mesesText = texto,
                mesesErro = validarMeses(texto),
                resultado = null,
                taxaCalculada = null
            )
        }
    }

    fun limpar() {
        uiState = SimulacaoUiState()
    }

    fun simular() {
        // Revalidar tudo antes de simular
        val montanteErro = validarMontante(uiState.montanteText)
        val mesesErro = validarMeses(uiState.mesesText)

        updateState { s ->
            s.copy(
                montanteErro = montanteErro,
                mesesErro = mesesErro,
                resultado = null,
                taxaCalculada = null
            )
        }

        if (montanteErro != null || mesesErro != null) return
        if (!uiState.podeSimular) return

        val montante = uiState.montanteText.toDoubleOrNull() ?: return
        val meses = uiState.mesesText.toIntOrNull() ?: return

        // ✅ Taxa calculada automaticamente (modelo "banco-like")
        val taxa = calcularTaxaAnual(montante, meses)

        try {
            val resultado = CalculoEmprestimo.calcular(
                montante = montante,
                taxaAnual = taxa,
                meses = meses
            )

            updateState { s ->
                s.copy(
                    taxaCalculada = taxa,
                    resultado = resultado
                )
            }
        } catch (e: IllegalArgumentException) {
            updateState { s -> s.copy(resultado = null, taxaCalculada = null) }
        }
    }

    /**
     * Modelo simples e realista:
     * taxaFinal = taxaBase + ajusteMontante + ajustePrazo
     */
    private fun calcularTaxaAnual(montante: Double, meses: Int): Double {
        val taxaBase = 6.0

        val ajusteMontante = when {
            montante < 5_000.0 -> 3.0
            montante <= 15_000.0 -> 2.0
            else -> 1.0
        }

        val ajustePrazo = when {
            meses <= 24 -> 1.0
            meses <= 60 -> 2.0
            else -> 3.0
        }

        return taxaBase + ajusteMontante + ajustePrazo
    }

    private fun validarMontante(texto: String): String? {
        val t = texto.trim()
        if (t.isBlank()) return "Obrigatório."

        val normalizado = t.replace(',', '.')

        // Aceita só dígitos e no máximo 1 separador decimal e até 2 casas decimais
        val regex = Regex("^\\d+(\\.\\d{0,2})?$")
        if (!regex.matches(normalizado)) return "Use até 2 casas decimais."

        val v = normalizado.toDoubleOrNull() ?: return "Valor inválido."
        if (v <= 0.0) return "Tem de ser maior que 0."

        // (opcional) limites realistas
        if (v > 1_000_000.0) return "Montante demasiado alto (máx. 1 000 000€)."

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

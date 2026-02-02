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

    fun onMontanteChange(novo: String) {
        val texto = novo.replace(',', '.')
        uiState = uiState.copy(
            montanteText = texto,
            montanteErro = validarMontante(texto),
            resultado = null
        )
    }

    fun onTaxaChange(novo: String) {
        val texto = novo.replace(',', '.')
        uiState = uiState.copy(
            taxaText = texto,
            taxaErro = validarTaxa(texto),
            resultado = null
        )
    }

    fun onMesesChange(novo: String) {
        val texto = novo.filter { it.isDigit() }
        uiState = uiState.copy(
            mesesText = texto,
            mesesErro = validarMeses(texto),
            resultado = null
        )
    }

    fun simular() {
        // Revalidar tudo antes de simular
        val montanteErro = validarMontante(uiState.montanteText)
        val taxaErro = validarTaxa(uiState.taxaText)
        val mesesErro = validarMeses(uiState.mesesText)

        uiState = uiState.copy(
            montanteErro = montanteErro,
            taxaErro = taxaErro,
            mesesErro = mesesErro,
            resultado = null
        )

        if (montanteErro != null || taxaErro != null || mesesErro != null) return

        val montante = uiState.montanteText.toDouble()
        val taxa = uiState.taxaText.toDouble()
        val meses = uiState.mesesText.toInt()

        uiState = try {
            uiState.copy(
                resultado = CalculoEmprestimo.calcular(
                    montante = montante,
                    taxaAnual = taxa,
                    meses = meses
                )
            )
        } catch (e: IllegalArgumentException) {
            // Se a lógica do domínio lançar erro, distribuímos de forma genérica
            uiState.copy(
                montanteErro = uiState.montanteErro,
                taxaErro = uiState.taxaErro,
                mesesErro = uiState.mesesErro
            )
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
        // opcional: limitações realistas
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

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
        val normalizado = novo.replace(',', '.')

        // aceita apenas dígitos e UM ponto decimal
        val filtrado = buildString {
            var jaTemPonto = false
            for (c in normalizado) {
                when {
                    c.isDigit() -> append(c)
                    c == '.' && !jaTemPonto -> {
                        append(c)
                        jaTemPonto = true
                    }
                }
            }
        }

        updateState { s ->
            s.copy(
                montanteText = filtrado,
                montanteErro = validarMontante(filtrado),
                resultado = null,
                taxaCalculada = null,
                detalheTaxa = null,
                mostrarDetalheTaxa = false
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
                taxaCalculada = null,
                detalheTaxa = null,
                mostrarDetalheTaxa = false
            )
        }
    }


    fun limpar() {
        uiState = SimulacaoUiState()
    }

    fun simular() {
        val montanteErro = validarMontante(uiState.montanteText)
        val mesesErro = validarMeses(uiState.mesesText)

        // Atualiza erros e limpa resultado se inválido
        updateState { s ->
            s.copy(
                montanteErro = montanteErro,
                mesesErro = mesesErro,
                resultado = null,
                taxaCalculada = null,
                detalheTaxa = null,
                mostrarDetalheTaxa = false
            )
        }

        if (montanteErro != null || mesesErro != null) return
        if (!uiState.podeSimular) return

        val montante = uiState.montanteText.replace(',', '.').toDoubleOrNull() ?: return
        val meses = uiState.mesesText.toIntOrNull() ?: return

        val (taxa, detalheTaxa) = calcularTaxaAnual(montante, meses)

        val resultado = CalculoEmprestimo.calcular(
            montante = montante,
            taxaAnual = taxa,
            meses = meses
        )

        updateState { s ->
            s.copy(
                taxaCalculada = taxa,
                detalheTaxa = detalheTaxa,
                mostrarDetalheTaxa = false,
                resultado = resultado
            )
        }



}

    /**
     * A taxa é calculada no ViewModel por se tratar de uma regra de negócio
     * dependente do contexto da simulação (montante e prazo),
     * enquanto o CalculoEmprestimo se limita a cálculos matemáticos puros.
     */

    private fun calcularTaxaAnual(montante: Double, meses: Int): Pair<Double, String> {
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

        val taxaFinal = taxaBase + ajusteMontante + ajustePrazo

        val detalhe = "Taxa base: ${taxaBase}%\n" +
                "Ajuste montante: +${ajusteMontante}%\n" +
                "Ajuste prazo: +${ajustePrazo}%"


        return Pair(taxaFinal, detalhe)
    }


    private fun validarMontante(texto: String): String? {
        val t = texto.trim()
        if (t.isBlank()) return "Obrigatório."

        val normalizado = t.replace(',', '.')

        // aceitar apenas dígitos e no máximo 1 ponto, com até 2 casas decimais
        val regex = Regex("^\\d+(\\.\\d{0,2})?$")
        if (!regex.matches(normalizado))
            return "Use apenas números (até 2 casas decimais)."


        val v = normalizado.toDoubleOrNull() ?: return "Valor inválido."
        if (v <= 0.0) return "Tem de ser maior que 0."

        // Montante mínimo e máximo para crédito pessoal
        if (v < 500.0) return "Montante mínimo para crédito pessoal: 500€."
        if (v > 75_000.0) return "Montante máximo habitual para crédito pessoal: 75 000€."

        return null
    }




    private fun validarMeses(texto: String): String? {
        if (texto.isBlank()) return "Obrigatório."

        val v = texto.toIntOrNull() ?: return "Valor inválido."
        if (v <= 0) return "Tem de ser pelo menos 1 mês."

        // Prazo máximo para crédito pessoal
        if (v > 84) return "Prazo acima do habitual para crédito pessoal (máx. 84 meses)."

        return null
    }

}

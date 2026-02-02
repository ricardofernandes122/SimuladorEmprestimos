package com.example.simuladoremprestimmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simuladoremprestimmos.domain.ResultadoEmprestimo
import com.example.simuladoremprestimmos.ui.theme.SimuladorEmprestimmosTheme
import com.example.simuladoremprestimmos.viewmodel.SimulacaoViewModel
import java.util.Locale

@Composable
fun SimulacaoScreen(
    modifier: Modifier = Modifier,
    vm: SimulacaoViewModel = viewModel()
) {
    val state = vm.uiState

    SimulacaoContent(
        modifier = modifier,
        state = state,
        onMontanteChange = vm::onMontanteChange,
        onTaxaChange = vm::onTaxaChange,
        onMesesChange = vm::onMesesChange,
        onLimpar = vm::limpar,
        onSimular = vm::simular
    )
}

@Composable
private fun SimulacaoContent(
    modifier: Modifier = Modifier,
    state: SimulacaoUiState,
    onMontanteChange: (String) -> Unit,
    onTaxaChange: (String) -> Unit,
    onMesesChange: (String) -> Unit,
    onLimpar: () -> Unit,
    onSimular: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Simulador de Empréstimos", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.montanteText,
            onValueChange = onMontanteChange,
            label = { Text("Montante (€)") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.montanteErro != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        state.montanteErro?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = state.taxaText,
            onValueChange = onTaxaChange,
            label = { Text("Taxa anual (%)") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.taxaErro != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        state.taxaErro?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = state.mesesText,
            onValueChange = onMesesChange,
            label = { Text("Prazo (meses)") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.mesesErro != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (state.podeSimular) onSimular()
                }
            )
        )
        state.mesesErro?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // ✅ Botão principal: Simular
        Button(
            onClick = {
                focusManager.clearFocus()
                onSimular()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = state.podeSimular
        ) {
            Text("Simular")
        }

        // ✅ Botão secundário: Limpar
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onLimpar()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpar")
        }

        // ✅ Resultado com detalhes do cálculo
        state.resultado?.let { res ->
            Spacer(modifier = Modifier.height(8.dp))
            ResultadoCard(
                resultado = res,
                montante = state.montanteText.toDoubleOrNull() ?: 0.0,
                taxaAnual = state.taxaText.toDoubleOrNull() ?: 0.0,
                meses = state.mesesText.toIntOrNull() ?: 0
            )
        }
    }
}

@Composable
private fun ResultadoCard(
    resultado: ResultadoEmprestimo,
    montante: Double,
    taxaAnual: Double,
    meses: Int
) {
    val taxaMensal = taxaAnual / 12.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium)

            // ✅ Detalhes do pedido (académico/profissional)
            Text("Montante: ${formatEuro(montante)}")
            Text("Taxa anual: ${formatPercent(taxaAnual)}")
            Text("Prazo: $meses meses")
            Text("Taxa mensal (aprox.): ${formatPercent(taxaMensal)}")

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Resultado do cálculo
            Text("Prestação mensal: ${formatEuro(resultado.prestacaoMensal)}")
            Text("Total pago: ${formatEuro(resultado.totalPago)}")
            Text("Total de juros: ${formatEuro(resultado.totalJuros)}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SimulacaoScreenPreviewVazio() {
    SimuladorEmprestimmosTheme {
        SimulacaoContent(
            state = SimulacaoUiState(),
            onMontanteChange = {},
            onTaxaChange = {},
            onMesesChange = {},
            onLimpar = {},
            onSimular = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimulacaoScreenPreviewComResultado() {
    SimuladorEmprestimmosTheme {
        SimulacaoContent(
            state = SimulacaoUiState(
                montanteText = "10000",
                taxaText = "7.5",
                mesesText = "60",
                resultado = ResultadoEmprestimo(
                    prestacaoMensal = 200.00,
                    totalPago = 12000.00,
                    totalJuros = 2000.00
                )
            ),
            onMontanteChange = {},
            onTaxaChange = {},
            onMesesChange = {},
            onLimpar = {},
            onSimular = {}
        )
    }
}

private fun formatEuro(valor: Double): String =
    String.format(Locale.getDefault(), "%.2f €", valor)

private fun formatPercent(valor: Double): String =
    String.format(Locale.getDefault(), "%.3f %%", valor)

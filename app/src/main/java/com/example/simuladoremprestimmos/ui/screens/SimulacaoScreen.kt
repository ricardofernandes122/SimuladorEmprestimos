package com.example.simuladoremprestimmos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
    onMesesChange: (String) -> Unit,
    onLimpar: () -> Unit,
    onSimular: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val podeLimpar =
        state.montanteText.isNotBlank() ||
                state.mesesText.isNotBlank() ||
                state.resultado != null


    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Simulador de Empréstimos",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Dados do empréstimo",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = state.montanteText,
                    onValueChange = onMontanteChange,
                    label = { Text("Montante (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.montanteErro != null,
                    singleLine = true,
                    supportingText = if (state.montanteErro != null) {
                        { Text(state.montanteErro!!) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = state.mesesText,
                    onValueChange = onMesesChange,
                    label = { Text("Prazo (meses)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.mesesErro != null,
                    singleLine = true,
                    supportingText = if (state.mesesErro != null) {
                        { Text(state.mesesErro!!) }
                    } else null,
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

            }
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                onSimular()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = state.podeSimular,
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Simular", style = MaterialTheme.typography.titleSmall)
        }

        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onLimpar()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = podeLimpar
        ) {
            Text("Limpar", style = MaterialTheme.typography.titleSmall)
        }


        state.resultado?.let { res ->
            Spacer(modifier = Modifier.height(4.dp))
            ResultadoCard(
                resultado = res,
                montante = state.montanteText.toDoubleOrNull() ?: 0.0,
                taxaAnual = state.taxaCalculada ?: 0.0,
                meses = state.mesesText.toIntOrNull() ?: 0,
                detalheTaxa = state.detalheTaxa
            )

        }
    }
}

@Composable

private fun ResultadoCard(
    resultado: ResultadoEmprestimo,
    montante: Double,
    taxaAnual: Double,
    meses: Int,
    detalheTaxa: String?
) {
    // coerente com o CalculoEmprestimo:
    // i = (taxaAnual/100)/12  -> decimal
    // para mostrar em percentagem: i*100

    val i = (taxaAnual / 100.0) / 12.0
    val taxaMensalPercent = i * 100.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium)

            Text("Resumo do pedido", style = MaterialTheme.typography.labelLarge)
            InfoRow("Montante", formatEuro(montante))
            InfoRow("Taxa anual (estimada)", formatPercent(taxaAnual))

            detalheTaxa?.let { detalhe ->
                Text(
                    detalhe,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            InfoRow("Prazo", "$meses meses")
            InfoRow("Taxa mensal (aprox.)", formatPercent(taxaMensalPercent))

            HorizontalDivider()

            Text("Resumo financeiro", style = MaterialTheme.typography.labelLarge)
            InfoRow("Prestação mensal", formatEuro(resultado.prestacaoMensal))
            InfoRow("Total pago", formatEuro(resultado.totalPago))
            InfoRow("Total de juros", formatEuro(resultado.totalJuros))
        }
    }
}




@Composable
private fun InfoRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val valueStyle =
        if (highlight)
            MaterialTheme.typography.bodyMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        else
            MaterialTheme.typography.bodyMedium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = valueStyle)
    }
}


@Preview(showBackground = true)
@Composable
private fun SimulacaoScreenPreviewVazio() {
    SimuladorEmprestimmosTheme {
        SimulacaoContent(
            state = SimulacaoUiState(),
            onMontanteChange = {},
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
                mesesText = "60",
                taxaCalculada = 10.0,
                resultado = ResultadoEmprestimo(
                    prestacaoMensal = 200.00,
                    totalPago = 12000.00,
                    totalJuros = 2000.00
                )
            ),
            onMontanteChange = {},
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

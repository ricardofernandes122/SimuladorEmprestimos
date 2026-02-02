package com.example.simuladoremprestimmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simuladoremprestimmos.domain.ResultadoEmprestimo
import com.example.simuladoremprestimmos.viewmodel.SimulacaoViewModel
import java.util.Locale

@Composable
fun SimulacaoScreen(
    modifier: Modifier = Modifier,
    vm: SimulacaoViewModel = viewModel()
) {
    val state = vm.uiState

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Simulador de Empréstimos",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = state.montanteText,
            onValueChange = vm::onMontanteChange,
            label = { Text("Montante (€)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.montanteErro != null,
            supportingText = {
                state.montanteErro?.let { Text(it) }
            }
        )

        OutlinedTextField(
            value = state.taxaText,
            onValueChange = vm::onTaxaChange,
            label = { Text("Taxa anual (%)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.taxaErro != null,
            supportingText = {
                state.taxaErro?.let { Text(it) }
            }
        )

        OutlinedTextField(
            value = state.mesesText,
            onValueChange = vm::onMesesChange,
            label = { Text("Prazo (meses)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.mesesErro != null,
            supportingText = {
                state.mesesErro?.let { Text(it) }
            }
        )

        Button(
            onClick = vm::simular,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.podeSimular
        ) {
            Text("Simular")
        }

        if (state.resultado != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ResultadoCard(resultado = state.resultado!!)
        }
    }
}

@Composable
private fun ResultadoCard(resultado: ResultadoEmprestimo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium)
            Text("Prestação mensal: ${formatEuro(resultado.prestacaoMensal)}")
            Text("Total pago: ${formatEuro(resultado.totalPago)}")
            Text("Total de juros: ${formatEuro(resultado.totalJuros)}")
        }
    }
}

private fun formatEuro(valor: Double): String {
    return String.format(Locale.getDefault(), "%.2f €", valor)
}

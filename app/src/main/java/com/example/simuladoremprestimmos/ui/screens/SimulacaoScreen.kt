package com.example.simuladoremprestimmos.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simuladoremprestimmos.R
import com.example.simuladoremprestimmos.domain.ResultadoEmprestimo
import com.example.simuladoremprestimmos.ui.theme.SimuladorEmprestimmosTheme
import com.example.simuladoremprestimmos.viewmodel.SimulacaoViewModel
import java.text.SimpleDateFormat
import java.util.Date
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
                        { Text(state.montanteErro) }
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
                        { Text(state.mesesErro) }
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

        val context = LocalContext.current

        state.resultado?.let { res ->
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = {
                    val uri = exportarPdfEmprestimo(
                        context = context,
                        montante = state.montanteText.toDoubleOrNull() ?: 0.0,
                        meses = state.mesesText.toIntOrNull() ?: 0,
                        taxaAnual = state.taxaCalculada ?: 0.0,
                        detalheTaxa = state.detalheTaxa,
                        prestacao = res.prestacaoMensal,
                        totalPago = res.totalPago,
                        totalJuros = res.totalJuros
                    )

                    if (uri != null) {
                        Toast.makeText(context, "PDF guardado em Downloads.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Erro a exportar PDF.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = true
            ) {
                Text("Exportar PDF", style = MaterialTheme.typography.titleSmall)
            }

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
    val mostrarInfoTaxa = remember { mutableStateOf(false) }

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

            // Taxa anual com ícone informativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Taxa anual (estimada)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(
                        onClick = { mostrarInfoTaxa.value = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informação sobre a taxa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    formatPercent(taxaAnual),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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

            InfoRow(
                "Prestação mensal",
                formatEuro(resultado.prestacaoMensal),
                highlight = true
            )

            InfoRow(
                "Total pago",
                formatEuro(resultado.totalPago),
                highlight = true
            )

            InfoRow(
                "Total de juros",
                formatEuro(resultado.totalJuros)
            )
        }
    }

    //  Diálogo informativo da taxa
    if (mostrarInfoTaxa.value) {
        AlertDialog(
            onDismissRequest = { mostrarInfoTaxa.value = false },
            title = {
                Text(text = "Taxa anual estimada")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.imagem),
                        contentDescription = "Ilustração da taxa anual",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Texto principal
                    Text(
                        text = "A taxa apresentada é uma estimativa académica, calculada com base numa taxa base de 6% e ajustes em função do montante e do prazo.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Aviso discreto
                    Text(
                        text = "Não corresponde a uma proposta contratual real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarInfoTaxa.value = false }) {
                    Text("OK")
                }
            }
        )
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

fun exportarPdfEmprestimo(
    context: Context,
    montante: Double,
    meses: Int,
    taxaAnual: Double,
    detalheTaxa: String?,
    prestacao: Double,
    totalPago: Double,
    totalJuros: Double
): Uri? {
    // 1) Criar PDF em memória
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 aprox (pt)
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val paintTitle = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 18f
    }
    val paintLabel = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 12f
    }
    val paintText = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 12f
    }

    var y = 60f
    val x = 40f
    val line = 22f

    fun drawPair(label: String, value: String) {
        canvas.drawText(label, x, y, paintLabel)
        canvas.drawText(value, x + 220f, y, paintText)
        y += line
    }

    val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    canvas.drawText("Simulador de Crédito Pessoal - Resultado", x, y, paintTitle)
    y += 2 * line
    canvas.drawText("Gerado em: $dataHora", x, y, paintText)
    y += 2 * line

    drawPair("Montante:", formatEuroPt(montante))
    drawPair("Prazo:", "$meses meses")
    drawPair("Taxa anual (estimada):", formatPercentPt(taxaAnual))
    y += line / 2

    detalheTaxa?.let {
        canvas.drawText("Detalhe da taxa:", x, y, paintLabel); y += line
        // quebra simples por linhas
        it.lines().forEach { ln ->
            canvas.drawText(ln, x, y, paintText)
            y += line
        }
        y += line / 2
    }

    y += line / 2
    canvas.drawText("Resumo financeiro", x, y, paintLabel)
    y += line

    drawPair("Prestação mensal:", formatEuroPt(prestacao))
    drawPair("Total pago:", formatEuroPt(totalPago))
    drawPair("Total de juros:", formatEuroPt(totalJuros))

    y += 2 * line
    canvas.drawText(
        "Nota: Valores com fins académicos. Não constitui proposta contratual.",
        x, y, paintText
    )

    pdf.finishPage(page)

    //  Guardar em Downloads (MediaStore)
    val resolver = context.contentResolver

    val fileName = "Simulacao_Emprestimo_${System.currentTimeMillis()}.pdf"
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(
        MediaStore.Files.getContentUri("external"),
        values
    ) ?: run {
        pdf.close()
        return null
    }


    return try {
        resolver.openOutputStream(uri)?.use { out ->
            pdf.writeTo(out)
        } ?: run {
            pdf.close()
            return null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        pdf.close()
        uri
    } catch (e: Exception) {
        pdf.close()
        // tenta apagar o ficheiro incompleto
        runCatching { resolver.delete(uri, null, null) }
        null
    }
}
private fun formatEuroPt(valor: Double): String =
    String.format(Locale("pt", "PT"), "%.2f €", valor)

private fun formatPercentPt(valor: Double): String =
    String.format(Locale("pt", "PT"), "%.3f %%", valor)

private fun formatEuro(valor: Double): String =
    String.format(Locale.getDefault(), "%.2f €", valor)

private fun formatPercent(valor: Double): String =
    String.format(Locale.getDefault(), "%.3f %%", valor)

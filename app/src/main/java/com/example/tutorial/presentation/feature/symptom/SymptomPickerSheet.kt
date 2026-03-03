package com.example.tutorial.com.example.tutorial.presentation.feature.symptom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomPickerSheet(
    data: List<SymptomUi>,
    onToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Select symptoms")
            Spacer(Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.forEach {
                    SymptomTag(
                        label = it.label,
                        severity = it.severity,
                        onClick = { onToggle(it.id) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onSave(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

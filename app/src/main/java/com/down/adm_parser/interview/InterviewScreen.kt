package com.down.adm_parser.interview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun InterviewScreen(
    viewModel: InterviewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column {
        Button(
            onClick = {
                viewModel.btnClicked(context)
//            viewModel.loadData()
            }
        ) {
            Text("Load Data")
        }
        if (state.isRequesting) {
            CircularProgressIndicator()
        } else {
            Column {
                LazyColumn {
                    items(state.students) {
                        Text(
                            text = it, modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 10.dp, vertical = 8.dp
                                )
                        )
                    }
                }
            }
        }
    }

}
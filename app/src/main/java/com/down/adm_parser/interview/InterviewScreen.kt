package com.down.adm_parser.interview

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InterviewScreen(
    viewModel: InterviewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
    ) {
        TextField(
            value = state.text,
            onValueChange = {
                viewModel.changeText(it)
            }
        )
        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        SecondActivity::class.java
                    )
                )
            }
        ) {
            Text("Load Data")
        }
        LazyColumn(
            modifier = Modifier
                .testTag("item_list")
                .semantics { testTagsAsResourceId = true } // Explicitly enable it for LazyColumn
        ) {
            items(state.list) {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Blue)
                        .padding(20.dp)
                        .clickable {
                            Toast
                                .makeText(context, "Clicked $it", Toast.LENGTH_SHORT)
                                .show()
                        }
                )
            }
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
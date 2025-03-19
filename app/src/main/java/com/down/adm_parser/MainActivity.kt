package com.down.adm_parser

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adm.url_parser.models.ParsedVideo
import com.adm.url_parser.sdk.UrlParserSdk
import com.down.adm_parser.ui.theme.AdmparserTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private val TAG = "TestClass"

class TestClass() {
    private val urlParser = UrlParserSdk()
    val state = MutableStateFlow<ParsedVideo?>(null)
    fun parse(text: String = "", context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            state.update { null }
            Log.d(TAG, "parse Started $text")
            val data = urlParser.scrapeLink(text)
            Log.d(TAG, "parsing error: ${data.error}")
            if (data.model == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show()
                }
            }
            state.update { data.model }
            Log.d(TAG, "parse Results = $data")
        }
    }
}

class MainActivity : ComponentActivity() {
    private val testClass = TestClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdmParserTesting(testClass)
        }
    }
}

@Composable
private fun AdmParserTesting(testClass: TestClass) {
    val state by testClass.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var text by remember {
        mutableStateOf("")
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 30.dp)
    ) {

        TextField(
            value = text,
            onValueChange = {
                text = it
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        Button(
            onClick = {
                testClass.parse(text, context)
            }
        ) {
            Text("Parse")
        }
        Button(
            onClick = {
                clipboard.getText()?.let {
                    text = it.toString()
                }
            }
        ) {
            Text("Paste")
        }
        Button(
            onClick = {
                text = ""
            }
        ) {
            Text("Clear")
        }
        if (state != null) {
            Spacer(
                modifier = Modifier
                    .height(10.dp)
            )
            Text("Title:${state?.title}")
            Text("Qualities:${state?.qualities?.size}")
            Spacer(
                modifier = Modifier
                    .height(10.dp)
            )
            LazyColumn {
                items(state?.qualities ?: emptyList()) {
                    Text("Media Type: " + it.mediaType.toString())
                    Text(("Name " + it.name) ?: "")
                    Text(
                        it.url ?: "",
                        fontSize = 8.sp
                    )
                    Spacer(
                        modifier = Modifier
                            .height(5.dp)
                    )
                    Text("---------------------------------------")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AdmparserTheme {
        Greeting("Android")
    }
}
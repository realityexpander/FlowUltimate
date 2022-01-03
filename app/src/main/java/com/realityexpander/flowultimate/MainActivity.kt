package com.realityexpander.flowultimate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.realityexpander.flowultimate.databinding.ActivityMainBinding
import com.realityexpander.flowultimate.ui.theme.FlowUltimateTheme
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() { // for Compose
//class MainActivity : AppCompatActivity() {  // for XML
//    private lateinit var bind: ActivityMainBinding // for XML
//    private val viewModel: MainViewModel by viewModels() // for XML

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // For XML apps
//        bind = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(bind.root)
//        collectLatestLifecycleFlow(viewModel.stateFlow) { number ->
//            bind.counterTv.text = number.toString()
//        }

        setContent {
            FlowUltimateTheme {
                val viewModel = viewModel<MainViewModel>()
                val time = viewModel.countdownFlow.collectAsState(initial = 10)
                val count = viewModel.stateFlow.collectAsState(initial = 0)

                LaunchedEffect(key1 = true) {
                    viewModel.sharedFlow.collect { number ->
                        Toast.makeText(applicationContext, "The number is $number", Toast.LENGTH_SHORT ).show()
                    }
                }
                
                Box( modifier = Modifier.fillMaxSize(1f)) {
                    Text(
                        text = time.value.toString(),
                        fontSize = 30.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Button(onClick = { viewModel.incrementCounter() }) {
                        Text(text = "Counter: ${count.value}")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlowUltimateTheme {
        Greeting("Android")
    }
}

// // collect for StateFlows
//fun <T> AppCompatActivity.collectLatestLifecycleFlow(flow: Flow<T>, suspend (T) -> Unit) { // For XML projects
//fun <T> ComponentActivity.collectLatestLifecycleFlow(flow: Flow<T>, suspend (T) -> Unit) {
//    lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                flow.collectLatest(collect)
//            }
//        }
//}

// // Collect for sharedFlows
//fun <T> AppCompatActivity.collectLifecycleFlow(flow: Flow<T>, suspend (T) -> Unit) { // For XML projects
//fun <T> ComponentActivity.collectLifecycleFlow(flow: Flow<T>, suspend (T) -> Unit) {
//    lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                flow.collect(collect)
//            }
//        }
//}
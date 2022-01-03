package com.realityexpander.flowultimate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    companion object {
        var outString = ""
    }

    val countdownFlow = flow<Int> {
        val startingValue = 100
        var currentValue = startingValue

        emit(startingValue)
        while(currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
        collectFlow()
        collectFlowLatest()
    }

    private fun collectFlow() {
        viewModelScope.launch {
            countdownFlow.collect { time ->
//                println("The current time is $time")
                outString = "The current time is $time"
                println(outString)
            }
        }
    }

    private fun collectFlowLatest() {
        viewModelScope.launch {
            countdownFlow.collectLatest { time ->
                delay(1002L)
                outString += (", The latest time is $time")
                println(outString)
            }
        }
    }

}
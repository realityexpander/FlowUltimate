package com.realityexpander.flowultimate

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.flowultimate.MainViewModel.Companion.outString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    companion object {
        var outString = ""
    }

    val countdownFlow = flow<Int> {
        val startingValue = 5
        var currentValue = startingValue

        emit(startingValue)
        while(currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    private val _stateFlow = MutableStateFlow(0)
    val stateFlow = _stateFlow.asStateFlow()

    private val _sharedFlow = MutableSharedFlow<Int>(0) // Hot
    val sharedFlow = _sharedFlow.asSharedFlow()

    private val _sharedFlowReplayed = MutableSharedFlow<Int>(5) // Hot & replays to new collectors
    val sharedFlowReplayed = _sharedFlowReplayed.asSharedFlow()

    init {
//        collectFlow()
//        collectFlowLatest()
//        collectFlowFiltered()
//        collectFlattenFlows()
//        collectFlattenRecipesFlow()
//        collectMealsFlow()


        // Sharedflow emitters
        //squaredNumber(10) // this will be lost because sharedFlow is a hot flow, and this will be lost!
        viewModelScope.launch {
            sharedFlow.collect {
                delay(1000)
                println("FIRST FLOW: The received number is $it")
            }
        }
        viewModelScope.launch {
            sharedFlow.collect {
                delay(1500)
                println("SECOND FLOW: The received number is $it")
            }
        }
        squaredNumber(10)


        // ShareFlowReplayed
        squaredNumberReplayed(10)  // this will be flowed bc this is using a replay > 0
        viewModelScope.launch {
            sharedFlowReplayed.collect {
                delay(1500)
                println("REPLAYED FLOW: The received number is $it")
            }
        }

        // Change the sharedFlow after the viewmodel is setup
        viewModelScope.launch {
            _sharedFlow.emit(999)
        }

    }

    fun incrementCounter() {
        _stateFlow.value += 1
    }

    private fun squaredNumber(number: Int) {
        viewModelScope.launch {
            _sharedFlow.emit(number * number)
        }
    }

    private fun squaredNumberReplayed(number: Int) {
        viewModelScope.launch {
            _sharedFlowReplayed.emit(number * number)
        }
    }

    private fun collectFlow() {
        viewModelScope.launch {
            countdownFlow.collect { time ->
                outString = "The current time is $time"
                println(outString)
            }
        }
    }

    private fun collectFlowLatest() {
        viewModelScope.launch {
            countdownFlow.collectLatest { time ->
                delay(1002L) // cancels when next emission happens in countdownFlow
                outString += (", The latest time is $time")
                println(outString)
            }
        }
    }

    private fun collectFlowFiltered() {

        // alternate way of doing launch
        countdownFlow.onEach { time ->
            println("from alternate launch time: $time")
        }.launchIn(viewModelScope)

        // filter, map, onEach
        viewModelScope.launch {
            countdownFlow
                .filter { time ->
                    time %2 == 0
                }
                .map { time ->
                    time * 2
                }
                .onEach { time ->
                    println(time)
                }
                .collect { time ->
                    outString = "The filtered time is $time"
                    println(outString)
                }
        }

        // count
        viewModelScope.launch {
            val countEvens = countdownFlow.count { time ->
                println("counting time= $time")
                time % 2 == 0
            }
            println("time - count of Evens: $countEvens") // will wait for the countdownFlow to complete before hitting this line
        }

        // reduce
        viewModelScope.launch {
            val reduceResult = countdownFlow
                .reduce { acc, time ->
                    acc + time
                }
            println("time - the reduce acc = $reduceResult") // will wait for the countdownFlow to complete before hitting this line
        }

        // fold
        viewModelScope.launch {
            val foldResult = countdownFlow
                .fold(1000) { acc, time ->
                    acc + time
                }
            println("time - the fold acc = $foldResult") // will wait for the countdownFlow to complete before hitting this line
        }
    }

    private fun collectFlattenFlows() {
        val flow1 = flow {
            emit(1)
            delay(500)
            emit(2)
        }

        viewModelScope.launch {
            flow1.flatMapConcat { value -> // each time this emits...
                flow {                          // ... this flow emits
                    emit(10 + value)
                    delay(400)
                    emit(20 + value)
                    delay(600)
                    emit(30 + value)
                }
            }.collect { value ->
                println("the value is $value")
            }
        }
    }

    private fun collectFlattenRecipesFlow() {

        val recipeIds = (1..5).asFlow()

        fun getRecipeById(id: Int) = flow {
            emit("Recipe $id")
            delay(1000)
        }

        viewModelScope.launch {
            recipeIds.flatMapConcat { id -> // respects the delay
                delay(200)
                getRecipeById(id)  // by using flatMapConcat, we are now returning the values of the flow, and not just the flow
            }.collect { value ->
                println("the flatMapConcat value is $value")
            }
        }

        viewModelScope.launch {
            recipeIds.flatMapMerge { id ->  // flatMapMerge does not use "delay" to receive values, (not recommended for apps)
                delay(200)
                getRecipeById(id)  // by using flatMapMerge, we are now returning the values of the flow, and not just the flow
            }.collect { value ->
                println("the flatMapMerge value is $value")
            }
        }

        viewModelScope.launch {
            recipeIds.flatMapLatest { id ->  // flatMapLatest will cancel if there are new values emitted
                delay(200)
                getRecipeById(id)  // by using flatMapLatest, we are now returning the values of the flow, and not just the flow
            }.collect { value ->
                println("the flatMapLatest value is $value")
            }
        }
    }

    private fun collectMealsFlow() {

        val flow = flow {
            delay(250)
            emit("Appetizer 1")
            delay(1000)
            emit("Main Dish 2")
            delay(100)
            emit("Dessert   3")
        }


        viewModelScope.launch {

            // Unbuffered (sequential)
            flow.onEach {
                println("MEAL:A Delivered $it")
            }.collect {
                println("MEAL:B Eating    $it")
                delay(1500)
                println("MEAL:C Finished  $it")
            }

            // Buffered - collect flow in a seperate coroutine
            println("\n\nMEAL: Buffered:")
            flow.onEach {
                println("MEAL:A Delivered $it")
            }
            .buffer()  // collects in separate coroutine, doesn't wait for emitter
            .collect {
                println("MEAL:B Eating    $it")
                delay(1500)
                println("MEAL:C Finished  $it")
            }

            // Conflated - cancels flow if new emission
            println("\n\nMEAL: Conflate:")
            flow.onEach {
                println("MEAL:A Delivered $it")
            }
            .conflate()  // runs in separate coroutine, cancels the collect if new emit arrives
            .collect {
                println("MEAL:B Eating    $it")
                delay(1500)
                println("MEAL:C Finished  $it")
            }

            // CollectLatest - cancels collect if new emit, starts with latest emit
            println("\n\nMEAL: CollectLatest:")
            flow.onEach {
                println("MEAL:A Delivered $it")
            }
            .collectLatest {
                println("MEAL:B Eating    $it")
                delay(1500)
                println("MEAL:C Finished  $it")
            }
        }
    }

}
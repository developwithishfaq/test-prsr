package com.down.adm_parser.interview

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.down.adm_parser.interview.domain.GetData
import com.down.adm_parser.interview.models.MainData
import com.down.adm_parser.interview.services.MyBoundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.concurrent.Callable
import java.util.concurrent.Executors

private val TAG = "TestViewModel"

fun main() {
    rvInt()
}

fun rvInt() {
    val original = 1012
    var reversedNumber = 0
    var temp = original
    while (temp != 0) {
        val digit = temp % 10
        reversedNumber = reversedNumber * 10 + digit
        temp /= 10
    }
    println(reversedNumber)
}

fun sorttt() {
    val list = mutableListOf(4, 3, 2, 1)
    for (i in list.indices) {
        for (j in 0 until list.size - i - 1) {
            if (list[j + 1] < list[j]) {
                val temp = list[j]
                list[j] = list[j + 1]
                list[j + 1] = temp
            }
        }
    }
    println(list)
}

fun facto(): Int {
    val number = 3
    var factorial = 1
    if (number == 0 || number == 1) {
        return factorial
    }
    for (i in 2..number) {
        factorial *= i
    }
    println(factorial)
    return factorial
}

fun revrse() {
    val text = "Ishfaq"
    var reversed = ""
    for (i in text.length - 1 downTo 0) {
        reversed += text[i]
    }
    println(reversed)
}


@Keep
data class Test(
    val name: String
) : Serializable

@Keep
data class User(val name: String, val age: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(age)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}


fun executors() {
    val executor = Executors.newFixedThreadPool(1)
    val taskList = listOf(
        Callable { "Task 1" },
        Callable { "Task 2" },
        Callable { "Task 3" },
        Callable { "Task 4" },
        Callable { "Task 5" },
    )
    val future = executor.invokeAll(taskList)
    for (i in future) {
        println(i.get())
    }
    for (i in 0..2) {
        executor.execute {
            println("Executing ${i}")
            Thread.sleep(3000)
            println("Done ${i}")
        }
    }
    executor.shutdown()
}

fun handlerThread() {
    val handlerThread = HandlerThread("Test")
    handlerThread.start()

    val handler = Handler(handlerThread.looper)
    handler.post {
        println("Handler Post")
    }
}

fun threadPractice() {
    val thread = Thread {
        println("In Thread")
        Thread.sleep(2000)
        println("End Thread")
    }
    thread.start()
}

fun sort() {
    val list1 = mutableListOf(7, 6, 5, 4, 3, 2, 1)
    for (i in list1.indices) {
        for (j in 0..<list1.size - i - 1) {
            if (list1[j] > list1[j + 1]) {
                val temp = list1[j]
                list1[j] = list1[j + 1]
                list1[j + 1] = temp
            }
        }
    }
    println("List $list1")
}

fun merger() {
    val list1 = mutableListOf(2, 3, 4)
    val list2 = mutableListOf(1, 2, 5)
    val merged = mutableListOf<Int>()

    var i = 0
    var j = 0
    while (i < list1.size && j < list2.size) {
        if (list1[i] < list2[j]) {
            merged.add(list1[i])
            i++
        } else {
            merged.add(list2[j])
            j++
        }
    }

    while (i < list1.size) {
        merged.add(list1[i])
        i++
    }
    while (j < list2.size) {
        merged.add(list2[j])
        j++
    }
    println(merged)
}


fun mergeTwoSortedList() {
    val list1 = mutableListOf(2, 3, 4)
    val list2 = mutableListOf(1, 2, 5)
    val merged = mutableListOf<Int>()
    var i = 0
    var j = 0
    while (i < list1.size && j < list2.size) {
        if (list1[i] < list2[j]) {
            merged.add(list1[i])
            i++
        } else {
            merged.add(list2[j])
            j++
        }
    }
    while (i < list1.size) {
        merged.add(list1[i])
        i++
    }
    while (j < list2.size) {
        merged.add(list2[j])
        j++
    }
    println(merged)

}

fun sortByBubble() {
    val list = mutableListOf(4, 2, 9, 3, 6)
    for (i in list.indices) {
        for (j in 0 until list.size - i - 1) {
            if (list[j] > list[j + 1]) {
                val temp = list[j]
                list[j] = list[j + 1]
                list[j + 1] = temp
            }
        }
    }
    println(list)
}


fun reverseInt() {
    val originalValue = 12345
    var reversedNumber = 0
    var temp = originalValue
    while (temp != 0) {
        val digit = temp % 10
        reversedNumber = reversedNumber * 10 + digit
        temp /= 10
    }
    println(reversedNumber)
}

fun getFactorial(): Int {
    val value = 4
    if (value == 0 || value == 1) {
        return 1
    }
    var factorial = 1
    for (i in 2..value) {
        factorial *= i
    }
    println("Factorial $factorial")
    return factorial
}

fun getLargest() {
    val list = mutableListOf(9, 4, 12, 1, 3, 0, 5)
    var largest = list[0]
    for (i in list.indices) {
        if (list[i] > largest) {
            largest = list[i]
        }
    }
    println(largest)
}


fun bubbleSort() {
    val list = mutableListOf(9, 4, 6, 1, 3, 0, 5)
    for (i in list.indices) {
        for (j in 0 until list.size - 1 - i) {
            if (list[j] > list[j + 1]) {
                val temp = list[j]
                list[j] = list[j + 1]
                list[j + 1] = temp
            }
        }
    }
    println(list)
}/*
fun bubbleSort() {
    val list = mutableListOf(9, 4, 6, 1, 3, 0, 5)
    for (i in list.indices) {
        for (j in 0..list.size - 1 - i) {
            if (list[i] > list[j]) {
                val temp = list[i]
                list[i] = list[j]
                list[j] = temp
            }
        }
    }
    println(list)
}*/


class InterviewModel(
    private val getData: GetData
) : ViewModel() {
    private var mService: MyBoundService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val mBinder = service as MyBoundService.MyBinder
            mService = mBinder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mService = null
            isBound = false
        }
    }

    private val _state = MutableStateFlow(MainData())
    val state = _state.asStateFlow()

    private val _students = MutableLiveData<MainData>(MainData())
    val students: LiveData<MainData>
        get() = _students

    fun changeText(text: String) {
        _state.update {
            it.copy(
                text = text
            )
        }
    }

    fun addItem() {
        val list = _state.value.list.toMutableList()
        list.add(list.size.toString())
        _state.update {
            it.copy(
                list = list, text = ""
            )
        }
    }

    fun btnClicked(context: Context) {
        factorial(25)
//        reverseString("Hello")
//        val isPalindrome = isPalindrome("Madam")
        val getSubString = getSubString("Madam", 0, 3)
        Log.d(
            TAG, "getSubString:${getSubString} "
        )
//        fetchFileDirectory(context)
    }

    fun bubbleSort() {
        val list = mutableListOf(9, 4, 6, 1, 3, 0, 5)
        for (i in list.indices) {
            for (j in 0..list.size - 1 - i) {
                if (list[i] > list[j]) {
                    val temp = list[i]
                    list[i] = list[j]
                    list[j] = temp
                }
            }
        }
        println(list)
    }

    fun reverseAnArray(list: List<Int>) {
        val newList = mutableListOf<Int>()
        for (i in list.size - 1 downTo 0) {
            newList.add(i)
        }
    }

    private fun getLargest(list: List<Int>): Int {
        var largest = 0
        for (i in list.indices) {
            if (list[i] > largest) {
                largest = list[i]
            }
        }
        return largest
    }

    private fun removeDuplicates(list: List<Int>): List<Int> {
        val newList = mutableListOf<Int>()
        for (item in list) {
            if (!newList.contains(item)) {
                newList.add(item)
            }
        }
        return newList
    }

    private fun selectionSort(list: List<Int>): List<Int> {
        val sortedList = listOf(5, 2, 3, 9, 0, 1, 3).toMutableList()
        val size = list.size
        for (i in sortedList.indices) {
            var minIndex = i
            for (j in i + 1 until size) {
                if (sortedList[j] < minIndex) {
                    minIndex = j
                }
            }
            val temp = sortedList[i]
            sortedList[i] = sortedList[minIndex]
            sortedList[minIndex] = temp
        }
        return sortedList
    }

    private fun bubbleSort(list: List<Int>): List<Int> {
        val sortedList = list.toMutableList()
        val size = list.size
        var swapped = false
        for (i in list.indices) {
            swapped = false
            for (j in 0 until size - i) {
                if (list[j] > list[j + 1]) {
                    val temp = list[j]
                    sortedList[j] = list[j + 1]
                    sortedList[j + 1] = temp
                    swapped = true
                }
            }
            if (!swapped) {
                break
            }
        }
        return sortedList
    }

    private fun getSubString(input: String, start: Int, end: Int): String {
        var subString = ""
        for (i in start until end) {
            subString += input[i]
        }
        return subString
    }

    private fun isPalindrome(input: String): Boolean {
        val filteredInput = input.lowercase()
        var reversed = ""
        for (i in filteredInput.length - 1 downTo 0) {
            reversed += filteredInput[i]
        }
        return reversed == filteredInput
    }

    private fun reverseString(msg: String): String {
        var reversed = ""
        for (i in msg.length - 1 downTo 0) {
            reversed += msg[i]
        }
        return reversed
    }

    private fun factorial(value: Int): Int {
        var factorial = 1
        if (value == 0 || value == 1) {
            return factorial
        }
        for (i in 2..value) {
            factorial *= i
        }
        return factorial
    }

    fun startService(context: Context) {
        Intent(
            context, MyBoundService::class.java
        ).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun fetchFileDirectory(context: Context) {
//        val path = context.getExternalFilesDir(null)?.absolutePath
//        val path = Environment.getExternalStorageDirectory().path
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        Log.d("cvv", "fetchFileDirectory: $path")
    }

    fun loadData() {
        _state.update {
            it.copy(
                isRequesting = true, students = emptyList()
            )
        }
        viewModelScope.launch {
            val results = getData.getStudents()
            _state.update {
                it.copy(
                    isRequesting = false, students = results
                )
            }
        }
    }

}

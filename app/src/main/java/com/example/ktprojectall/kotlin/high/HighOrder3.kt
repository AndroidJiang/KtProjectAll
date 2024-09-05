package com.example.ktprojectall.kotlin.high

fun main() {


    test1()
    test2()
    a()


}

fun show1(l1: () -> Unit, l2: () -> Unit, l3: () -> Unit) {
    l1()
    l2()
    l3()
}

fun show2(): String {
    return "EEEEEE"
}

fun a() = { a: String, b: Int ->
    "3333"
}

fun test1() {
    var names = listOf<String>("A", "B", "C")
    var ages = listOf<Int>(1, 2, 3)
    val mutableMapOf = mutableMapOf<String, Int>()
    names.forEachIndexed { index, s ->
        mutableMapOf[s] = ages[index]
    }
    val mutableListOf = mutableListOf<String>()
    for (mutableEntry in mutableMapOf) {
        var result = "name ${mutableEntry.key},age ${mutableEntry.value}"
        mutableListOf += result
    }
    for (s in mutableListOf) {
        println(s)
    }
}

fun test2() {
    listOf<String>("A", "B", "C").zip(listOf<Int>(1, 2, 3)).toMap()
        .map { println("name ${it.key},age ${it.value}") }
}



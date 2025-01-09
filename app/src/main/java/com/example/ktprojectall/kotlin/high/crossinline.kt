package com.example.ktprojectall.kotlin.high

import kotlin.concurrent.thread

inline fun test(crossinline block: () -> Unit) {
    println("After block")
    thread {
        block()
    }
}
fun main() {
    test {
        return@test // 非本地返回，直接返回到 main 函数，指定@test
    }
    println("This will not be printed")
    crossinlineTest({
        return
    },{
//        return  //crossinline限制后会报错
    })
}
inline fun crossinlineTest(block: () -> Unit, crossinline cross: () -> Unit) {
    println("crossinlineTest")
    thread {  //前提下
//        block() //没有crossinline限制会报错
        cross()
    }
}
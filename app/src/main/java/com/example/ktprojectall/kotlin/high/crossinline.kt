package com.example.ktprojectall.kotlin.high

import kotlin.concurrent.thread

inline fun test(crossinline block: () -> Unit) {
    println("After block")
    thread {  block()
    }
}

fun main() {
    test {
//        return // 非本地返回，直接返回到 main 函数
    }
    println("This will not be printed")
}
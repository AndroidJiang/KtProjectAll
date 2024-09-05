package com.example.ktprojectall.kotlin.high

//let（it） 和 run （this）
fun main() {
//    cao("3")(4)
//    cao2()({ str, n -> "" }, 1)
//    cao3()({ str, n -> "" }) { m, n -> 3 }
//    cao4<String, String, Int, String>()("1111", "222", { it.length }) {
//        it
//    }
    /*************/
    create {
        "111"
    }.trans {
        toInt()
    }.trans {
        plus(50)
    }.end {
        println(toString())
    }
    /*************/

    println(11111.myApply {
        plus(33333)
    }.myApply {
        plus(33333)
    })
    /*************/

    var list = listOf<Int>(1, 2, 3, 4, 5, 6)
    list.mforEach2 { print(toString()) }

    println()
    /*************/

    repeat2(10) {
        print(it)
    }
    println()
    /*************/
    var sss = "".takeIf2 { it.isEmpty() }
    println(Thread.currentThread())

    /*************/
    thread2 { println(Thread.currentThread()) }
}

fun <T> create(l1: () -> T): T {
    return l1()
}

fun <T, R> T.trans(l1: T.() -> R): R {
    return l1(this)
}

fun <T> T.end(l1: T.() -> Unit): Unit {
    l1(this)
}

fun <T> T.myApply(l: T.() -> Unit): T {
    l()
    return this
}

fun <T> Iterable<T>.mforEach(l: (T) -> Unit): Unit {
    for (item in this) {
        l(item)
    }
}

fun <T> Iterable<T>.mforEach2(l: T.() -> Unit): Unit {
    for (item in this) {
        l(item)
    }
}

fun <T> Iterable<T>.mforEach4(l: T.() -> Unit): Unit = run2 {
    for (item in this) {
        l(item)
    }
}

fun <R> run2(l: () -> R): R {
    return l()
}

fun repeat2(n: Int, l: (Int) -> Unit): Unit {
    for (i in 0 until n) {
        l(i)
    }
}

fun <T> T.takeIf2(l: (T) -> Boolean): T? {
    return if (l(this)) this else null
}

fun thread2(l: () -> Unit) {
    Thread { l() }.start()
}



//fun cao(str: String): (Int) -> Unit = { n: Int ->
//    var a = "3"
//    println(str.plus(n.toString()))
//}
//
//fun cao2(): ((((String, Int) -> String), Int) -> Unit) =
//    { l1: (String, Int) -> String, l2: Int
//        ->
//        println(l1("1", 2).plus(l2))
//    }
//
//fun cao3(): ((((String, Int) -> String), (Int, Int) -> Int) -> Unit) =
//    { l1: (String, Int) -> String, l2: (Int, Int) -> Int
//        ->
//        println(l1("1", 2).plus(l2(1, 1)))
//    }
//
//fun <T1, T2, R1, R2> cao4() = { t1: T1, t2: T2, l1: (T1) -> R1, l2: (T2) -> R2
//    ->
//    println(l1(t1))
//    println(l2(t2))
//}


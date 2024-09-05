package com.example.ktprojectall.kotlin.high

fun main() {
    //经典写法
    var method02_1: (Int) -> String = { n: Int -> n.toString() }
    println(method02_1(111))
    var method02_2: (Int) -> String = fun(n: Int) = n.toString()
    println(method02_2(222))
//很少使用fun(n)这种写法
    var method02_3: (Int) -> String = fun(n): String { return n.toString() }
    println(method02_3(333))
    /***********************fun相关****************************************/
//    fun method04(): () -> Unit ={}
//    fun method05() = ::a
    fun method06() = { a: Int, b: String -> println(a.toString() + b) }
    var method07 = { a: Int, b: String -> println(a.toString() + b) }
//    method05()(3, "4")
    method06()(5, "6")
    method07(7, "8")
//    a(9, "10")


    var method02_4 = fun(n: Int): (Int) -> String = { n: Int -> n.toString() }
//类似 var method02_1:         (Int) -> String = { n: Int -> n.toString() }
//等价于如下
    var method02_45: (Int) -> String = { n: Int -> n.toString() }
    fun method02_5(n: Int): (Int) -> String = { n: Int -> n.toString() }
    println(method02_4(4444))//错误
    println(method02_4(4444)(4444))//正确
    println(method02_5(2222)(5555))//正确

//难点
    var kkk: (String) -> (String) -> (Int) -> String =
        { a: String ->
            { b: String ->
                { c: Int ->
                    "输入长度：${c}"
                }
            }
        }
    println(kkk("33333")("3")(4))

    /*********************************************************************/

}

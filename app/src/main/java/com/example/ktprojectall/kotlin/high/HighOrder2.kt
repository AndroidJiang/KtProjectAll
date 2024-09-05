package com.example.ktprojectall.kotlin.high

fun main() {

    var result = login("AA", "123") { name, pwd ->
        name == "AA" && pwd == "123"
    }
    println(result)
    var result2 = login2("AA", "123") { name, pwd ->
        name == "AA" && pwd == "123"
    }
    println("!!!!!"+result2)
//
    var r = "AA".login("123") {
        it == "123"
    }
    println(r)
    "123".fuck {
        println(this)
    }
    "456".fuck2("333"){
        println(it)
    }

}

/*分清高阶函数后面是=还是{} 会导致返回值不一样*/
fun login(name: String, pwd: String, response: (String, String) -> Boolean) {
    response(name, pwd)
}
//想返回则
fun login1_1(name: String, pwd: String, response: (String, String) -> Boolean) :Boolean{
    return response(name, pwd)
}

fun login2(name: String, pwd: String, response: (String, String) -> Boolean) = response(name, pwd)


fun String.login(pwd: String, response: (pwd: String) -> Boolean) =
    response(pwd)


fun <T> T.fuck(a: T.() -> Unit) = a()
fun <T> T.fuck2(s:T,a: T.(T) -> Unit) = a(s)
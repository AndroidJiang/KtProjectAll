package com.example.ktprojectall.kotlin.high

fun main() {
    testMap()
    Util.doAction2()
    doSomeThing()
}

fun add(a: Int, b: Int): Int = add(a, b)
/***********************************************************************************
 *                       if语句
 ***********************************************************************************/
fun max(a: Int, b: Int): Int {
//    return a>b?a:b  // kotlin不支持三元运算符
    return if (a > b) a else b
}

fun max2(a: Int, b: Int) = if (a > b) a else b     //if可以返回结果

/***********************************************************************************
 *                       when语句
***********************************************************************************/

fun getScore(name: String): Int {
    return when (name) {
        "小明" -> {
            28
        }
        "小红" -> {
            69
        }
        is String -> {
            55
        }
        else -> {
            60
        }
    }
}

/**
 * 该场景上面案例无法实现，所有tom开头的都是28
 * @param name String
 * @return Int
 */
fun getScore2(name: String) = when {
    name.startsWith("Tom") -> 28
    else -> 0
}
/***********************************************************************************
                                     循环语句
***********************************************************************************/
val range = 0..10   //[0,10]
val range2=0 until 10 //[0,10)
val range3=10 downTo 0 //[10,0]

fun forIn(){
    for (i in 0..10){
        print(i)
    }
}

fun forStep(){
    for (i in range2 step 2){//递增3 相当于i=i+2
        print(i)
    }
}

/***********************************************************************************
                            对象编程
 ***********************************************************************************/
fun testHeap(){
    var p = Person(3)
    p.eat()

    var s = Student(33)
    s.readBooks()

}

fun testBean(){
    var phone1 = CellPhone("小米",1599)
    var phone2 = CellPhone("小米",1599)
    println(phone1)
    println(phone1 == phone2)

    Singleton.test()
}

fun testList(){
    var list = listOf("a","b","c")
    for (item in list){
        println(item)
    }
}
fun testMutableList(){
    var list = mutableListOf("a","b","c")
    list.add("d")
    for (item in list){
        println(item)
    }
}
fun testMap(){
    var map=HashMap<String,Int>()
    map["a"] = 1;
    map["b"] = 2;
    map["c"] = 3;

    for((a,b) in map){
        println("$a，$b")
    }
    var map2 = mapOf("a" to 1,"b" to 2,"c" to 3)

    for((a,b) in map2){
        println(a+"，"+b)
    }
}



package com.example.ktprojectall.kotlin.high

import android.content.ContentValues

fun doSomeThing() {
    print("33333")
}

fun main() {
    show({
        println("3333")
        "333"
    }) {
        println("2222")
        "222"
    }


    show2(3) {
        it.toString()
    }
    33.www {
        it.toString()
    }

    var sb = StringBuilder()
    "sb".let { }

/*************区分 let  run  with*******/
    var kkk = sb.aaa {
        it.append("3")
        it.append("4")
    }
    println(kkk)
    var ttt = sb.bbb {
        append("5")
        append("6")
    }
    println(ttt)
    var ppp = ccc(sb) {
        append("7")
        append("8")
    }
    println(ppp)
    /*****************************************/
    var c=ContentValues()
    with(c){
        put("a",1)
        put("b",2)
    }
}

fun show(l1: (Int) -> String, l2: (Int) -> String) {
    l1(100)
    l2(100)
}

fun show2(n: Int, action: (Int) -> String) {
    println(action(88))
}

inline fun <T> T.www(aa: (Int) -> String) {
    aa(5)
}

fun StringBuilder.aaa(aa: (StringBuilder) -> StringBuilder): StringBuilder {
    return aa(this)
}

fun StringBuilder.bbb(aa: StringBuilder.() -> StringBuilder): StringBuilder {
    return aa()
}

fun ccc(bb: StringBuilder, aa: StringBuilder.() -> StringBuilder): StringBuilder {
    return bb.aa()
}

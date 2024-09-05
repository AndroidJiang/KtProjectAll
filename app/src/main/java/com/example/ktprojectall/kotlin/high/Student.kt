package com.example.ktprojectall.kotlin.high

class Student(a:Int) : Person(a), Study {
    override fun readBooks() {
        println("Student readBooks")
    }
//    constructor(a:Int) : super(a)
}
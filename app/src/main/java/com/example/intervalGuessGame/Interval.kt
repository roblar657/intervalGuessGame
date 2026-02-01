package com.example.intervalGuessGame

import com.example.intervalGuessGame.interfaces.IInterval

class Interval(override var min: Int, override var max: Int)  : IInterval {

    override fun tooLow(value: Int) {
        min = value + 1
    }

    override fun tooHigh(value: Int) {
        max = value - 1
    }

    override fun isValid(): Boolean = min <= max

    override fun isInsideInterval(value: Int): Boolean = value >= min && value <= max

}

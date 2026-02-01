package com.example.intervalGuessGame.interfaces

/**
 * Et interval [a,b], tilpasset
 * det å gjette riktig tall.
 */
interface IInterval {
    val min: Int
    val max: Int

    /** Oppdaterer intervallet hvis tallet er for lavt */
    fun tooLow(value: Int)

    /** Oppdaterer intervallet hvis tallet er for høyt */
    fun tooHigh(value: Int)

    /** Sjekker om intervallet fortsatt er gyldig */
    fun isValid(): Boolean

    /** Sjekker om tallet er innenfor intervallet */
    fun isInsideInterval(value: Int): Boolean
}
package kirjanpito.ui

import java.math.BigDecimal
import java.text.DateFormat
import java.text.ParseException
import java.util.*

/**
 * Validation utilities for form inputs
 * Provides null-safe, idiomatic Kotlin validation functions
 */
object ValidationUtils {

    /**
     * Validates that string is not null or empty
     */
    fun String?.isNotNullOrEmpty(): Boolean {
        return !isNullOrEmpty()
    }

    /**
     * Validates that string is a valid number
     */
    fun String?.isValidNumber(): Boolean {
        if (isNullOrEmpty()) return false
        return try {
            toBigDecimal()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Safely parses string to BigDecimal
     */
    fun String?.toBigDecimalOrNull(): BigDecimal? {
        if (isNullOrEmpty()) return null
        return try {
            toBigDecimal()
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Validates that string is a valid date
     */
    fun String?.isValidDate(format: DateFormat): Boolean {
        if (isNullOrEmpty()) return false
        return try {
            format.parse(this)
            true
        } catch (e: ParseException) {
            false
        }
    }

    /**
     * Safely parses string to Date
     */
    fun String?.toDateOrNull(format: DateFormat): Date? {
        if (isNullOrEmpty()) return null
        return try {
            format.parse(this)
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * Validates that number is positive
     */
    fun BigDecimal?.isPositive(): Boolean {
        return this != null && this > BigDecimal.ZERO
    }

    /**
     * Validates that number is non-negative
     */
    fun BigDecimal?.isNonNegative(): Boolean {
        return this != null && this >= BigDecimal.ZERO
    }

    /**
     * Validates that string matches pattern
     */
    fun String?.matchesPattern(pattern: Regex): Boolean {
        return this?.matches(pattern) ?: false
    }

    /**
     * Trims string and returns null if empty
     */
    fun String?.trimOrNull(): String? {
        val trimmed = this?.trim()
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }
}

/**
 * Extension function for String validation
 */
fun String?.requireNotEmpty(fieldName: String = "Kenttä"): String {
    require(!isNullOrEmpty()) { "$fieldName ei voi olla tyhjä" }
    return this
}

/**
 * Extension function for BigDecimal validation
 */
fun BigDecimal?.requirePositive(fieldName: String = "Arvo"): BigDecimal {
    require(this != null && this > BigDecimal.ZERO) { "$fieldName täytyy olla positiivinen" }
    return this
}

/**
 * Extension function for BigDecimal validation
 */
fun BigDecimal?.requireNonNegative(fieldName: String = "Arvo"): BigDecimal {
    require(this != null && this >= BigDecimal.ZERO) { "$fieldName ei voi olla negatiivinen" }
    return this
}

package `in`.okcredit.sales_ui.utils

import tech.okcredit.android.base.language.LocaleManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class SalesUtil {

    companion object {

        fun currencyDisplayFormat(amount: Double): String {
            return format("##,##,###.##", amount)
        }

        fun eval(str: String): Long {
            return object : Any() {
                var pos = -1
                var ch = 0
                fun nextChar() {
                    ch = if (++pos < str.length) str[pos].toInt() else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.toInt()) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Long {
                    nextChar()
                    val x = parseExpression()
                    if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return Math.round(x * 100)
                }

                // Grammar:
                // expression = term | expression `+` term | expression `-` term
                // term = factor | term `*` factor | term `/` factor
                // factor = `+` factor | `-` factor | `(` expression `)`
                //        | number | functionName factor | factor `^` factor
                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+'.toInt())) x += parseTerm() // addition
                        else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                        else return x
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*'.toInt())) x *= parseFactor() // multiplication
                        else if (eat('/'.toInt())) x /= parseFactor() // division
                        else return x
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.toInt())) return parseFactor() // unary plus
                    if (eat('-'.toInt())) return -parseFactor() // unary minus
                    var x: Double
                    val startPos = pos
                    if (eat('('.toInt())) { // parentheses
                        x = parseExpression()
                        eat(')'.toInt())
                    } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                        while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                        x = str.substring(startPos, pos).toDouble()
                    } else {
                        throw RuntimeException("Unexpected: " + ch.toChar())
                    }
                    if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
                    return x
                }
            }.parse()
        }

        fun findLastIndexOfDigit(text: String?): Int {
            if (text == null) {
                return -1
            }
            var lastIndex = -1
            for (i in text.length downTo 1) {
                val lastChar = text.substring(i - 1)
                if (Character.isDigit(lastChar.toCharArray()[0])) {
                    lastIndex = i
                    break
                }
            }
            return lastIndex
        }

        fun formatV2(amount: Long): String? {
            var amount = amount
            if (amount < 0L) {
                amount *= -1
            }
            val fraction = amount % 100
            val fractionString: String
            fractionString = if (fraction == 0L) {
                ""
            } else if (fraction < 10) {
                ".0$fraction"
            } else {
                ".$fraction"
            }
            amount = amount / 100
            return if (amount < 1000) {
                String.format("%s%s", format("###", amount), fractionString)
            } else {
                val hundreds = amount % 1000.toDouble()
                val other = (amount / 1000).toInt()
                String.format("%s,%s%s", format(",##", other), format("000", hundreds), fractionString)
            }
        }

        private fun format(pattern: String, value: Any): String {
            val symbols =
                DecimalFormatSymbols(LocaleManager.englishLocale)
            return DecimalFormat(pattern, symbols).format(value)
        }

        fun displayDecimalNumber(number: Double): String {
            val value = number.toInt()
            if (number - value != 0.00) {
                return String.format(Locale.ENGLISH, "%.2f", number)
            }
            return value.toString()
        }

        fun isDecimal(d: Double): Boolean {
            if (d.toInt() - d != 0.0) {
                return true
            }
            return false
        }
    }
}

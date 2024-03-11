package com.example.calculadora10

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.calculadora10.databinding.ActivityMainBinding
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.ArithmeticException
import javax.xml.xpath.XPathExpression
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    var btn_elimin: TextView ?= null
    var resul_tv: TextView ?= null
    var data_tv: TextView ?= null
    private lateinit var binding: ActivityMainBinding
    var lastNumeric = false
    var stateError = false
    var lastDot = false

    private lateinit var expression: Expression

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resul_tv = findViewById(R.id.resul_tv)
        data_tv = findViewById(R.id.data_tv)
        btn_elimin = findViewById(R.id.btn_elimin)
    }

    fun eliminarDigito(view: View) {
        if (data_tv?.text?.isNotEmpty() == true) {
            val textoActual = data_tv?.text.toString()
            val nuevoTexto = textoActual.substring(0, textoActual.length - 1)
            data_tv?.text = if (nuevoTexto.isNotEmpty()) nuevoTexto else "0"
        }
    }

    fun Calcular(view: View){

        var boton = view as Button
        var textboton = boton.text.toString()
        var concatenar = data_tv?.text.toString()+textboton
        var mostrar = quitarcerosIzquierda(concatenar)
        var mostrar2 = agregarcerosIzquierda(concatenar)
        if(textboton == "="){
            var resultado = 0.0
            try {
                resul_tv?.text = data_tv?.text
                resultado = eval(resul_tv?.text.toString())
                resul_tv?.text = resultado.toString()

            }catch (e : Exception){
                resul_tv?.text = e.toString()
            }


        }else if (textboton == "AC"){

            resul_tv?.text = ""
            data_tv?.text = "0"
            resul_tv?.visibility = View.GONE


        }else{
            resul_tv?.visibility = View.VISIBLE
            data_tv?.text = mostrar

        }

    }

    fun quitarcerosIzquierda(str : String) : String {

        var i = 0
        var firstNonZero = -1

        while (i < str.length) {
            if (str[i] == '0' && (i + 1 == str.length || str[i + 1] == '.')) {
                // Mantener el 0 a la izquierda del punto
                firstNonZero = i
                break
            } else if (str[i] != '0') {
                firstNonZero = i
                break
            }
            i++
        }

        if (firstNonZero >= 0) {
            val sb = StringBuffer(str)
            sb.replace(0, firstNonZero, "")
            return sb.toString()
        } else {
            // La cadena consiste solo en ceros
            return "0"
        }
    }

    fun agregarcerosIzquierda(str : String) : String {

        var i = 0
        while (i<str.length && str[i] == '0')i++
        val sb = StringBuffer(str)
        sb.replace(0,i, "0")
        return sb.toString()

    }

    fun eval(str: String): Double {
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

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

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
                    else if (eat('%'.toInt())) x = x * 0.01 // percentage
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
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> Math.sqrt(x)
                        "sin" -> Math.sin(Math.toRadians(x))
                        "cos" -> Math.cos(Math.toRadians(x))
                        "tan" -> Math.tan(Math.toRadians(x))
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
                return x
            }
        }.parse()
    }

}













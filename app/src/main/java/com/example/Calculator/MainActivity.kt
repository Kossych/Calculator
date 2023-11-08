package com.example.Calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.Calculator.databinding.ActivityMainBinding
import java.security.InvalidParameterException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var lastIsOperation: Boolean = true
    private var lastIsDecimal: Boolean = true
    private var hasntNumber: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun numberAction(view: View)
    {
        if(view is Button)
        {
            if(view.text == ".")
            {
                if(lastIsDecimal) return
                lastIsDecimal = true
            }
            binding.workingsTV.append(view.text)
            lastIsOperation = false
            hasntNumber = false
        }
    }

    fun operationAction(view: View)
    {
        if(hasntNumber) return
        if(view is Button) {
            if(lastIsOperation)
                binding.workingsTV.text = binding.workingsTV.text.replace(".$".toRegex(), "")
            binding.workingsTV.append(view.text)
            lastIsOperation = true
            lastIsDecimal = false
        }
    }

    fun allClearAction(view: View)
    {
        binding.resultsTV.text = ""
        binding.workingsTV.text = ""
    }

    fun backSpaceAction(view: View)
    {
        val length = binding.workingsTV.length()
        if(length > 0)
            binding.workingsTV.text = binding.workingsTV.text.subSequence(0, length - 1)
    }

    fun equalsAction(view: View)
    {
        binding.resultsTV.text = calculateResult()
    }

    private fun calculateResult(): String
    {
        return evaluateExpression(getPolishNotation(digitsOperators())).toString()
    }

    private fun digitsOperators(): MutableList<ISymbol>
    {
        val formula = mutableListOf<ISymbol>()
        var currentDigit = ""
        for(character in binding.workingsTV.text)
        {
            if(character.isDigit() || character == '.')
                currentDigit += character
            else
            {
                formula.add(Number(currentDigit.toFloat()))
                currentDigit = ""
                formula.add(when(character)
                {
                    '/' -> Operator(1){left: Float, right:Float -> left / right}
                    'x' -> Operator(1){left: Float, right:Float -> left * right}
                    '%' -> Operator(1){left: Float, right:Float -> left % right}
                    '+' -> Operator(2){left: Float, right:Float -> left + right}
                    '-' -> Operator(2){left: Float, right:Float -> left - right}
                    else -> throw InvalidParameterException()
                })

            }
        }

        if(currentDigit != "")
            formula.add(Number(currentDigit.toFloat()))

        return formula
    }

    private fun getPolishNotation(formula: MutableList<ISymbol>): MutableList<ISymbol>{
        val result = mutableListOf<ISymbol>()
        val operators = mutableListOf<Operator>()
        for(symbol in formula)
        {
            if(symbol is Number){
                result.add(symbol)
                continue
            }

            val symbolOperation = symbol as Operator

            do{
                if(operators.isEmpty()) break
                val topOperation: Operator = operators.last()

                if(topOperation.order <= symbolOperation.order)
                {
                    result.add(operators.removeLast())
                    continue
                }
                break
            }while(operators.isNotEmpty())
            operators.add(symbol)
        }

        while(operators.isNotEmpty())
            result.add(operators.removeLast())
        println("getPolishNotation")
        return result
    }

    private fun evaluateExpression(expression: MutableList<ISymbol>): Float
    {
        val number: MutableList<Float> = mutableListOf()
        var right: Float = 0f
        var left: Float = 0f
        println("evaluateExpression")
        while(expression.isNotEmpty())
        {
            var symbol = expression.removeAt(0)
            if(symbol is Number)
            {
                number.add(symbol.numberValue)
                continue
            }
            val symbolOp = symbol as Operator

            right = number.removeLast()
            left = number.removeLast()
            val resOperation = symbolOp.operation(left, right)

            number.add(resOperation)
        }
        return number.last()
    }

}

interface ISymbol{}

class Number(var numberValue: Float): ISymbol{}

class Operator(val order: Int, val operation: (Float, Float) -> Float): ISymbol{}


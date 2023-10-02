package com.example.adivinaartistas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.airbnb.lottie.LottieAnimationView
import com.github.javafaker.Color
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexboxLayout
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var txtPregunta:TextView
    private var respuesta:String = ""
    private lateinit var flexAlfabeto:FlexboxLayout
    private lateinit var flexResponse:FlexboxLayout
    private var indicesOcupados:ArrayList<Int> = arrayListOf()
    private var intentosPermitidos:Int = 0
    private var intentosHechos:Int = 0
    private lateinit var txtCantIntentos:TextView
    private lateinit var txtMsjIntentos:TextView
    private var finalizado:Boolean = false
    private lateinit var lottieResult:LottieAnimationView
    private lateinit var lotieAnimThinking:LottieAnimationView
    private lateinit var textMsjResultado:TextView
    private lateinit var txtMsjRespuestaCorrecta:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Mostrar el Splash antes de que se le asigne a esta pantalla su recurso de diseño
        installSplashScreen()

        setContentView(R.layout.activity_main)

        //widgets
        txtPregunta = findViewById(R.id.txtPregunta)
        lotieAnimThinking = findViewById(R.id.animation_view_thik)
        flexResponse = findViewById(R.id.edt)
        flexAlfabeto = findViewById(R.id.flexboxLayout)
        txtCantIntentos = findViewById(R.id.txtCantIntentos)
        txtMsjIntentos = findViewById(R.id.txtMsjIntentos)
        lottieResult = findViewById(R.id.animation_view_resultado)
        textMsjResultado = findViewById(R.id.txtMsjResultado)
        txtMsjRespuestaCorrecta = findViewById(R.id.txtMsjRespuestaCorrecta)

        //Boton de reinicio
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)

        btnSiguiente.setOnClickListener {
            reiniciarAdivinanza()
        }

        //Boton Cerrar App
        val btnCerrarApp = findViewById<Button>(R.id.btnCerrarApp)
        btnCerrarApp.setOnClickListener {
            cerrarApp() }


        //1. generar palabra a adivinar
        //1.1 la cantidad de intentos permitidos se le dara: tamaño de caracteres + 2
        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 2
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
        //2. generar alfabeto que incluya las letras de la palabra a adivinar
        val alfabeto = generarAlfabeto(respuesta)
        //3. desordenar el alfabeto generado para que sea mas dinamica
        val alfabetoDesorden = desordenar(alfabeto)
        //4. generar los espacios donde se iran mostrando la respuesta
        mostrarEspacioRespuesta(respuesta.length, flexResponse)
        //5. mostrar en la vista cada letra generada como boton para que se pueda seleccionar
        mostrarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)

    }

    //Función que a partir de una palabra dada(la respuesta) nos genere una especie de alfabeto.
    fun generarAlfabeto(semilla: String):String {
        val randomValues = List(5) { Random.nextInt(65, 90).toChar() }
        return "$semilla${randomValues.joinToString(separator = "")}"
    }

    //Función que agarre la especie de alfabeto y lo desordene
    fun desordenar(theWord: String):String {
        val theTempWord=theWord.toMutableList()
        for (item in 0..Random.nextInt(1,theTempWord.count()-1))
        {
            val indexA=Random.nextInt(theTempWord.count()-1)
            val indexB=Random.nextInt(theTempWord.count()-1)
            val temp=theTempWord[indexA]
            theTempWord[indexA]=theTempWord[indexB]
            theTempWord[indexB]=temp
        }
        return theTempWord.joinToString(separator = "")
    }

    //para generar de forma aleatoria un nombre de un artista
    fun obtenerPalabraAleatoria(): String {
        val faker = Faker()
        val palabra = faker.artist().name()
        return palabra.split(' ').get(0) //a veces devuelve nombres compuestos
    }

    //Función para mostrar dinámicamente los espacios donde se iran mostrando cada una de las letras de
    //la respuesta cuando aciertes y selecciones una letra correcta
    fun mostrarEspacioRespuesta(cantidad:Int, vista:FlexboxLayout){
        for (letter in 1..cantidad) {
            val btnLetra = EditText(this)
            btnLetra.isEnabled = false
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
        }
    }

    //Función para mostrar dinámicamente en la vista el alfabeto generado en el paso 1 y que se vea en
    //forma de columnas y filas mediante FlexboxLayout.
    fun mostrarAlfabeto(alfabeto:String, vista:FlexboxLayout){
        for (letter in alfabeto) {
            val btnLetra = Button(this)
            btnLetra.text = letter.toString()
            btnLetra.textSize = 12f
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
            btnLetra.setOnClickListener{
                clickLetra(it as Button)
            }
        }
    }

     //escuchador(listener) para cuando se presione clic en
    // cada una de las letras del alfabeto verificar si es una letra correcta de la respuesta
    fun clickLetra(btnClicked:Button) {
        if(!finalizado){
        //obtener el indice de la letra seleccionada inicialmente
            var starIndex = 0
            var resIndex = respuesta.indexOf(btnClicked.text.toString())
        //si el indice ya fue ocupado entonces no tomar en cuenta los indices hacia atras
            while(indicesOcupados.contains(resIndex)){
                starIndex = resIndex + 1
                resIndex = respuesta.indexOf(btnClicked.text.toString(), starIndex)
            }
        //si la respuesta contiene la letra seleccionada
            if(resIndex != -1){
                val flexRow = flexResponse.get(resIndex) as EditText
                flexRow.setText( respuesta.get(resIndex).toString())
                indicesOcupados.add(resIndex)
                btnClicked.setBackgroundColor(android.graphics.Color.GREEN)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(android.graphics.Color.WHITE)
            }
            else{
                Toast.makeText(applicationContext, "No es una letra valida",
                    Toast.LENGTH_SHORT).show()
                btnClicked.setBackgroundColor(android.graphics.Color.RED)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(android.graphics.Color.WHITE)
            }
            intentosHechos++
            txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
            verificarResultado()
        }
    }

    //función verificar resultado, la cual, valida si se te terminaron los intentos o bien si ya adivinaste
    fun verificarResultado(){
        if (intentosHechos == intentosPermitidos || indicesOcupados.size == respuesta.length){
            finalizado = true

        //si gano o perdió
            if (indicesOcupados.size == respuesta.length){
                lottieResult.setAnimation(R.raw.winner)
                textMsjResultado.text = "Felicidades!"
            }
            else{
                lottieResult.setAnimation(R.raw.lost)
                textMsjResultado.text = "Perdiste :("
            }
            txtMsjRespuestaCorrecta.setText("La respuesta correcta es: $respuesta")
        //despues de configurar la vista ponerlas como visibles
            textMsjResultado.visibility = View.VISIBLE
            lottieResult.visibility = View.VISIBLE
            txtMsjRespuestaCorrecta.visibility = View.VISIBLE
        //ocultar los que no se deben mostrar
            flexResponse.visibility = View.GONE
            txtCantIntentos.visibility = View.GONE
            flexAlfabeto.visibility = View.GONE
            txtMsjIntentos.visibility = View.GONE
            txtPregunta.visibility = View.GONE
            lotieAnimThinking.visibility = View.GONE
        }
    }

    //Funcion Reiniciar Adivinanza
    private fun reiniciarAdivinanza() {
        // Restablecer variables y elementos de la interfaz de usuario
        finalizado = false
        intentosHechos = 0
        indicesOcupados.clear()
        flexResponse.removeAllViews()
        flexAlfabeto.removeAllViews()
        textMsjResultado.visibility = View.GONE
        lottieResult.visibility = View.GONE
        txtMsjRespuestaCorrecta.visibility = View.GONE
        flexResponse.visibility = View.VISIBLE
        txtCantIntentos.visibility = View.VISIBLE
        flexAlfabeto.visibility = View.VISIBLE
        txtMsjIntentos.visibility = View.VISIBLE
        txtPregunta.visibility = View.VISIBLE
        lotieAnimThinking.visibility = View.VISIBLE

        // Generar una nueva adivinanza
        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 2
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"

        // Generar un nuevo alfabeto que incluya las letras de la nueva respuesta
        val alfabeto = generarAlfabeto(respuesta)
        val alfabetoDesorden = desordenar(alfabeto)

        // Mostrar los espacios para la nueva respuesta
        mostrarEspacioRespuesta(respuesta.length, flexResponse)

        // Mostrar el nuevo alfabeto en la vista
        mostrarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)

        // También puedes configurar el texto de la pregunta o descripción aquí si es necesario
        // txtPregunta.text = "Nueva pregunta o descripción"
    }

    //Funcion Cerrar App
    private fun cerrarApp() {
        finish() // Cierra la actividad actual
    }

}

package com.proxlu.starbreak

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.manalkaff.jetstick.JoyStick
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
// import androidx.compose.foundation.layout.wrapContentSize
// import androidx.compose.runtime.DisposableEffect

class MainActivity : ComponentActivity(), LifecycleObserver {

    // private lateinit var webView: WebView
    private var isJoyStickVisible by mutableStateOf(true)
    private var isAppInBackground = false
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        // Configurar a orientação da tela para paisagem
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Criar o layout Compose
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    WebViewContent()
                    JoyStickContent()
                    ShowKeyboardButton()
                }
            }
        }

        // Inicializar o AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Registrar o observador do ciclo de vida
        lifecycle.addObserver(this)
    }

    // Função para manipular o volume com base no estado do aplicativo em segundo plano ou primeiro plano
    private fun handleVolume() {
        if (isAppInBackground) {
            // Mute o volume quando o aplicativo estiver em segundo plano
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        } else {
            // Desmute o volume quando o aplicativo estiver em primeiro plano
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        }
    }

    // Função para verificar se o aplicativo está em segundo plano
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isAppInBackground = false
        handleVolume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        isAppInBackground = true
        handleVolume()
    }

    @Composable
    fun WebViewContent() {
        val webViewHeight = 768.dp
        val context = LocalContext.current
        val webView = rememberWebViewWithCustomConfiguration(context)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // Cor de fundo preta para destacar a área da WebView
        ) {
            Column(
                modifier = Modifier
                    .height(webViewHeight)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AndroidView(
                    factory = { webView },
                    update = {
                        it.settings.javaScriptEnabled = true
                        it.settings.domStorageEnabled = true
                        it.settings.databaseEnabled = true
                        it.settings.allowFileAccess = true
                        it.settings.loadWithOverviewMode = true
                        it.settings.useWideViewPort = true
                        it.settings.setSupportMultipleWindows(true)
                        it.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                        it.settings.displayZoomControls = false
                        it.settings.setUseWideViewPort(true)
                        it.isVerticalScrollBarEnabled = false
                        it.isHorizontalScrollBarEnabled = false
                        it.requestFocus()
                        it.isFocusableInTouchMode = true
                        it.webViewClient = NewWebViewClient()
                        it.loadUrl("https://www.starbreak.com/")
                    }
                )
            }
        }
    }

    private class NewWebViewClient : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        // Executar JavaScript para remover a div com a classe "footer"
        val jsCode = """
            var elements = document.getElementsByClassName('footer');
            for (var i = 0; i < elements.length; i++) {
                elements[i].parentNode.removeChild(elements[i]);
            }
        """.trimIndent()
        view?.evaluateJavascript(jsCode, null)

        // Executar JavaScript para remover a tag "br"
        val jsTag = """
        var elements = document.getElementsByTagName("br");
        for (var i = 0; i < elements.length; i++) {
            elements[i].parentNode.removeChild(elements[i]);
        }
        """.trimIndent()
        view?.evaluateJavascript(jsTag, null)
    }
}

    @Composable
    private fun rememberWebViewWithCustomConfiguration(context: android.content.Context): WebView {
        return remember(context) {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    @Composable
    fun ShowKeyboardButton() {
        val context = LocalContext.current
        // val rootView = (LocalContext.current as? ComponentActivity)?.window?.decorView?.rootView

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        isJoyStickVisible = !isJoyStickVisible
                    },
                    modifier = Modifier
                        //.wrapContentSize()  // Defina o tamanho do conteúdo
                        .border(
                            3.dp,
                            Color(0xFFFFFFFF),
                            shape = CircleShape
                        ),  // Alinhe na parte inferior central
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14E4FC))
                ) {
                    Text(text = "\uD83D\uDC41️", fontSize = 20.sp)
                }
                Button(
                    onClick = {
                        val inputMethodManager =
                            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                        // Mostrar o teclado virtual
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    },
                    modifier = Modifier
                        //.wrapContentSize()  // Defina o tamanho do conteúdo
                        .border(
                            3.dp,
                            Color(0xFFFFFFFF),
                            shape = CircleShape
                        ),  // Alinhe na parte inferior central
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14E4FC))
                ) {
                    Text(text = "⌨️", fontSize = 20.sp)
                }
            }
        }
    }

    @Composable
    fun JoyStickContent() {

        fun onButtonClick(label: String, isPress: Boolean) {
            Log.d("ButtonClicked", "Button $label ${if (isPress) "pressed" else "released"}")

            // Mapear os rótulos dos botões para os códigos de tecla correspondentes
            val buttonKeyCodeMap = mapOf(
                " " to KeyEvent.KEYCODE_SPACE,
                "D" to KeyEvent.KEYCODE_D,
                "S" to KeyEvent.KEYCODE_S,
                "A" to KeyEvent.KEYCODE_A,
                "H" to KeyEvent.KEYCODE_H,
                "L" to KeyEvent.KEYCODE_SHIFT_LEFT,
                "T" to KeyEvent.KEYCODE_TAB,
                "C" to KeyEvent.KEYCODE_C,
                "E" to KeyEvent.KEYCODE_ESCAPE
            )

            // Obter o código de tecla correspondente ao rótulo do botão
            val keyCode = buttonKeyCodeMap[label] ?: return

            // Simular a tecla pressionada ou liberada com base no estado
            if (isPress) {
                GlobalScope.launch {
                    window.decorView.dispatchKeyEvent(KeyEvent(0, keyCode))  // Pressiona a tecla
                    delay(1000)  // Atraso de 1 segundo (1000 milissegundos)
                    window.decorView.dispatchKeyEvent(KeyEvent(1, keyCode))  // Libera a tecla
                }
            }
        }

        // Função para lidar com a direção do JoyStick
        fun onJoyStick(x: Float, y: Float, isPress: Boolean) {
            Log.d("JoyStick", "JS x: $x, y: $y ${if (isPress) "pressed" else "released"}")

            // Lógica para a direção direita
            val rightArrowCode = KeyEvent.KEYCODE_DPAD_RIGHT
            val rightEvent = if (x > 25) KeyEvent(0, rightArrowCode) else KeyEvent(1, rightArrowCode)
            window.decorView.dispatchKeyEvent(rightEvent)

            // Lógica para a direção esquerda
            val leftArrowCode = KeyEvent.KEYCODE_DPAD_LEFT
            val leftEvent = if (x < -25) KeyEvent(0, leftArrowCode) else KeyEvent(1, leftArrowCode)
            window.decorView.dispatchKeyEvent(leftEvent)

            // Lógica para a direção baixo
            val downArrowCode = KeyEvent.KEYCODE_DPAD_DOWN
            val downEvent = if (y < -25) KeyEvent(0, downArrowCode) else KeyEvent(1, downArrowCode)
            window.decorView.dispatchKeyEvent(downEvent)

            // Lógica para a direção cima
            val upArrowCode = KeyEvent.KEYCODE_DPAD_UP
            val upEvent = if (y > 25) KeyEvent(0, upArrowCode) else KeyEvent(1, upArrowCode)
            window.decorView.dispatchKeyEvent(upEvent)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Adicionar o JoyStick na parte inferior esquerda
                if (isJoyStickVisible) {
                    JoyStick(
                        Modifier
                            .padding(10.dp)
                            .weight(1f),
                            //.fillMaxWidth(),
                        size = 150.dp,
                        dotSize = 75.dp
                    ) { x, y ->
                        Log.d("JoyStick", "$x, $y")
                        // Lógica do JoyStick
                        val rightJoyStickPressed = x > 25
                        onJoyStick(x, y, rightJoyStickPressed)
                        val leftJoyStickPressed = x < -25
                        onJoyStick(x, y, leftJoyStickPressed)
                        val downJoyStickPressed = y < -25
                        onJoyStick(x, y, downJoyStickPressed)
                        val upJoyStickPressed = y > 25
                        onJoyStick(x, y, upJoyStickPressed)
                    }
                }
                // Adicionar os botões em uma grade à direita
                if (isJoyStickVisible) {
                    Grid(
                        items = listOf(" ", "D", "S", "A", "H", "L", "T", "C", "E"),
                        rows = 3,
                        columns = 3,
                    ) { label ->
                        Button(
                            onClick = {
                                // Chamar a função onButtonClick com o rótulo do botão
                                onButtonClick(label, true)
                            },
                            modifier = Modifier
                                .border(3.dp, Color(0xFFFFFFFF), shape = CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14E4FC))
                        ) {
                            Text(
                                text = label,
                                color = Color(0xFFEEEEEE),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun <T> Grid(
        items: List<T>,
        rows: Int,
        columns: Int,
        modifier: Modifier = Modifier,
        content: @Composable (item: T) -> Unit
    ) {
        Column(modifier = modifier) {
            repeat(rows) { rowIndex ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    repeat(columns) { columnIndex ->
                        val index = rowIndex * columns + columnIndex
                        if (index < items.size) {
                            content(items[index])
                        } else {
                            // Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
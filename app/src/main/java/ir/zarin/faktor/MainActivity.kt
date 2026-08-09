package ir.zarin.faktor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ir.zarin.faktor.ui.ZarinApp as ZarinAppUi
import ir.zarin.faktor.ui.theme.ZarinFaktorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ZarinFaktorTheme {
                ZarinAppUi()
            }
        }
    }
}

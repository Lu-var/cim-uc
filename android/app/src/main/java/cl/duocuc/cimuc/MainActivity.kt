package cl.duocuc.cimuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duocuc.cimuc.navigation.AppNavigation
import cl.duocuc.cimuc.ui.theme.CimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as CimApplication
        setContent {
            CimTheme {
                AppNavigation(
                    cimRepository = application.cimRepository,
                    contactoRepository = application.contactoRepository
                )
            }
        }
    }
}

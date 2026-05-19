package com.echo.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.echo.app.ui.home.HomeScreen
import com.echo.app.ui.home.HomeViewModel
import com.echo.app.ui.settings.SettingsScreen
import com.echo.app.data.repository.EchoRepository
import com.echo.app.ui.settings.SettingsViewModel
import com.echo.app.util.DataExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import android.widget.Toast
import com.echo.app.util.DataImportHelper
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.GlobalContext

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun EchoNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = koinViewModel()
            val repository: EchoRepository = GlobalContext.get().get()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onExport = {
                    scope.launch {
                        val records = withContext(Dispatchers.IO) {
                            repository.observeAll().first()
                        }
                        DataExportHelper.exportToJson(context, records)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "已导出到下载目录", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onImport = { uri: Uri ->
                    scope.launch {
                        try {
                            val dao = GlobalContext.get().get<com.echo.app.data.db.EchoRecordDao>()
                            val count = withContext(Dispatchers.IO) {
                                DataImportHelper.importFromJson(context, uri, dao)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "导入了 $count 条记录", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "导入失败：${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

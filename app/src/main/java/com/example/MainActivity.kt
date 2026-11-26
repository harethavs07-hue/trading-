package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HowToPlayModal
import com.example.ui.components.ProUnlockModal
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ArenaGameplayScreen
import com.example.ui.screens.LeaderboardAndTournamentScreen
import com.example.ui.theme.BullishNeonMint
import com.example.ui.theme.CardElevated
import com.example.ui.theme.ChartArenaTheme
import com.example.ui.theme.OuterBackground
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChartArenaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(OuterBackground.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(OuterBackground.toArgb())
        )
        setContent {
            ChartArenaTheme {
                val viewModel: ChartArenaViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.statusBannerMessage) {
                    val msg = uiState.statusBannerMessage
                    if (!msg.isNullOrBlank()) {
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusBanner()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OuterBackground),
                    containerColor = OuterBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = CardElevated,
                                contentColor = BullishNeonMint
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentScreen) {
                            AppScreen.ARENA -> ArenaGameplayScreen(
                                uiState = uiState,
                                onSelectCategory = viewModel::selectCategory,
                                onSelectGameMode = viewModel::selectGameMode,
                                onToggleCurrency = viewModel::toggleCurrency,
                                onOpenProModal = viewModel::openProModal,
                                onToggleMute = viewModel::toggleSoundMute,
                                onOpenLeaderboard = { viewModel.navigateTo(AppScreen.LEADERBOARD_TOURNAMENT) },
                                onOpenGuide = viewModel::openHowToPlayModal,
                                onOpenAdmin = { viewModel.navigateTo(AppScreen.OWNER_ADMIN) },
                                onSelectDirection = viewModel::selectDirection,
                                onLockIn = viewModel::lockInPrediction,
                                onNextChart = viewModel::generateNextChart,
                                onResetProgress = viewModel::resetPlayerProgress
                            )

                            AppScreen.LEADERBOARD_TOURNAMENT -> LeaderboardAndTournamentScreen(
                                ownerData = uiState.ownerData,
                                todayDate = viewModel.todayDate(),
                                isProUnlocked = uiState.stats.isProDemoUnlocked,
                                onBack = { viewModel.navigateTo(AppScreen.ARENA) },
                                onOpenProModal = viewModel::openProModal
                            )

                            AppScreen.OWNER_ADMIN -> AdminDashboardScreen(
                                ownerData = uiState.ownerData,
                                isUnlocked = uiState.isAdminUnlocked,
                                todayDate = viewModel.todayDate(),
                                uniqueChartsCount = uiState.uniqueChartsPlayedCount,
                                onUnlockWithPin = viewModel::unlockAdminWithPin,
                                onLockAdmin = viewModel::lockAdmin,
                                onBack = { viewModel.navigateTo(AppScreen.ARENA) },
                                onUpsertPlayer = viewModel::upsertPlayer,
                                onDeletePlayer = viewModel::deletePlayer,
                                onUpdateRevenue = viewModel::updateMonthlyRevenue,
                                onUpsertWinner = viewModel::upsertDailyWinner,
                                onDeleteWinner = viewModel::deleteDailyWinner,
                                onUpsertTournamentEntry = viewModel::upsertTournamentEntry,
                                onDeleteTournamentEntry = viewModel::deleteTournamentEntry,
                                onUpdateTournamentMeta = viewModel::updateTournamentDetails,
                                onUpdateSimilarityThreshold = viewModel::updateSimilarityThreshold,
                                onClearChartHistory = viewModel::clearChartHistory,
                                onExportCsv = viewModel::exportCsvString,
                                onExportJsonBackup = viewModel::exportJsonBackupString,
                                onImportJsonBackup = viewModel::importJsonBackup,
                                onToastMessage = viewModel::showMessage
                            )
                        }

                        if (uiState.showProModal) {
                            ProUnlockModal(
                                isAlreadyUnlocked = uiState.stats.isProDemoUnlocked,
                                onTryDemoUnlock = viewModel::unlockProDemo,
                                onDismiss = viewModel::dismissProModal
                            )
                        }

                        if (uiState.showHowToPlayModal) {
                            HowToPlayModal(
                                onDismiss = viewModel::dismissHowToPlayModal,
                                onJumpToMode = { mode ->
                                    viewModel.dismissHowToPlayModal()
                                    viewModel.selectGameMode(mode)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

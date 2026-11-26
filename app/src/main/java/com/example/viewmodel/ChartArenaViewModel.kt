package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ArcadeSoundEngine
import com.example.data.ArenaRepository
import com.example.engine.AntiRepetitionEngine
import com.example.engine.ChartGeneratorEngine
import com.example.model.ChartChallenge
import com.example.model.CurrencyMode
import com.example.model.DailyWinnerRecord
import com.example.model.GameMode
import com.example.model.MarketCategory
import com.example.model.MascotState
import com.example.model.OwnerConsoleData
import com.example.model.PlayerRecord
import com.example.model.PlayerStats
import com.example.model.RoundOutcomeSummary
import com.example.model.RoundPhase
import com.example.model.TournamentEntry
import com.example.model.TradeDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    ARENA,
    LEADERBOARD_TOURNAMENT,
    OWNER_ADMIN
}

data class ChartArenaUiState(
    val currentScreen: AppScreen = AppScreen.ARENA,
    val selectedCategory: MarketCategory = MarketCategory.ALL_MIX,
    val gameMode: GameMode = GameMode.NORMAL,
    val stats: PlayerStats = PlayerStats(),
    val currentChallenge: ChartChallenge? = null,
    val roundPhase: RoundPhase = RoundPhase.AWAITING_PREDICTION,
    val selectedDirection: TradeDirection? = null,
    val revealedCount: Int = 0, // 0..8
    val mascotState: MascotState = MascotState.IDLE,
    val lastOutcome: RoundOutcomeSummary? = null,
    val showProModal: Boolean = false,
    val showHowToPlayModal: Boolean = false,
    val isMuted: Boolean = false,
    val uniqueChartsPlayedCount: Int = 0,
    val ownerData: OwnerConsoleData = OwnerConsoleData(
        players = emptyList(),
        dailyWinners = emptyList(),
        tournament = com.example.model.MonthlyTournament("", "", "", "", emptyList()),
        monthlyRevenueInr = 0.0
    ),
    val isAdminUnlocked: Boolean = false,
    val statusBannerMessage: String? = null
)

class ChartArenaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArenaRepository(application.applicationContext)
    private val antiRepetitionEngine = AntiRepetitionEngine(maxHistorySize = 10_000)
    private val chartGenerator = ChartGeneratorEngine(antiRepetitionEngine)
    private val soundEngine = ArcadeSoundEngine()

    private val _uiState = MutableStateFlow(ChartArenaUiState())
    val uiState: StateFlow<ChartArenaUiState> = _uiState.asStateFlow()

    private var revealJob: Job? = null

    init {
        val loadedStats = repository.loadPlayerStats()
        val loadedHistory = repository.loadFingerprints()
        antiRepetitionEngine.loadInitialHistory(loadedHistory)
        val loadedOwnerData = repository.loadOwnerConsoleData()

        val initialChallenge = chartGenerator.generateUniqueChallenge(
            category = MarketCategory.ALL_MIX,
            gameMode = GameMode.NORMAL,
            similarityThreshold = loadedOwnerData.similarityThreshold
        )
        repository.saveFingerprints(antiRepetitionEngine.getHistorySnapshot())

        _uiState.value = ChartArenaUiState(
            stats = loadedStats,
            currentChallenge = initialChallenge,
            showHowToPlayModal = !loadedStats.hasSeenOnboarding,
            uniqueChartsPlayedCount = antiRepetitionEngine.size(),
            ownerData = loadedOwnerData
        )
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun toggleSoundMute() {
        val next = !_uiState.value.isMuted
        soundEngine.isMuted = next
        _uiState.update { it.copy(isMuted = next) }
    }

    fun toggleCurrency() {
        _uiState.update { state ->
            val nextCurrency = if (state.stats.currencyMode == CurrencyMode.INR) {
                CurrencyMode.USD
            } else {
                CurrencyMode.INR
            }
            val updated = state.stats.copy(currencyMode = nextCurrency)
            repository.savePlayerStats(updated)
            state.copy(stats = updated)
        }
    }

    fun selectCategory(category: MarketCategory) {
        if (_uiState.value.roundPhase == RoundPhase.REVEALING) return
        _uiState.update { it.copy(selectedCategory = category) }
        generateNextChart()
    }

    fun selectGameMode(mode: GameMode) {
        if (_uiState.value.roundPhase == RoundPhase.REVEALING) return
        if (mode == GameMode.TRICKY && !_uiState.value.stats.isProDemoUnlocked) {
            _uiState.update { it.copy(showProModal = true) }
            return
        }
        _uiState.update { it.copy(gameMode = mode) }
        generateNextChart()
    }

    fun openProModal() {
        _uiState.update { it.copy(showProModal = true) }
    }

    fun dismissProModal() {
        _uiState.update { it.copy(showProModal = false) }
    }

    fun openHowToPlayModal() {
        _uiState.update { it.copy(showHowToPlayModal = true) }
    }

    fun dismissHowToPlayModal() {
        val currentStats = _uiState.value.stats
        val updatedStats = if (!currentStats.hasSeenOnboarding) {
            currentStats.copy(hasSeenOnboarding = true).also { repository.savePlayerStats(it) }
        } else {
            currentStats
        }
        _uiState.update {
            it.copy(
                stats = updatedStats,
                showHowToPlayModal = false
            )
        }
    }

    fun unlockProDemo() {
        if (_uiState.value.roundPhase == RoundPhase.REVEALING) return
        val updatedStats = _uiState.value.stats.copy(isProDemoUnlocked = true)
        repository.savePlayerStats(updatedStats)
        _uiState.update {
            it.copy(
                stats = updatedStats,
                gameMode = GameMode.TRICKY,
                showProModal = false,
                statusBannerMessage = "Pro Demo unlocked! Institutional Tricky Charts active."
            )
        }
        generateNextChart()
    }

    fun selectDirection(direction: TradeDirection) {
        val phase = _uiState.value.roundPhase
        if (phase != RoundPhase.AWAITING_PREDICTION && phase != RoundPhase.DIRECTION_PICKED) return
        soundEngine.playSelectDirection(direction == TradeDirection.BULL)
        _uiState.update {
            it.copy(
                selectedDirection = direction,
                roundPhase = RoundPhase.DIRECTION_PICKED,
                mascotState = MascotState.DIRECTION_SELECTED
            )
        }
    }

    fun lockInPrediction() {
        val state = _uiState.value
        val challenge = state.currentChallenge ?: return
        val picked = state.selectedDirection ?: return
        if (state.roundPhase != RoundPhase.DIRECTION_PICKED) return
        if (revealJob?.isActive == true) return

        soundEngine.playLockIn()
        _uiState.update {
            it.copy(
                roundPhase = RoundPhase.REVEALING,
                revealedCount = 0,
                mascotState = MascotState.REVEALING
            )
        }

        revealJob = viewModelScope.launch {
            for (step in 1..8) {
                delay(300L)
                soundEngine.playRevealTick(step - 1)
                _uiState.update { it.copy(revealedCount = step) }
            }
            finalizeRound(challenge, picked)
        }
    }

    private fun finalizeRound(challenge: ChartChallenge, picked: TradeDirection) {
        val actual = challenge.expectedDirection
        val isWin = picked == actual
        val currentStats = _uiState.value.stats
        val currency = currentStats.currencyMode

        val inrDelta = if (isWin) 5_000.0 else -5_000.0
        val usdDelta = if (isWin) 500.0 else -500.0
        val displayDelta = if (currency == CurrencyMode.INR) inrDelta else usdDelta

        val nextInr = (currentStats.capitalInr + inrDelta).coerceAtLeast(0.0)
        val nextUsd = (currentStats.capitalUsd + usdDelta).coerceAtLeast(0.0)
        val nextStreak = if (isWin) currentStats.winningStreak + 1 else 0
        val nextBest = maxOf(currentStats.bestStreak, nextStreak)

        val updatedStats = currentStats.copy(
            capitalInr = nextInr,
            capitalUsd = nextUsd,
            winningStreak = nextStreak,
            bestStreak = nextBest,
            totalWins = currentStats.totalWins + if (isWin) 1 else 0,
            totalLosses = currentStats.totalLosses + if (isWin) 0 else 1,
            totalRounds = currentStats.totalRounds + 1
        )
        repository.savePlayerStats(updatedStats)

        if (isWin) soundEngine.playVictory() else soundEngine.playLoss()

        val nextMascot = when {
            isWin && challenge.isTrickyMode -> MascotState.TRAP_SURVIVED
            isWin -> MascotState.WIN
            else -> MascotState.LOSS
        }

        val updatedCapital = if (currency == CurrencyMode.INR) nextInr else nextUsd
        val summary = RoundOutcomeSummary(
            challenge = challenge,
            selectedDirection = picked,
            actualOutcome = actual,
            isWin = isWin,
            deltaAmount = displayDelta,
            updatedCapital = updatedCapital,
            currencyMode = currency
        )

        _uiState.update {
            it.copy(
                stats = updatedStats,
                roundPhase = RoundPhase.RESULT_READY,
                mascotState = nextMascot,
                lastOutcome = summary
            )
        }
    }

    fun generateNextChart() {
        if (_uiState.value.roundPhase == RoundPhase.REVEALING) return
        val state = _uiState.value
        val nextChallenge = chartGenerator.generateUniqueChallenge(
            category = state.selectedCategory,
            gameMode = state.gameMode,
            similarityThreshold = state.ownerData.similarityThreshold
        )
        repository.saveFingerprints(antiRepetitionEngine.getHistorySnapshot())

        _uiState.update {
            it.copy(
                currentChallenge = nextChallenge,
                roundPhase = RoundPhase.AWAITING_PREDICTION,
                selectedDirection = null,
                revealedCount = 0,
                mascotState = MascotState.IDLE,
                lastOutcome = null,
                uniqueChartsPlayedCount = antiRepetitionEngine.size()
            )
        }
    }

    fun resetPlayerProgress() {
        if (_uiState.value.roundPhase == RoundPhase.REVEALING) return
        val reset = PlayerStats(
            currencyMode = _uiState.value.stats.currencyMode,
            isProDemoUnlocked = _uiState.value.stats.isProDemoUnlocked
        )
        repository.savePlayerStats(reset)
        _uiState.update {
            it.copy(
                stats = reset,
                statusBannerMessage = "Virtual capital & arcade stats reset."
            )
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(statusBannerMessage = msg) }
    }

    // --- Owner Admin Access (Local Prototype Gate) ---

    fun unlockAdminWithPin(pin: String): Boolean {
        val ok = pin.trim() == "2026"
        if (ok) {
            _uiState.update { it.copy(isAdminUnlocked = true) }
        }
        return ok
    }

    fun lockAdmin() {
        _uiState.update { it.copy(isAdminUnlocked = false) }
    }

    fun todayDate(): String = repository.todayDateString()

    // --- Owner CRUD: Players & Subscriptions ---

    fun upsertPlayer(record: PlayerRecord) {
        val current = _uiState.value.ownerData
        val list = current.players.toMutableList()
        val idx = list.indexOfFirst { it.id == record.id }
        if (idx >= 0) {
            list[idx] = record
        } else {
            list.add(0, record.copy(id = record.id.ifBlank { UUID.randomUUID().toString() }))
        }
        persistOwnerData(current.copy(players = list))
    }

    fun deletePlayer(id: String) {
        val current = _uiState.value.ownerData
        persistOwnerData(current.copy(players = current.players.filterNot { it.id == id }))
    }

    fun updateMonthlyRevenue(amount: Double) {
        val current = _uiState.value.ownerData
        persistOwnerData(current.copy(monthlyRevenueInr = amount.coerceAtLeast(0.0)))
    }

    // --- Owner CRUD: Daily Winners ---

    fun upsertDailyWinner(winner: DailyWinnerRecord) {
        val current = _uiState.value.ownerData
        val list = current.dailyWinners.toMutableList()
        val idx = list.indexOfFirst { it.id == winner.id }
        if (idx >= 0) {
            list[idx] = winner
        } else {
            list.add(winner.copy(id = winner.id.ifBlank { UUID.randomUUID().toString() }))
        }
        persistOwnerData(current.copy(dailyWinners = list))
    }

    fun deleteDailyWinner(id: String) {
        val current = _uiState.value.ownerData
        persistOwnerData(current.copy(dailyWinners = current.dailyWinners.filterNot { it.id == id }))
    }

    // --- Owner CRUD: Monthly Tournament ---

    fun upsertTournamentEntry(entry: TournamentEntry) {
        val current = _uiState.value.ownerData
        val list = current.tournament.entries.toMutableList()
        val idx = list.indexOfFirst { it.id == entry.id }
        if (idx >= 0) {
            list[idx] = entry
        } else {
            list.add(entry.copy(id = entry.id.ifBlank { UUID.randomUUID().toString() }))
        }
        val sorted = list.sortedBy { it.rank }
        persistOwnerData(current.copy(tournament = current.tournament.copy(entries = sorted)))
    }

    fun deleteTournamentEntry(id: String) {
        val current = _uiState.value.ownerData
        val updated = current.tournament.entries.filterNot { it.id == id }.sortedBy { it.rank }
        persistOwnerData(current.copy(tournament = current.tournament.copy(entries = updated)))
    }

    fun updateTournamentDetails(title: String, startDate: String, endDate: String, rules: String) {
        val current = _uiState.value.ownerData
        persistOwnerData(
            current.copy(
                tournament = current.tournament.copy(
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    rules = rules
                )
            )
        )
    }

    // --- Anti-Repetition & Backup Controls ---

    fun updateSimilarityThreshold(threshold: Float) {
        val current = _uiState.value.ownerData
        persistOwnerData(current.copy(similarityThreshold = threshold.coerceIn(0.65f, 0.98f)))
    }

    fun clearChartHistory() {
        antiRepetitionEngine.clearHistory()
        repository.saveFingerprints(emptyList())
        _uiState.update {
            it.copy(
                uniqueChartsPlayedCount = 0,
                statusBannerMessage = "Fingerprint history cleared."
            )
        }
    }

    fun exportCsvString(): String = repository.exportDailyWinnersCsv(_uiState.value.ownerData.dailyWinners)

    fun exportJsonBackupString(): String = repository.exportOwnerBackupJson(_uiState.value.ownerData)

    fun importJsonBackup(rawJson: String): Boolean {
        val res = repository.importOwnerBackupJson(rawJson)
        return res.fold(
            onSuccess = { imported ->
                _uiState.update {
                    it.copy(
                        ownerData = imported,
                        statusBannerMessage = "Backup restored (${imported.players.size} players, ${imported.dailyWinners.size} daily records)."
                    )
                }
                true
            },
            onFailure = {
                showMessage("Invalid backup JSON format.")
                false
            }
        )
    }

    private fun persistOwnerData(data: OwnerConsoleData) {
        repository.saveOwnerConsoleData(data)
        _uiState.update { it.copy(ownerData = data) }
    }
}

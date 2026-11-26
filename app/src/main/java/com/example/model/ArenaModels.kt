package com.example.model

enum class CurrencyMode(val symbol: String, val label: String) {
    INR("₹", "INR"),
    USD("$", "USD")
}

enum class GameMode(val title: String) {
    NORMAL("NORMAL ARENA"),
    TRICKY("TRICKY CHARTS")
}

enum class MarketCategory(val label: String, val assets: List<String>) {
    ALL_MIX("ALL MIX", listOf(
        "BTC/USD", "ETH/USD", "SOL/USD",
        "NIFTY 50", "BANKNIFTY", "RELIANCE",
        "XAU/USD", "EUR/USD",
        "NVDA", "TSLA", "AAPL"
    )),
    CRYPTO("CRYPTO", listOf("BTC/USD", "ETH/USD", "SOL/USD")),
    INDIAN_STOCKS("INDIAN STOCKS", listOf("NIFTY 50", "BANKNIFTY", "RELIANCE")),
    FOREX_GOLD("FOREX & GOLD", listOf("XAU/USD", "EUR/USD")),
    US_STOCKS("US STOCKS", listOf("NVDA", "TSLA", "AAPL"))
}

enum class TradeDirection(val label: String) {
    BULL("BULL ↑"),
    BEAR("BEAR ↓")
}

enum class MascotState {
    IDLE,
    DIRECTION_SELECTED,
    REVEALING,
    WIN,
    TRAP_SURVIVED,
    LOSS
}

enum class RoundPhase {
    AWAITING_PREDICTION,
    DIRECTION_PICKED,
    REVEALING,
    RESULT_READY
}

enum class PriceActionArchetype(
    val displayName: String,
    val expectedDirection: TradeDirection,
    val isTrickyNative: Boolean
) {
    DOUBLE_BOTTOM_REVERSAL("Double Bottom Reversal", TradeDirection.BULL, false),
    DOUBLE_TOP_REVERSAL("Double Top Reversal", TradeDirection.BEAR, false),
    ASCENDING_TRIANGLE_BREAKOUT("Ascending Triangle Breakout", TradeDirection.BULL, false),
    DESCENDING_TRIANGLE_BREAKDOWN("Descending Triangle Breakdown", TradeDirection.BEAR, false),
    LIQUIDITY_SWEEP_FAKEOUT("Liquidity Sweep Fakeout", TradeDirection.BULL, true),
    HEAD_AND_SHOULDERS_DROP("Head & Shoulders Drop", TradeDirection.BEAR, false),
    INVERSE_HEAD_AND_SHOULDERS("Inverse Head & Shoulders", TradeDirection.BULL, false),
    NFP_VOLATILITY_SPIKE("NFP Volatility Spike", TradeDirection.BULL, true),
    BULL_FLAG_CONTINUATION("Bull Flag Continuation", TradeDirection.BULL, false),
    BEAR_FLAG_CONTINUATION("Bear Flag Continuation", TradeDirection.BEAR, false),
    SUPPORT_BOUNCE("Support Bounce", TradeDirection.BULL, false),
    RESISTANCE_REJECTION("Resistance Rejection", TradeDirection.BEAR, false)
}

enum class TrickyScenarioType(val label: String) {
    STOP_LOSS_HUNT("Stop-Loss Hunt"),
    LIQUIDITY_SWEEP("Liquidity Sweep"),
    BULL_TRAP_FAKEOUT("Bull Trap Fakeout"),
    BEAR_TRAP_FAKEOUT("Bear Trap Fakeout"),
    NFP_NEWS_WHIPSAW("NFP News Whipsaw")
}

data class Candle(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float
) {
    val isBullish: Boolean get() = close >= open
}

data class ChartFingerprint(
    val id: String,
    val asset: String,
    val timeframe: String,
    val archetypeName: String,
    val trickySubtype: String,
    val expectedDirection: String,
    val normalizedVector: List<Float>,
    val wickRatio: Float,
    val volatility: Float,
    val trendBias: Float,
    val supportNormalized: Float,
    val resistanceNormalized: Float,
    val structureHash: String,
    val createdAtEpochMs: Long
)

data class ChartChallenge(
    val id: String,
    val asset: String,
    val category: MarketCategory,
    val timeframe: String,
    val archetype: PriceActionArchetype,
    val trickySubtype: TrickyScenarioType?,
    val isTrickyMode: Boolean,
    val candles: List<Candle>, // 22 historical + 8 hidden outcome = 30
    val targetPrice: Float,
    val stopLossPrice: Float,
    val expectedDirection: TradeDirection,
    val supportZone: Float,
    val resistanceZone: Float,
    val volatility: Float,
    val trendBias: Float,
    val wickRatio: Float,
    val coachExplanation: String,
    val fingerprint: ChartFingerprint
)

data class PlayerStats(
    val capitalInr: Double = 100_000.0,
    val capitalUsd: Double = 10_000.0,
    val currencyMode: CurrencyMode = CurrencyMode.INR,
    val winningStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val totalRounds: Int = 0,
    val isProDemoUnlocked: Boolean = false,
    val hasSeenOnboarding: Boolean = false
) {
    val winPercentage: Int
        get() = if (totalRounds == 0) 0 else ((totalWins.toFloat() / totalRounds.toFloat()) * 100f).toInt()
}

data class RoundOutcomeSummary(
    val challenge: ChartChallenge,
    val selectedDirection: TradeDirection,
    val actualOutcome: TradeDirection,
    val isWin: Boolean,
    val deltaAmount: Double,
    val updatedCapital: Double,
    val currencyMode: CurrencyMode
)

data class PlayerRecord(
    val id: String,
    val name: String,
    val instagramHandle: String,
    val membershipStatus: String, // "Free" or "Pro"
    val paymentStatus: String,    // "Paid", "Pending", "Trial", "Unpaid"
    val startDate: String,
    val expiryDate: String,
    val active: Boolean
)

data class DailyWinnerRecord(
    val id: String,
    val date: String,             // YYYY-MM-DD
    val rank: Int,
    val playerName: String,
    val instagramHandle: String,
    val score: Int,
    val wins: Int,
    val losses: Int,
    val streak: Int,
    val gameMode: String
)

data class TournamentEntry(
    val id: String,
    val rank: Int,
    val playerName: String,
    val instagramHandle: String,
    val score: Int,
    val wins: Int,
    val proEligible: Boolean
)

data class MonthlyTournament(
    val title: String,
    val rules: String,
    val startDate: String,
    val endDate: String,
    val entries: List<TournamentEntry>
)

data class OwnerConsoleData(
    val players: List<PlayerRecord>,
    val dailyWinners: List<DailyWinnerRecord>,
    val tournament: MonthlyTournament,
    val monthlyRevenueInr: Double,
    val similarityThreshold: Float = 0.85f
)

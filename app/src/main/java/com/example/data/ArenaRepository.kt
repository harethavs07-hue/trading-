package com.example.data

import android.content.Context
import com.example.model.ChartFingerprint
import com.example.model.CurrencyMode
import com.example.model.DailyWinnerRecord
import com.example.model.MonthlyTournament
import com.example.model.OwnerConsoleData
import com.example.model.PlayerRecord
import com.example.model.PlayerStats
import com.example.model.TournamentEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ArenaRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("chart_arena_store", Context.MODE_PRIVATE)
    private val fingerprintsFile = File(context.filesDir, "chart_fingerprints_v1.json")
    private val ownerDataFile = File(context.filesDir, "owner_console_v1.json")

    fun todayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun yesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun loadPlayerStats(): PlayerStats {
        return runCatching {
            val raw = prefs.getString("player_stats_json", null) ?: return PlayerStats()
            val obj = JSONObject(raw)
            PlayerStats(
                capitalInr = obj.optDouble("capitalInr", 100_000.0),
                capitalUsd = obj.optDouble("capitalUsd", 10_000.0),
                currencyMode = runCatching {
                    CurrencyMode.valueOf(obj.optString("currencyMode", CurrencyMode.INR.name))
                }.getOrDefault(CurrencyMode.INR),
                winningStreak = obj.optInt("winningStreak", 0),
                bestStreak = obj.optInt("bestStreak", 0),
                totalWins = obj.optInt("totalWins", 0),
                totalLosses = obj.optInt("totalLosses", 0),
                totalRounds = obj.optInt("totalRounds", 0),
                isProDemoUnlocked = obj.optBoolean("isProDemoUnlocked", false),
                hasSeenOnboarding = obj.optBoolean("hasSeenOnboarding", false)
            )
        }.getOrDefault(PlayerStats())
    }

    fun savePlayerStats(stats: PlayerStats) {
        runCatching {
            val obj = JSONObject().apply {
                put("capitalInr", stats.capitalInr)
                put("capitalUsd", stats.capitalUsd)
                put("currencyMode", stats.currencyMode.name)
                put("winningStreak", stats.winningStreak)
                put("bestStreak", stats.bestStreak)
                put("totalWins", stats.totalWins)
                put("totalLosses", stats.totalLosses)
                put("totalRounds", stats.totalRounds)
                put("isProDemoUnlocked", stats.isProDemoUnlocked)
                put("hasSeenOnboarding", stats.hasSeenOnboarding)
            }
            prefs.edit().putString("player_stats_json", obj.toString()).apply()
        }
    }

    fun loadFingerprints(): List<ChartFingerprint> {
        return runCatching {
            if (!fingerprintsFile.exists()) return emptyList()
            val text = fingerprintsFile.readText()
            if (text.isBlank()) return emptyList()
            val arr = JSONArray(text)
            val list = ArrayList<ChartFingerprint>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val vecArr = o.optJSONArray("v") ?: JSONArray()
                val vec = ArrayList<Float>(vecArr.length())
                for (j in 0 until vecArr.length()) {
                    vec.add(vecArr.optDouble(j, 0.0).toFloat())
                }
                list.add(
                    ChartFingerprint(
                        id = o.optString("id"),
                        asset = o.optString("a"),
                        timeframe = o.optString("t"),
                        archetypeName = o.optString("ar"),
                        trickySubtype = o.optString("tr"),
                        expectedDirection = o.optString("d"),
                        normalizedVector = vec,
                        wickRatio = o.optDouble("wr", 0.5).toFloat(),
                        volatility = o.optDouble("vol", 0.01).toFloat(),
                        trendBias = o.optDouble("tb", 0.0).toFloat(),
                        supportNormalized = o.optDouble("sn", 0.2).toFloat(),
                        resistanceNormalized = o.optDouble("rn", 0.8).toFloat(),
                        structureHash = o.optString("h"),
                        createdAtEpochMs = o.optLong("ts", 0L)
                    )
                )
            }
            list
        }.getOrElse { emptyList() }
    }

    fun saveFingerprints(items: List<ChartFingerprint>) {
        runCatching {
            // Trim safely to stay within storage limits while keeping up to 10,000 entries
            val bounded = if (items.size > 10_000) items.takeLast(10_000) else items
            val arr = JSONArray()
            bounded.forEach { fp ->
                val vecArr = JSONArray()
                fp.normalizedVector.forEach { v -> vecArr.put(v.toDouble()) }
                arr.put(JSONObject().apply {
                    put("id", fp.id)
                    put("a", fp.asset)
                    put("t", fp.timeframe)
                    put("ar", fp.archetypeName)
                    put("tr", fp.trickySubtype)
                    put("d", fp.expectedDirection)
                    put("v", vecArr)
                    put("wr", fp.wickRatio.toDouble())
                    put("vol", fp.volatility.toDouble())
                    put("tb", fp.trendBias.toDouble())
                    put("sn", fp.supportNormalized.toDouble())
                    put("rn", fp.resistanceNormalized.toDouble())
                    put("h", fp.structureHash)
                    put("ts", fp.createdAtEpochMs)
                })
            }
            fingerprintsFile.writeText(arr.toString())
        }.onFailure {
            // Storage-limit fallback: retain latest 2,500 if disk/write failed
            runCatching {
                val smaller = items.takeLast(2_500)
                val arr = JSONArray()
                smaller.forEach { fp ->
                    arr.put(JSONObject().apply {
                        put("id", fp.id)
                        put("a", fp.asset)
                        put("t", fp.timeframe)
                        put("ar", fp.archetypeName)
                        put("tr", fp.trickySubtype)
                        put("d", fp.expectedDirection)
                        put("h", fp.structureHash)
                        put("ts", fp.createdAtEpochMs)
                    })
                }
                fingerprintsFile.writeText(arr.toString())
            }
        }
    }

    fun loadOwnerConsoleData(): OwnerConsoleData {
        return runCatching {
            if (!ownerDataFile.exists()) {
                val seeded = defaultDemoOwnerData()
                saveOwnerConsoleData(seeded)
                return seeded
            }
            val raw = ownerDataFile.readText()
            parseOwnerConsoleJson(raw)
        }.getOrElse {
            val seeded = defaultDemoOwnerData()
            saveOwnerConsoleData(seeded)
            seeded
        }
    }

    fun saveOwnerConsoleData(data: OwnerConsoleData) {
        runCatching {
            ownerDataFile.writeText(serializeOwnerConsoleJson(data))
        }
    }

    fun exportOwnerBackupJson(data: OwnerConsoleData): String = serializeOwnerConsoleJson(data)

    fun importOwnerBackupJson(rawJson: String): Result<OwnerConsoleData> {
        return runCatching {
            val parsed = parseOwnerConsoleJson(rawJson)
            saveOwnerConsoleData(parsed)
            parsed
        }
    }

    fun exportDailyWinnersCsv(winners: List<DailyWinnerRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Rank,PlayerName,Instagram,Score,Wins,Losses,Streak,GameMode")
        winners.sortedWith(compareByDescending<DailyWinnerRecord> { it.date }.thenBy { it.rank })
            .forEach { w ->
                sb.appendLine(
                    listOf(
                        csvEscape(w.date),
                        w.rank.toString(),
                        csvEscape(w.playerName),
                        csvEscape(w.instagramHandle),
                        w.score.toString(),
                        w.wins.toString(),
                        w.losses.toString(),
                        w.streak.toString(),
                        csvEscape(w.gameMode)
                    ).joinToString(",")
                )
            }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }

    private fun serializeOwnerConsoleJson(data: OwnerConsoleData): String {
        val root = JSONObject()
        root.put("monthlyRevenueInr", data.monthlyRevenueInr)
        root.put("similarityThreshold", data.similarityThreshold.toDouble())

        val playersArr = JSONArray()
        data.players.forEach { p ->
            playersArr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("instagramHandle", p.instagramHandle)
                put("membershipStatus", p.membershipStatus)
                put("paymentStatus", p.paymentStatus)
                put("startDate", p.startDate)
                put("expiryDate", p.expiryDate)
                put("active", p.active)
            })
        }
        root.put("players", playersArr)

        val winnersArr = JSONArray()
        data.dailyWinners.forEach { w ->
            winnersArr.put(JSONObject().apply {
                put("id", w.id)
                put("date", w.date)
                put("rank", w.rank)
                put("playerName", w.playerName)
                put("instagramHandle", w.instagramHandle)
                put("score", w.score)
                put("wins", w.wins)
                put("losses", w.losses)
                put("streak", w.streak)
                put("gameMode", w.gameMode)
            })
        }
        root.put("dailyWinners", winnersArr)

        val tObj = JSONObject().apply {
            put("title", data.tournament.title)
            put("rules", data.tournament.rules)
            put("startDate", data.tournament.startDate)
            put("endDate", data.tournament.endDate)
            val entriesArr = JSONArray()
            data.tournament.entries.forEach { e ->
                entriesArr.put(JSONObject().apply {
                    put("id", e.id)
                    put("rank", e.rank)
                    put("playerName", e.playerName)
                    put("instagramHandle", e.instagramHandle)
                    put("score", e.score)
                    put("wins", e.wins)
                    put("proEligible", e.proEligible)
                })
            }
            put("entries", entriesArr)
        }
        root.put("tournament", tObj)
        return root.toString(2)
    }

    private fun parseOwnerConsoleJson(rawJson: String): OwnerConsoleData {
        val root = JSONObject(rawJson)
        val players = mutableListOf<PlayerRecord>()
        val pArr = root.optJSONArray("players") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            val o = pArr.optJSONObject(i) ?: continue
            val name = o.optString("name").trim()
            if (name.isEmpty()) continue
            players.add(
                PlayerRecord(
                    id = o.optString("id").ifBlank { "p_$i" },
                    name = name,
                    instagramHandle = o.optString("instagramHandle"),
                    membershipStatus = o.optString("membershipStatus", "Free"),
                    paymentStatus = o.optString("paymentStatus", "Paid"),
                    startDate = o.optString("startDate"),
                    expiryDate = o.optString("expiryDate"),
                    active = o.optBoolean("active", true)
                )
            )
        }

        val winners = mutableListOf<DailyWinnerRecord>()
        val wArr = root.optJSONArray("dailyWinners") ?: JSONArray()
        for (i in 0 until wArr.length()) {
            val o = wArr.optJSONObject(i) ?: continue
            val name = o.optString("playerName").trim()
            if (name.isEmpty()) continue
            winners.add(
                DailyWinnerRecord(
                    id = o.optString("id").ifBlank { "w_$i" },
                    date = o.optString("date").ifBlank { todayDateString() },
                    rank = o.optInt("rank", i + 1).coerceAtLeast(1),
                    playerName = name,
                    instagramHandle = o.optString("instagramHandle"),
                    score = o.optInt("score", 0),
                    wins = o.optInt("wins", 0).coerceAtLeast(0),
                    losses = o.optInt("losses", 0).coerceAtLeast(0),
                    streak = o.optInt("streak", 0).coerceAtLeast(0),
                    gameMode = o.optString("gameMode", "NORMAL ARENA")
                )
            )
        }

        val tObj = root.optJSONObject("tournament")
        val entries = mutableListOf<TournamentEntry>()
        val eArr = tObj?.optJSONArray("entries") ?: JSONArray()
        for (i in 0 until eArr.length()) {
            val o = eArr.optJSONObject(i) ?: continue
            val name = o.optString("playerName").trim()
            if (name.isEmpty()) continue
            entries.add(
                TournamentEntry(
                    id = o.optString("id").ifBlank { "t_$i" },
                    rank = o.optInt("rank", i + 1).coerceAtLeast(1),
                    playerName = name,
                    instagramHandle = o.optString("instagramHandle"),
                    score = o.optInt("score", 0),
                    wins = o.optInt("wins", 0).coerceAtLeast(0),
                    proEligible = o.optBoolean("proEligible", true)
                )
            )
        }

        val tournament = MonthlyTournament(
            title = tObj?.optString("title")?.ifBlank { "Monthly Chart Sniper Cup" }
                ?: "Monthly Chart Sniper Cup",
            rules = tObj?.optString("rules")?.ifBlank { defaultTournamentRules() }
                ?: defaultTournamentRules(),
            startDate = tObj?.optString("startDate")?.ifBlank { "2026-11-01" } ?: "2026-11-01",
            endDate = tObj?.optString("endDate")?.ifBlank { "2026-11-30" } ?: "2026-11-30",
            entries = entries.sortedBy { it.rank }
        )

        return OwnerConsoleData(
            players = players,
            dailyWinners = winners,
            tournament = tournament,
            monthlyRevenueInr = root.optDouble("monthlyRevenueInr", 15_920.0),
            similarityThreshold = root.optDouble("similarityThreshold", 0.85).toFloat().coerceIn(0.65f, 0.98f)
        )
    }

    private fun defaultTournamentRules(): String =
        "1. Pro Arena members qualify for official monthly prize pool.\n" +
            "2. Minimum 25 verified rounds across Normal & Tricky Charts.\n" +
            "3. Tie-breaker decided by highest peak winning streak.\n" +
            "4. Official rankings are verified and published daily by the Owner."

    private fun defaultDemoOwnerData(): OwnerConsoleData {
        val today = todayDateString()
        val yesterday = yesterdayDateString()
        return OwnerConsoleData(
            players = listOf(
                PlayerRecord("p1", "Aarav Verma", "@aarav.trades", "Pro", "Paid", "2026-11-01", "2026-12-01", true),
                PlayerRecord("p2", "Kiran Patel", "@kiran_fx", "Pro", "Paid", "2026-11-05", "2026-12-05", true),
                PlayerRecord("p3", "Elena Rostova", "@elena.quant", "Pro", "Paid", "2026-11-10", "2026-12-10", true),
                PlayerRecord("p4", "Rohan Mehta", "@rohan_nifty", "Free", "Unpaid", "2026-11-12", "-", true),
                PlayerRecord("p5", "Devika Nair", "@devika.scalp", "Pro", "Expired", "2026-10-01", "2026-11-01", false)
            ),
            dailyWinners = listOf(
                DailyWinnerRecord("w1", today, 1, "Aarav Verma", "@aarav.trades", 45_000, 11, 2, 7, "TRICKY CHARTS"),
                DailyWinnerRecord("w2", today, 2, "Elena Rostova", "@elena.quant", 35_000, 9, 2, 5, "TRICKY CHARTS"),
                DailyWinnerRecord("w3", today, 3, "Kiran Patel", "@kiran_fx", 25_000, 8, 3, 4, "NORMAL ARENA"),
                DailyWinnerRecord("w4", yesterday, 1, "Kiran Patel", "@kiran_fx", 40_000, 10, 2, 6, "TRICKY CHARTS"),
                DailyWinnerRecord("w5", yesterday, 2, "Aarav Verma", "@aarav.trades", 30_000, 8, 2, 4, "NORMAL ARENA"),
                DailyWinnerRecord("w6", yesterday, 3, "Rohan Mehta", "@rohan_nifty", 20_000, 6, 2, 3, "NORMAL ARENA")
            ),
            tournament = MonthlyTournament(
                title = "Monthly Chart Sniper Cup",
                rules = defaultTournamentRules(),
                startDate = "2026-11-01",
                endDate = "2026-11-30",
                entries = listOf(
                    TournamentEntry("t1", 1, "Aarav Verma", "@aarav.trades", 285_000, 68, true),
                    TournamentEntry("t2", 2, "Elena Rostova", "@elena.quant", 240_000, 59, true),
                    TournamentEntry("t3", 3, "Kiran Patel", "@kiran_fx", 215_000, 52, true),
                    TournamentEntry("t4", 4, "Rohan Mehta", "@rohan_nifty", 145_000, 38, false)
                )
            ),
            monthlyRevenueInr = 15_920.0,
            similarityThreshold = 0.85f
        )
    }
}

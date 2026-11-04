package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val genericName: String = "",
    val dosage: String,
    val form: String = "片剂", // 片剂, 胶囊, 缓释片, 滴剂, 喷雾, 注射剂
    val time: String, // format "HH:mm"
    val period: String = inferPeriodFromTime(time), // 晨起, 午餐, 晚餐, 睡前
    val mealInstruction: String = "餐后服用", // 空腹服用, 随餐服用, 餐后服用, 睡前服用, 不限饮食
    val frequency: String = "每天",
    val evidenceLevel: String = "Grade A • RCT强证据",
    val clinicalNote: String = "遵循现代循证医学指南按时定量服用，切勿与不明成分草药或偏方同服。",
    val isTakenToday: Boolean = false,
    val lastTakenDate: String = "", // ISO format "yyyy-MM-dd"
    val lastTakenTime: String = "", // format "HH:mm"
    val stockCount: Int = 28,
    val streakDays: Int = 3,
    val color: Int = 0xFFFF2D55.toInt()
)

fun normalizeTimeFormat(raw: String): String {
    val cleaned = raw.trim().replace('：', ':').replace('.', ':')
    val parts = cleaned.split(":")
    if (parts.size >= 2) {
        val h = parts[0].trim().toIntOrNull()?.coerceIn(0, 23)
        val m = parts[1].trim().take(2).toIntOrNull()?.coerceIn(0, 59)
        if (h != null && m != null) {
            return "%02d:%02d".format(h, m)
        }
    }
    val digitsOnly = cleaned.filter { it.isDigit() }
    if (digitsOnly.length in 3..4) {
        val hStr = if (digitsOnly.length == 3) digitsOnly.substring(0, 1) else digitsOnly.substring(0, 2)
        val mStr = if (digitsOnly.length == 3) digitsOnly.substring(1, 3) else digitsOnly.substring(2, 4)
        val h = hStr.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val m = mStr.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return "%02d:%02d".format(h, m)
    }
    val hourOnly = digitsOnly.toIntOrNull()?.coerceIn(0, 23)
    if (hourOnly != null && digitsOnly.isNotEmpty()) {
        return "%02d:00".format(hourOnly)
    }
    return "08:00"
}

fun inferPeriodFromTime(time: String): String {
    val normalized = normalizeTimeFormat(time)
    val hour = normalized.substringBefore(":").toIntOrNull() ?: 8
    return when (hour) {
        in 5..10 -> "晨起"
        in 11..15 -> "午餐"
        in 16..20 -> "晚餐"
        else -> "睡前"
    }
}

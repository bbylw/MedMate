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

fun inferPeriodFromTime(time: String): String {
    val hour = time.substringBefore(":").toIntOrNull() ?: 8
    return when (hour) {
        in 5..10 -> "晨起"
        in 11..15 -> "午餐"
        in 16..20 -> "晚餐"
        else -> "睡前"
    }
}

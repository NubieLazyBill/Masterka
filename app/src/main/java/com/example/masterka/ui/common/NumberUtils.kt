package com.example.masterka.ui.common

/**
 * Форматирует количество:
 * - -1 → "много"
 * - 5.0 → "5"
 * - 2.5 → "2.5"
 */
fun formatQty(q: Float): String {
    if (q < 0f) return "много"
    return if (q == q.toInt().toFloat()) q.toInt().toString()
    else "%.2f".format(q).trimEnd('0').trimEnd('.')
}

/**
 * Парсит ввод пользователя:
 * - "много", "мн", "∞", "-1" → -1f
 * - "5" → 5f
 * - "2.5" / "2,5" → 2.5f
 * - пусто или мусор → null
 */
fun parseQty(text: String): Float? {
    val t = text.trim().lowercase()
    if (t.isBlank()) return null
    if (t == "много" || t == "мн" || t == "∞" || t == "-1") return -1f
    return t.replace(',', '.').toFloatOrNull()
}
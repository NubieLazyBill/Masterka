package com.example.masterka.data

import org.json.JSONArray

object PhotoPathsCodec {

    fun encode(paths: List<String>): String {
        val arr = JSONArray()
        paths.forEach { arr.put(it) }
        return arr.toString()
    }

    fun decode(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    add(arr.getString(i))
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
package com.example.masterka.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Точка в нормализованных координатах [0..1] относительно размера фото.
 */
data class NormalizedPoint(val x: Float, val y: Float)

object PolygonCodec {

    fun encode(points: List<NormalizedPoint>): String {
        val arr = JSONArray()
        points.forEach { p ->
            val obj = JSONObject()
            obj.put("x", p.x.toDouble())
            obj.put("y", p.y.toDouble())
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(json: String?): List<NormalizedPoint> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        NormalizedPoint(
                            x = obj.getDouble("x").toFloat(),
                            y = obj.getDouble("y").toFloat()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
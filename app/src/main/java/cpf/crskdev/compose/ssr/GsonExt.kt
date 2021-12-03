package cpf.crskdev.compose.ssr

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap

/**
 * Created by Cristian Pela on 20.11.2021.
 */

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson(json, T::class.java) as T

fun Gson.edit(json: String, block: JsonScope.(LinkedTreeMap<*, *>) -> Unit): String {
    val jsonTreeMap = this.fromJson<LinkedTreeMap<*, *>>(json)
    JsonScopeImpl().block(jsonTreeMap)
    return this.toJson(jsonTreeMap)
}

fun <T> Gson.read(json: String, block: JsonScope.(LinkedTreeMap<*, *>) -> T): T {
    val jsonTreeMap = this.fromJson<LinkedTreeMap<*, *>>(json)
    return JsonScopeImpl().run { block(jsonTreeMap) }
}

interface JsonScope {

    fun LinkedTreeMap<*, *>.obj(path: String): LinkedTreeMap<String, Any>

    fun LinkedTreeMap<*, *>.arr(path: String): List<LinkedTreeMap<String, Any>>

    fun <T> LinkedTreeMap<*, *>.value(path: String): T

}

private class JsonScopeImpl : JsonScope {

    companion object {
        private val ARR_REGEX = """(.+)(\[\d+\])""".toRegex()
    }

    override fun LinkedTreeMap<*, *>.obj(path: String): LinkedTreeMap<String, Any> =
        entry(this, path) as LinkedTreeMap<String, Any>

    override fun LinkedTreeMap<*, *>.arr(path: String): List<LinkedTreeMap<String, Any>> =
        entry(this, path) as List<LinkedTreeMap<String, Any>>

    override fun <T> LinkedTreeMap<*, *>.value(path: String): T {
        val key = path.split("/").last()
        return obj(path)[key] as T
    }

    private fun entry(src: LinkedTreeMap<*, *>, path: String): Any {
        val segments = path.split("/")
        var curr: Any = src
        segments.forEachIndexed { i, s ->
            val matchesArr = ARR_REGEX.find(s)
            val obj = curr as LinkedTreeMap<String, Any>
            curr = if (matchesArr != null) {
                val (key, indexRaw) = matchesArr.destructured
                val index = indexRaw.trimStart('[').trimEnd(']').toInt()
                (obj[key] as List<LinkedTreeMap<String, Any>>)[index]
            } else {
                if (i == segments.lastIndex) {
                    when {
                        obj[s] is List<*> -> {
                            obj[s] as List<LinkedTreeMap<String, Any>>
                        }
                        obj[s] is LinkedTreeMap<*, *> -> {
                            obj[s] as LinkedTreeMap<String, Any>
                        }
                        else -> {
                            LinkedTreeMap<String, Any>().apply { put(s, obj[s]!!) }
                        }
                    }
                } else {
                    obj[s] as LinkedTreeMap<String, Any>
                }
            }
        }
        return curr
    }

}
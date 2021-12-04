package cpf.crskdev.compose.ssr.interceptors.dsl

import android.content.UriMatcher
import android.net.Uri

/**
 * Created by Cristian Pela on 04.12.2021.
 *
 * Convenience wrapper around [android.content.UriMatcher]
 */
sealed class UriMatching {

    abstract fun matches(uri: Uri): Boolean

    class Exact(private val uri: Uri) : UriMatching() {
        override fun matches(uri: Uri): Boolean = uri == this.uri
    }

    class Matcher(vararg constructs: MatcherConstruct) : UriMatching() {

        private val codes = mutableListOf<Int>()

        private val nativeMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            constructs.forEach {
                addURI(it.authority, it.path, it.code)
                codes.add(it.code)
            }
        }

        override fun matches(uri: Uri): Boolean {
            codes.forEach {
                if (this.nativeMatcher.match(uri) == it) {
                    return true
                }
            }
            return false
        }

    }

}

/**
 * @see [android.content.UriMatcher.addURI]
 */
typealias MatcherConstruct = Pair<String, Pair<String, Int>>

val MatcherConstruct.authority
    get() = this.first

val MatcherConstruct.path
    get() = this.second.first

val MatcherConstruct.code
    get() = this.second.second


fun Uri.exactMatching(): UriMatching = UriMatching.Exact(this)

fun matching(authority: String, vararg pathCodeConstructs: Pair<String, Int>): UriMatching = pathCodeConstructs
    .map { (authority to it) as MatcherConstruct }
    .let { UriMatching.Matcher(*it.toTypedArray()) }
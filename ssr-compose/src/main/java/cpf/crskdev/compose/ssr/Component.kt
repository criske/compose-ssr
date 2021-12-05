package cpf.crskdev.compose.ssr

import androidx.compose.material.Colors
import androidx.compose.material.Typography
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import cpf.crskdev.compose.ssr.interceptors.core.Page
import java.time.format.TextStyle

/**
 * Created by Cristian Pela on 20.11.2021.
 */
sealed class Component(val id: String, val modifier: Modifier) {

    object None : Component("", Modifier)

    class Text(id: String, var text: String, modifier: Modifier = Modifier) : Component(id = id, modifier = modifier)

    class TextField(id: String, modifier: Modifier, var text: String = "", var onValueChange: (String) -> Unit = {}) :
        Component(id, modifier)

    class Button(id: String,
                 var text: String,
                 modifier: Modifier = Modifier) :
        Component(id, modifier) {
        var onClick: () -> Unit = {}
    }

    sealed class Group(id: String, modifier: Modifier, val children: List<Component>) : Component(id, modifier) {
        class Container(id: String, children: List<Component>, modifier: Modifier = Modifier) :
            Group(id = id, modifier = modifier, children = children)

        class PagedList(id: String, children: List<Component>,
                        modifier: Modifier = Modifier,
                        val page: Page,
                        var onPageEndReached: (Page) -> Unit = {}
        ) : Group(id = id, modifier = modifier, children = children)

        class Screen(id: String, val content: Component, val history: Boolean = false, val theme: Theme) :
            Group(id, Modifier, listOf(content)) {
                data class Theme(val colors: Colors)
            }
    }
}

class MapModifier(val key: String, val value: Any) : Modifier.Element

fun Modifier.putAttr(key: String, value: Any) = CombinedModifier(this, MapModifier(key, value))

fun <T> Modifier.attr(key: String, default: T): T =
    this.foldIn(default) { acc, curr ->
        var newAcc = acc
        if (curr is MapModifier && curr.key == key) {
            newAcc = curr.value as T
        }
        newAcc
    }


class ComponentContext(val root: Component) {

    private val fastLookUp = mutableMapOf<String, Component>()

     inline fun <T : Component> id(id: String, componentBlock: T.() -> Unit = {}): T? =
        (lookUp(id, root) as T?)?.apply(componentBlock)


    fun contains(id: String): Boolean = this.lookUp(id, root) != null

    @PublishedApi
    internal fun lookUp(id: String, curr: Component): Component? {
        if (fastLookUp.containsKey(id)) {
            return fastLookUp[id]
        }
        if (id == curr.id) {
            fastLookUp[id] = curr
            return curr
        }
        var found: Component? = null
        if (curr is Component.Group) {
            for (child in curr.children) {
                val nullableFound = lookUp(id, child)
                if (nullableFound != null) {
                    found = nullableFound;
                    break;
                }
            }
        }
        return found
    }

}

fun Component.startContext(): ComponentContext = ComponentContext(this)

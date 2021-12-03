package cpf.crskdev.compose.ssr

/**
 * Created by Cristian Pela on 20.11.2021.
 */

typealias ComponentBackstack = List<Component>

fun ComponentBackstack(): ComponentBackstack = listOf(Component.None)

val ComponentBackstack.willBeEmpty: Boolean
    get() = (this.size == 1) || (this.size - 1 <= 1 && this.first() == Component.None)

val ComponentBackstack.peek: Component
    get() = this.last()

fun ComponentBackstack.pop(): ComponentBackstack = this.subList(0, this.lastIndex)

fun ComponentBackstack.replaceTop(component: Component): ComponentBackstack =
    this.toMutableList().apply { set(this.lastIndex, component) }.toList()

fun ComponentBackstack.push(component: Component): ComponentBackstack = this + component
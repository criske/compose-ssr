@file:Suppress("FunctionName")

package cpf.crskdev.compose.ssr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Created by Cristian Pela on 20.11.2021.
 */
@Composable
fun Screen(
    component: Component,
    componentContextScope: ComponentContext.(Updater) -> Unit = { }
) {
    val (screen, update) = remember(component) {
        mutableStateOf(MutableComponent(component))
    }
    val componentCompose = remember(screen) {
        ComponentComposer(screen()) {
            this.componentContextScope(UpdaterImpl(screen, update))
        }
    }
    componentCompose()
}

private class MutableComponent(private val component: Component) {

    fun mutate(block: Component.() -> Unit = {}): MutableComponent = MutableComponent(component.apply(block))

    operator fun invoke(): Component = this.component
}


interface Updater {
    operator fun invoke(block: () -> Unit)
}

private class UpdaterImpl(
    private val mutableComponent: MutableComponent,
    private val updater: (MutableComponent) -> Unit
) : Updater {

    override operator fun invoke(block: () -> Unit) {
        block()
        updater(mutableComponent.mutate())
    }
}

private fun ComponentComposer(
    component: Component,
    lookUpScope: ComponentContext.() -> Unit = {}
): @Composable () -> Unit {
    ComponentContext(component).apply(lookUpScope)
    fun ComponentComposerInternal(component: Component): @Composable () -> Unit =
        when (component) {
            is Component.Group.Container -> ({
                Column(modifier = component.modifier.fillMaxSize()) {
                    for (child in component.children) {
                        key(child.id) {
                            ComponentComposerInternal(child)()
                        }
                    }
                }
            })
            is Component.Group.PagedList -> ({
                val scrollIndex = remember {
                    mutableStateOf(0)
                }
                val page = remember(component.page) {
                    mutableStateOf(component.page)
                }
                val offScreenOffset = scrollIndex.value + 10
                val canLoad = offScreenOffset >= page.value.itemsSize && offScreenOffset < page.value.total
                LaunchedEffect(canLoad) {
                    if (canLoad) {
                        println("Reached " + (scrollIndex.value) + " " + page.value.itemsSize)
                        component.onPageEndReached(page.value)
                        page.value = page.value + 1
                    }
                }
                LazyColumn(modifier = component.modifier.fillMaxSize()) {
                    itemsIndexed(items = component.children, key = { _, comp -> comp.id }) { index, child ->
                        scrollIndex.value = index
                        ComponentComposerInternal(child)()
                    }
                }
            })
            is Component.Group.Screen -> ({
                Column(modifier = Modifier.fillMaxSize()) {
                    ComponentComposerInternal(component.content)()
                }
            })
            is Component.Text -> ({
                Text(text = component.text, modifier = component.modifier)
            })
            is Component.Button -> ({
                Button(
                    modifier = component.modifier,
                    enabled = !component.modifier.attr("disabled", false),
                    onClick = {
                        component.onClick()
                    }) {
                    Text(component.text)
                }
            })
            is Component.TextField -> ({
                var value by remember(component) { mutableStateOf(component.text) }
                TextField(
                    value = value,
                    onValueChange = {
                        component.text = it
                        component.onValueChange(it)
                        value = it
                    },
                    visualTransformation = component.modifier.attr("editType", VisualTransformation.None),
                    label = { Text(component.modifier.attr("label", "")) },
                    maxLines = 2,
                    textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                    modifier = component.modifier
                )
            })
            else -> ({})
        }
    return ComponentComposerInternal(component)
}
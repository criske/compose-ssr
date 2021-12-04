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
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 20.11.2021.
 */
@Composable
fun Screen(
    component: Component,
    componentContextScope: suspend ComponentContext.() -> Unit = { }
) {
    LaunchedEffect(component) {
        launch {
            ComponentContext(component).componentContextScope()
        }
    }
    val componentComposer = remember(component) {
        ComponentComposer(component)
    }
    componentComposer()
}

private fun ComponentComposer(component: Component): @Composable () -> Unit {
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
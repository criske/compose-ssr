@file:Suppress("FunctionName")

package cpf.crskdev.compose.ssr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.*
import java.lang.reflect.Type

val json = """
    {
        "id": "main",
        "type": "screen",
        "history": true,
        "path": "/",
        "content": {
            "id": "2",
            "type": "container",
            "children" : [
                {
                    "id": "3",
                    "type": "button",
                    "text": "BUTTON"
                },
                {
                    "id": "4",
                    "type": "text",
                    "text": "This is a text"
                },
                {
                    "id": "5",
                    "type": "edit",
                    "text": "Edit"
                },
                {
                    "id": "6",
                    "type": "button",
                    "text": "Next Screen"
                }
            ]
        }
    }
""".trimIndent()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = json.toComponent()
        setContent {
            App(component)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App(json.toComponent())
}

@Composable
fun App(component: Component) {

    val (screen, newScreen) = remember(component) {
        mutableStateOf(
            component
//            Component.Container(
//                id = idGen(),
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxSize(),
//                children = arrayListOf(
//                    Component.Button(
//                        id = idGen(),
//                        text = "Text Button",
//                        modifier = Modifier.padding(8.dp)
//                    ),
//                    Component.Text(
//                        id = idGen(),
//                        modifier = Modifier
//                            .height(64.dp)
//                            .fillMaxWidth()
//                            .background(Color.Gray),
//                        text = "This a text"
//                    ),
//                    Component.TextField(
//                        id = idGen(),
//                        modifier = Modifier.fillMaxWidth()
//                    ),
//                    Component.Button(
//                        id = idGen(),
//                        text = "New Screen",
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                )
//            )
        )
    }

//    LaunchedEffect(true) {
//        delay(8000)
//        newScreen(
//            Component.Text(
//                id = idGen(),
//                modifier = Modifier
//                    .height(64.dp)
//                    .fillMaxWidth()
//                    .background(Color.Red),
//                text = "GameOver"
//            )
//        )
//    }

    Screen(screen) { update ->
        id<Component.Screen>("main") {
            handleMainScreen(update, newScreen)
        }
    }
}

fun ComponentContext.handleMainScreen(update: Updater, nextScreen: (Component) -> Unit) {
    id<Component.Button>("3") {
        onClick = {
            id<Component.Text>("4") {
                update {
                    text = "Updated from button click"
                }
            }
        }
    }
    id<Component.TextField>("5") {
        onValueChange = { input ->
            id<Component.Text>("4") {
                update {
                    text = input
                }
            }
        }
    }
    id<Component.Button>("6") {
        onClick = {
            if (!id<Component.TextField>("5")?.text.isNullOrBlank()) {
                nextScreen(
                    Component.Text(
                        id = "1",
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth()
                            .background(Color.Yellow),
                        text = id<Component.TextField>("5")?.text!!
                    )
                )
            } else {
                id<Component.Text>("4") {
                    update {
                        text = "Error: Input text is empty"
                    }
                }
            }
        }
    }
}

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
            this.componentContextScope(Updater(screen, update))
        }
    }
    componentCompose()
}

class Updater(
    private val mutableComponent: MutableComponent,
    private val updater: (MutableComponent) -> Unit
) {

    operator fun invoke(block: () -> Unit) {
        block()
        updater(mutableComponent.mutate { })
    }
}

class MutableComponent(private val component: Component) {

    fun mutate(block: Component.() -> Unit = {}): MutableComponent = MutableComponent(component.apply(block))

    operator fun invoke(): Component = this.component
}

sealed class Component(val id: String, val modifier: Modifier) {

    class Screen(id: String, val content: Component, val history: Boolean = false) : Component(id, Modifier)

    class Text(id: String, var text: String, modifier: Modifier = Modifier) : Component(id = id, modifier = modifier)

    class TextField(id: String, modifier: Modifier, var text: String = "", var onValueChange: (String) -> Unit = {}) :
        Component(id, modifier)

    class Button(id: String,
                 var text: String,
                 modifier: Modifier = Modifier) :
        Component(id, modifier) {
        var onClick: () -> Unit = {}
    }

    class Container(id: String, val children: List<Component>, modifier: Modifier = Modifier) :
        Component(id = id, modifier = modifier)

}

class ComponentContext(private val root: Component) {

    private val fastLookUp = mutableMapOf<String, Component>()

    fun <T : Component> id(id: String, componentBlock: T.() -> Unit = {}): T? =
        (lookUp(id, root) as T?)?.apply(componentBlock)


    fun contains(id: String): Boolean = this.lookUp(id, root) != null

    private fun lookUp(id: String, curr: Component): Component? {
        if (fastLookUp.containsKey(id)) {
            return fastLookUp[id]
        }
        if (id == curr.id) {
            fastLookUp[id] = curr
            return curr
        }
        var found: Component? = null
        if (curr is Component.Screen) {
            val nullableFound = lookUp(id, curr.content)
            if (nullableFound != null) {
                found = nullableFound
            }
        } else if (curr is Component.Container) {
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

fun Component.startContext() : ComponentContext = ComponentContext(this)

fun ComponentComposer(
    component: Component,
    lookUpScope: ComponentContext.() -> Unit = {}
): @Composable () -> Unit {
    ComponentContext(component).apply(lookUpScope)
    fun ComponentComposerInternal(component: Component): @Composable () -> Unit =
        when (component) {
            is Component.Text -> ({
                Text(text = component.text, modifier = component.modifier)
            })
            is Component.Button -> ({
                Button(modifier = component.modifier, onClick = {
                    component.onClick()
                }) {
                    Text(component.text)
                }
            })
            is Component.Container -> ({
                Column(modifier = component.modifier.fillMaxSize()) {
                    for (child in component.children) {
                        key(child.id) {
                            ComponentComposerInternal(child)()
                        }
                    }
                }
            })
            is Component.TextField -> ({
                var value by remember { mutableStateOf(component.text) }
                TextField(
                    value = value,
                    onValueChange = {
                        component.onValueChange(it)
                        component.text = it
                        value = it
                    },
                    label = { Text("Enter text") },
                    maxLines = 2,
                    textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                    modifier = component.modifier
                )
            })
            is Component.Screen -> ({
                Column(modifier = Modifier.fillMaxSize()) {
                    ComponentComposerInternal(component.content)()
                }
            })
        }
    return ComponentComposerInternal(component)
}

class ComponentDeserializer : JsonDeserializer<Component> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Component =
        when (json) {
            is JsonObject -> {
                when (json["type"].asString) {
                    "screen" -> json.deserializeScreen(context, typeOfT)
                    "container" -> json.deserializeContainer(context, typeOfT)
                    "button" -> json.deserializeButton()
                    "text" -> json.deserializeText()
                    "edit" -> json.deserializeEdit()
                    else -> throw UnsupportedOperationException()
                }
            }
            else -> throw UnsupportedOperationException("$json not supported for deserialization")
        }

    private fun JsonObject.deserializeScreen(context: JsonDeserializationContext, typeOfT: Type): Component {
        return Component.Screen(
            id = this["id"].asString,
            content = context.deserialize(this["content"].asJsonObject, typeOfT)
        )
    }

    private fun JsonObject.deserializeContainer(context: JsonDeserializationContext, typeOfT: Type): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Container(
                id = id,
                modifier = modifier,
                children = this["children"].asJsonArray.map { context.deserialize(it, typeOfT) }
            )
        }
    }

    private fun JsonObject.deserializeButton(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Button(
                id = id,
                modifier = modifier,
                text = this["text"].asString ?: ""
            )
        }
    }

    private fun JsonObject.deserializeText(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Text(
                id = id,
                modifier = modifier,
                text = this["text"].asString ?: "",
            )
        }
    }

    private fun JsonObject.deserializeEdit(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.TextField(
                id = id,
                modifier = modifier
            )
        }
    }

    private inline fun JsonObject.deserializePrepComponent(factory: (String, Modifier) -> Component): Component =
        factory(this["id"].asString, modifiers())

    private fun JsonObject.modifiers(): Modifier = Modifier

}


fun String.toComponent(): Component {
    val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Component::class.java, ComponentDeserializer())
            .create()
    }
    return gson.fromJson(this, Component::class.java)
}

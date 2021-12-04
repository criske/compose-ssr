package cpf.crskdev.compose.ssr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import cpf.crskdev.compose.ssr.interceptors.core.Page
import java.lang.reflect.Type

class ComponentDeserializer : JsonDeserializer<Component> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Component =
        when (json) {
            is JsonObject -> {
                when (json["type"]?.asString ?: "") {
                    "screen" -> json.deserializeScreen(context, typeOfT)
                    "container" -> json.deserializeContainer(context, typeOfT)
                    "list" -> json.deserializeList(context, typeOfT)
                    "button" -> json.deserializeButton()
                    "text" -> json.deserializeText()
                    "edit" -> json.deserializeEdit()
                    else -> Component.None
                }
            }
            else -> Component.None
        }

    private fun JsonObject.deserializeScreen(context: JsonDeserializationContext, typeOfT: Type): Component {
        return Component.Group.Screen(
            id = this["id"].asString,
            content = context.deserialize(this["content"].asJsonObject, typeOfT)
        )
    }

    private fun JsonObject.deserializeContainer(context: JsonDeserializationContext, typeOfT: Type): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Group.Container(
                id = id,
                modifier = modifier,
                children = this["children"].asJsonArray.map { context.deserialize(it, typeOfT) }
            )
        }
    }

    private fun JsonObject.deserializeList(context: JsonDeserializationContext, typeOfT: Type): Component =
        deserializePrepComponent { id, modifier ->
            val children = this["children"].asJsonArray
            Component.Group.PagedList(
                id = id,
                modifier = modifier,
                children = children.map { context.deserialize(it, typeOfT) },
                page = Page(
                    id,
                    this["pageSize"]?.asInt ?: children.size(),
                    this["pageNo"]?.asInt ?: 1,
                    this["pageTotal"]?.asInt ?: children.size()
                )
            )
        }

    private fun JsonObject.deserializeButton(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Button(
                id = id,
                modifier = modifier,
                text = modifier.attr("text", "")
            )
        }
    }

    private fun JsonObject.deserializeText(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.Text(
                id = id,
                modifier = modifier,
                text = modifier.attr("text", ""),
            )
        }
    }

    private fun JsonObject.deserializeEdit(): Component {
        return deserializePrepComponent { id, modifier ->
            Component.TextField(
                id = id,
                modifier = modifier
                    .putAttrFrom(this, "editType") {
                        when (asString) {
                            "password" -> PasswordVisualTransformation()
                            else -> VisualTransformation.None
                        }
                    }
                    .putAttrFrom(this, "label") { asString },
                text = modifier.attr("text", ""),
            )
        }
    }

    private inline fun JsonObject.deserializePrepComponent(factory: (String, Modifier) -> Component): Component =
        factory(this["id"].asString, modifiers())

    private fun JsonObject.modifiers(): Modifier = Modifier
        .putAttrFrom(this, "disabled") { asBoolean }
        .putAttrFrom(this, "text") { asString }
        .from(this, "border") {
            val split = asString.split(",")
            val border = BorderStroke(
                split[0].toInt().dp,
                SolidColor(
                    Color(
                        android.graphics.Color.parseColor(split[1])
                    )
                )
            )
            Modifier.border(border)
        }
        .from(this, "background") {
            Modifier.background(
                Color(
                    android.graphics.Color.parseColor(asString)
                )
            )
        }
        .from(this, "padding") {
            val split = IntArray(4).apply {
                asString
                    .split(",")
                    .take(4)
                    .map { it.toInt() }
                    .forEachIndexed { idx, dp -> this[idx] = dp }
            }
            val paddingValues = PaddingValues(split[0].dp, split[1].dp, split[2].dp, split[3].dp)
            Modifier.padding(paddingValues)
        }
        .from(this, "width") {
            when (val value = asString) {
                "fill" -> Modifier.fillMaxWidth()
                else -> Modifier.width(value.toInt().dp)
            }
        }
        .from(this, "height") {
            when (val value = asString) {
                "fill" -> Modifier.fillMaxHeight()
                else -> Modifier.height(value.toInt().dp)
            }
        }


    private fun Modifier.putAttrFrom(src: JsonObject, key: String, caster: JsonElement.() -> Any) =
        if (src[key] != null) {
            putAttr(key, src[key].caster())
        } else {
            this
        }

    private fun Modifier.from(src: JsonObject, key: String, modifier: JsonElement.() -> Modifier): Modifier =
        if (src[key] != null) {
            this.then(src[key].modifier())
        } else {
            this
        }
}
package cpf.crskdev.compose.ssr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Colors
import androidx.compose.material.lightColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import cpf.crskdev.compose.ssr.interceptors.core.Page
import java.lang.reflect.Type

@ExperimentalUnitApi
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
            content = context.deserialize(this["content"].asJsonObject, typeOfT),
            theme = this["theme"]?.let {
                val colors = it.asJsonObject["colors"]?.let { colorsEl ->
                    val colorsObj = colorsEl.asJsonObject
                    val current = lightColors()
                    Colors(
                        primary = colorsObj["primary"]?.asString?.toColor() ?: current.primary,
                        primaryVariant = colorsObj["primary-variant"]?.asString?.toColor() ?: current.primaryVariant,
                        secondary = colorsObj["secondary"]?.asString?.toColor() ?: current.secondary,
                        secondaryVariant = colorsObj["secondary-variant"]?.asString?.toColor() ?: current.secondaryVariant,
                        background = colorsObj["background"]?.asString?.toColor() ?: current.background,
                        surface = colorsObj["surface"]?.asString?.toColor() ?: current.surface,
                        error = colorsObj["primary"]?.asString?.toColor() ?: current.error,
                        onPrimary = colorsObj["on-primary"]?.asString?.toColor() ?: current.onPrimary,
                        onSecondary = colorsObj["on-secondary"]?.asString?.toColor() ?: current.onSecondary,
                        onBackground = colorsObj["on-background"]?.asString?.toColor() ?: current.onBackground,
                        onSurface = colorsObj["on-surface"]?.asString?.toColor() ?: current.onSurface,
                        onError = colorsObj["on-error"]?.asString?.toColor() ?: current.onError,
                        true
                    )
                } ?: lightColors()
                Component.Group.Screen.Theme(colors)
            } ?: Component.Group.Screen.Theme(lightColors())
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
                SolidColor(split[1].toColor())
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
                    .map {
                        it
                            .trim()
                            .toInt()
                    }
                    .forEachIndexed { idx, dp -> this[idx] = dp }
            }
            val paddingValues = PaddingValues(split[0].dp, split[1].dp, split[2].dp, split[3].dp)
            Modifier.padding(paddingValues)
        }
        .from(this, "width") {
            val value = asString
            when {
                value == "fill" -> Modifier.fillMaxWidth()
                value == "wrap" -> Modifier.wrapContentWidth()
                value.endsWith("fr") -> Modifier.fillMaxWidth(
                    value
                        .trimEnd('f', 'r')
                        .toFloat()
                )
                else -> Modifier.width(value.toInt().dp)
            }
        }
        .from(this, "height") {
            val value = asString
            when {
                value == "fill" -> Modifier.fillMaxHeight()
                value == "wrap" -> Modifier.wrapContentHeight()
                value.endsWith("fr") -> Modifier.fillMaxHeight(
                    value
                        .trimEnd('f', 'r')
                        .toFloat()
                )
                else -> Modifier.height(value.toInt().dp)
            }
        }
        .from(this, "alignment") {
            Modifier
                .from(asJsonObject, "main-axis") {
                    val value = asString
                    Modifier.putAttr(
                        "main-axis-alignment", when {
                            value == "center" -> Arrangement.Center
                            value.endsWith("spacing") -> Arrangement.spacedBy(
                                value
                                    .trimEnd(*"spacing".toCharArray())
                                    .toInt().dp
                            )
                            else -> Arrangement.Top
                        }
                    )
                }
                .from(asJsonObject, "cross-axis") {
                    Modifier.putAttr(
                        "cross-axis-alignment", when (val value = asString) {
                            "center" -> Alignment.CenterHorizontally
                            else -> Alignment.Start
                        }
                    )
                }

        }
        .from(this, "style") {
            val style = asJsonObject
            Modifier.putAttr("style", TextStyle(
                color = style["color"]?.asString?.toColor() ?: Color.Unspecified,
                fontWeight = style["font-weight"]?.let { FontWeight(it.asInt.coerceIn(1, 1000)) },
                fontSize = style["font-size"]?.let { TextUnit(it.asFloat, TextUnitType.Sp) } ?: TextUnit.Unspecified,
                fontStyle = style["font-style"]?.let {
                    when (val value = it.asString) {
                        "italic" -> FontStyle.Italic
                        else -> FontStyle.Normal
                    }
                }
            ))
        }


    private fun Modifier.putAttrFrom(src: JsonObject, key: String, caster: JsonElement.() -> Any) =
        if (src[key] != null) {
            putAttr(key, src[key].caster())
        } else {
            this
        }

    private fun Modifier.from(src: JsonObject,
                              key: String,
                              modifier: JsonElement.() -> Modifier): Modifier =
        if (src[key] != null) {
            this.then(src[key].modifier())
        } else {
            this
        }

    private fun String.toColor() = Color(android.graphics.Color.parseColor(this))
}
package cpf.crskdev.compose.ssr

import org.junit.Test

/**
 * Created by Cristian Pela on 16.11.2021.
 */
class MainActivityTest {

    @Test
    fun `should deserialize json to component`() {
        val component = json.toComponent()
        println(component)
    }

}
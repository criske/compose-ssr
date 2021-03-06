package cpf.crskdev.compose.ssr.backend

import android.net.Uri

/**
 * Created by Cristian Pela on 20.11.2021.
 */
sealed class Rendered {

    companion object {
        operator fun invoke(value: Response) = Data(value)
    }

    class Data(val value: Response) : Rendered()

    class Redirect(val uri: Uri, val jsonBody: String = "") : Rendered()

}

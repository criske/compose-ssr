package cpf.crskdev.compose.ssr.interceptors.core

data class Page(val componentId: String, val size: Int, val number: Int, val total: Int){

    val itemsSize: Int
        get() = size * number

    operator fun plus(no: Int): Page = this.copy(number = number + no)

}
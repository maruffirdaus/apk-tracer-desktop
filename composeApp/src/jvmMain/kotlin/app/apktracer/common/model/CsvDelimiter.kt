package app.apktracer.common.model

enum class CsvDelimiter(
    val label: String,
    val value: Char
) {
    COMMA("Comma (,)", ','),
    SEMICOLON("Semicolon (;)", ';')
}
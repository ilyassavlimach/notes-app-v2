package com.machinarium.notesv2.feature.noteeditor

/** The editor's input rules, separate from the ViewModel so they are tested on their own. */
internal object NoteFieldValidator {
    const val TITLE_MAX_LENGTH = 100
    const val BODY_MAX_LENGTH = 5_000

    /** "Required" shows only once the user wrote a body, so an empty new note doesn't start with an error. */
    fun titleError(
        title: String,
        body: String,
    ): FieldError? = when {
        title.length > TITLE_MAX_LENGTH -> FieldError.TooLong
        title.isBlank() && body.isNotBlank() -> FieldError.Required
        else -> null
    }

    fun bodyError(body: String): FieldError? = if (body.length > BODY_MAX_LENGTH) FieldError.TooLong else null

    fun isSavable(
        title: String,
        body: String,
    ): Boolean = title.isNotBlank() && titleError(title, body) == null && bodyError(body) == null
}

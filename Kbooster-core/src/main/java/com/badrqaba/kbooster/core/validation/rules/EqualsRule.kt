package com.badrqaba.kbooster.core.validation.rules

import com.badrqaba.kbooster.core.validation.core.FieldError

class EqualsRule<T>(
    override val field: String,
    private val otherField: String,
    private val message: String = "Does not match"
) : BaseRule<T>(field) {

    @Suppress("UNCHECKED_CAST")
    override fun validate(value: T?): FieldError? {
        if (value == null) return null

        val prop = value::class.members
            .first { it.name == field } as kotlin.reflect.KProperty1<T, *>

        val otherProp = value::class.members
            .first { it.name == otherField } as kotlin.reflect.KProperty1<T, *>

        val fieldValue = prop.get(value)
        val otherFieldValue = otherProp.get(value)

        return if (fieldValue != otherFieldValue) {
            FieldError(message, "equals")
        } else null
    }
}
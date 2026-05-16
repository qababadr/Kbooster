package com.badrqaba.kbooster.core.validation.core

import com.badrqaba.kbooster.core.validation.rules.ValidationRule

open class Validator<T>(
    private val rules: List<ValidationRule<T>>
) {

    fun validate(target: T): ValidationResult {

        val errors = mutableMapOf<String, MutableList<FieldError>>()

        for (rule in rules) {
            val error = rule.validate(target)
            if (error != null) {
                errors.getOrPut(rule.field) { mutableListOf() }
                    .add(error)
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

//open class Validator<T>(
//    private val rules: List<FieldRule<T>>
//) {
//
//    fun validate(target: T): ValidationResult {
//        if (rules.isEmpty()) return ValidationResult.Success
//
//        val errors = mutableMapOf<String, MutableList<FieldError>>()
//
//        for (rule in rules) {
//            val value = rule.extractor(target)
//
//            val error = rule.validate(value)
//            if (error != null) {
//                errors.getOrPut(rule.fieldName) { mutableListOf() }
//                    .add(error)
//            }
//        }
//
//        return if (errors.isEmpty()) {
//            ValidationResult.Success
//        } else {
//            ValidationResult(
//                isValid = false,
//                errors = errors
//            )
//        }
//    }
//}
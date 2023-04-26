package com.instacart.sample.di

import kotlin.annotation.AnnotationTarget.*
import me.tatarka.inject.annotations.Scope

@Scope @Target(CLASS, FUNCTION, PROPERTY_GETTER) annotation class AppScope

package ch.trick17.rolez.lang.desugar

import java.lang.annotation.Retention
import java.lang.annotation.Target

@Target(METHOD)
@Retention(RUNTIME)
annotation Rule {}
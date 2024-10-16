package org.hszg.sign_analyzer

/**
 * Exception that is thrown when an error occurs during sign analysis.
 */
class SignAnalysisException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
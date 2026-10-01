package teamcityapp.libraries.utils

/**
 * Hilt installs screen modules in every component of the corresponding Android type.
 * A concrete screen binding must therefore validate its owner when it is requested.
 */
inline fun <reified T : Any> Any.requireScreenOwner(): T {
    check(this is T) {
        "Screen binding for ${T::class.java.name} was requested from ${javaClass.name}. " +
            "Request this dependency only from its owning screen."
    }
    return this
}

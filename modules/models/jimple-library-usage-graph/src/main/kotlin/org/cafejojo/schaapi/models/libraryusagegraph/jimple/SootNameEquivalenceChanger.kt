package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.G
import soot.Singletons.Global
import soot.Type
import soot.jimple.Jimple
import soot.jimple.internal.JimpleLocal

/**
 * Hacks around a supposed error in Soot that makes two [JimpleLocal]s structurally inequivalent if their names are
 * unequal.
 *
 * Also see https://github.com/Sable/soot/pull/954.
 */
object SootNameEquivalenceChanger {
    /**
     * Makes [soot.jimple.Jimple.v] return a [NameIgnoringJimple] so that [Jimple.newLocal] will create
     * [NameIgnoringJimpleLocal]s.
     */
    fun activate() {
        G.setGlobalObjectGetter(NameIgnoringGlobalObjectGetter())
    }
}

/**
 * A [G.GlobalObjectGetter] that uses a [NameIgnoringG].
 */
private class NameIgnoringGlobalObjectGetter : G.GlobalObjectGetter {
    private var g: G = NameIgnoringG()

    override fun getG() = g

    override fun reset() {
        g = G()
    }
}

/**
 * A [G] that contains [NameIgnoringJimple].
 */
private class NameIgnoringG : G() {
    private var jimple: Jimple? = null

    init {
        soot_jimple_Jimple()
    }

    @Suppress("FunctionNaming") // Cannot change name of overridden function
    override fun soot_jimple_Jimple(): Jimple? {
        if (jimple == null) {
            synchronized(this) {
                if (jimple == null) {
                    jimple = NameIgnoringJimple(g)
                }
            }
        }

        return jimple
    }

    @Suppress("FunctionNaming") // Cannot change name of overridden function
    override fun release_soot_jimple_Jimple() {
        jimple = null
    }
}

/**
 * A [Jimple] that creates [NameIgnoringJimpleLocal]s instead of regular [JimpleLocal]s.
 */
private class NameIgnoringJimple(g: Global) : Jimple(g) {
    override fun newLocal(name: String?, type: Type?) = NameIgnoringJimpleLocal(name, type)
}

/**
 * A [JimpleLocal] that is equivalent to other [JimpleLocal]s based only on its type and not on its name.
 */
private class NameIgnoringJimpleLocal(name: String?, type: Type?) : JimpleLocal(name, type) {
    override fun equivTo(other: Any) = other is JimpleLocal && this.type == other.type

    @Suppress("MagicNumber") // Necessary for compatibility with Soot
    override fun equivHashCode() = 31 + (type?.hashCode() ?: 0)

    override fun clone() = NameIgnoringJimpleLocal(name, type)
}

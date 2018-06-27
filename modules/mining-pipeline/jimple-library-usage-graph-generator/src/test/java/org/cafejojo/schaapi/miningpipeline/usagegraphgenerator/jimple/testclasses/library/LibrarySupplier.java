package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library;

import java.util.function.Supplier;

/**
 * Exactly the same as {@link Supplier}, except that it counts as a library usage.
 */
public interface LibrarySupplier<T> extends Supplier<T> {
}

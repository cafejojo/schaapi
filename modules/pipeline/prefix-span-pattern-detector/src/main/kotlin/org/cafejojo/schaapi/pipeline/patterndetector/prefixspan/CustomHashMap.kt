package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

/**
 * A (mutable) [HashMap] that allows one to use a custom hash function, [customHash].
 */
class CustomHashMap<K, V>(private val customHash: (K) -> Int) : MutableMap<K, V> {
    private val innerMap = HashMap<HashWrapper<K>, V>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = innerMap.entries.map { (key, value) -> Entry(key.value, value) }.toMutableSet()
    override val keys: MutableSet<K>
        get() = innerMap.keys.map { it.value }.toMutableSet()
    override val size: Int
        get() = innerMap.size
    override val values: MutableCollection<V>
        get() = innerMap.values

    override fun containsKey(key: K) = innerMap.containsKey(wrapKey(key))

    override fun containsValue(value: V) = innerMap.containsValue(value)

    override fun get(key: K) = innerMap[wrapKey(key)]

    override fun isEmpty() = innerMap.isEmpty()

    override fun clear() = innerMap.clear()

    override fun put(key: K, value: V) = innerMap.put(wrapKey(key), value)

    override fun putAll(from: Map<out K, V>) = innerMap.putAll(from.map { (key, value) -> Pair(wrapKey(key), value) })

    override fun remove(key: K) = innerMap.remove(wrapKey(key))

    private fun wrapKey(key: K) = HashWrapper(key, customHash)

    /**
     * A simple implementation of [MutableMap.MutableEntry].
     */
    internal class Entry<K, V>(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val oldValue = value
            value = newValue
            return oldValue
        }
    }
}

/**
 * Wraps an object such that its [hashCode] calls [customHash]. This allows one to "override" the [hashCode] method
 * without actually changing the object.
 */
class HashWrapper<K>(val value: K, private val customHash: (K) -> Int) {
    /**
     * Returns true iff [other]'s value equals [value].
     *
     * @param other the object to compare to this
     * @return true iff [other]'s value equals [value]
     */
    override fun equals(other: Any?) = other is HashWrapper<*> && this.value == other.value

    /**
     * Returns the result of applying [customHash] to [value].
     *
     * @return the result of applying [customHash] to [value]
     */
    override fun hashCode(): Int {
        return customHash(value)
    }
}

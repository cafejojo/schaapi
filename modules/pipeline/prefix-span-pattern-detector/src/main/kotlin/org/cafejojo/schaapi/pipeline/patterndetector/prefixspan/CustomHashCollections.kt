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
 * A (mutable) [HashSet] that allows one to use a custom hash function, [customHash].
 */
class CustomHashSet<K>(private val customHash: (K) -> Int) : MutableSet<K> {
    private val innerSet = HashSet<HashWrapper<K>>()

    override val size: Int
        get() = innerSet.size

    override fun add(element: K) = innerSet.add(wrapElement(element))

    override fun addAll(elements: Collection<K>) = innerSet.addAll(elements.map { wrapElement(it) })

    override fun clear() = innerSet.clear()

    override fun iterator(): MutableIterator<K> {
        return object : MutableIterator<K> {
            private val innerIterator = innerSet.iterator()

            override fun hasNext() = innerIterator.hasNext()

            override fun next() = innerIterator.next().value

            override fun remove() = innerIterator.remove()
        }
    }

    override fun remove(element: K) = innerSet.remove(wrapElement(element))

    override fun removeAll(elements: Collection<K>) = innerSet.removeAll(elements.map { wrapElement(it) })

    override fun retainAll(elements: Collection<K>) = innerSet.retainAll(elements.map { wrapElement(it) })

    override fun contains(element: K) = innerSet.contains(wrapElement(element))

    override fun containsAll(elements: Collection<K>) = innerSet.containsAll(elements.map { wrapElement(it) })

    override fun isEmpty() = innerSet.isEmpty()

    private fun wrapElement(key: K) = HashWrapper(key, customHash)
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

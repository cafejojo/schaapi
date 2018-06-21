package org.cafejojo.schaapi.models

/**
 * A (mutable) [HashMap] that allows one to use custom [equals] and [hashCode] functions.
 */
class CustomEqualsHashMap<K, V>(
    private val customEquals: (K, Any?) -> Boolean,
    private val customHash: (K) -> Int
) : MutableMap<K, V> {
    private val innerMap = HashMap<EqualsWrapper<K>, V>()

    /**
     * A copy of the entries.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = innerMap.entries.map { (key, value) -> Entry(key.value, value) }.toMutableSet()
    /**
     * A copy of the keys.
     */
    override val keys: MutableSet<K>
        get() = innerMap.keys.map { it.value }.toMutableSet()
    override val size: Int
        get() = innerMap.size
    /**
     * A copy of the values.
     */
    override val values: MutableCollection<V>
        get() = innerMap.values

    override fun containsKey(key: K) = innerMap.containsKey(wrapKey(key))

    override fun containsValue(value: V) = innerMap.containsValue(value)

    override fun get(key: K) = innerMap[wrapKey(key)]

    override fun isEmpty() = innerMap.isEmpty()

    override fun clear() = innerMap.clear()

    override fun put(key: K, value: V) = innerMap.put(wrapKey(key), value)

    override fun putAll(from: Map<out K, V>) = innerMap.putAll(from.map { (key, value) -> wrapKey(key) to value })

    override fun remove(key: K) = innerMap.remove(wrapKey(key))

    private fun wrapKey(key: K) = EqualsWrapper(key, customEquals, customHash)

    /**
     * A simple implementation of [MutableMap.MutableEntry].
     */
    internal class Entry<K, V>(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V) = value.also { value = newValue }
    }
}

/**
 * A (mutable) [HashSet] that allows one to use custom [equals] and [hashCode] functions.
 */
class CustomEqualsHashSet<K>(
    private val customEquals: (K, Any?) -> Boolean,
    private val customHash: (K) -> Int
) : MutableSet<K> {
    private val innerSet = HashSet<EqualsWrapper<K>>()

    override val size: Int
        get() = innerSet.size

    override fun add(element: K) = innerSet.add(wrapElement(element))

    override fun addAll(elements: Collection<K>) = innerSet.addAll(elements.map { wrapElement(it) })

    override fun clear() = innerSet.clear()

    override fun iterator() =
        object : MutableIterator<K> {
            private val innerIterator = innerSet.iterator()

            override fun hasNext() = innerIterator.hasNext()

            override fun next() = innerIterator.next().value

            override fun remove() = innerIterator.remove()
        }

    override fun remove(element: K) = innerSet.remove(wrapElement(element))

    override fun removeAll(elements: Collection<K>) = innerSet.removeAll(elements.map { wrapElement(it) })

    override fun retainAll(elements: Collection<K>) = innerSet.retainAll(elements.map { wrapElement(it) })

    override fun contains(element: K) = innerSet.contains(wrapElement(element))

    override fun containsAll(elements: Collection<K>) = innerSet.containsAll(elements.map { wrapElement(it) })

    override fun isEmpty() = innerSet.isEmpty()

    private fun wrapElement(key: K) = EqualsWrapper(key, customEquals, customHash)
}

/**
 * A [MutableList] that allows one to use custom [equals] and [hashCode] functions.
 */
@Suppress("TooManyFunctions") // All methods are overrides
class CustomEqualsList<K>(
    private val customEquals: (K, Any?) -> Boolean,
    private val customHash: (K) -> Int
) : MutableList<K> {
    private val innerList = mutableListOf<EqualsWrapper<K>>()

    override val size: Int
        get() = innerList.size

    /**
     * Constructs a new [CustomEqualsList] that contains [elements].
     */
    constructor(
        elements: Collection<K>,
        customEquals: (K, Any?) -> Boolean,
        customHash: (K) -> Int
    ) : this(customEquals, customHash) {
        addAll(elements)
    }

    override fun contains(element: K) = innerList.contains(wrapElement(element))

    override fun containsAll(elements: Collection<K>) = innerList.containsAll(elements.map { wrapElement(it) })

    override fun get(index: Int) = innerList[index].value

    override fun indexOf(element: K) = innerList.indexOf(wrapElement(element))

    override fun isEmpty() = innerList.isEmpty()

    override fun iterator() = listIterator()

    override fun lastIndexOf(element: K) = innerList.lastIndexOf(wrapElement(element))

    override fun add(element: K) = innerList.add(wrapElement(element))

    override fun add(index: Int, element: K) = innerList.add(index, wrapElement(element))

    override fun addAll(elements: Collection<K>) = innerList.addAll(elements.map { wrapElement(it) })

    override fun addAll(index: Int, elements: Collection<K>) = innerList.addAll(index, elements.map { wrapElement(it) })

    override fun clear() = innerList.clear()

    override fun listIterator() = listIterator(0)

    override fun listIterator(index: Int) = object : MutableListIterator<K> {
        private val innerIterator = innerList.listIterator(index)

        override fun hasPrevious() = innerIterator.hasPrevious()

        override fun nextIndex() = innerIterator.nextIndex()

        override fun previous() = innerIterator.previous().value

        override fun previousIndex() = innerIterator.previousIndex()

        override fun add(element: K) = innerIterator.add(wrapElement(element))

        override fun hasNext() = innerIterator.hasNext()

        override fun next() = innerIterator.next().value

        override fun remove() = innerIterator.remove()

        override fun set(element: K) = innerIterator.set(wrapElement(element))
    }

    override fun remove(element: K) = innerList.remove(wrapElement(element))

    override fun removeAt(index: Int) = innerList.removeAt(index).value

    override fun removeAll(elements: Collection<K>) = innerList.removeAll(elements.map { wrapElement(it) })

    override fun retainAll(elements: Collection<K>) = innerList.retainAll(elements.map { wrapElement(it) })

    override fun set(index: Int, element: K) = innerList.set(index, wrapElement(element)).value

    override fun subList(fromIndex: Int, toIndex: Int) =
        CustomEqualsList(
            innerList.subList(fromIndex, toIndex).map { it.value },
            customEquals,
            customHash
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomEqualsList<*>) return false
        if (this.size != other.size) return false

        return (0 until innerList.size).all { this.innerList[it] == other.innerList[it] }
    }

    override fun hashCode() = innerList.hashCode()

    private fun wrapElement(key: K) = EqualsWrapper(key, customEquals, customHash)
}

/**
 * Wraps an object such that [equals] is implemented as [customEquals] and [hashCode] is implemented as [customHash].
 */
data class EqualsWrapper<K>(
    val value: K,
    private val customEquals: (K, Any?) -> Boolean = { self, other -> self == other },
    private val customHash: (K) -> Int = { self -> self?.hashCode() ?: 0 }
) {
    /**
     * Returns the result of applying [customEquals] to the [value]s of this and [other].
     *
     * @param other the wrapper
     * @return the result of applying [customEquals] to the [value]s of this and [other]
     */
    override fun equals(other: Any?) = other is EqualsWrapper<*> && customEquals(this.value, other.value)

    /**
     * Returns the result of applying [customHash] to [value].
     *
     * @return the result of applying [customHash] to [value]
     */
    override fun hashCode() = customHash(value)
}

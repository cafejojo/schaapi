package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

data class HashedAny(private val hashCode: Int) {
    override fun equals(other: Any?) = this === other
    override fun hashCode() = hashCode
}

class HashWrapperTest : Spek({
    describe("value wrapper") {
        it("returns the inserted value") {
            val key = Any()
            val wrapper = HashWrapper(key, Any::hashCode)

            assertThat(wrapper.value).isEqualTo(key)
        }

        it("uses the given hash function") {
            val key = Any()
            val wrapper = HashWrapper(key, { 11716 })

            assertThat(wrapper.hashCode()).isEqualTo(11716)
        }

        it("equals itself") {
            val key = Any()
            val wrapper = HashWrapper(key, Any::hashCode)

            assertThat(wrapper).isEqualTo(wrapper)
        }

        it("does not equal a non-value wrapper") {
            val key = Any()
            val wrapper = HashWrapper(key, Any::hashCode)

            assertThat(wrapper).isNotEqualTo(Any())
        }

        it("equals a wrapper containing the same value") {
            val key = Any()
            val wrapperA = HashWrapper(key, Any::hashCode)
            val wrapperB = HashWrapper(key, Any::hashCode)

            assertThat(wrapperA).isEqualTo(wrapperB)
        }

        it("equals a wrapper containing an equal value") {
            val keyA = emptyList<String>()
            val keyB = emptyList<String>()
            val wrapperA = HashWrapper(keyA, List<String>::hashCode)
            val wrapperB = HashWrapper(keyB, List<String>::hashCode)

            assertThat(wrapperA).isEqualTo(wrapperB)
        }

        it("equals a wrapper containing a value of which the hash code coincides") {
            val keyA = HashedAny(14083)
            val keyB = HashedAny(14083)
            val wrapperA = HashWrapper(keyA, Any::hashCode)
            val wrapperB = HashWrapper(keyB, Any::hashCode)

            assertThat(wrapperA).isEqualTo(wrapperB)
        }
    }
})

class CustomHashHashMapTest : Spek({
    describe("custom hash map") {
        lateinit var map: CustomHashHashMap<Any, Any>

        beforeEachTest {
            map = CustomHashHashMap(Any::hashCode)
        }

        it("starts out empty") {
            assertThat(map).hasSize(0).isEmpty()
            assertThat(map.entries).isEmpty()
        }

        it("can store multiple key-value pairs") {
            val keyA = HashedAny(47204)
            val keyB = HashedAny(42751)
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomHashHashMap.Entry(keyA, valueA),
                    CustomHashHashMap.Entry(keyB, valueB)
                )
        }

        it("can store multiple key-value pairs in one call") {
            val pairs = listOf(
                Pair(HashedAny(16445), Any()),
                Pair(HashedAny(91213), Any()),
                Pair(HashedAny(23669), Any())
            ).toMap()

            map.putAll(pairs)

            assertThat(map)
                .hasSize(3)
                .containsAllEntriesOf(pairs)
        }

        it("can store a value under two keys") {
            val keyA = HashedAny(43940)
            val keyB = HashedAny(18339)
            val value = Any()

            map[keyA] = value
            map[keyB] = value

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomHashHashMap.Entry(keyA, value),
                    CustomHashHashMap.Entry(keyB, value)
                )
        }

        it("does not hold duplicates") {
            val key = Any()
            val valueA = Any()
            val valueB = Any()

            map[key] = valueA
            map[key] = valueB

            assertThat(map)
                .hasSize(1)
                .contains(CustomHashHashMap.Entry(key, valueB))
        }

        it("uses the custom hash code to determine duplicates") {
            val keyA = HashedAny(14083)
            val keyB = HashedAny(14083)
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            assertThat(map[keyA])
                .isEqualTo(map[keyB])
                .isEqualTo(valueB)
        }

        it("can remove keys") {
            val keyA = HashedAny(79283)
            val keyB = HashedAny(82993)
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            map.remove(keyA)

            assertThat(map.keys).doesNotContain(keyA)
            assertThat(map).doesNotContainKey(keyA)
        }

        it("can be emptied") {
            map[Any()] = Any()
            map.clear()

            assertThat(map).isEmpty()
        }

        it("can be searched by value") {
            val keyA = HashedAny(46083)
            val keyB = HashedAny(89198)
            val keyC = HashedAny(48262)
            val valueA = Any()
            val valueB = Any()
            val valueC = Any()

            map[keyA] = valueA
            map[keyB] = valueB
            map[keyC] = valueC

            assertThat(map.values).contains(valueB)
            assertThat(map.containsValue(valueB)).isTrue()
        }
    }
})

class CustomHashHashSetTest : Spek({
    describe("custom hash set") {
        lateinit var set: CustomHashHashSet<Any>

        beforeEachTest {
            set = CustomHashHashSet(Any::hashCode)
        }

        it("can store multiple elements") {
            val elements = listOf(HashedAny(16977), HashedAny(18313))

            set.add(elements[0])
            set.add(elements[1])

            assertThat(set)
                .hasSize(2)
                .containsAll(elements)
        }

        it("does not store duplicates") {
            val element = Any()

            set.add(element)
            set.add(element)

            assertThat(set)
                .hasSize(1)
                .containsExactly(element)
        }

        it("determines duplicates by hash code") {
            val elements = listOf(HashedAny(60786), HashedAny(60786))

            set.addAll(elements)

            assertThat(set).hasSize(1)
        }

        it("can accept multiple elements at once") {
            val elements = listOf(HashedAny(62536), HashedAny(61084), HashedAny(49265))

            set.addAll(elements)

            assertThat(set)
                .hasSize(3)
                .containsAll(elements)
        }

        it("can be cleared") {
            val elements = listOf(HashedAny(40881), HashedAny(26322), HashedAny(95920))

            set.addAll(elements)
            set.clear()

            assertThat(set).hasSize(0).isEmpty()
        }

        it("can be iterated over") {
            val elements = listOf(HashedAny(14408), HashedAny(19725), HashedAny(29802))

            set.addAll(elements)

            assertThat(set.iterator())
                .hasSize(3)
                .containsAll(elements)
        }

        it("can delete while iterating") {
            val elements = listOf(HashedAny(26944), HashedAny(83091), HashedAny(22670))

            set.addAll(elements)
            val iterator = set.iterator()
            iterator.next()
            iterator.next()
            iterator.remove()

            assertThat(set).hasSize(2)
        }

        it("can remove elements") {
            val elements = listOf(HashedAny(19613), HashedAny(78295), HashedAny(28409))

            set.addAll(elements)
            set.remove(elements[1])

            assertThat(set)
                .hasSize(2)
                .contains(elements[0], elements[2])
        }

        it("can remove multiple elements at once") {
            val elements = listOf(
                HashedAny(22282), HashedAny(96470), HashedAny(60858), HashedAny(26560), HashedAny(53746)
            )

            set.addAll(elements)
            set.removeAll(listOf(elements[1], elements[3]))

            assertThat(set)
                .hasSize(3)
                .contains(elements[0], elements[2], elements[4])
        }

        it("can be filtered") {
            val elements = listOf(HashedAny(83102), HashedAny(84548), HashedAny(42789))
            val retain = listOf(elements[1])

            set.addAll(elements)
            set.retainAll(retain)

            assertThat(set)
                .hasSize(1)
                .containsAll(retain)
        }

        it("knows which element it contains") {
            val elements = listOf(HashedAny(41367), HashedAny(45409), HashedAny(28624))

            set.addAll(elements)

            assertThat(set.contains(elements[1])).isTrue()
        }

        it("knows which elements it contains") {
            val elements = listOf(HashedAny(46175), HashedAny(76787), HashedAny(32460))

            set.addAll(elements)

            assertThat(set.containsAll(elements)).isTrue()
        }

        it("starts out empty") {
            assertThat(set)
                .hasSize(0)
                .isEmpty()
        }
    }
})

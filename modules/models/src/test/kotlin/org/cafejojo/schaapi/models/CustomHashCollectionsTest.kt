package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class ValueWrapperTest : Spek({
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
    }
})

class CustomHashMapTest : Spek({
    describe("custom hash map") {
        lateinit var map: CustomHashMap<Any, Any>

        beforeEachTest {
            map = CustomHashMap(Any::hashCode)
        }

        it("starts out empty") {
            assertThat(map)
                .hasSize(0)
                .isEmpty()
            assertThat(map.entries)
                .isEmpty()
        }

        it("can store multiple key-value pairs") {
            val keyA = Any()
            val keyB = Any()
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomHashMap.Entry(keyA, valueA),
                    CustomHashMap.Entry(keyB, valueB)
                )
        }

        it("can store multiple key-value pairs in one call") {
            val pairs = listOf(
                Pair(Any(), Any()),
                Pair(Any(), Any()),
                Pair(Any(), Any())
            ).toMap()

            map.putAll(pairs)

            assertThat(map)
                .hasSize(3)
                .containsAllEntriesOf(pairs)
        }

        it("can store a value under two keys") {
            val keyA = Any()
            val keyB = Any()
            val value = Any()

            map[keyA] = value
            map[keyB] = value

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomHashMap.Entry(keyA, value),
                    CustomHashMap.Entry(keyB, value)
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
                .contains(CustomHashMap.Entry(key, valueB))
        }

        it("can remove keys") {
            val keyA = Any()
            val keyB = Any()
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
            val keyA = Any()
            val keyB = Any()
            val keyC = Any()
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

class CustomHashSetTest : Spek({
    describe("custom hash set") {
        lateinit var set: CustomHashSet<Any>

        beforeEachTest {
            set = CustomHashSet(Any::hashCode)
        }

        it("can store multiple elements") {
            val elements = listOf(Any(), Any())

            set.add(elements[0])
            set.add(elements[1])

            assertThat(set)
                .hasSize(2)
                .containsAll(elements)
        }

        it("can accept multiple elements at once") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)

            assertThat(set)
                .hasSize(3)
                .containsAll(elements)
        }

        it("can be cleared") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)
            set.clear()

            assertThat(set)
                .hasSize(0)
                .isEmpty()
        }

        it("can be iterated over") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)

            assertThat(set.iterator())
                .hasSize(3)
                .containsAll(elements)
        }

        it("can delete while iterating") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)
            val iterator = set.iterator()
            iterator.next()
            iterator.next()
            iterator.remove()

            assertThat(set).hasSize(2)
        }

        it("can remove elements") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)
            set.remove(elements[1])

            assertThat(set)
                .hasSize(2)
                .contains(elements[0], elements[2])
        }

        it("can remove multiple elements at once") {
            val elements = listOf(Any(), Any(), Any(), Any(), Any())

            set.addAll(elements)
            set.removeAll(listOf(elements[1], elements[3]))

            assertThat(set)
                .hasSize(3)
                .contains(elements[0], elements[2], elements[4])
        }

        it("can be filtered") {
            val elements = listOf(Any(), Any(), Any())
            val retain = listOf(elements[1])

            set.addAll(elements)
            set.retainAll(retain)

            assertThat(set)
                .hasSize(1)
                .containsAll(retain)
        }

        it("knows which element it contains") {
            val elements = listOf(Any(), Any(), Any())

            set.addAll(elements)

            assertThat(set.contains(elements[1])).isTrue()
        }

        it("knows which elements it contains") {
            val elements = listOf(Any(), Any(), Any())

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

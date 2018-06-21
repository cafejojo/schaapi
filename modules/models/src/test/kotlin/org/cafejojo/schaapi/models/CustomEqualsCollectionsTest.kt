package org.cafejojo.schaapi.models

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal object EqualsWrapperTest : Spek({
    describe("value wrapper") {
        it("returns the inserted value") {
            val key = Any()
            val wrapper = EqualsWrapper(key, Any::equals, Any::hashCode)

            assertThat(wrapper.value).isEqualTo(key)
        }

        it("uses the wrapped type's equals implementation by default") {
            @Suppress("EqualsWithHashCodeExist")
            val key = object : Any() {
                @Suppress("EqualsAlwaysReturnsTrueOrFalse")
                override fun equals(other: Any?) = false
            }
            val wrapper = EqualsWrapper(
                value = key,
                customHash = Any::hashCode
            )

            assertThat(wrapper).isNotEqualTo(wrapper)
        }

        it("uses the given equals function") {
            val key = Any()
            val wrapper = EqualsWrapper(key, { _, _ -> false }, Any::hashCode)

            assertThat(wrapper).isNotEqualTo(wrapper)
        }

        it("uses the wrapped type's hash code implementation by default") {
            @Suppress("EqualsWithHashCodeExist")
            val key = object : Any() {
                override fun hashCode() = 31749
            }
            val wrapper = EqualsWrapper(
                value = key,
                customEquals = Any::equals
            )

            assertThat(wrapper.hashCode()).isEqualTo(31749)
        }

        it("uses the given hash function") {
            val key = Any()
            val wrapper = EqualsWrapper(key, Any::equals, { 11716 })

            assertThat(wrapper.hashCode()).isEqualTo(11716)
        }
    }
})

internal object CustomEqualsHashMapTest : Spek({
    describe("custom hash map") {
        lateinit var map: CustomEqualsHashMap<Any, Any>

        beforeEachTest {
            map = CustomEqualsHashMap(Any::equals, Any::hashCode)
        }

        it("starts out empty") {
            assertThat(map).hasSize(0).isEmpty()
            assertThat(map.entries).isEmpty()
        }

        it("can store multiple key-value pairs") {
            val keyA = EqualsWrapper("benelux")
            val keyB = EqualsWrapper("acolyte")
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomEqualsHashMap.Entry(keyA, valueA),
                    CustomEqualsHashMap.Entry(keyB, valueB)
                )
        }

        it("can store multiple key-value pairs in one call") {
            val pairs = listOf(
                EqualsWrapper(16445) to Any(),
                EqualsWrapper(91213) to Any(),
                EqualsWrapper(23669) to Any()
            ).toMap()

            map.putAll(pairs)

            assertThat(map)
                .hasSize(3)
                .containsAllEntriesOf(pairs)
        }

        it("can store one value under two keys") {
            val keyA = EqualsWrapper(43940)
            val keyB = EqualsWrapper(18339)
            val value = Any()

            map[keyA] = value
            map[keyB] = value

            assertThat(map)
                .hasSize(2)
                .contains(
                    CustomEqualsHashMap.Entry(keyA, value),
                    CustomEqualsHashMap.Entry(keyB, value)
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
                .contains(CustomEqualsHashMap.Entry(key, valueB))
        }

        it("uses the custom equals to determine duplicates") {
            val keyA = EqualsWrapper(68327, { _, _ -> true }, { _ -> 72170 })
            val keyB = EqualsWrapper(21531, { _, _ -> true }, { _ -> 72170 })
            val valueA = Any()
            val valueB = Any()

            map[keyA] = valueA
            map[keyB] = valueB

            assertThat(map[keyA])
                .isEqualTo(map[keyB])
                .isEqualTo(valueB)
        }

        it("can remove keys") {
            val keyA = EqualsWrapper(79283)
            val keyB = EqualsWrapper(82993)
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
            val keyA = EqualsWrapper(46083)
            val keyB = EqualsWrapper(89198)
            val keyC = EqualsWrapper(48262)
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

internal object CustomEqualsHashSetTest : Spek({
    describe("custom hash set") {
        lateinit var set: CustomEqualsHashSet<Any>

        beforeEachTest {
            set = CustomEqualsHashSet(Any::equals, Any::hashCode)
        }

        it("can store multiple elements") {
            val elements = listOf(EqualsWrapper(16977), EqualsWrapper(18313))

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

        it("uses the custom equals to determine duplicates") {
            val elements = listOf(
                EqualsWrapper(16597, { _, _ -> true }, { 28457 }),
                EqualsWrapper(60786, { _, _ -> true }, { 28457 })
            )

            set.addAll(elements)

            assertThat(set).hasSize(1)
        }

        it("can accept multiple elements at once") {
            val elements = listOf(EqualsWrapper(62536), EqualsWrapper(61084), EqualsWrapper(49265))

            set.addAll(elements)

            assertThat(set)
                .hasSize(3)
                .containsAll(elements)
        }

        it("can be cleared") {
            val elements = listOf(EqualsWrapper(40881), EqualsWrapper(26322), EqualsWrapper(95920))

            set.addAll(elements)
            set.clear()

            assertThat(set).hasSize(0).isEmpty()
        }

        it("can be iterated over") {
            val elements = listOf(EqualsWrapper(14408), EqualsWrapper(19725), EqualsWrapper(29802))

            set.addAll(elements)

            assertThat(set.iterator())
                .hasSize(3)
                .containsAll(elements)
        }

        it("can delete while iterating") {
            val elements = listOf(EqualsWrapper(26944), EqualsWrapper(83091), EqualsWrapper(22670))

            set.addAll(elements)
            val iterator = set.iterator()
            iterator.next()
            iterator.next()
            iterator.remove()

            assertThat(set).hasSize(2)
        }

        it("can remove elements") {
            val elements = listOf(EqualsWrapper(19613), EqualsWrapper(78295), EqualsWrapper(28409))

            set.addAll(elements)
            set.remove(elements[1])

            assertThat(set)
                .hasSize(2)
                .contains(elements[0], elements[2])
        }

        it("can remove multiple elements at once") {
            val elements = listOf(
                EqualsWrapper(22282),
                EqualsWrapper(96470),
                EqualsWrapper(60858),
                EqualsWrapper(26560),
                EqualsWrapper(53746)
            )

            set.addAll(elements)
            set.removeAll(listOf(elements[1], elements[3]))

            assertThat(set)
                .hasSize(3)
                .contains(elements[0], elements[2], elements[4])
        }

        it("can be filtered") {
            val elements = listOf(EqualsWrapper(83102), EqualsWrapper(84548), EqualsWrapper(42789))
            val retain = listOf(elements[1])

            set.addAll(elements)
            set.retainAll(retain)

            assertThat(set)
                .hasSize(1)
                .containsAll(retain)
        }

        it("knows which element it contains") {
            val elements = listOf(EqualsWrapper(41367), EqualsWrapper(45409), EqualsWrapper(28624))

            set.addAll(elements)

            assertThat(set.contains(elements[1])).isTrue()
        }

        it("knows which elements it contains") {
            val elements = listOf(EqualsWrapper(46175), EqualsWrapper(76787), EqualsWrapper(32460))

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

internal object CustomEqualsListTest : Spek({
    describe("custom equals list") {
        lateinit var list: CustomEqualsList<Any>

        beforeEachTest {
            list = CustomEqualsList(Any::equals, Any::hashCode)
        }

        it("knows which elements it contains by value") {
            val elements = Array(3, { Any() })

            list.addAll(elements)

            assertThat(list.contains(elements[0])).isTrue()
        }

        it("knows which elements it contains") {
            val elements = Array(3, { Any() })

            list.addAll(elements)

            assertThat(list.containsAll(elements.toList())).isTrue()
        }

        it("can contain elements multiple times") {
            val element = Any()

            list.add(element)
            list.add(element)

            assertThat(list).containsExactly(element, element)
        }

        it("can find the index of a given value") {
            val elements = Array(3, { Any() })

            list.addAll(elements)

            assertThat(list.indexOf(elements[2])).isEqualTo(2)
        }

        it("can find the last index of a given value") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.addAll(elements)
            list.addAll(elements)

            assertThat(list.lastIndexOf(elements[2])).isEqualTo(8)
        }

        it("can have an element inserted at an index") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.add(2, elements[0])

            assertThat(list[2]).isEqualTo(elements[0])
        }

        it("can have elements inserted at an index") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.addAll(2, elements.toList())

            assertThat(list[2]).isEqualTo(elements[0])
        }

        it("can be cleared") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.clear()

            assertThat(list).isEmpty()
        }

        it("can have an element removed by index") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.removeAt(1)

            assertThat(list).containsExactly(elements[0], elements[2])
        }

        it("can have an element removed by value") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.remove(elements[1])

            assertThat(list).containsExactly(elements[0], elements[2])
        }

        it("can have elements removed") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.removeAll(listOf(elements[0], elements[2]))

            assertThat(list).containsExactly(elements[1])
        }

        it("can retain elements") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list.retainAll(listOf(elements[0], elements[2]))

            assertThat(list).containsExactly(elements[0], elements[2])
        }

        it("can have an element changed") {
            val elements = Array(3, { Any() })

            list.addAll(elements)
            list[1] = elements[0]

            assertThat(list[1]).isEqualTo(elements[0])
        }

        it("can create a sublist") {
            val elements = Array(3, { Any() })

            list.addAll(elements)

            assertThat(list.subList(1, 3))
                .isInstanceOf(CustomEqualsList::class.java)
                .containsExactly(elements[1], elements[2])
        }

        it("can be iterated over forwards") {
            val elements = Array(3, { Any() })
            list.addAll(elements)

            val iterator = list.iterator()
            iterator.next()
            val previous = iterator.previous()

            assertThat(iterator.hasNext()).isTrue()
            assertThat(iterator.nextIndex()).isEqualTo(0)
            assertThat(iterator.next()).isEqualTo(elements[0])
            assertThat(iterator.previous()).isEqualTo(previous)
        }

        it("can be iterated over backwards") {
            val elements = Array(3, { Any() })
            list.addAll(elements)

            val iterator = list.iterator()
            val next = iterator.next()

            assertThat(iterator.hasPrevious()).isTrue()
            assertThat(iterator.previousIndex()).isEqualTo(0)
            assertThat(iterator.previous()).isEqualTo(elements[0])
            assertThat(iterator.next()).isEqualTo(next)
        }

        it("can have elements removed while iterating") {
            val elements = Array(3, { Any() })
            list.addAll(elements)

            val iterator = list.iterator()
            iterator.next()
            iterator.remove()

            assertThat(list).containsExactly(elements[1], elements[2])
        }

        it("can have elements changed while iterating") {
            val elements = Array(3, { Any() })
            list.addAll(elements)

            val iterator = list.iterator()
            iterator.next()
            iterator.set(elements[1])

            assertThat(list[0]).isEqualTo(elements[1])
        }

        it("can have elements added while iterating") {
            val elements = Array(3, { Any() })
            list.addAll(elements)

            val iterator = list.iterator()
            iterator.next()
            iterator.add(elements[2])

            assertThat(list).containsExactly(elements[0], elements[2], elements[1], elements[2])
        }

        it("starts out empty") {
            assertThat(list)
                .hasSize(0)
                .isEmpty()
        }
    }
})

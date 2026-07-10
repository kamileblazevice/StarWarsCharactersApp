package com.example.starwarscharactersapp.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromStringList serializes a list to json`() {
        val json = converters.fromStringList(listOf("a", "b", "c"))

        assertEquals("[\"a\",\"b\",\"c\"]", json)
    }

    @Test
    fun `fromStringList serializes an empty list to an empty json array`() {
        val json = converters.fromStringList(emptyList())

        assertEquals("[]", json)
    }

    @Test
    fun `toStringList deserializes json back to a list`() {
        val list = converters.toStringList("[\"a\",\"b\",\"c\"]")

        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test
    fun `toStringList deserializes an empty json array to an empty list`() {
        val list = converters.toStringList("[]")

        assertTrue(list.isEmpty())
    }

    @Test
    fun `round trip preserves list contents`() {
        val original = listOf("https://swapi.dev/api/films/1/", "https://swapi.dev/api/films/2/")

        val roundTripped = converters.toStringList(converters.fromStringList(original))

        assertEquals(original, roundTripped)
    }
}

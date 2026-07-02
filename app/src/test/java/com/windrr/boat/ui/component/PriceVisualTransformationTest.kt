package com.windrr.boat.ui.component

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceVisualTransformationTest {

    private val transformation = PriceVisualTransformation()

    @Test
    fun `formats digits with thousands separators`() {
        val result = transformation.filter(AnnotatedString("1234567"))
        assertEquals("1,234,567", result.text.text)
    }

    @Test
    fun `short numbers are unchanged`() {
        val result = transformation.filter(AnnotatedString("123"))
        assertEquals("123", result.text.text)
    }

    @Test
    fun `empty input stays empty`() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
    }

    @Test
    fun `cursor offset maps forward across inserted commas`() {
        val result = transformation.filter(AnnotatedString("1234567"))
        val mapping = result.offsetMapping
        // "1,234,567" -> cursor after "1" (orig=1) lands after "1," (=2)
        assertEquals(2, mapping.originalToTransformed(1))
        // cursor after "1234" (orig=4) lands after "1,234," (=6)
        assertEquals(6, mapping.originalToTransformed(4))
        // cursor at end (orig=7) lands at end of formatted (=9)
        assertEquals(9, mapping.originalToTransformed(7))
    }

    @Test
    fun `cursor offset maps backward across inserted commas`() {
        val result = transformation.filter(AnnotatedString("1234567"))
        val mapping = result.offsetMapping
        assertEquals(1, mapping.transformedToOriginal(2))
        assertEquals(4, mapping.transformedToOriginal(6))
        assertEquals(7, mapping.transformedToOriginal(9))
    }
}

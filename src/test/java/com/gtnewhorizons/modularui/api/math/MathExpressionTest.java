package com.gtnewhorizons.modularui.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MathExpressionTest {

    @Test
    void NumbersBasic_Test() {
        assertEquals(42, MathExpression.parseMathExpression("42"));
        assertEquals(42, MathExpression.parseMathExpression("  42  "));

        assertEquals(123456, MathExpression.parseMathExpression("123,456"));
        assertEquals(123456, MathExpression.parseMathExpression("123 456"));
        assertEquals(123456, MathExpression.parseMathExpression("123_456"));

        assertEquals(123.456, MathExpression.parseMathExpression("123.456"));
    }

    @Test
    void ArithmeticBasic_Test() {
        assertEquals(5, MathExpression.parseMathExpression("2+3"));
        assertEquals(-1, MathExpression.parseMathExpression("2-3"));
        assertEquals(6, MathExpression.parseMathExpression("2*3"));
        assertEquals(2, MathExpression.parseMathExpression("6/3"));
        assertEquals(1, MathExpression.parseMathExpression("7%3"));
        assertEquals(8, MathExpression.parseMathExpression("2^3"));

        assertEquals(5, MathExpression.parseMathExpression("2 + 3"));
    }

    @Test
    void ArithmeticPriority_Test() {
        assertEquals(4, MathExpression.parseMathExpression("2+3-1"));
        assertEquals(14, MathExpression.parseMathExpression("2+3*4"));
        assertEquals(10, MathExpression.parseMathExpression("2*3+4"));
        assertEquals(7, MathExpression.parseMathExpression("2^3-1"));
        assertEquals(13, MathExpression.parseMathExpression("1+2^3+4"));
    }

    @Test
    void UnaryZero_Test() {
        assertEquals(-5, MathExpression.parseMathExpression("-5"));
        assertEquals(-3, MathExpression.parseMathExpression("-5+2"));
        assertEquals(-7, MathExpression.parseMathExpression("-5-2"));
        assertEquals(-10, MathExpression.parseMathExpression("-5*2"));
        assertEquals(-2.5, MathExpression.parseMathExpression("-5/2"));
        assertEquals(-1, MathExpression.parseMathExpression("-5%2"));
        assertEquals(25, MathExpression.parseMathExpression("-5^2")); // ! this is (-5)^2, not -(5^2).
    }

    @Test
    void ExponentBasic_Test() {
        assertEquals(2000, MathExpression.parseMathExpression("2e3"));
        assertEquals(3000, MathExpression.parseMathExpression("3E3"));
        assertEquals(4000, MathExpression.parseMathExpression("4 e 3"));
        assertEquals(5600, MathExpression.parseMathExpression("5.6e3"));
        assertEquals(70_000, MathExpression.parseMathExpression("700e2"));
        assertEquals(8, MathExpression.parseMathExpression("8e0"));
    }

    @Test
    void ExponentArithmetic_Test() {
        assertEquals(4000, MathExpression.parseMathExpression("2*2e3"));
        assertEquals(6000, MathExpression.parseMathExpression("2e3 * 3"));
        assertEquals(-200, MathExpression.parseMathExpression("-2e2"));
        assertEquals(1024, MathExpression.parseMathExpression("2^1e1"));

        // Not supported, but shouldn't fail. (2e2)e2 = 200e2 = 20_000.
        assertEquals(20_000, MathExpression.parseMathExpression("2e2e2"));
    }

    @Test
    void SuffixesBasic_Test() {
        assertEquals(2000, MathExpression.parseMathExpression("2k"));
        assertEquals(3000, MathExpression.parseMathExpression("3K"));
        assertEquals(4_000_000, MathExpression.parseMathExpression("4m"));
        assertEquals(5_000_000, MathExpression.parseMathExpression("5M"));
        assertEquals(6_000_000_000D, MathExpression.parseMathExpression("6b"));
        assertEquals(7_000_000_000D, MathExpression.parseMathExpression("7B"));
        assertEquals(8_000_000_000D, MathExpression.parseMathExpression("8g"));
        assertEquals(9_000_000_000D, MathExpression.parseMathExpression("9G"));
        assertEquals(10_000_000_000_000D, MathExpression.parseMathExpression("10t"));
        assertEquals(11_000_000_000_000D, MathExpression.parseMathExpression("11T"));

        assertEquals(2050, MathExpression.parseMathExpression("2.05k"));
        assertEquals(50, MathExpression.parseMathExpression("0.05k"));
        assertEquals(3000, MathExpression.parseMathExpression("3 k"));
    }

    @Test
    void SuffixesArithmetic_Test() {
        assertEquals(2005, MathExpression.parseMathExpression("2k+5"));
        assertEquals(2005, MathExpression.parseMathExpression("5+2k"));
        assertEquals(4000, MathExpression.parseMathExpression("2k*2"));
        assertEquals(4000, MathExpression.parseMathExpression("2*2k"));
        assertEquals(-2000, MathExpression.parseMathExpression("-2k"));

        assertEquals(3_000_000, MathExpression.parseMathExpression("3kk"));
        assertEquals(4_000_000_000D, MathExpression.parseMathExpression("4kkk"));

        // Not supported, but shouldn't fail.
        assertEquals(6_000_000_000D, MathExpression.parseMathExpression("6km"));
        assertEquals(500_000, MathExpression.parseMathExpression("0.5ke3"));

        // Please don't do this.
        assertEquals(20_000_000_000D, MathExpression.parseMathExpression("2e0.01k"));
    }
}

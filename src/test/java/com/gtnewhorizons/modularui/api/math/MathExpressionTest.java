package com.gtnewhorizons.modularui.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class MathExpressionTest {

    MathExpression.Context ctxEN = new MathExpression.Context()
            .setNumberFormat(NumberFormat.getNumberInstance(Locale.US));
    MathExpression.Context ctxFR = new MathExpression.Context()
            .setNumberFormat(NumberFormat.getNumberInstance(Locale.FRENCH));
    MathExpression.Context ctxES = new MathExpression.Context()
            .setNumberFormat(NumberFormat.getNumberInstance(Locale.forLanguageTag("ES")));

    @Test
    void NumbersBasic_Test() {
        assertEquals(41, MathExpression.parseMathExpression("41"));
        assertEquals(42, MathExpression.parseMathExpression("  42  "));

        assertEquals(123456.789, MathExpression.parseMathExpression("123456.789", ctxEN));
        assertEquals(234567.891, MathExpression.parseMathExpression("234,567.891", ctxEN));

        assertEquals(345678.912, MathExpression.parseMathExpression("345 678,912", ctxFR));

        String s = NumberFormat.getNumberInstance(Locale.FRENCH).format(456789.123);
        assertEquals(456789.123, MathExpression.parseMathExpression(s, ctxFR));

        assertEquals(567891.234, MathExpression.parseMathExpression("567.891,234", ctxES));
    }

    @Test
    void ArithmeticBasic_Test() {
        assertEquals(5, MathExpression.parseMathExpression("2+3"));
        assertEquals(-1, MathExpression.parseMathExpression("2-3"));
        assertEquals(6, MathExpression.parseMathExpression("2*3"));
        assertEquals(2, MathExpression.parseMathExpression("6/3"));
        assertEquals(8, MathExpression.parseMathExpression("2^3"));
    }

    @Test
    void UnaryMinus_Test() {
        assertEquals(-5, MathExpression.parseMathExpression("-5"));
        assertEquals(-3, MathExpression.parseMathExpression("-5+2"));
        assertEquals(-7, MathExpression.parseMathExpression("-5-2"));
        assertEquals(-15, MathExpression.parseMathExpression("-5*3"));
        assertEquals(-2.5, MathExpression.parseMathExpression("-5/2"));
        assertEquals(-25, MathExpression.parseMathExpression("-5^2")); // ! this is -(5^2), not (-5)^2.

        assertEquals(2, MathExpression.parseMathExpression("4+-2"));
        assertEquals(6, MathExpression.parseMathExpression("4--2"));
    }

    @Test
    void ArithmeticPriority_Test() {
        assertEquals(4, MathExpression.parseMathExpression("2+3-1"));
        assertEquals(14, MathExpression.parseMathExpression("2+3*4"));
        assertEquals(10, MathExpression.parseMathExpression("2*3+4"));
        assertEquals(7, MathExpression.parseMathExpression("2^3-1"));
        assertEquals(13, MathExpression.parseMathExpression("1+2^3+4"));

        // a^b^c = a^(b^c)
        assertEquals(262_144, MathExpression.parseMathExpression("4^3^2"));
    }

    @Test
    void Brackets_Test() {
        assertEquals(5, MathExpression.parseMathExpression("(2+3)"));
        assertEquals(20, MathExpression.parseMathExpression("(2+3)*4"));
        assertEquals(14, MathExpression.parseMathExpression("2+(3*4)"));
        assertEquals(42, MathExpression.parseMathExpression("(((42)))"));

        assertEquals(14, MathExpression.parseMathExpression("2(3+4)"));
    }

    @Test
    void ScientificBasic_Test() {
        assertEquals(2000, MathExpression.parseMathExpression("2e3"));
        assertEquals(3000, MathExpression.parseMathExpression("3E3"));
        assertEquals(0.04, MathExpression.parseMathExpression("4e-2"));
        assertEquals(0.05, MathExpression.parseMathExpression("5E-2"));

        assertEquals(6000, MathExpression.parseMathExpression("6 e 3"));
        assertEquals(7800, MathExpression.parseMathExpression("7.8e3"));
        assertEquals(90_000, MathExpression.parseMathExpression("900e2"));
        assertEquals(1, MathExpression.parseMathExpression("1e0"));
    }

    @Test
    void ScientificArithmetic_Test() {
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

        assertEquals(2050, MathExpression.parseMathExpression("2.05k", ctxEN));
        assertEquals(50, MathExpression.parseMathExpression("0.05k", ctxEN));
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
        assertEquals(6_000_000_000d, MathExpression.parseMathExpression("6km"));
        assertEquals(500_000, MathExpression.parseMathExpression("0.5ke3", ctxEN));

        // Please don't do this.
        assertEquals(20_000_000_000D, MathExpression.parseMathExpression("2e0.01k", ctxEN));
    }

    @Test
    void Percent_Test() {
        ctxEN.setHundredPercent(1000);

        assertEquals(100, MathExpression.parseMathExpression("10%", ctxEN));
        assertEquals(2000, MathExpression.parseMathExpression("200%", ctxEN));
        assertEquals(-300, MathExpression.parseMathExpression("-30%", ctxEN));

        assertEquals(450, MathExpression.parseMathExpression("40% + 50", ctxEN));
        assertEquals(500, MathExpression.parseMathExpression("(20+30)%", ctxEN));
    }
}

package com.gtnewhorizons.modularui.api;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.modularui.config.Config;

class NumberFormatMUITest {

    @Test
    void basicUS_Test() {
        Config.locale = Locale.US;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("123,456,789", nf.format(123_456_789));
        assertEquals("234,567.891", nf.format(234_567.891));
        assertEquals("-345,678,912", nf.format(-345_678_912));
        assertEquals("-456,789.123", nf.format(-456_789.123));

        assertEquals("[567,891,234]", nf.format(567_891_234, new StringBuffer("[")).append(']').toString());
        assertEquals("[678,912.345]", nf.format(678_912.345, new StringBuffer("[")).append(']').toString());
        assertEquals("[-789,123,456]", nf.format(-789_123_456, new StringBuffer("[")).append(']').toString());
        assertEquals("[-891,234.567]", nf.format(-891_234.567, new StringBuffer("[")).append(']').toString());

        try {
            assertEquals(912_345_678L, nf.parse("912345678").longValue());
            assertEquals(123_456.789D, nf.parse("123,456.789").doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void basicFR_Test() {
        Config.locale = Locale.FRANCE;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("123 456 789", nf.format(123_456_789));
        assertEquals("234 567,891", nf.format(234_567.891));
        assertEquals("-345 678 912", nf.format(-345_678_912));
        assertEquals("-456 789,123", nf.format(-456_789.123));

        assertEquals("[567 891 234]", nf.format(567_891_234, new StringBuffer("[")).append(']').toString());
        assertEquals("[678 912,345]", nf.format(678_912.345, new StringBuffer("[")).append(']').toString());
        assertEquals("[-789 123 456]", nf.format(-789_123_456, new StringBuffer("[")).append(']').toString());
        assertEquals("[-891 234,567]", nf.format(-891_234.567, new StringBuffer("[")).append(']').toString());

        try {
            assertEquals(912_345_678L, nf.parse("912345678").longValue());
            assertEquals(123_456.789D, nf.parse("123 456,789").doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void basicES_Test() {
        Config.locale = Locale.forLanguageTag("es-ES");
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("123.456.789", nf.format(123_456_789));
        assertEquals("234.567,891", nf.format(234_567.891));
        assertEquals("-345.678.912", nf.format(-345_678_912));
        assertEquals("-456.789,123", nf.format(-456_789.123));

        assertEquals("[567.891.234]", nf.format(567_891_234, new StringBuffer("[")).append(']').toString());
        assertEquals("[678.912,345]", nf.format(678_912.345, new StringBuffer("[")).append(']').toString());
        assertEquals("[-789.123.456]", nf.format(-789_123_456, new StringBuffer("[")).append(']').toString());
        assertEquals("[-891.234,567]", nf.format(-891_234.567, new StringBuffer("[")).append(']').toString());

        try {
            assertEquals(912_345_678L, nf.parse("912345678").longValue());
            assertEquals(123_456.789D, nf.parse("123.456,789").doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void basicCN_Test() {
        Config.locale = Locale.CHINA;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("123,456,789", nf.format(123_456_789));
        assertEquals("234,567.891", nf.format(234_567.891));
        assertEquals("-345,678,912", nf.format(-345_678_912));
        assertEquals("-456,789.123", nf.format(-456_789.123));

        assertEquals("[567,891,234]", nf.format(567_891_234, new StringBuffer("[")).append(']').toString());
        assertEquals("[678,912.345]", nf.format(678_912.345, new StringBuffer("[")).append(']').toString());
        assertEquals("[-789,123,456]", nf.format(-789_123_456, new StringBuffer("[")).append(']').toString());
        assertEquals("[-891,234.567]", nf.format(-891_234.567, new StringBuffer("[")).append(']').toString());

        try {
            assertEquals(912_345_678L, nf.parse("912345678").longValue());
            assertEquals(123_456.789D, nf.parse("123,456.789").doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void suffixes_Test() {
        Config.locale = Locale.US;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("9,999", nf.formatWithSuffix(9999));
        assertEquals("12k", nf.formatWithSuffix(12000));
        assertEquals("23k", nf.formatWithSuffix(23999));

        assertEquals("4M", nf.formatWithSuffix(4_000_000));
        assertEquals("5.6M", nf.formatWithSuffix(5_600_000));
        assertEquals("6.7M", nf.formatWithSuffix(6_799_999));

        assertEquals("8G", nf.formatWithSuffix(8_000_000_000L));
        assertEquals("9T", nf.formatWithSuffix(9_000_000_000_000L));
        assertEquals("10P", nf.formatWithSuffix(10_000_000_000_000_000L));

        assertEquals("-9,999", nf.formatWithSuffix(-9999));
        assertEquals("-12k", nf.formatWithSuffix(-12000));
        assertEquals("-23k", nf.formatWithSuffix(-23999));

        assertEquals("-4M", nf.formatWithSuffix(-4_000_000));
        assertEquals("-5.6M", nf.formatWithSuffix(-5_600_000));
        assertEquals("-6.7M", nf.formatWithSuffix(-6_799_999));
    }

    @Test
    void settings_Test() {
        Config.locale = Locale.US;
        NumberFormatMUI nf = new NumberFormatMUI();

        nf.setMaximumFractionDigits(2);
        assertEquals("1,234.57", nf.format(1234.5678));

        nf.setMinimumIntegerDigits(6);
        nf.setMinimumFractionDigits(6);
        assertEquals("002,345.678900", nf.format(2345.6789));

        nf.setMinimumFractionDigits(0);
        nf.setGroupingUsed(false);
        assertEquals("345678912", nf.format(345_678_912));
    }

    @Test
    void changeLocale_Test() {
        Config.locale = Locale.US;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("123,456.789", nf.format(123_456.789));

        Config.locale = Locale.FRANCE;
        assertEquals("234 567,891", nf.format(234_567.891));

        Config.locale = Locale.forLanguageTag("es-ES");
        try {
            assertEquals(345_678.912d, nf.parse("345.678,912").doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void changeLocaleKeepSettings_Test() {
        Config.locale = Locale.US;
        NumberFormatMUI nf = new NumberFormatMUI();

        assertEquals("1,234.568", nf.format(1234.5678));

        nf.setMinimumIntegerDigits(6);
        nf.setMinimumFractionDigits(6);
        assertEquals("002,345.678900", nf.format(2345.6789));

        Config.locale = Locale.FRANCE;

        assertEquals("003 456,789100", nf.format(3456.7891));

        nf.setMaximumFractionDigits(1);
        assertEquals("004 567,9", nf.format(4567.8912));
    }

}

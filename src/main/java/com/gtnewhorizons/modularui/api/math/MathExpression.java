package com.gtnewhorizons.modularui.api.math;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

public class MathExpression {

    private static final List<Object> DEFAULT = Collections.singletonList(0);

    public static double parseMathExpression(String expr) {
        return parseMathExpression(expr, 0);
    }

    public static double parseMathExpression(String expr, double onFailReturn) {
        List<Object> parsed = buildParsedList(expr, onFailReturn);
        if (parsed == DEFAULT || parsed.size() == 0) {
            return onFailReturn;
        }
        if (parsed.size() == 1) {
            Object value = parsed.get(0);
            return value instanceof Double ? (double) value : onFailReturn;
        }

        if (Operator.MINUS == parsed.get(0)) {
            parsed.remove(0);
            parsed.set(0, -(Double) parsed.get(0));
        }

        for (int i = 1; i < parsed.size(); i++) {
            Object obj = parsed.get(i);
            if (obj instanceof Suffix) {
                Double left = (Double) parsed.get(i - 1);
                Double result = left * ((Suffix) obj).multiplier;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            }
        }

        for (int i = 1; i < parsed.size() - 1; i++) {
            Object obj = parsed.get(i);
            if (obj == Operator.SCIENTIFIC) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left * Math.pow(10, right);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            }
        }

        // ^ is right-associative: a^b^c = a^(b^c)
        for (int i = parsed.size() - 2; i > 0; i--) {
            Object obj = parsed.get(i);
            if (obj == Operator.POWER) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = Math.pow(left, right);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            }
        }

        for (int i = 1; i < parsed.size() - 1; i++) {
            Object obj = parsed.get(i);
            if (obj == Operator.MULTIPLY) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left * right;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            } else if (obj == Operator.DIVIDE) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left / right;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            } else if (obj == Operator.MOD) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left % right;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            }
        }

        for (int i = 1; i < parsed.size() - 1; i++) {
            Object obj = parsed.get(i);
            if (obj == Operator.PLUS) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left + right;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            } else if (obj == Operator.MINUS) {
                Double left = (Double) parsed.get(i - 1);
                Double right = (Double) parsed.get(i + 1);
                Double result = left - right;
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.remove(i - 1);
                parsed.add(i - 1, result);
                i--;
            }
        }

        if (parsed.size() != 1) {
            throw new IllegalStateException("Calculated expr has more than 1 result. " + parsed);
        }
        return (Double) parsed.get(0);
    }

    public static List<Object> buildParsedList(String expr, double onFailReturn) {
        List<Object> parsed = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        if (expr == null || expr.isEmpty()) return parsed;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            switch (c) {
                case ' ':
                case ',':
                case '_':
                    break;

                case '+': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.PLUS);
                    break;
                }
                case '-': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.MINUS);
                    break;
                }
                case '*': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.MULTIPLY);
                    break;
                }
                case '/': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.DIVIDE);
                    break;
                }
                case '%': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.MOD);
                    break;
                }
                case '^': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.POWER);
                    break;
                }
                case 'e':
                case 'E': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Operator.SCIENTIFIC);
                    break;
                }
                case 'k':
                case 'K': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Suffix.THOUSAND);
                    break;
                }
                case 'm':
                case 'M': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Suffix.MILLION);
                    break;
                }
                case 'b':
                case 'B':
                case 'g':
                case 'G': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Suffix.BILLION);
                    break;
                }
                case 't':
                case 'T': {
                    if (builder.length() > 0) {
                        parsed.add(parse(builder.toString(), onFailReturn));
                        builder.delete(0, builder.length());
                    }
                    parsed.add(Suffix.TRILLION);
                    break;
                }

                default:
                    builder.append(c);
            }
        }
        if (builder.length() > 0) {
            parsed.add(parse(builder.toString(), onFailReturn));
        }

        if (parsed.isEmpty()) return DEFAULT;

        Object prevToken = null;
        Object thisToken = null;
        for (int i = 0; i < parsed.size(); ++i) {
            prevToken = thisToken;
            thisToken = parsed.get(i);

            if (prevToken == null && (thisToken instanceof Double || Operator.MINUS == thisToken)) continue;
            if (prevToken instanceof Double && (thisToken instanceof Operator || thisToken instanceof Suffix)) continue;
            if (prevToken instanceof Operator && thisToken instanceof Double) continue;
            if (prevToken instanceof Suffix && (thisToken instanceof Operator || thisToken instanceof Suffix)) continue;
            return DEFAULT;
        }
        if (thisToken instanceof Operator) return DEFAULT;

        return parsed;
    }

    public static double parse(String num, double onFailReturn) {
        try {
            return TextFieldWidget.format.parse(num).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return onFailReturn;
    }

    public enum Operator {

        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MOD("%"),
        POWER("^"),
        SCIENTIFIC("e");

        public final String sign;

        Operator(String sign) {
            this.sign = sign;
        }

        @Override
        public String toString() {
            return sign;
        }
    }

    public enum Suffix {

        THOUSAND(1_000D),
        MILLION(1_000_000D),
        BILLION(1_000_000_000D),
        TRILLION(1_000_000_000_000D);

        public final double multiplier;

        Suffix(double multiplier) {
            this.multiplier = multiplier;
        }
    }
}

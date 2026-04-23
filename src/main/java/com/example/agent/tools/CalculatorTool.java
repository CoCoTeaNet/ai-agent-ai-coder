package com.example.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 计算器工具
 * 提供数学计算功能
 */
public class CalculatorTool {

    @Tool("计算数学表达式的值")
    public String calculate(@P("数学表达式（支持 +、-、*、/、(、) 和三角函数等）") String expression) {
        try {
            return evaluateExpression(expression);
        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }

    private String evaluateExpression(String expression) {
        // 移除所有空白字符
        String cleanExpr = expression.replaceAll("\\s+", "");

        if (cleanExpr.isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }

        try {
            // 首先尝试直接计算简单表达式
            return SimpleCalculator.eval(cleanExpr);
        } catch (Exception e) {
            // 如果简单计算失败，尝试更安全的方法
            return "无法计算表达式: " + expression + ". 请尝试简单的数学表达式，如 '1+1' 或 '5*3'";
        }
    }

    /**
     * 简单的表达式计算器
     * 不依赖于外部脚本引擎
     */
    private static class SimpleCalculator {

        public static String eval(String expression) {
            try {
                // 处理简单的加减乘除和括号表达式
                return String.valueOf(evaluateExpressionSimple(expression));
            } catch (Exception e) {
                // 如果复杂表达式无法计算，尝试简单的替换
                return simpleParse(expression);
            }
        }

        /**
         * 简单的表达式计算，使用递归下降
         */
        private static double evaluateExpressionSimple(String expr) {
            // 先处理括号
            int lastOpen = expr.lastIndexOf('(');
            if (lastOpen != -1) {
                int firstClose = expr.indexOf(')', lastOpen);
                if (firstClose == -1) {
                    throw new IllegalArgumentException("括号不匹配");
                }
                String inside = expr.substring(lastOpen + 1, firstClose);
                String result = eval(inside);
                String newExpr = expr.substring(0, lastOpen) + result + expr.substring(firstClose + 1);
                return evaluateExpressionSimple(newExpr);
            }

            // 处理加减
            int plusIndex = expr.lastIndexOf('+');
            int minusIndex = expr.lastIndexOf('-');

            // 找到最后的加减运算符，但要避免处理负数的开始
            int operatorIndex = -1;
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == '+' || c == '-') {
                    // 确保不是负数的开始
                    if (i > 0 && (Character.isDigit(expr.charAt(i-1)) || expr.charAt(i-1) == ')')) {
                        operatorIndex = i;
                        break;
                    }
                }
            }

            if (operatorIndex != -1) {
                char op = expr.charAt(operatorIndex);
                String left = expr.substring(0, operatorIndex);
                String right = expr.substring(operatorIndex + 1);

                if (op == '+') {
                    return evaluateExpressionSimple(left) + evaluateExpressionSimple(right);
                } else {
                    return evaluateExpressionSimple(left) - evaluateExpressionSimple(right);
                }
            }

            // 处理乘除
            int mulIndex = expr.lastIndexOf('*');
            int divIndex = expr.lastIndexOf('/');

            if (mulIndex != -1 && divIndex != -1) {
                operatorIndex = Math.max(mulIndex, divIndex);
            } else if (mulIndex != -1) {
                operatorIndex = mulIndex;
            } else {
                operatorIndex = divIndex;
            }

            if (operatorIndex != -1) {
                char op = expr.charAt(operatorIndex);
                String left = expr.substring(0, operatorIndex);
                String right = expr.substring(operatorIndex + 1);

                if (op == '*') {
                    return evaluateExpressionSimple(left) * evaluateExpressionSimple(right);
                } else {
                    double denominator = evaluateExpressionSimple(right);
                    if (denominator == 0) {
                        throw new ArithmeticException("除数不能为0");
                    }
                    return evaluateExpressionSimple(left) / denominator;
                }
            }

            // 如果只是一个数字，直接返回
            return Double.parseDouble(expr);
        }

        /**
         * 最简单的计算尝试，处理一些常见的简单表达式
         */
        private static String simpleParse(String expr) {
            // 尝试直接解析简单的表达式
            try {
                // 处理简单的加减法和乘除法
                String result = "";
                // 这里是最基础的替代方案，对于复杂情况给出友好提示
                if (expr.matches("\\d+\\+\\d+")) {
                    String[] parts = expr.split("\\+");
                    return String.valueOf(Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]));
                } else if (expr.matches("\\d+\\-\\d+")) {
                    String[] parts = expr.split("-");
                    return String.valueOf(Integer.parseInt(parts[0]) - Integer.parseInt(parts[1]));
                } else if (expr.matches("\\d+\\*\\d+")) {
                    String[] parts = expr.split("\\*");
                    return String.valueOf(Integer.parseInt(parts[0]) * Integer.parseInt(parts[1]));
                } else if (expr.matches("\\d+\\/\\d+")) {
                    String[] parts = expr.split("/");
                    if (Integer.parseInt(parts[1]) != 0) {
                        return String.valueOf(Integer.parseInt(parts[0]) / (double) Integer.parseInt(parts[1]));
                    }
                }
            } catch (Exception e) {
                // 忽略错误
            }

            // 如果是纯数字，直接返回
            if (expr.matches("-?\\d+(\\.\\d+)?")) {
                return expr;
            }

            throw new IllegalArgumentException("无法解析表达式");
        }
    }
}

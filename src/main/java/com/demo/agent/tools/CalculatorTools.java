package com.demo.agent.tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {
    @Tool
    double sum(double a, double b) {
        System.out.println("调用加法运算");
        return a + b;
    }
    @Tool(name = "平方根运算",value = "算一个数的平方根")
    double squareRoot(@ToolMemoryId int memoryId, double x) {
        System.out.println("调用平方根运算 memory: "+memoryId);
        return Math.sqrt(x);
    }

}

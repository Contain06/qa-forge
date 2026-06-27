package com.atcontain.qaforge.executor;

import com.atcontain.qaforge.entity.ApiAssertion;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AssertionExecutor {
    // 后续可优化为 chai.js 库来进行断言匹配
    private boolean compare(String actualValue, String operator, String expectedValue) {
        switch (operator) {
            case "=":
                log.info("实际的值为：{}，预期的值为：{}", actualValue, expectedValue);
                return actualValue.equals(expectedValue);
            case "!=":
                return !actualValue.equals(expectedValue);
            case "contains":
                return actualValue.contains(expectedValue);
            case ">":
                return Double.parseDouble(actualValue) > Double.parseDouble(expectedValue);
            case ">=":
                return Double.parseDouble(actualValue) >= Double.parseDouble(expectedValue);
            case "<":
                return Double.parseDouble(actualValue) < Double.parseDouble(expectedValue);
            case "<=":
                return Double.parseDouble(actualValue) <= Double.parseDouble(expectedValue);
            case "exists":
                return actualValue != null;
            case "notEmpty":
                return actualValue != null && !actualValue.isBlank();
            default:
                return false;
        }
    }

    public boolean execute(ApiAssertion assertion, String responseBody, Integer statusCode) {
        try {
            Object actualValue;

            if ("JSON_PATH".equals(assertion.getAssertType())) {
                actualValue = JsonPath.read(responseBody, assertion.getExpression());
            } else if ("STATUS_CODE".equals(assertion.getAssertType())) {
                actualValue = statusCode;
            } else if ("BODY".equals(assertion.getAssertType())) {
                actualValue = responseBody;
            } else {
                return false;
            }

            return compare(
                    actualValue == null ? null : String.valueOf(actualValue),
                    assertion.getAssertOperator(),
                    assertion.getExpectedValue()
            );
        } catch (Exception e) {
            return false;
        }
    }
}

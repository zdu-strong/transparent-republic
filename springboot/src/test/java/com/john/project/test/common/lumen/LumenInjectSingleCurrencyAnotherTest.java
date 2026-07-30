package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.jinq.orm.stream.JinqStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenInjectSingleCurrencyAnotherTest extends BaseTest {

    private LumenContextCoreModel lumenContext;

    @Test
    public void test() {
        var obtainCcuOfFirst = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(10000));
        var obtainCcuOfSecond = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(20000));
        var obtainCcuOfThree = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(40000));
        var obtainCcuOfFour = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(80000));
        var obtainCcuOfFive = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(160000));
        var obtainCcuOfSix = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(320000));
        var obtainCcuOfSeven = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal("999999999999999999999999999999"));
        var result = JinqStream.from(List.of(
                        obtainCcuOfFirst,
                        obtainCcuOfSecond,
                        obtainCcuOfThree,
                        obtainCcuOfFour,
                        obtainCcuOfFive,
                        obtainCcuOfSix,
                        obtainCcuOfSeven
                ))
                .sumBigDecimal(s -> s);
        var obtainJapanCurrencyBalance = this.lumenContext.withdrawal(this.lumenContext.getJapan(), result);
        assertTrue(ObjectUtil.equals(new BigDecimal("29799.999999"), result));
        assertTrue(ObjectUtil.equals(new BigDecimal("9966.555183"), obtainJapanCurrencyBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal(100), new BigDecimal(10000));
    }

}

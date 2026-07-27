package com.john.project.test.common.lumen;

import cn.hutool.core.util.NumberUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.jinq.orm.stream.JinqStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LumenExchangeStabilityTest extends BaseTest {

    private LumenContextCoreModel lumenContext;

    @Test
    public void test() {
        var result = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultTwo = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultThree = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultFour = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultFive = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultSix = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultSeven = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultEight = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultNine = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var resultTen = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal("2000000000000000000000000000000"));
        var resultOfMaxRatio = resultTen.divide(new BigDecimal("100000000000000000"), 6, RoundingMode.FLOOR);
        var countOfStabilityExchange = JinqStream.from(List.of(
                result,
                resultTwo,
                resultThree,
                resultFour,
                resultFive,
                resultSix,
                resultSeven,
                resultEight,
                resultNine
        )).where(s -> NumberUtil.equals(new BigDecimal("50"), s.setScale(5, RoundingMode.HALF_UP))).count();
        assertEquals(new BigDecimal("66666666666661722.222224"), resultTen);
        assertEquals(new BigDecimal("0.666666"), resultOfMaxRatio);
        assertEquals(9, countOfStabilityExchange);
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal("200000000000000000"), new BigDecimal("100000000000000000"));
    }

}

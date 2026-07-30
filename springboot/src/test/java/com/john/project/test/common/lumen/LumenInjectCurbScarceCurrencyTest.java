package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenInjectCurbScarceCurrencyTest extends BaseTest {

    private LumenContextCoreModel lumenContext;

    @Test
    public void test() {
        var obtainCcuBalanceFirst = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceSecond = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceThird = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceFourth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceFifth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceSixth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var usdCurrencyBalance = this.lumenContext.getUsdCurrencyBalance();
        var japanCurrencyBalance = this.lumenContext.getJapanCurrencyBalance();
        var totalCcuBalance = this.lumenContext.getTotalCcuBalance();

        assertTrue(ObjectUtil.equals(new BigDecimal("99995.000249"), obtainCcuBalanceFirst));
        assertTrue(ObjectUtil.equals(new BigDecimal("99990.001249"), obtainCcuBalanceSecond));
        assertTrue(ObjectUtil.equals(new BigDecimal("99985.002999"), obtainCcuBalanceThird));
        assertTrue(ObjectUtil.equals(new BigDecimal("99980.005498"), obtainCcuBalanceFourth));
        assertTrue(ObjectUtil.equals(new BigDecimal("99975.008746"), obtainCcuBalanceFifth));
        assertTrue(ObjectUtil.equals(new BigDecimal("99970.012743"), obtainCcuBalanceSixth));
        assertTrue(ObjectUtil.equals(new BigDecimal("1000600000"), usdCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("100000000000"), japanCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("2000599895.031484"), totalCcuBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal(1000 * 1000 * 1000), new BigDecimal("100000000000"));
    }

}


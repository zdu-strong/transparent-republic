package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenInjectCurbScarceCurrencyTest extends BaseTest {

    private LumenContextCoreModel lumenContext;
    private BigDecimal initialUsdCurrencyBalance;
    private BigDecimal initialJapanCurrencyBalance;

    @Test
    public void test() {
        var obtainCcuBalanceFirst = this.lumenContext.inject(lumenContext.getUsd(), this.initialUsdCurrencyBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR));
        var obtainCcuBalanceSecond = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceThird = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceFourth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceFifth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceSixth = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var obtainCcuBalanceSeventh = this.lumenContext.inject(lumenContext.getUsd(), new BigDecimal(100000));
        var usdCurrencyBalance = this.lumenContext.getUsdCurrencyBalance();
        var japanCurrencyBalance = this.lumenContext.getJapanCurrencyBalance();
        var totalCcuBalance = this.lumenContext.getTotalCcuBalance();
        var obtainCcuBalanceFirstRatio = obtainCcuBalanceFirst.multiply(BigDecimal.TWO).divide(this.initialUsdCurrencyBalance, 6, RoundingMode.FLOOR);
        assertTrue(ObjectUtil.equals(new BigDecimal("400000000.000000"), obtainCcuBalanceFirst));
        assertTrue(ObjectUtil.equals(new BigDecimal("0.800000"), obtainCcuBalanceFirstRatio));
        assertTrue(ObjectUtil.equals(new BigDecimal("79997.333422"), obtainCcuBalanceSecond));
        assertTrue(ObjectUtil.equals(new BigDecimal("79994.667111"), obtainCcuBalanceThird));
        assertTrue(ObjectUtil.equals(new BigDecimal("79992.001066"), obtainCcuBalanceFourth));
        assertTrue(ObjectUtil.equals(new BigDecimal("79989.335288"), obtainCcuBalanceFifth));
        assertTrue(ObjectUtil.equals(new BigDecimal("79986.669776"), obtainCcuBalanceSixth));
        assertTrue(ObjectUtil.equals(new BigDecimal("79984.004531"), obtainCcuBalanceSeventh));
        assertTrue(ObjectUtil.equals(new BigDecimal("1500600000.000000"), usdCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("1000000000000"), japanCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("2400479944.011194"), totalCcuBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.initialUsdCurrencyBalance = new BigDecimal(1000 * 1000 * 1000);
        this.initialJapanCurrencyBalance = this.initialUsdCurrencyBalance.multiply(new BigDecimal(1000));
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(initialUsdCurrencyBalance, initialJapanCurrencyBalance);
    }

}


package com.john.project.model;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.john.project.common.uuid.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jinq.orm.stream.JinqStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Accessors(chain = true)
public class LumenContextCoreModel {

    private List<LumenCcuBalanceModel> ccuBalanceList;

    private LumenCurrencyModel usd;
    private LumenCurrencyModel japan;

    public BigDecimal injectPair(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        return this.injectPair(this.usd, sourceUsdCurrencyBalance, this.japan, sourceJapanCurrencyBalance);
    }

    @SneakyThrows
    public LumenCcuBalanceModel withdrawalPair(BigDecimal ccuBalance) {
        checkLumenCcuBalance();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var totalCcuBalance = getTotalCcuBalance();
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);

        this.addCcuToList(usd, obtainUsdCurrencyBalance.negate(), japan, obtainJapanCurrencyBalance.negate(), ccuBalance.negate());

        var obtainLumenCcuBalance = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(obtainUsdCurrencyBalance)
                .setJapanCurrencyBalance(obtainJapanCurrencyBalance)
                .setCcuBalance(ccuBalance.negate());

        checkLumenCcuBalance();
        return obtainLumenCcuBalance;
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForExchange) {
        var obtainSourceCcuBalance = this.inject(sourceCurrency, sourceCurrencyBalanceForExchange);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        return this.withdrawal(targetCurrency, obtainSourceCcuBalance);
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForInject) {
        var targetCurrency = getTargetCurrency(sourceCurrency);
        return injectPair(sourceCurrency, sourceCurrencyBalanceForInject, targetCurrency, BigDecimal.ZERO);
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkLumenCcuBalance();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        var sourceCurrency = getTargetCurrency(targetCurrency);
        var totalCcuBalance = getTotalCcuBalance();
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrencyBalance() : getJapanCurrencyBalance();

        var obtainTargetCurrencyBalanceFirst = targetCurrencyBalance.multiply(ccuBalance).divide(ccuBalance.add(totalCcuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR)), 6, RoundingMode.FLOOR);
        var obtainTargetCurrencyBalanceSecond = targetCurrencyBalance.multiply(ccuBalance).multiply(BigDecimal.TWO).divide(ccuBalance.add(totalCcuBalance), 6, RoundingMode.FLOOR);

        var obtainTargetCurrencyBalance = obtainTargetCurrencyBalanceFirst.max(obtainTargetCurrencyBalanceSecond);

        this.addCcuToList(sourceCurrency, BigDecimal.ZERO, targetCurrency, obtainTargetCurrencyBalance.negate(), ccuBalance.negate());

        checkLumenCcuBalance();

        return obtainTargetCurrencyBalance;
    }

    private BigDecimal injectPair(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForInput, LumenCurrencyModel targetCurrency, BigDecimal targetCurrencyBalanceForInput) {
        checkLumenCcuBalance();
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceCurrencyBalanceForInput);
        checkSourceCurrencyBalanceGreaterOrEqualZero(targetCurrencyBalanceForInput);

        if (ObjectUtil.equals(sourceCurrencyBalanceForInput, BigDecimal.ZERO) && ObjectUtil.equals(targetCurrencyBalanceForInput, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        var obtainCcuBalance = getCcuBalanceBeGenerate(sourceCurrency, sourceCurrencyBalanceForInput, targetCurrency, targetCurrencyBalanceForInput);

        this.addCcuToList(sourceCurrency, sourceCurrencyBalanceForInput, targetCurrency, targetCurrencyBalanceForInput, obtainCcuBalance);

        return obtainCcuBalance;
    }

    private void addCcuToList(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForInput, LumenCurrencyModel targetCurrency, BigDecimal targetCurrencyBalanceForInput, BigDecimal ccuBalance) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);

        var isUsdOfSourceFirstCurrency = ObjectUtil.equals(usd.getId(), sourceCurrency.getId());

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(isUsdOfSourceFirstCurrency ? sourceCurrencyBalanceForInput : targetCurrencyBalanceForInput)
                .setJapanCurrencyBalance(isUsdOfSourceFirstCurrency ? targetCurrencyBalanceForInput : sourceCurrencyBalanceForInput)
                .setCcuBalance(ccuBalance));
    }

    private BigDecimal getCcuBalanceBeGenerate(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForInput, LumenCurrencyModel targetCurrency, BigDecimal targetCurrencyBalanceForInput) {
        var sourceCurrencyBalance = getCurrencyBalance(sourceCurrency);
        var targetCurrencyBalance = getCurrencyBalance(targetCurrency);
        var ccuBalanceOfBase = sourceCurrencyBalanceForInput.add(sourceCurrencyBalance).min(targetCurrencyBalanceForInput.add(targetCurrencyBalance));
        var totalCcuBalance = getTotalCcuBalance();

        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceCurrencyBalanceForInput);
        checkSourceCurrencyBalanceGreaterOrEqualZero(targetCurrencyBalanceForInput);

        if (ObjectUtil.equals(sourceCurrencyBalanceForInput, BigDecimal.ZERO) || ObjectUtil.equals(targetCurrencyBalanceForInput, BigDecimal.ZERO)) {
            checkBalanceGreaterThanZero();
        }

        if (ObjectUtil.equals(sourceCurrencyBalanceForInput, BigDecimal.ZERO) && ObjectUtil.equals(targetCurrencyBalanceForInput, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        if (NumberUtil.isGreater(sourceCurrencyBalanceForInput.add(sourceCurrencyBalance), targetCurrencyBalanceForInput.add(targetCurrencyBalance))) {
            return getCcuBalanceBeGenerate(targetCurrency, targetCurrencyBalanceForInput, sourceCurrency, sourceCurrencyBalanceForInput);
        }

        if (hasCcuBalanceEqualsToZero()) {
            return sourceCurrencyBalanceForInput.multiply(BigDecimal.TWO);
        }

        var obtainSourceCcuBalance = sourceCurrencyBalanceForInput;
        var obtainTargetCcuBalance = BigDecimal.ZERO;
        var obtainTargetCcuBalanceFirst = targetCurrencyBalanceForInput.multiply(totalCcuBalance).divide(targetCurrencyBalance.multiply(BigDecimal.TWO), 6, RoundingMode.FLOOR);
        var obtainTargetCcuBalanceSecond = BigDecimal.ZERO;
        if (NumberUtil.isLessOrEqual(obtainTargetCcuBalanceFirst, obtainSourceCcuBalance)) {
            obtainTargetCcuBalance = obtainTargetCcuBalance.add(obtainTargetCcuBalanceFirst);
        } else {
            obtainTargetCcuBalance = obtainTargetCcuBalance.add(obtainSourceCcuBalance);
            obtainTargetCcuBalanceSecond = obtainTargetCcuBalanceFirst.subtract(obtainSourceCcuBalance);
        }
        if (NumberUtil.isGreater(obtainTargetCcuBalanceSecond, BigDecimal.ZERO)) {
            var surplusCcuBalance = ccuBalanceOfBase.multiply(new BigDecimal(3)).subtract(totalCcuBalance).subtract(obtainTargetCcuBalance).max(BigDecimal.ZERO);
            var obtainTargetCcuBalanceThird = surplusCcuBalance.multiply(obtainTargetCcuBalanceSecond).divide(obtainTargetCcuBalanceSecond.add(surplusCcuBalance), 6, RoundingMode.FLOOR);
            obtainTargetCcuBalance = obtainTargetCcuBalance.add(obtainTargetCcuBalanceThird);
        }
        var obtainCcuBalance = obtainSourceCcuBalance.add(obtainTargetCcuBalance);
        return obtainCcuBalance;
    }

    private LumenCurrencyModel getTargetCurrency(LumenCurrencyModel sourceCurrency) {
        var targetCurrency = JinqStream.from(
                        List.of(
                                usd,
                                japan
                        )
                )
                .where(s -> ObjectUtil.notEqual(sourceCurrency.getId(), s.getId()))
                .getOnlyValue();
        return targetCurrency;
    }

    public BigDecimal getUsdCurrencyBalance() {
        var balance = this.combineBalance();
        return balance.getUsdCurrencyBalance();
    }

    public BigDecimal getJapanCurrencyBalance() {
        var balance = this.combineBalance();
        return balance.getJapanCurrencyBalance();
    }

    public BigDecimal getTotalCcuBalance() {
        var balance = this.combineBalance();
        return balance.getCcuBalance();
    }

    private BigDecimal getCurrencyBalance(LumenCurrencyModel lumenCurrencyModel) {
        if (ObjectUtil.equals(usd.getId(), lumenCurrencyModel.getId())) {
            return getUsdCurrencyBalance();
        } else {
            return getJapanCurrencyBalance();
        }
    }

    private LumenCcuBalanceModel combineBalance() {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var balance = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getUsdCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setJapanCurrencyBalance(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getJapanCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setCcuBalance(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getCcuBalance())).orElse(BigDecimal.ZERO));
        return balance;
    }

    public LumenContextCoreModel() {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        usd = new LumenCurrencyModel()
                .setId(uuidUtil.v4())
                .setName("USD");
        japan = new LumenCurrencyModel()
                .setId(uuidUtil.v4())
                .setName("JAPAN");
        this.ccuBalanceList = new ArrayList<>();
    }

    private void checkCcuBalanceGreaterThanOrEqualZero(BigDecimal withdrawalCcuBalance) {
        if (NumberUtil.isLess(getTotalCcuBalance(), withdrawalCcuBalance)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    private void checkSourceCurrencyBalanceGreaterOrEqualZero(BigDecimal sourceCurrencyBalance) {
        if (NumberUtil.isLess(sourceCurrencyBalance, BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    private void checkBalanceGreaterThanZero() {
        if (NumberUtil.isLessOrEqual(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getTotalCcuBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    private void checkBalanceGreaterThanOrEqualToZero() {
        if (NumberUtil.isLess(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getTotalCcuBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    private void checkLumenCcuBalance() {
        hasCcuBalanceEqualsToZero();
    }

    private boolean hasCcuBalanceEqualsToZero() {
        this.checkBalanceGreaterThanOrEqualToZero();
        if (NumberUtil.isGreater(getUsdCurrencyBalance().add(getJapanCurrencyBalance()).add(getTotalCcuBalance()), BigDecimal.ZERO)) {
            this.checkBalanceGreaterThanZero();
            return false;
        } else {
            return true;
        }
    }

}

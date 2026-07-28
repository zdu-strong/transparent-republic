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
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceUsdCurrencyBalance);
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceJapanCurrencyBalance);

        if (NumberUtil.equals(sourceUsdCurrencyBalance, BigDecimal.ZERO) && NumberUtil.equals(sourceJapanCurrencyBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        if (hasEqualToZero()) {
            return injectPairByZeroBalance(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
        }

        var usdCcuBalance = getUsdCcu();
        var japanCcuBalance = getJapanCcu();
        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var obtainUsdCcuBalanceFirst = usdCcuBalance.multiply(sourceUsdCurrencyBalance).divide(usdCurrencyBalance, 6, RoundingMode.FLOOR);
        var obtainJapanCcuBalanceFirst = japanCcuBalance.multiply(sourceJapanCurrencyBalance).divide(japanCurrencyBalance, 6, RoundingMode.FLOOR);
        var obtainUsdCcuBalance = obtainUsdCcuBalanceFirst.min(obtainJapanCcuBalanceFirst);
        var obtainJapanCcuBalance = obtainUsdCcuBalance;

        if (NumberUtil.isGreater(obtainUsdCcuBalanceFirst, obtainJapanCcuBalanceFirst)) {
            obtainUsdCcuBalance = japanCcuBalance.add(obtainJapanCcuBalance).multiply(BigDecimal.TWO).multiply(obtainUsdCcuBalanceFirst.add(usdCcuBalance)).divide(obtainUsdCcuBalanceFirst.add(usdCcuBalance).add(japanCcuBalance).add(obtainJapanCcuBalance), 6, RoundingMode.FLOOR).subtract(usdCcuBalance).max(BigDecimal.ZERO);
        } else {
            obtainJapanCcuBalance = usdCcuBalance.add(obtainUsdCcuBalance).multiply(BigDecimal.TWO).multiply(obtainJapanCcuBalanceFirst.add(japanCcuBalance)).divide(obtainJapanCcuBalanceFirst.add(japanCcuBalance).add(usdCcuBalance).add(obtainUsdCcuBalance), 6, RoundingMode.FLOOR).subtract(japanCcuBalance).max(BigDecimal.ZERO);
        }

        var obtainCcuBalance = obtainUsdCcuBalance.add(obtainJapanCcuBalance);
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(sourceUsdCurrencyBalance)
                .setCcuBalance(obtainUsdCcuBalance));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalance(obtainJapanCcuBalance));
        return obtainCcuBalance;
    }

    @SneakyThrows
    public List<LumenCcuBalanceModel> withdrawalPair(BigDecimal ccuBalance) {
        checkBalanceGreaterThanOrEqualToZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var usdCcuBalance = getUsdCcu();
        var japanCcuBalance = getJapanCcu();
        var totalCcuBalance = usdCcuBalance.add(japanCcuBalance);
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(ccuBalance).multiply(usdCcuBalance).divide(totalCcuBalance.multiply(usdCcuBalance), 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(ccuBalance).multiply(japanCcuBalance).divide(totalCcuBalance.multiply(japanCcuBalance), 6, RoundingMode.FLOOR);

        var obtainUsdCcuBalance = BigDecimal.ZERO;
        var obtainJapanCcuBalance = BigDecimal.ZERO;

        if (NumberUtil.isLess(usdCcuBalance, japanCcuBalance)) {
            obtainUsdCcuBalance = usdCcuBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
            obtainJapanCcuBalance = ccuBalance.subtract(obtainUsdCcuBalance);
        } else {
            obtainJapanCcuBalance = japanCcuBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
            obtainUsdCcuBalance = ccuBalance.subtract(obtainJapanCcuBalance);
        }

        var obtainLumenCcuBalanceList = List.of(new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(usd)
                        .setCurrencyBalance(obtainUsdCurrencyBalance)
                        .setCcuBalance(obtainUsdCcuBalance.negate()),
                new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(japan)
                        .setCurrencyBalance(obtainJapanCurrencyBalance)
                        .setCcuBalance(obtainJapanCcuBalance.negate())
        );
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(obtainUsdCurrencyBalance.negate())
                .setCcuBalance(obtainUsdCcuBalance.negate()));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(obtainJapanCurrencyBalance.negate())
                .setCcuBalance(obtainJapanCcuBalance.negate()));
        checkBalanceGreaterThanOrEqualToZero();
        return obtainLumenCcuBalanceList;
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForExchange) {
        var obtainSourceCcuBalance = this.inject(sourceCurrency, sourceCurrencyBalanceForExchange);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        return this.withdrawal(targetCurrency, obtainSourceCcuBalance);
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalanceForInject) {
        var sourceUsdCurrencyBalance = Optional.of(sourceBalanceForInject).filter(s -> ObjectUtil.equals(usd.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        var sourceJapanCurrencyBalance = Optional.of(sourceBalanceForInject).filter(s -> ObjectUtil.equals(japan.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        return injectPair(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkBalanceGreaterThanZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var sourceCurrency = getTargetCurrency(targetCurrency);
        var sourceCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCcu() : getUsdCcu();
        var targetCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCcu() : getJapanCcu();
        var sourceCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCurrencyBalance() : getUsdCurrencyBalance();
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrencyBalance() : getJapanCurrencyBalance();
        var totalCcuBalance = sourceCcuBalance.add(targetCcuBalance);

        var obtainSourceCcuBalance = BigDecimal.ZERO;
        var obtainTargetCcuBalance = BigDecimal.ZERO;

        if (NumberUtil.isLess(sourceCcuBalance, targetCurrencyBalance)) {
            obtainSourceCcuBalance = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
            obtainTargetCcuBalance = ccuBalance.subtract(obtainSourceCcuBalance);
        } else {
            obtainTargetCcuBalance = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
            obtainSourceCcuBalance = ccuBalance.subtract(obtainTargetCcuBalance);
        }

        var obtainTargetCurrencyBalance = targetCurrencyBalance.multiply(ccuBalance).multiply(BigDecimal.TWO).divide(totalCcuBalance, 6, RoundingMode.FLOOR);

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(sourceCurrency)
                .setCurrencyBalance(BigDecimal.ZERO)
                .setCcuBalance(obtainSourceCcuBalance.negate()));

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(targetCurrency)
                .setCurrencyBalance(obtainTargetCurrencyBalance.negate())
                .setCcuBalance(obtainTargetCcuBalance.negate()));

        checkBalanceGreaterThanOrEqualToZero();

        return obtainTargetCurrencyBalance;
    }

    private BigDecimal injectPairByZeroBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var obtainCcuBalanceEachSide = sourceUsdCurrencyBalance.max(sourceJapanCurrencyBalance);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(sourceUsdCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
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
        var balance = this.combineBalance(usd);
        return balance.getCurrencyBalance();
    }

    public BigDecimal getUsdCcu() {
        var balance = this.combineBalance(usd);
        return balance.getCcuBalance();
    }

    public BigDecimal getJapanCurrencyBalance() {
        var balance = this.combineBalance(japan);
        return balance.getCurrencyBalance();
    }

    public BigDecimal getJapanCcu() {
        var balance = this.combineBalance(japan);
        return balance.getCcuBalance();
    }

    public LumenCcuBalanceModel combineBalance(LumenCurrencyModel currency) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var list = JinqStream.from(ccuBalanceList)
                .where(s -> s.getCurrency().getId().equals(currency.getId()))
                .toList();
        var balance = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(currency)
                .setCurrencyBalance(Optional.ofNullable(JinqStream.from(list).sumBigDecimal(s -> s.getCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setCcuBalance(Optional.ofNullable(JinqStream.from(list).sumBigDecimal(s -> s.getCcuBalance())).orElse(BigDecimal.ZERO));
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

    public void checkCcuBalanceGreaterThanOrEqualZero(BigDecimal withdrawalCcuBalance) {
        if (NumberUtil.isLess(getUsdCcu().add(getJapanCcu()), withdrawalCcuBalance)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    public void checkSourceCurrencyBalanceGreaterOrEqualZero(BigDecimal sourceCurrencyBalance) {
        if (NumberUtil.isLess(sourceCurrencyBalance, BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkBalanceGreaterThanZero() {
        if (NumberUtil.isLessOrEqual(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkBalanceGreaterThanOrEqualToZero() {
        if (NumberUtil.isLess(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    public boolean hasEqualToZero() {
        this.checkBalanceGreaterThanOrEqualToZero();
        if (NumberUtil.isGreater(getUsdCurrencyBalance().add(getUsdCcu()).add(getJapanCurrencyBalance()).add(getJapanCcu()), BigDecimal.ZERO)) {
            this.checkBalanceGreaterThanZero();
            return false;
        } else {
            return true;
        }
    }

}

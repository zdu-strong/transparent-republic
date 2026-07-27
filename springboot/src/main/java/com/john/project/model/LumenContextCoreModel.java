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

        var totalCCUOfNew = getTotalCcuBalance(getUsdCurrencyBalance().add(sourceUsdCurrencyBalance), getJapanCurrencyBalance().add(sourceJapanCurrencyBalance));
        var totalCCUOfOld = getCcuBalanceOfEachSide().multiply(BigDecimal.TWO);
        var obtainCcuBalanceEachSide = totalCCUOfNew.subtract(totalCCUOfOld).max(BigDecimal.ZERO).divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(sourceUsdCurrencyBalance)
                .setJapanCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalanceOfEachSide(obtainCcuBalanceEachSide));
        return obtainCcuBalance;
    }

    @SneakyThrows
    public LumenCcuBalanceModel withdrawalPair(BigDecimal ccuBalance) {
        checkBalanceGreaterThanOrEqualToZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var usdCcuBalance = getCcuBalanceOfEachSide();
        var japanCcuBalance = getCcuBalanceOfEachSide();
        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var obtainCcuBalanceEachSide = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(obtainCcuBalanceEachSide).divide(usdCcuBalance, 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(obtainCcuBalanceEachSide).divide(japanCcuBalance, 6, RoundingMode.FLOOR);
        var obtainLumenCcuBalanceModel = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(obtainUsdCurrencyBalance)
                .setJapanCurrencyBalance(obtainJapanCurrencyBalance)
                .setCcuBalanceOfEachSide(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1)));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(obtainUsdCurrencyBalance.multiply(new BigDecimal(-1)))
                .setJapanCurrencyBalance(obtainJapanCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalanceOfEachSide(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        checkBalanceGreaterThanOrEqualToZero();
        return obtainLumenCcuBalanceModel;
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        checkBalanceGreaterThanZero();
        var ccuBalanceOfInject = inject(sourceCurrency, sourceBalance);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        var targetCurrencyBalance = withdrawal(targetCurrency, ccuBalanceOfInject);
        return targetCurrencyBalance;
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        var sourceUsdCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(usd.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        var sourceJapanCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(japan.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        return injectPair(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrencyBalance() : getJapanCurrencyBalance();
        var targetCcuBalance = getCcuBalanceOfEachSide();

        var obtainCcuBalanceEachSide = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainTargetCurrencyBalance = targetCurrencyBalance.multiply(ccuBalance).divide(targetCcuBalance.add(obtainCcuBalanceEachSide), 6, RoundingMode.FLOOR);

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? obtainTargetCurrencyBalance.multiply(new BigDecimal(-1)) : BigDecimal.ZERO)
                .setJapanCurrencyBalance(ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? BigDecimal.ZERO : obtainTargetCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalanceOfEachSide(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        return obtainTargetCurrencyBalance;
    }

    private BigDecimal getTotalCcuBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var minCurrencyBalance = sourceUsdCurrencyBalance.min(sourceJapanCurrencyBalance);
        var maxCurrencyBalance = sourceUsdCurrencyBalance.max(sourceJapanCurrencyBalance);
        var ccuBalanceOfThird = minCurrencyBalance.multiply(maxCurrencyBalance.subtract(minCurrencyBalance)).divide(maxCurrencyBalance.add(minCurrencyBalance.multiply(BigDecimal.TWO)), 6, RoundingMode.FLOOR);
        var totalCcuBalance = minCurrencyBalance.multiply(BigDecimal.TWO).add(ccuBalanceOfThird);
        return totalCcuBalance;
    }

    private BigDecimal injectPairByZeroBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var totalCcuBalance = getTotalCcuBalance(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
        var obtainCcuBalanceEachSide = totalCcuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(sourceUsdCurrencyBalance)
                .setJapanCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalanceOfEachSide(obtainCcuBalanceEachSide));
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

    public BigDecimal getCcuBalanceOfEachSide() {
        var balance = this.combineBalance();
        return balance.getCcuBalanceOfEachSide();
    }

    private LumenCcuBalanceModel combineBalance() {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var balance = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setUsdCurrencyBalance(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getUsdCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setJapanCurrencyBalance(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getJapanCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setCcuBalanceOfEachSide(Optional.ofNullable(JinqStream.from(ccuBalanceList).sumBigDecimal(s -> s.getCcuBalanceOfEachSide())).orElse(BigDecimal.ZERO));
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
        if (NumberUtil.isLess(getCcuBalanceOfEachSide().multiply(BigDecimal.TWO), withdrawalCcuBalance)) {
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
        if (NumberUtil.isLessOrEqual(getCcuBalanceOfEachSide(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getCcuBalanceOfEachSide(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    private void checkBalanceGreaterThanOrEqualToZero() {
        if (NumberUtil.isLess(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getCcuBalanceOfEachSide(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getCcuBalanceOfEachSide(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    private boolean hasEqualToZero() {
        this.checkBalanceGreaterThanOrEqualToZero();
        if (NumberUtil.isGreater(getUsdCurrencyBalance().add(getJapanCurrencyBalance()).add(getCcuBalanceOfEachSide()), BigDecimal.ZERO)) {
            this.checkBalanceGreaterThanZero();
            return false;
        } else {
            return true;
        }
    }

}

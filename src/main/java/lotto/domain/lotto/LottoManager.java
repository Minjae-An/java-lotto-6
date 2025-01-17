package lotto.domain.lotto;

import static camp.nextstep.edu.missionutils.Randoms.pickUniqueNumbersInRange;
import static lotto.config.GameConfig.END_INCLUSIVE;
import static lotto.config.GameConfig.LOTTO_NUMBER_UNIT;
import static lotto.config.GameConfig.NUMBER_OF_LOTTO_NUMBERS;
import static lotto.config.GameConfig.START_INCLUSIVE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lotto.domain.lotto_policy.FixedLottoWinningPolicy;
import lotto.domain.lotto_prize.FixedLottoPrizeStandard;

public class LottoManager {
    private List<Lotto> lotteries;
    private List<Integer> winningNumbers;

    private FixedLottoWinningPolicy fixedLottoWinningPolicy;
    private Integer purchaseAmount;
    private Integer purchaseQuantity;

    private Integer getNumberOfLotteries(Integer purchaseAmount) {
        return purchaseQuantity = purchaseAmount / LOTTO_NUMBER_UNIT;
    }

    private List<Integer> generateLottoNumbers() {
        return pickUniqueNumbersInRange(START_INCLUSIVE, END_INCLUSIVE, NUMBER_OF_LOTTO_NUMBERS);
    }

    private void setPurchaseAmount(Integer purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public void generateLotteries(Integer purchaseAmount) {
        setPurchaseAmount(purchaseAmount);

        Integer numberOfLotteries = getNumberOfLotteries(purchaseAmount);

        lotteries = new ArrayList<>(numberOfLotteries);
        IntStream.range(0, numberOfLotteries)
                .forEach(i -> lotteries.add(new Lotto(generateLottoNumbers())));
    }

    public void setWinningNumbers(List<Integer> winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    public List<Integer> getWinningNumbers() {
        return winningNumbers;
    }

    public void setFixedLottoWinningPolicy(FixedLottoWinningPolicy fixedLottoWinningPolicy) {
        this.fixedLottoWinningPolicy = fixedLottoWinningPolicy;
    }

    public List<List<Integer>> getLotteriesNumbers() {
        return lotteries.stream()
                .map(Lotto::getNumbers)
                .toList();
    }

    public Integer getPurchaseQuantity() {
        return purchaseQuantity;
    }

    private Map<FixedLottoPrizeStandard, Integer> sortLottoWinningResult(
            Map<FixedLottoPrizeStandard, Integer> lottoWinningResults) {
        return lottoWinningResults.entrySet().stream()
                .sorted(Comparator.comparingLong(winningResult -> winningResult.getKey().getMatchCount()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public Map<FixedLottoPrizeStandard, Integer> getLottoWinningResults() {
        Map<FixedLottoPrizeStandard, Integer> lottoWinningResults = new HashMap<>();
        fixedLottoWinningPolicy.getFixedLottoPrizeStandards()
                .forEach(prizeStandard -> lottoWinningResults.put(prizeStandard, 0));

        lotteries.forEach(lotto -> {
            List<FixedLottoPrizeStandard> winningResult = fixedLottoWinningPolicy
                    .getWinningResult(lotto.getNumbers(), winningNumbers);

            winningResult.forEach(prizeStandard ->
                    lottoWinningResults.put(prizeStandard, lottoWinningResults.get(prizeStandard) + 1));
        });

        return sortLottoWinningResult(lottoWinningResults);
    }


    public Double getTotalReturnRate() {
        Map<FixedLottoPrizeStandard, Integer> lottoWinningResults = getLottoWinningResults();
        Integer totalReturn = lottoWinningResults.entrySet().stream()
                .mapToInt(winningResult -> winningResult.getKey().getPrize() * winningResult.getValue())
                .sum();

        return (1.0 * totalReturn / purchaseAmount) * 100;
    }
}

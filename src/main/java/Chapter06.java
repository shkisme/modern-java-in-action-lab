import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Chapter06 {

  public static void main(String[] args) {
    List<Dish> menu = asList(
        new Dish("pork", false, 800, Type.MEAT),
        new Dish("beef", false, 700, Type.MEAT),
        new Dish("chicken", false, 400, Type.MEAT),
        new Dish("french fries", true, 530, Type.OTHER),
        new Dish("rice", true, 350, Type.OTHER),
        new Dish("season fruit", true, 120, Type.OTHER),
        new Dish("pizza", true, 550, Type.OTHER),
        new Dish("prawns", false, 300, Type.FISH),
        new Dish("salmon", false, 450, Type.FISH)
    );
    Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();
    List<Transaction> transactions = new ArrayList<>();

    // ????????? ?????? ?????? ????????? ??????
    for (Transaction transaction : transactions) {
      Currency currency = transaction.getCurrency();
      List<Transaction> transactionsForCurrency = transactionsByCurrencies.get(currency);
      if (transactionsForCurrency == null) {
        transactionsForCurrency = new ArrayList<>();
        transactionsByCurrencies.put(currency, transactionsForCurrency);
      }
      transactionsForCurrency.add(transaction);
    }

    // ?????? ???????????? ????????? ??????????????? collect ???????????? ???????????? ???????????? ??????
    Map<Currency, List<Transaction>> transactionsByCurrencies2 = transactions.stream().collect(
        groupingBy(Transaction::getCurrency));

    // counting() ?????????
    long howManyDishes = menu.stream().collect(counting());
    howManyDishes = menu.stream().count();

    // 6.2.1 ?????????????????? ???????????? ????????? ??????
    Comparator<Dish> dishCaloriesComparator = comparingInt(Dish::getCalories);
    Optional<Dish> mostCalorieDish = menu.stream().collect(maxBy(dishCaloriesComparator));

    // 6.2.2 ?????? ?????? - summingInt
    int totalCalories = menu.stream()
        .collect(
            summingInt(Dish::getCalories) // int ??? ????????? ???????????? ????????????.
        ); // ?????? ????????? ????????????.

    // summingDouble - ?????? ????????? ????????????!
    /*int totalCalories2 = menu.stream()
        .collect(
            summingDouble(Dish::getDoubleCalories)
        );*/

    // ????????? ????????? ??????!
    double avgCalories = menu.stream().collect(averagingInt(Dish::getCalories));

    // ??? ??? ????????? ????????? ????????? ???????????? ????????? ! summarizingInt
    IntSummaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
    System.out.println("menuStatistics = " + menuStatistics); // ????????? ??????..
    System.out.println("menuStatistics.getSum() = " + menuStatistics.getSum());

    String shortMenu = menu.stream().map(Dish::getName).collect(joining()); // ?????? ?????? ??? ??????
    System.out.println("shortMenu = " + shortMenu);
    String shortMenu2 = menu.stream().map(Dish::getName).collect(joining(","));
    System.out.println("shortMenu2 = " + shortMenu2);

    // 6.2.4 ?????? ????????? ?????? ?????? - ?????? ????????? ?????? reduce ??? ????????????!
    int totalCalories2 = menu.stream()
        .collect(reducing(0, Dish::getCalories, (i, j) -> i + j));

    Optional<Dish> mostCalorieDish2 = menu.stream().collect(reducing((d1, d2) ->
        d1.getCalories() > d2.getCalories() ? d1 : d2));

    // collect ??? reduce
    Stream<Integer> stream = asList(1, 2, 3, 4, 5, 6).stream();
    List<Integer> numbers = stream.reduce(new ArrayList<Integer>(),
        (List<Integer> l, Integer e) -> {
          l.add(e);
          return l;
        }, (List<Integer> l1, List<Integer> l2) -> {
          l1.addAll(l2);
          return l1;
        });

    // ????????? ?????????????????? ????????? : ?????? ????????? ????????? ????????????
    int totalCalories3 = menu.stream().collect(reducing(0, Dish::getCalories, Integer::sum));
    Optional<Integer> totalCalories4 = menu.stream().map(Dish::getCalories).reduce(Integer::sum);
    int totalCalories5 = menu.stream().map(Dish::getCalories).reduce(Integer::sum).get();
    int totalCalories6 = menu.stream().mapToInt(Dish::getCalories).sum();

    // ?????? 6-1 , reducing ??? BinaryOperator<T>??? ????????? ?????????. ???, BiFunction<T,T,T> ??? ????????? ?????????.

    // 6.3 ?????????
    Map<Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
    System.out.println(dishesByType);

    // ????????? ?????? ?????? ?????? ?????????????????? ????????? ?????? ????????? ????????? ??? ??????.
    Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(groupingBy(
        dish -> {
          if (dish.getCalories() <= 400) {
            return CaloricLevel.DIET;
          } else if (dish.getCalories() <= 700) {
            return CaloricLevel.NORMAL;
          } else {
            return CaloricLevel.FAT;
          }
        }
    ));

    // 6.3.1 ???????????? ?????? ??????
    Map<Type, List<Dish>> caloricDishesByType = menu.stream()
        .filter(dish -> dish.getCalories() > 500)
        .collect(groupingBy(Dish::getType));
    System.out.println(caloricDishesByType); // FISH ?????? ?????? ????????? ????????? ?????????????????? ??????.
    // ?????? - ??? ?????? ????????? ?????? ????????????????????? ????????? ???????????? Collectors ?????????.
    Map<Type, List<Dish>> caloricDishesByType2 = menu.stream()
        .collect(groupingBy(Dish::getType, filtering(dish -> dish.getCalories() > 500, toList())));
    System.out.println(caloricDishesByType2);

    // ??? ?????? ????????? ?????? - ?????? ????????? ????????? ????????? ???????????? ??????
    Map<Type, List<String>> dishNamesByType = menu.stream()
        .collect(groupingBy(Dish::getType, mapping(Dish::getName, toList())));
    System.out.println(dishNamesByType);

    // flatMapping ???????????? ??????

    Map<String, List<String>> dishTags = new HashMap<>();
    dishTags.put("pork", asList("greasy", "salty"));
    dishTags.put("beef", asList("salty", "roasted"));
    dishTags.put("chicken", asList("fried", "crisp"));
    dishTags.put("french fries", asList("greasy", "fried"));
    dishTags.put("rice", asList("light", "natural"));
    dishTags.put("season fruit", asList("fresh", "natural"));
    dishTags.put("pizza", asList("tasty", "salty"));
    dishTags.put("prawns", asList("tasty", "roasted"));
    dishTags.put("salmon", asList("delicious", "fresh"));

    Map<Type, Set<String>> dishNamesByType2 = menu.stream()
        .collect(groupingBy(Dish::getType,
            flatMapping(dish -> dishTags.get(dish.getName()).stream(), toSet())));
    System.out.println(dishNamesByType2);

    // 6.3.2 ????????? ?????????
    Map<Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel = menu.stream()
        .collect(groupingBy(Dish::getType,
            groupingBy(dish -> {
              if (dish.getCalories() <= 400) {
                return CaloricLevel.DIET;
              } else if (dish.getCalories() <= 700) {
                return CaloricLevel.NORMAL;
              } else {
                return CaloricLevel.FAT;
              }
            })
        ));
    System.out.println(dishesByTypeCaloricLevel);

    // 6.3.3 ?????????????????? ????????? ?????? - groupingBy??? ???????????? ???????????? ????????? ????????? ??????.
    Map<Type, Long> typesCount = menu.stream().collect(groupingBy(Dish::getType, counting()));
    System.out.println(typesCount);

    // ????????? ????????? ???????????? ???????????? ???????????? ?????? ?????? ???????????? ?????? ????????? ?????? ??????????????? ?????? ??????
    Map<Type, Optional<Dish>> mostCaloricByType = menu.stream()
        .collect(groupingBy(Dish::getType, maxBy(comparingInt(Dish::getCalories))));

    // ????????? ????????? ?????? ????????? ????????????
    Map<Type, Dish> mostCaloricByType2 = menu.stream().collect(groupingBy(Dish::getType,
        collectingAndThen(maxBy(comparingInt(Dish::getCalories))
            // ????????? ????????????, ?????? ????????? ????????? ????????? ?????? ???????????? ????????????.
            , Optional::get)));

    // groupingBy ??? ?????? ???????????? ?????? ????????? ??????
    Map<Type, Integer> totalCaloriesByType3 = menu.stream()
        .collect(groupingBy(Dish::getType, summingInt(Dish::getCalories)));

    Map<Type, Set<CaloricLevel>> caloricLevelsByType = menu.stream()
        .collect(groupingBy(Dish::getType, mapping(dish -> {
              if (dish.getCalories() <= 400) {
                return CaloricLevel.DIET;
              } else if (dish.getCalories() <= 700) {
                return CaloricLevel.NORMAL;
              } else {
                return CaloricLevel.FAT;
              }
            }, toSet()
        )));

    Map<Type, HashSet<CaloricLevel>> caloricLevelsByType2 = menu.stream()
        .collect(groupingBy(Dish::getType, mapping(dish -> {
          if (dish.getCalories() <= 400) {
            return CaloricLevel.DIET;
          } else if (dish.getCalories() <= 700) {
            return CaloricLevel.NORMAL;
          } else {
            return CaloricLevel.FAT;
          }
        }, toCollection(HashSet::new))));

    // 6.4
    Map<Boolean, List<Dish>> partitionedMenu = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian));
    System.out.println(partitionedMenu);
    // ????????? ?????? - ???, ?????? ??? ?????? ????????? ????????? ???????????? ?????? ????????????.
    Map<Boolean, Map<Type, List<Dish>>> vegetarianDishesByType = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian, groupingBy(Dish::getType)));
    System.out.println(vegetarianDishesByType);

    Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian,
            collectingAndThen(maxBy(comparingInt(Dish::getCalories)), Optional::get)));
    System.out.println(mostCaloricPartitionedByVegetarian);

    // ?????? 6 - 2
    Map<Boolean, Map<Boolean, List<Dish>>> collect1 = menu.stream().collect(
        partitioningBy(Dish::isVegetarian, partitioningBy(dish -> dish.getCalories() > 500)));

    //menu.stream().collect(partitioningBy(Dish::isVegetarian, partitioningBy(Dish::getType)));

    Map<Boolean, Long> collect = menu.stream()
        .collect(partitioningBy(Dish::isVegetarian, counting()));

    // ?????? ?????? ???????????? toList() ?????? ????????? ??? ??????.
    List<Dish> dishes = menu.stream().collect(new ToListCollector<>());

    // ???????????? ???????????? ????????? ????????? ????????? ????????? ??? ??????.
    List<Dish> dishes2 = menu.stream().collect(
        ArrayList::new, // ??????
        List::add, // ??????
        List::addAll); // ??????

    // ?????? ?????? ????????? ???????????? ?????? ????????????
    Map<Boolean, List<Integer>> collect2 = IntStream.rangeClosed(2, 100).boxed()
        .collect(new PrimeNumbersCollector());

    // 6.6.2 ????????? ?????? ??????
    long fastest = Long.MAX_VALUE;
    for (int i = 0; i < 10; ++i) {
      long start = System.nanoTime();
      partitionPrimes(1_000_000);
      long duration = (System.nanoTime() - start) / 1_000_000;
      if (duration < fastest) {
        fastest = duration;
      }
    }
    System.out.println("[partitionPrimes] Fastest execution done in " + fastest + " msecs");

    long fastest2 = Long.MAX_VALUE;
    for (int i = 0; i < 10; ++i) {
      long start2 = System.nanoTime();
      partitionPrimesWithCustomCollector(1_000_000);
      long duration2 = (System.nanoTime() - start2) / 1_000_000;
      if (duration2 < fastest2) {
        fastest2 = duration2;
      }
    }
    System.out.println("[partitionPrimesWithCustomCollector] Fastest execution done in " + fastest2
        + " msecs"); // ????????? ??? 30????????? ???????????????!

    // ???????????? ?????? ???????????????, collect ??? ????????? ???????????? ?????? ????????? ?????? ??? ??????.
    IntStream.rangeClosed(2, 100).boxed().collect(
        () -> new HashMap<Boolean, List<Integer>>() {
          {
            put(true, new ArrayList<Integer>());
            put(false, new ArrayList<Integer>());
          }
        },
        (acc, candidate) -> {
          acc.get(isPrime_conveyList(acc.get(true), candidate))
              .add(candidate);
        },
        (map1, map2) -> {
          map1.get(true).addAll(map2.get(true));
          map2.get(false).addAll(map2.get(false));
        }
    );
  }

  // 6.4.2 ????????? ????????? ???????????? ????????????
  public static boolean isPrime(int candidate) {
    return IntStream.range(2, candidate)
        .noneMatch(i -> candidate % i == 0);
  }

  public static boolean isPrime2(int candidate) {
    int candidateRoot = (int) Math.sqrt(candidate); // ????????? ?????? ????????? ????????? ??????
    return IntStream.rangeClosed(2, candidateRoot)
        .noneMatch(i -> candidate % i == 0);
  }

  public static Map<Boolean, List<Integer>> partitionPrimes(int n) {
    return IntStream.rangeClosed(2, n).boxed()
        .collect(partitioningBy(Chapter06::isPrime2)); // ????????? isPrime2 ???????????? ????????????????????? ????????????!
  }

  // 6.6 ????????? ???????????? ???????????? ?????? ???????????? - ?????? ?????? ???????????? ??????????????? ????????? ??????
  public static boolean isPrime_conveyList(List<Integer> primes, int candidate) {
    int candidateRoot = (int) Math.sqrt(candidate);
    return primes.stream()
        .takeWhile(i -> i
            <= candidateRoot) // filter ??? ?????? ???????????? ????????? ????????? ????????? ??????????????? ?????? ????????? ?????? ??? ??????. takeWhile??? ??????.
        .noneMatch(i -> candidate % i == 0);
  }

  public static class PrimeNumbersCollector implements
      Collector<Integer, Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> {

    @Override
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
      return () -> new HashMap<>() {
        {
          put(true, new ArrayList<>());
          put(false, new ArrayList<>());
        }
      };
    }

    @Override
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
      return (Map<Boolean, List<Integer>> acc, Integer candidate) -> {
        acc.get(isPrime_conveyList(acc.get(true), candidate))
            .add(candidate);
      };
    }

    @Override
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
      return (Map<Boolean, List<Integer>> map1, Map<Boolean, List<Integer>> map2) -> {
        map1.get(true).addAll(map2.get(true));
        map1.get(false).addAll(map2.get(false));
        return map1;
      };
    }

    @Override
    public Function<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> finisher() {
      return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));
    }
  }

  public static Map<Boolean, List<Integer>> partitionPrimesWithCustomCollector(int n) {
    return IntStream.rangeClosed(2, n).boxed()
        .collect(new PrimeNumbersCollector());
  }

  // counting ???????????? reducing ??? ????????? ????????? ??? ??????.
  public static <T> Collector<T, ?, Long> counting() {
    return reducing(0L, e -> 1L, Long::sum);
  }

  // Stream<T> ??? ?????? ????????? List<T> ??? ???????????? ????????? ??????
  public static class ToListCollector<T> implements Collector<T, List<T>, List<T>> {

    @Override
    public Supplier<List<T>> supplier() {
      return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
      return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
      return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
      };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
      return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH, CONCURRENT));
    }
  }

}

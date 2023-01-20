import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class Chapter10 {

  public static void main(String[] args) throws IOException {

    // 10.1.2 내무 DSL
    List<String> numbers = Arrays.asList("one", "two", "three");
    numbers.forEach(new Consumer<String>() {
      @Override
      public void accept(String s) {
        System.out.println(s);
      }
    });

    numbers.forEach(System.out::println);

    // 10.2 최신 자바 API 의 작은 DSL
    List<Person> persons = new ArrayList<>();
    Collections.sort(persons, new Comparator<Person>() {
      @Override
      public int compare(Person p1, Person p2) {
        return p1.getAge() - p2.getAge();
      }
    });

    // 이 작은 API 는 컬렉션 정렬 도메인의 최소 DSL 이다. 코드 가독성, 재사용성, 결합성이 아주 높다.
    Collections.sort(persons, comparing(Person::getAge));
    persons.sort(comparing(Person::getAge).thenComparing(Person::getName));

    // 예제 10-2 함수형으로 로그 파일의 에러 행을 읽자
    List<String> errors = Files.lines(Paths.get("fileName"))
        .filter(line -> line.startsWith("ERROR"))
        .limit(40)
        .collect(toList());

    // 10.2.2 데이터를 수집하는 DSL 인 Collectors
    List<Car> cars = new ArrayList<>();
    Map<String, Map<Color, List<Car>>> carsByBrandAndColor = cars.stream()
        .collect(groupingBy(Car::getBrand, groupingBy(Car::getColor)));

    Comparator<Person> comparator = comparing(Person::getAge).thenComparing(Person::getName);

    Collector<Car, ?, Map<String, Map<Color, List<Car>>>> carGroupingCollector = groupingBy(
        Car::getBrand, groupingBy(Car::getColor));

    // 예제 10.4 도메인 객체의 API 를 직접 이용해 주식 거래 주문을 만든다.
    Order order = new Order();
    order.setCustomer("BigBack");

    Trade trade1 = new Trade();
    trade1.setType(Trade.Type.BUY);

    Stock stock1 = new Stock();
    stock1.setSymbol("IBM");
    //... 코드가 굉장히 장황해진다.

    // 메서드 체인
    Order order1 = MethodChainingOrderBuilder.forCustomer("BigBank")
        .buy(80)
        .stock("IBM")
        .on("NYSE")
        .at(125.00)
        .end();

    // 중첩된 함수 이용
    Order order2 = NestedFunctionOrderBuilder.order("BigBank", NestedFunctionOrderBuilder.buy(80,
        NestedFunctionOrderBuilder.stock("IBM", NestedFunctionOrderBuilder.on("NYSE")),
        NestedFunctionOrderBuilder.at(125.00)), NestedFunctionOrderBuilder.sell(50,
        NestedFunctionOrderBuilder.stock("GOOGLE", NestedFunctionOrderBuilder.on("NASDAQ")),
        NestedFunctionOrderBuilder.at(375.00)));

    // 함수 시퀀싱
    Order order3 = LambdaOrderBuilder.order(o -> {
      o.forCustomer("BigBank");
      o.buy(t -> {

      });
      o.sell(t -> {

      });
    });

    // 여러 DSL 패턴을 이용
    Order order4 = MixedBuilder.forCustomer("BigBank",
        MixedBuilder.buy(t -> {

        }));

    // 가독성 심각..
    double value = calculate(order, true, false, true);
    // 예제 10.15 에서 해결해보자.
    double value2 = new TaxCalculator().withTaxGeneral()
        .withTaxSurcharge() // 지역 세금, 추가요금은 주문에 추가하고 싶다고 잘 표현하고 있음
        .calculate(order);
    // 여전히 장황한 코드, 예제 10.16에서 해결해보자.
    double value3 = new TaxCalculator2()
        .with(Tax::regional)
        .with(Tax::surcharge)
        .calculate(order);
  }

  // 예제 10-6 주문 빌더 패턴
  static class MethodChainingOrderBuilder {

    public final Order order = new Order();

    private MethodChainingOrderBuilder(String customer) {
      order.setCustomer(customer);
    }

    public static MethodChainingOrderBuilder forCustomer(String customer) {
      return new MethodChainingOrderBuilder(customer);
    }

    public TradeBuilder buy(int quantity) {
      return new TradeBuilder(this, Trade.Type.BUY, quantity);
    }

    public TradeBuilder sell(int quantity) {
      return new TradeBuilder(this, Trade.Type.SELL, quantity);
    }

    public MethodChainingOrderBuilder addTrade(Trade trade) {
      order.addTrade(trade);
      return this;
    }

    public Order end() {
      return order;
    }
  }

  static class TradeBuilder {

    private MethodChainingOrderBuilder builder;
    private final Trade trade = new Trade();

    public TradeBuilder() {
    }

    private TradeBuilder(MethodChainingOrderBuilder builder, Trade.Type type, int quantity) {
      this.builder = builder;
      trade.setType(type);
      trade.setQuantity(quantity);
    }

    public StockBuilder stock(String symbol) {
      return new StockBuilder(builder, trade, symbol);
    }
  }

  static class StockBuilder {

    private final MethodChainingOrderBuilder builder;
    private final Trade trade;
    private final Stock stock = new Stock();

    private StockBuilder(MethodChainingOrderBuilder builder, Trade trade, String symbol) {
      this.builder = builder;
      this.trade = trade;
      stock.setSymbol(symbol);
    }

    public TradeBuilderWithStock on(String market) {
      stock.setMarket(market);
      trade.setStock(stock);
      return new TradeBuilderWithStock(builder, trade);
    }
  }

  static class TradeBuilderWithStock {

    private final MethodChainingOrderBuilder builder;
    private final Trade trade;

    public TradeBuilderWithStock(MethodChainingOrderBuilder builder, Trade trade) {
      this.builder = builder;
      this.trade = trade;
    }

    public MethodChainingOrderBuilder at(double price) {
      trade.setPrice(price);
      return builder.addTrade(trade);
    }
  }

  // 예제 10-8 중첩된 함수 DSL 을 제공하는 주문 빌더
  static class NestedFunctionOrderBuilder {

    public static Order order(String customer, Trade... trades) {
      Order order = new Order();
      order.setCustomer(customer);
      Stream.of(trades)
          .forEach(order::addTrade);
      return order;
    }

    public static Trade buy(int quantity, Stock stock, double price) {
      return buildTrade(quantity, stock, price, Trade.Type.BUY);
    }

    public static Trade sell(int quantity, Stock stock, double price) {
      return buildTrade(quantity, stock, price, Trade.Type.SELL);
    }

    private static Trade buildTrade(int quantity, Stock stock, double price, Trade.Type buy) {
      Trade trade = new Trade();
      trade.setQuantity(quantity);
      trade.setType(buy);
      trade.setStock(stock);
      trade.setPrice(price);
      return trade;
    }

    public static double at(double price) {
      return price;
    }

    public static Stock stock(String symbol, String market) {
      Stock stock = new Stock();
      stock.setSymbol(symbol);
      stock.setMarket(market);
      return stock;
    }

    public static String on(String market) {
      return market;
    }
  }

  // 예제 10-9 함수 시퀀싱 DSL을 제공하는 주문 빌더
  static class LambdaOrderBuilder {

    private Order order = new Order();

    public static Order order(Consumer<LambdaOrderBuilder> consumer) {
      LambdaOrderBuilder builder = new LambdaOrderBuilder();
      consumer.accept(builder);
      return builder.order;
    }

    public void forCustomer(String customer) {
      order.setCustomer(customer);
    }

    public void buy(Consumer<TradeBuilder> consumer) {

    }

    public void sell(Consumer<TradeBuilder> consumer) {

    }

    private void trade(Consumer<TradeBuilder2> consumer, Trade.Type type) {
      TradeBuilder2 builder = new TradeBuilder2();
      builder.trade.setType(type);
      consumer.accept(builder);
      order.addTrade(builder.trade);
    }
  }

  static class TradeBuilder2 {

    private Trade trade = new Trade();

    public void quantity(int quantity) {
      trade.setQuantity(quantity);
    }

    public void price(double price) {
      trade.setPrice(price);
    }

    public void stock(Consumer<StockBuilder2> consumer) {
      StockBuilder2 builder = new StockBuilder2();
      consumer.accept(builder);
      trade.setStock(builder.stock);
    }
  }

  static class StockBuilder2 {

    private Stock stock = new Stock();

    public void symbol(String symbol) {
      stock.setSymbol(symbol);
    }

    public void market(String market) {
      stock.setMarket(market);
    }
  }

  // 예제 10-12 여러 형식을 혼합한 DSL 을 제공하는 주문 빌더
  static class MixedBuilder {

    public static Order forCustomer(String customer, TradeBuilder... builders) {
      Order order = new Order();
      order.setCustomer(customer);
      Stream.of(builders).forEach(b -> order.addTrade(b.trade));
      return order;
    }

    public static TradeBuilder buy(Consumer<TradeBuilder> consumer) {
      return buildTrade(consumer, Trade.Type.BUY);
    }

    public static TradeBuilder sell(Consumer<TradeBuilder> consumer) {
      return buildTrade(consumer, Trade.Type.SELL);
    }

    private static TradeBuilder buildTrade(Consumer<TradeBuilder> consumer, Trade.Type buy) {
      TradeBuilder builder = new TradeBuilder();
      builder.trade.setType(buy);
      consumer.accept(builder);
      return builder;
    }
  }

  // 예제 10-13 주문의 총 합에 적용할 세금
  static class Tax {

    public static double regional(double value) {
      return value * 1.1;
    }

    public static double general(double value) {
      return value * 1.3;
    }

    public static double surcharge(double value) {
      return value * 1.05;
    }
  }

  // 예제 10-14 boolean flag 집합을 이용해 세금 적용
  public static double calculate(Order order, boolean useRegional, boolean useGeneral,
      boolean useSurcharge) {
    double value = order.getValue();
    if (useRegional) {
      value = Tax.regional(value);
    }
    if (useGeneral) {
      value = Tax.general(value);
    }
    if (useSurcharge) {
      value = Tax.surcharge(value);
    }
    return value;
  }

  // 예제 10.15 적용할 세금을 유창하게 정의하는 세금 계산기
  static class TaxCalculator {

    private boolean useRegional;
    private boolean useGeneral;
    private boolean useSurcharge;

    public TaxCalculator withTaxRegional() {
      useRegional = true;
      return this;
    }

    public TaxCalculator withTaxGeneral() {
      useGeneral = true;
      return this;
    }

    public TaxCalculator withTaxSurcharge() {
      useSurcharge = true;
      return this;
    }

    public double calculate(Order order) {
      return Chapter10.calculate(order, useRegional, useGeneral, useSurcharge);
    }
  }

  // 예제 10.16 유창하게 세금 함수를 적용하는 세금 계산기
  static class TaxCalculator2 {

    public DoubleUnaryOperator taxFunction = d -> d;

    public TaxCalculator2 with(DoubleUnaryOperator f) {
      taxFunction = taxFunction.andThen(f);
      return this;
    }

    public double calculate(Order order) {
      return taxFunction.applyAsDouble(order.getValue());
    }
  }

}

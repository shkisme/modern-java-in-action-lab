import java.util.ArrayList;
import java.util.List;

public class Order {

  private String customer;

  private List<Trade> trades = new ArrayList<>();

  public String getCustomer() {
    return customer;
  }

  public void setCustomer(String customer) {
    this.customer = customer;
  }

  public double getValue() {
    return trades.stream().mapToDouble(Trade::getValue).sum();
  }

  public void addTrade(Trade trade) {
    this.trades.add(trade);
  }
}

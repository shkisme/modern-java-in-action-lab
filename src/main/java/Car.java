import java.util.Optional;

public class Car {

  private String brand;

  private Color color;

  private Optional<Insurance> insurance; // 자동차가 보험에 가입되어 있을수도, 아닐 수 도 있음을 표시

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Optional<Insurance> getInsurance() {
    return insurance;
  }
}

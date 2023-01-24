import java.util.Optional;

public class Person {

  private String name;

  private int age;

  private Optional<Car> car; // 사람이 차를 소유했을 수도, 아닐 수도 있음을 표시한다.

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Optional<Car> getCar() {
    return car;
  }
}

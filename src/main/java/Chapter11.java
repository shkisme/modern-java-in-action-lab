import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class Chapter11 {

  public static void main(String[] args) {
    Car car = new Car();
    // 빈 Optional
    Optional<Car> optCar = Optional.empty();
    // null 이 아닌 값으로 Optional 만들기
    Optional<Car> optCar2 = Optional.of(car); // 이제 car가 null 이면 즉시 에러를 뱉는다.
    // null 값으로 Optional 만들기
    Optional<Car> optCar3 = Optional.ofNullable(car); // null 값을 저장할 수 있는 Optional

    // 11.3.2 맵으로 Optional의 값을 추출하고 변환하기
    Insurance insurance = new Insurance();
    Optional<Insurance> optionalInsurance = Optional.ofNullable(insurance);
    Optional<String> name = optionalInsurance.map(Insurance::getName);

    // 11.3.3 flatMap으로 Optional 객체 연결
    Person person = new Person();
    Optional<Person> optionalPerson = Optional.of(person);
    String s = optionalPerson.flatMap(Person::getCar)
        .flatMap(Car::getInsurance)
        .map(Insurance::getName).orElse("Unknown");

    Optional<Optional<Car>> car1 = optionalPerson
        .map(Person::getCar);

    // 예제 11.6 사람 목록을 이용해서 가입한 보험 회사의 이름을 찾기
    List<Person> persons = new ArrayList<>();
    Set<String> collect = persons.stream()
        .map(Person::getCar)
        .map(optC -> optC.flatMap(Car::getInsurance))
        .map(optIns -> optIns.map(Insurance::getName))
        .flatMap(Optional::stream)
        .collect(toSet());

    // 11.4.1 잠재적으로 null 이 될 수 있는 대상을 Optional로 감싸기
    Map<String, Object> map = new HashMap<>();
    Optional<Object> value = Optional.ofNullable(map.get("key"));
  }

  /*public String getCarInsuranceName(Person person) {
    return person.getCar().getInsurance().getName(); // NullPointerException 이 발생하기 쉽다.
  }*/

  // 11.1.1 보수적인 자세로 NullPointerException 줄이기
  public String getCarInsuranceName2(Person person) {
    if (person != null) {
      Car car = person.getCar().get();
      if (car != null) {
        Insurance insurance = car.getInsurance().get();
        if (insurance != null) {
          return insurance.getName();
        }
      }
    }
    return "Unknown";
  }

  // 예제 11.3 너무 많은 출구
  public String getCarInsuranceName3(Person person) {
    if (person == null) {
      return "Unknown";
    }
    Car car = person.getCar().get();
    if (car == null) {
      return "Unknown";
    }
    Insurance insurance = car.getInsurance().get();
    if (insurance == null) {
      return "Unknown";
    }
    return insurance.getName();
  }

  // 11.3.6 두 Optional 합치기
  public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person,
      Optional<Car> car) {
    if (person.isPresent() && car.isPresent()) {
      return Optional.of(findCheapestInsurance(person.get(), car.get()));
    } else {
      return Optional.empty();
    }
  }

  public Insurance findCheapestInsurance(Person person, Car car) {
    // 조회
    // 비교
    return new Insurance();
  }

  // 퀴즈 11.1 nullSafeFindCheapestInsurance() 리팩토링 해보기
  public Optional<Insurance> nullSafeFindCheapestInsurance2(Optional<Person> person,
      Optional<Car> car) {
    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
  }

  // 퀴즈 11.2 Optional 필터링
  public String getCarInsuranceName(Optional<Person> person, int minAge) {
    return person.filter(p -> p.getAge() >= minAge)
        .flatMap(Person::getCar)
        .flatMap(Car::getInsurance)
        .map(Insurance::getName)
        .orElse("Unknown");
  }

  // 예제 11.7 문자열을 정수 Optional 로 변환
  public static Optional<Integer> stringToInt(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  // 예제 11.8 프로퍼티에서 지속 시간을 읽는 명령형 코드 - 가독성이 나쁜 코드. 개선해보자.
  public int readDuration(Properties properties, String name) {
    String value = properties.getProperty(name);
    if (value != null) {
      try {
        int i = Integer.parseInt(value);
        if (i > 0) {
          return i;
        }
      } catch (NumberFormatException numberFormatException) {
        throw new RuntimeException(numberFormatException);
      }
    }
    return 0;
  }

  // 퀴즈 11.3 위의 코드 개선해보기
  public int readDuration2(Properties properties, String name) {
    return Optional.ofNullable(properties.getProperty(name))
        .flatMap(Chapter11::stringToInt)
        .filter(integer -> integer > 0)
        .orElse(0);
  }
}

public class Chapter13 {

  public static void main(String[] args) {

  }

}

interface A {

  default void hello() {
    System.out.println("Hello from A");
  }
}

interface B extends A {

}

interface C extends A {

}

class D implements B, C {

  public static void main(String[] args) {
    new D().hello(); // 무엇이 출력될까?
  }
}


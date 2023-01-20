import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Chapter07 {

  public static void main(String[] args) {
    // 7.1 병렬 스트림
    Stream.iterate(1L, i -> i + 1)
        .limit(10)
        .reduce(0L, Long::sum);

    // 7.1.1 병렬 스트림으로 변환하기.
    Stream.iterate(1L, i -> i + 1)
        .limit(10)
        .parallel()
        .reduce(0L, Long::sum);

    // 병렬 스트림을 순차 스트림으로 바꾸기 - sequential
    Stream.iterate(1L, i -> i + 1)
        .parallel()
        .sequential()
        .reduce(0L, Long::sum);

    // 병렬 스트림에서 사용하는 스레드 풀 설정
    System.setProperty("java.util.comcurrent.ForkJoinPool.common.parallelism", "12");
    // 위 코드는 전역 설정코드라서, 이후의 모든 병렬 스트림 연산에 영향을 준다.
    // 현재는 하나의 병렬 스트림에 사용할 수 있는 특정한 값을 지정할 수 없다.
    // 포크조인풀의 기본 값을 그대로 사용하는 편이 낫다고 한다.
  }

}

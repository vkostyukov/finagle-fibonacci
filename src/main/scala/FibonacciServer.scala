import com.twitter.finagle.dispatch.SerialServerDispatcher
import com.twitter.finagle.netty3.Netty3Listener
import com.twitter.finagle.server._
import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}

/**
 * A Fibonacci listener.
 */
object FibonacciListener extends Netty3Listener[String, String](
  "fibonacci-listener", StringServerPipeline
)

/**
 * A server itself.
 */
object FibonacciServer extends DefaultServer[String, String, String, String](
  "fibonacci-server", FibonacciListener, new SerialServerDispatcher(_, _)
)

/**
 * A Fibonacci calculator abstraction with single method 'calculate'
 * that makes all the magic happen.
 */
trait FibonacciCalculator {

  val Zero = BigInt(0)
  val One = BigInt(1)
  val Two = BigInt(2)

  /**
   * Calculates the 'n'-th Fibonacci number.
   */
  def calculate(n: BigInt): Future[BigInt]
}

/**
 * A local Fibonacci calculator that implements a straightforward 
 * Fibonacci calculation algorithm. This calculator represents
 * a Leaf node in the topology tree.
 */
object LocalFibonacciCalculator extends FibonacciCalculator {

  def calculate(n: BigInt): Future[BigInt] =
    if (n.equals(Zero) || n.equals(One)) Future.value(n)
    else for { a <- calculate(n - One)
               b <- calculate(n - Two) } yield (a + b)
}

/**
 * A remove Fibonacci calculator, which is actually wrapper around
 * Finagle's service. Since, we use String-based transport, this calculator
 * is responsible for conversion of a BigInt into a String and vise versa.
 */
class RemoteFibonacciCalculator(remote: Service[String, String]) 
    extends FibonacciCalculator {

  def calculate(n: BigInt): Future[BigInt] = 
    remote(n.toString) map { BigInt(_) }
}

/**
 * A fanout Fibonacci calculator that simply delegates the requests to
 * two child nodes - 'left' and 'right'. This calculator represents a 
 * Branch/Node in the topology tree.
 */
class FanoutFibonacciCalculator(
  left: FibonacciCalculator,
  right: FibonacciCalculator) extends FibonacciCalculator {
  
  def calculate(n: BigInt): Future[BigInt] =
    if (n.equals(Zero) || n.equals(One)) Future.value(n)
    else {
      val seq = Seq(left.calculate(n - One), right.calculate(n - Two))
      Future.collect(seq) map { _.sum }
    }
}

/**
 * A Fibonacci service that performs data conversion and delegates the 
 * request to either local, remote or fanout calculator.
 */
class FibonacciService(calculator: FibonacciCalculator) extends Service[String, String] {
  def apply(req: String): Future[String] =
    calculator.calculate(BigInt(req)) map { _.toString }
}

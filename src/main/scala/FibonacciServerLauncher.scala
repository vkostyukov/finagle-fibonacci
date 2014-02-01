import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Await, Future}

/**
 * The following configurations are supported:
 *  - leaf [port]
 *    example: leaf 2001
 *
 *  - node [self port] [left child's port] [right child's port]
 *    example: node 2001 2002 2003
 */
object FibonacciServerLauncher {
  def main(args: Array[String]): Unit = main(args.toSeq)

  def main(args: Seq[String]): Unit = args match {
    case Seq("leaf", port) =>
      val service = new FibonacciService(LocalFibonacciCalculator)
      Await.ready(FibonacciServer.serve(":" + port, service))
    case Seq("node", port, left, right) => 
      val lt = new RemoteFibonacciCalculator(FibonacciClient.newService("localhost:" + left))
      val rt = new RemoteFibonacciCalculator(FibonacciClient.newService("localhost:" + right))
      val service = new FibonacciService(new FanoutFibonacciCalculator(lt, rt))
      Await.ready(FibonacciServer.serve(":" + port, service))
  }
}
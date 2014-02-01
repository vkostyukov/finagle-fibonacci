import com.twitter.finagle.{Group, Service, ServiceProxy, SimpleFilter}
import com.twitter.util.{Await, Future, Try, Throw, Time}

/**
 * The following configuration is available:
 *
 *  - [port] [Fibonacci number to calculate]
 */
object FibonacciClientLauncher {
  def main(args: Array[String]): Unit = main(args.toSeq)

  def main(args: Seq[String]): Unit = args match {
    case Seq(port, req) =>
      val client = FibonacciClient.newService("localhost:" + port)
      val rep = Await.result(client(req))
      printf("Fibonacci(%s) is %s\n", req, rep)
    case _ => println("Bad arguments!")
  }
}

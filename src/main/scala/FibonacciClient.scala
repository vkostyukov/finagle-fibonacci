import com.twitter.conversions.time._
import com.twitter.finagle.client._
import com.twitter.finagle.dispatch.SerialClientDispatcher
import com.twitter.finagle.filter.MaskCancelFilter
import com.twitter.finagle.netty3.Netty3Transporter
import com.twitter.finagle.service.{RetryingFilter, RetryPolicy, TimeoutFilter}
import com.twitter.finagle.stats.{NullStatsReceiver, InMemoryStatsReceiver}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Group, Service, ServiceProxy, SimpleFilter}
import com.twitter.util.{Await, Future, Try, Throw, Time}

/**
 * A transporter.
 */
object StringClientTransporter extends Netty3Transporter[String, String](
  "string-client-transporter", StringClientPipeline
)

/**
 * A client.
 */
object FibonacciClient extends DefaultClient[String, String](
  name = "fibonacci-client",
  endpointer = {
    val bridge = Bridge[String, String, String, String](
      StringClientTransporter, new SerialClientDispatcher(_)
    )
    (addr, stats) => bridge(addr, stats)
  }
)


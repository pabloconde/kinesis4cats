/*
 * Copyright 2023-2023 etspaceman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kinesis4cats.kpl

import java.nio.ByteBuffer

import cats.effect.{IO, SyncIO}
import com.amazonaws.services.kinesis.producer._
import io.circe.syntax._
import org.scalacheck.Arbitrary

import kinesis4cats.localstack.TestData
import kinesis4cats.localstack.kpl.LocalstackKPLProducer
import kinesis4cats.localstack.syntax.scalacheck._

abstract class KPLProducerSpec(implicit LE: KPLProducer.LogEncoders)
    extends munit.CatsEffectSuite
    with munit.CatsEffectFunFixtures {
  def fixture(
      streamName: String,
      shardCount: Int
  ): SyncIO[FunFixture[KPLProducer[IO]]] = ResourceFixture(
    LocalstackKPLProducer.producerWithStream(streamName, shardCount)
  )

  fixture("test1", 1).test("It should produce successfully") { producer =>
    val testData = Arbitrary.arbitrary[TestData].one
    val testDataBB = ByteBuffer.wrap(testData.asJson.noSpaces.getBytes())

    producer.put(new UserRecord("test1", "partitionKey", testDataBB)).map {
      result =>
        assert(result.isSuccessful())
    }
  }
}
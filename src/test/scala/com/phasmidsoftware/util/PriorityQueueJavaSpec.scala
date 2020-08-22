package com.phasmidsoftware.util

import org.scalatest.{flatspec, matchers}
import scala.collection.mutable.Queue
import scala.util.Random

class PriorityQueueJavaSpec extends flatspec.AnyFlatSpec with matchers.should.Matchers {

	behavior of "insert"

	it should "work" in {
		val target = new PriorityQueueJava[Int]()
		val result: PriorityQueueJava[Int] = target.insert(1)
		result.size() shouldBe 1
		val q: Array[AnyRef] = result.heap
		q.length shouldBe 32
		q(0) shouldBe 1
		q(1) shouldBe null
		val z = result.del()
		z.getPq.isEmpty shouldBe true
		z.getValue shouldBe 1
	}

	it should "allow del" in {
		val target = new PriorityQueueJava[Int]()
		val result: PriorityQueueJava[Int] = target.insert(1)
		val z = result.del()
		z.getPq.isEmpty shouldBe true
		z.getValue shouldBe 1
	}

	it should "allow mix of insert and del" in {
		val r = new Random(0)
		val q: Queue[Double] = new Queue[Double]()
		var pq: PriorityQueueJava[Double] = new PriorityQueueJava[Double]
		for (_ <- 1 to 100) {
			val x = r.nextDouble() - 0.5
			if (x >= 0) pq = pq.insert(x)
			else {
				val z: PriorityQueueJava.DeleteResult[Double] = pq.del()
				pq = z.getPq
				q.enqueue(z.getValue)
			}
		}
	}

	behavior of "iterator"

	it should "work for empty" in {
		val target = new PriorityQueueJava[Int]()
		val iterator = target.iterator()
		iterator.hasNext shouldBe false
		target.size() shouldBe 0
	}

	it should "work for one element" in {
		val target = new PriorityQueueJava[Int]()
		val result: PriorityQueueJava[Int] = target.insert(1)
		val iterator = result.iterator()
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 1
		iterator.hasNext shouldBe false
		result.size() shouldBe 1
	}

	it should "work for two elements" in {
		val target = new PriorityQueueJava[Int]()
		val result: PriorityQueueJava[Int] = target.insert(1).insert(2)
		val iterator = result.iterator()
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 1
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 2
		iterator.hasNext shouldBe false
		result.size() shouldBe 2
	}

	it should "work for three elements" in {
		val target = new PriorityQueueJava[Int]()
		val result: PriorityQueueJava[Int] = target.insert(1).insert(2).insert(0)
		val iterator = result.iterator()
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 0
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 1
		iterator.hasNext shouldBe true
		iterator.next() shouldBe 2
		iterator.hasNext shouldBe false
		result.size() shouldBe 3
	}

}

package com.datanarchi.libs.scala.trees

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CombinedTreeTests extends AnyFlatSpec with Matchers {
  // Définition de fonctions de jointure simples pour les tests
  implicit val joinFunction: Tree[Int] => Tree[Int] => Boolean = left => right => left.getValue.contains(right.getValue.get / 2)

  // Arbres de test
  val treeA: Tree[Int] = Tree(1, List(Tree(2), Tree(3)))
  val treeB: Tree[Int] = Tree(2, List(Tree(4), Tree(6)))

  // CombinedTree potentiel
  val combined: Option[CombinedTree[Int, Int]] = CombinedTree(treeA, treeB)(joinFunction)

  "CombinedTree.zip" should "correctly combine trees based on the join function" in {
    println(combined.get.zip.toList)
    combined match {
      case Some(ct) =>
        val zipped = ct.zip
        zipped.toList should contain theSameElementsAs List(
          (Some(1), Some(2)),
          (Some(2), Some(4)),
          (Some(3), Some(6))
        )
      case None => fail("CombinedTree was not created")
    }
  }

  "CombinedTree.zipMap" should "correctly apply a function to the zipped results" in {
    combined match {
      case Some(ct) =>
        val zippedMapped = ct.zipMap {
          case (Some(a), Some(b)) => s"$a combined with $b"
          case (Some(a), None) => s"$a with no match"
          case (None, Some(b)) => s"$b with no match"
        }
        zippedMapped.toList should contain theSameElementsAs List(
          "1 combined with 2",
          "2 combined with 4",
          "3 combined with 6"
        )
      case None => fail("CombinedTree was not created")
    }
  }


}

package com.datanarchi.libs.scala.trees

/** ********************************************************************************
 *
 * @author Samih Elj
 *
 * ******************************************************************************** */


/**
 * A Tree is a wrapper of a type T that allows the creation of a tree of nodes and leaves of type T
 *
 * @tparam A some type
 */
sealed trait Tree[+A] {
  def getValue: Option[A]

  def map[B](f: A => B): Tree[B] = Tree.map(this)(f)

  def toList: List[A] = Tree.toList(this)

  def toChildParentTupleList: List[(A, Option[A])] = Tree.toChildParentTupleList(treeToTransform = this, parentBranch = None)

  /**
   * Enriches the current tree with elements from tree B on a tree of another type C
   * The Elements of the current tree are conserved even if they cannot receive a graft
   * Examples of use: we need to associate the population of a country / cities to the tree containing the countries and
   * their segmentation to cities
   *
   * @param treeToGraft another tree
   * @param MatchingFunction          allows to find the pairs
   * @param PatchFunction          Transformation/Enrichment function element into C
   * @tparam B Type of the graft
   * @tparam C Type of the result (A enriched with B)
   * @return The enriched treee
   */
  def graft[B, C >: A](treeToGraft: Tree[B])(MatchingFunction: A => B => Boolean)(PatchFunction: A => B => C): Tree[C]

  def display(level: Int = 0): String

  override def toString: String = display()
}

/**
 * Empty Tree
 */
case object EmptyTree extends Tree[Nothing] {
  def getValue: Option[Nothing] = None

  def display(level: Int): String = "-- " * level + "{ Ø } \n"

  override def graft[B, C](treeToGraft: Tree[B])(MatchingFunction: Nothing => B => Boolean)(PatchFunction: Nothing => B => C): Tree[C] = this
}

/**
 * Non Empty Trees trait
 * For leaves and branches
 *
 * @tparam A some type
 */
sealed trait NonEmptyTree[+A] extends Tree[A] {
  override def getValue: Option[A] = Some(value)

  val value: A

}


case class Leaf[+A](value: A) extends NonEmptyTree[A] {
  def display(level: Int): String = "-- " * level + value.toString + "\n"

  /**
   * Enriches the current tree with elements from tree B on a tree of another type C
   * The Elements of the current tree are conserved even if they cannot receive a graft
   * Examples of use: we need to associate the population of a country / cities to the tree containing the countries and
   * their segmentation to cities
   *
   * @param treeToGraft another tree
   * @param MatchingFunction          allows to find the pairs
   * @param PatchFunction          Transformation/Enrichment function element into C
   * @tparam B Type of the graft
   * @tparam C Type of the result (A enriched with B)
   * @return The enriched treee
   */
  override def graft[B, C >: A](treeToGraft: Tree[B])(MatchingFunction: A => B => Boolean)(PatchFunction: A => B => C): Tree[C] = {
    if (treeToGraft.getValue.isDefined && MatchingFunction(this.value)(treeToGraft.getValue.get))
      Leaf(PatchFunction(this.value)(treeToGraft.getValue.get))
    else this
  }
}

case class Branch[+A](value: A, subTree: List[Tree[A]]) extends NonEmptyTree[A] {
  def display(level: Int): String = "-- " * level + value.toString + "\n" + subTree.map(_.display(level + 1)).mkString("")

  /**
   * Enriches the current tree with elements from tree B on a tree of another type C
   * The Elements of the current tree are conserved even if they cannot receive a graft
   * Examples of use: we need to associate the population of a country / cities to the tree containing the countries and
   * their segmentation to cities
   *
   * @param treeToGraft another tree
   * @param MatchingFunction          allows to find the pairs
   * @param PatchFunction          Transformation/Enrichment function element into C
   * @tparam B Type of the graft
   * @tparam C Type of the result (A enriched with B)
   * @return The enriched treee
   */
  override def graft[B, C >: A](treeToGraft: Tree[B])(MatchingFunction: A => B => Boolean)(PatchFunction: A => B => C): Tree[C] = {
    // we are in a branch, we check if the current element matches, if so we iterate over the child nodes. Else we do nothing
    //Branch(tf(this.value)(treeToGraft.getValue.get), this.subTree.map(_.graft(treeToGraft)))
    // if the tree to graft is a leaf, we dont have to go much on mapping child nodes of the current tree
    treeToGraft match {
      case EmptyTree => this
      case Leaf(valueToGraft) => if (MatchingFunction(this.value)(valueToGraft)) {
        Branch(PatchFunction(this.value)(valueToGraft), this.subTree)
      } else this
      case Branch(valueToGraft, _) => if (MatchingFunction(this.value)(valueToGraft)) {
        val toto: List[Tree[C]] = for {
          currentSubTree: Tree[A] <- this.subTree
          graftSubTreeElement: Tree[B] <- treeToGraft.asInstanceOf[Branch[B]].subTree
          if (currentSubTree.getValue.isDefined && graftSubTreeElement.getValue.isDefined && MatchingFunction(currentSubTree.getValue.get)(graftSubTreeElement.getValue.get))
        } yield {
          currentSubTree.graft[B, C](graftSubTreeElement)(MatchingFunction)(PatchFunction)
        }
        Branch(PatchFunction(this.value)(valueToGraft), toto )
      } else this
    }
  }
}

object Branch {
  /**
   * This function gets two branches, and tries to find couples of the sub elements of each branch that
   * match through the join function. For example, if we have two trees of elements having identifiers,
   * the join function may be "the elements have the same id", and our coupling function will return true if ids are equal.
   * The element who do not have corresponding on the other side will be paired with empty tree
   *
   * NB: if the join function returns true for more than one element of each side, then all the couples will
   * be created: ex  Left = ("(1,1)","(1,2)") right = ("(1,3)") and f= (x,y) => x._1 == y._1 then the result will be [(1,1),(1,3)]
   * and [(1,2),(1,3)]
   *
   * @param joinFunction a function that decides that left and right are to be paired or not
   * @param left         the left branch
   * @param right        the right branch
   * @tparam A The type of the wrapped elements in the branches of the first tree
   * @tparam B the type of the wrapped elements in the second tree
   * @return
   */
  def calculateCorrespondingTuplesInTheSameLevel[A, B](left: Branch[A], right: Branch[B])(implicit joinFunction: Tree[A] => Tree[B] => Boolean): List[(Tree[A], Tree[B])] = {
    if (joinFunction(left)(right)) {
      val commonCouples: List[(Tree[A], Tree[B])] = for {
        l <- left.subTree
        r <- right.subTree
        if joinFunction(l)(r)
      } yield (l, r)
      val onlyInLeft = for {
        l <- left.subTree if !right.subTree.exists(joinFunction(l))
      } yield (l, EmptyTree)
      val onlyInRight = for {
        r <- right.subTree if !left.subTree.exists(joinFunction(_)(r))
      } yield (EmptyTree, r)
      commonCouples ::: onlyInLeft ::: onlyInRight
    }
    else List()
  }
}

object Tree {
  def apply[A](value: A): Tree[A] = Leaf(value)

  def apply[A](value: A, sub: List[Tree[A]]): Tree[A] = Branch(value, sub)

  /**
   * Constructs a tree from a list of tuples matching each element with his parent element
   *
   * @param tuples list of (element, mayBeParent)
   * @tparam A the type of the element
   * @return may be a tree
   */
  def apply[A](tuples: List[(A, Option[A])]): Option[Tree[A]] = {

    val childrenMap: Map[Option[A], List[A]] = tuples.groupBy(_._2).view.mapValues(_.map(_._1)).toMap

    // Fonction récursive pour construire l'arbre
    def buildTree(parent: Option[A]): List[Tree[A]] = {
      childrenMap.getOrElse(parent, List()).map { child =>
        childrenMap.get(Some(child)) match {
          case Some(_) => Branch(child, buildTree(Some(child)))
          case None => Leaf(child)
        }
      }
    }

    // Trouver la racine (élément sans parent) et construire l'arbre, encapsulé dans un Option
    tuples.find(_._2.isEmpty).map(_._1) match {
      case Some(root) => Some(Branch(root, buildTree(Some(root))))
      case None => None // Retourne None si aucune racine n'est trouvée
    }
  }

  def empty[A]: Tree[A] = EmptyTree

  def leaf[A](value: A): Tree[A] = Leaf(value)

  def branch[A](value: A, subTree: List[Tree[A]]): Tree[A] = Branch(value, subTree)

  def map[A, B](a: Tree[A])(f: A => B): Tree[B] = a match {
    case EmptyTree => EmptyTree
    case Leaf(v) => Leaf(f(v))
    case Branch(v, c: List[Tree[A]]) => Branch(f(v), c.map(Tree.map(_)(f)))
  }

  private def toList[A](ta: Tree[A]): List[A] = ta match {
    case EmptyTree => List()
    case Leaf(value) => List(value)
    case Branch(value, subTree) => List(value) ::: subTree.flatMap(toList)
  }

  /**
   * Transforms a tree or a portion of a tree to a list of pairs of all the tree elements (nodes, leaves, root) paired with
   * their parent element
   * when called with a tree root, this should be called with None as the parent node
   *
   * @param treeToTransform tree
   * @param parentBranch    None if tree root, the parent node else
   * @tparam A wrapped element type
   * @return List of pairs
   */
  private def toChildParentTupleList[A](treeToTransform: Tree[A], parentBranch: Option[A]): List[(A, Option[A])] = treeToTransform match {
    case EmptyTree => List.empty
    case Leaf(value) => List((value, parentBranch))
    case Branch(value, subTrees) =>
      val selfTuple = List((value, parentBranch))
      val childrenTuples = subTrees.flatMap(child => toChildParentTupleList(child, Some(value)))
      selfTuple ++ childrenTuples
  }
}

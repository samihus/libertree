package com.datanarchi.libs.scala.trees


/**********************************************************************************
 * @author Samih Elj
 * *******************************************************************************/


/**
 * this class is abstract to avoid the direct use of its default apply using left and right only
 * as we need to operate using the join function
 *
 * @param left the left tree
 * @param right the right tree
 * @tparam A Type of the wrapped element in left tree
 * @tparam B Type of the wrapped element in the right tree
 */
abstract case class CombinedTree[A,B](left: Tree[A], right: Tree[B]){
	/**
	 *
	 * @param jf the JoinFunction: how to find corresponding elements in two trees
	 *           For example, if T has an id, jf may be jf: left => right => left.value.id == right.value.id
 * @return
	 */
	def zip(implicit jf: Tree[A] => Tree[B] => Boolean): Tree[(Option[A], Option[B])] = this match {
		case CombinedTree(EmptyTree , EmptyTree) => EmptyTree
		case CombinedTree(EmptyTree , right: Leaf[B]) => Tree((None, Some(right.value)))
		case CombinedTree(left: Leaf[A], EmptyTree) => Tree((Some(left.value), None))
		case CombinedTree(left: Leaf[A], right: Leaf[B]) => Tree((Some(left.value), Some(right.value)))
		case CombinedTree(EmptyTree, right: Branch[B]) => Tree((None, Some(right.value)), right.subTree.flatMap(CombinedTree(EmptyTree, _)).map(_.zip))
		case CombinedTree(left: Branch[A], EmptyTree) => Tree((Some(left.value),None), left.subTree.flatMap(CombinedTree(_,EmptyTree)).map(_.zip))
		case CombinedTree(left: Branch[A], right: Branch[B]) =>
			Tree((Some(left.value),Some(right.value)), Branch.calculateCorrespondingTuplesInTheSameLevel(left,right).flatMap(x => CombinedTree(x._1,x._2)).map(_.zip))
		case _ => EmptyTree
	}

	/**
	 * executes zip then map result using the mapping function f
	 * @param appliedFunction the function to be applied to the zipped tree
	 * @param joinFunction the function allowing the grouping of the elements in the trees
	 * @tparam C the output C tree
	 * @return
	 */
	def zipMap[C](appliedFunction: ((Option[A], Option[B])) => C)(implicit joinFunction: Tree[A] => Tree[B] => Boolean): Tree[C] = this.zip.map(appliedFunction)
}

object CombinedTree {
	// apply method to create a combined tree with always the same conf item id
	def apply[A,B](left: Tree[A], right: Tree[B])(implicit joinFunction: Tree[A] => Tree[B] => Boolean): Option[CombinedTree[A,B]] = (left, right) match {
		case (EmptyTree, EmptyTree) => Some(new CombinedTree[A,B](EmptyTree, EmptyTree) {})
		case (EmptyTree, t) => Some(new CombinedTree[A,B](EmptyTree, t){})
		case (t, EmptyTree) => Some(new CombinedTree[A,B](t, EmptyTree){})
		case (l: NonEmptyTree[A], r: NonEmptyTree[B]) if joinFunction(l)(r) => Some(new CombinedTree[A,B](left, right){})
		case _ => None
	}
}




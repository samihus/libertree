# Libertree

Libertree is a Scala library designed to facilitate the creation, manipulation, and combination of tree data structures. It offers a variety of operations such as mapping, listing, grafting, and pretty-printing.
The library is under development and should be used with caution.

## Features

- **Tree Creation**: Simplify the creation of trees with nested branches and leaves.
- **Mapping**: Apply functions to every element in a tree, producing a transformed tree.
- **Listing**: Convert trees into list representations, maintaining hierarchical order.
- **Grafting**: Enrich trees with data from another tree based on matching criteria, perfect for associating related data across different trees.
- **Display**: Pretty-print trees for easy visualization and debugging.


## Usage

### Creating Trees

To create trees with `Libertree`, you can easily nest branches and leaves to represent complex hierarchical data structures.

```scala
import com.datanarchi.libs.scala.trees._

val myTree = Tree("Root", List(
  Tree.leaf("Child1"),
  Tree.branch("Child2", List(
    Tree.leaf("Grandchild1"),
    Tree.leaf("Grandchild2")
  ))
))
```
### Mapping Over Trees
Apply a function to each element in the tree, producing a new tree with the results of the function applied to each element.
```scala
val capitalizedTree = myTree.map(_.toUpperCase())
```
### Converting to List
Convert your tree into a list representation, preserving the hierarchical order of elements.

```scala
val treeAsList = myTree.toList
// Results in List("Root", "Child1", "Child2", "Grandchild1", "Grandchild2")
```
### Grafting Trees
Combine elements from one tree into another based on a matching function. This is particularly useful for enriching data structures with additional information.
```scala
val matchingFunction: String => ((String, Int)) => Boolean = name => nameWithPop => name == nameWithPop._1
val patchFunction: String => ((String, Int)) => String = name => nameWithPop => s"$name (${nameWithPop._2} inhabitants)"

val enrichedTree = treeA.graft(treeB)(matchingFunction)(patchFunction)
```
## Contributing

Contributions to Libertree are more than welcome! If you have any suggestions for improvement, or if you encounter any issues, please feel free to submit a pull request or open an issue.

## License
Libertree is made available under the MIT License.

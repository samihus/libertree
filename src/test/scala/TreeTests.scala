package com.datanarchi.libs.scala.trees

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TreeTests extends AnyFlatSpec with Matchers {
  val arbreDuMonde: Tree[String] =
    Tree(
      "Terre",
      List(
        Tree(
          "Afrique",
          List(
            Tree("Égypte", List(Tree("Le Caire"))),
            Tree("Nigéria", List(Tree("Lagos")))
          )
        ),
        Tree(
          "Amérique du Nord",
          List(
            Tree("États-Unis", List(Tree("New York"), Tree("Los Angeles"))),
            Tree("Canada", List(Tree("Toronto"), Tree("Montréal")))
          )
        ),
        Tree(
          "Europe",
          List(
            Tree(
              "France",
              List(
                Tree(
                  "Paris",
                  List(
                    Tree("1er arrondissement"),
                    Tree("2e arrondissement"),
                    Tree("3e arrondissement"),
                    Tree("4e arrondissement"),
                    Tree("5e arrondissement")
                  )
                ),
                Tree("Lyon")
              )
            ),
            Tree("Allemagne", List(Tree("Berlin"), Tree("Hambourg")))
          )
        ),
        Tree(
          "Asie",
          List(
            Tree("Chine", List(Tree("Pékin"), Tree("Shanghai"))),
            Tree("Japon", List(Tree("Tokyo"), Tree("Osaka")))
          )
        )
      )
    )

  val populations: Tree[(String, Int)] =
    Tree(
      ("Terre", 0), // La population de la Terre n'est pas spécifiée, donc 0 pour l'exemple
      List(
        Tree(
          ("Afrique", 0), // La population de l'Afrique n'est pas spécifiée
          List(
            Tree(("Égypte", 102334404)), // Population de l'Égypte
            Tree(("Nigéria", 206139589))  // Population du Nigéria
          )
        ),
        Tree(
          ("Amérique du Nord", 0),
          List(
            Tree(("États-Unis", 331002651)),
            Tree(("Canada", 37742154))
          )
        ),
        Tree(
          ("Europe", 0),
          List(
            Tree(
              ("France", 65273511),
              List(
                Tree(("Paris", 2148271)), // Population de Paris sans arrondissements
                Tree(("Lyon", 513275))
              )
            ),
            Tree(("Allemagne", 83783942))
          )
        ),
        Tree(
          ("Asie", 0),
          List(
            Tree(("Chine", 1439323776)),
            Tree(("Japon", 126476461))
          )
        )
      )
    )

  "toList method" should "correctly transform a Tree of strings into a flattened list of strings, preserving hierarchy" in {

    val expectedList = List(
      "Terre",
      "Afrique", "Égypte", "Le Caire", "Nigéria", "Lagos",
      "Amérique du Nord", "États-Unis", "New York", "Los Angeles", "Canada", "Toronto", "Montréal",
      "Europe", "France", "Paris", "1er arrondissement", "2e arrondissement", "3e arrondissement", "4e arrondissement", "5e arrondissement", "Lyon",
      "Allemagne", "Berlin", "Hambourg",
      "Asie", "Chine", "Pékin", "Shanghai", "Japon", "Tokyo", "Osaka"
    )

    val listeTransformee = arbreDuMonde.toList
    listeTransformee should contain theSameElementsAs expectedList
  }

  "toChildParentTupleList method" should "correctly transform the Tree into a list of child-parent tuples" in {
    val expectedTuples: List[(String, Option[String])] = List(
      ("Terre", None),
      ("Afrique", Some("Terre")), ("Égypte", Some("Afrique")), ("Le Caire", Some("Égypte")), ("Nigéria", Some("Afrique")), ("Lagos", Some("Nigéria")),
      ("Amérique du Nord", Some("Terre")), ("États-Unis", Some("Amérique du Nord")), ("New York", Some("États-Unis")), ("Los Angeles", Some("États-Unis")),
      ("Canada", Some("Amérique du Nord")), ("Toronto", Some("Canada")), ("Montréal", Some("Canada")),
      ("Europe", Some("Terre")), ("France", Some("Europe")), ("Paris", Some("France")),
      ("1er arrondissement", Some("Paris")), ("2e arrondissement", Some("Paris")), ("3e arrondissement", Some("Paris")),
      ("4e arrondissement", Some("Paris")), ("5e arrondissement", Some("Paris")),
      ("Lyon", Some("France")),
      ("Allemagne", Some("Europe")), ("Berlin", Some("Allemagne")), ("Hambourg", Some("Allemagne")),
      ("Asie", Some("Terre")), ("Chine", Some("Asie")), ("Pékin", Some("Chine")), ("Shanghai", Some("Chine")),
      ("Japon", Some("Asie")), ("Tokyo", Some("Japon")), ("Osaka", Some("Japon"))
    )

    val tuplesResult = arbreDuMonde.toChildParentTupleList
    tuplesResult should contain theSameElementsAs expectedTuples
  }


  "The apply method" should "reconstruct the tree correctly from a list of child-parent tuples" in {

    val childParentList = arbreDuMonde.toChildParentTupleList

    val reconstructedTreeOption = Tree.apply(childParentList)

    reconstructedTreeOption shouldBe Some(arbreDuMonde)
  }


  "graft" should "correctly enrich the world tree with population data" in {
    // Fonction de correspondance basée sur le nom
    val matchingFunction: String => ((String, Int)) => Boolean = name => nameWithPop => name == nameWithPop._1

    // Fonction de patch pour enrichir les noms avec les données de population
    val patchFunction: String => ((String, Int)) => String = name => nameWithPop => s"$name (${nameWithPop._2} habitants)"

    // Application de graft pour enrichir l'arbreDuMonde avec les populations
    val enrichedTree = arbreDuMonde.graft(populations)(matchingFunction)(patchFunction)

    println(enrichedTree.display())

    // Vérification que l'arbre enrichi contient les bonnes données
    val expectedList = List(
      "Terre (0 habitants)",
      "Afrique (0 habitants)", "Égypte (102334404 habitants)", "Le Caire", "Nigéria (206139589 habitants)", "Lagos",
      "Amérique du Nord (0 habitants)", "États-Unis (331002651 habitants)", "New York", "Los Angeles",
      "Canada (37742154 habitants)", "Toronto", "Montréal",
      "Europe (0 habitants)", "France (65273511 habitants)", "Paris (2148271 habitants)", "1er arrondissement",
      "2e arrondissement", "3e arrondissement", "4e arrondissement", "5e arrondissement",
      "Lyon (513275 habitants)", "Allemagne (83783942 habitants)", "Berlin", "Hambourg",
      "Asie (0 habitants)", "Chine (1439323776 habitants)", "Pékin", "Shanghai",
      "Japon (126476461 habitants)", "Tokyo", "Osaka"
    )

    enrichedTree.toList should contain theSameElementsAs expectedList
  }
}

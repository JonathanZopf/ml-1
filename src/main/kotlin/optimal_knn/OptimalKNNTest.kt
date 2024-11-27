package optimal_knn

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import nu.pattern.OpenCV
import org.hszg.learner_implementations.knn.KNearestNeighborDistanceCalculation
import org.hszg.learner_implementations.KNearestNeighbor
import org.hszg.optimal_knn.readClassificationFeatureVectorsFromFile
import org.hszg.training.readTrainingData
import java.io.File
import kotlin.math.*

/**
 * This function will test the optimal k value for the k-nearest neighbor algorithm using different distance calculation methods.
 * The results will be written to a CSV file.
 */
fun main() = runBlocking {
    OpenCV.loadLocally()

    println("Starting KNN optimization...")

    // Load data
    val trainingData = readTrainingData().toSet()
    val featureVectorsWithClassification = readClassificationFeatureVectorsFromFile()

    // Prepare to store results thread-safely
    val results = mutableListOf<Triple<Int, String, Double>>()
    val mutex = Mutex() // Mutex for thread-safe access to the results list

    val kValues = generateKValues(1, 200)
    println("Testing k values: $kValues")

    // Launch parallel computations
    val jobs = kValues.map { k ->
        GlobalScope.launch(Dispatchers.IO) {
            println("Testing for k = $k...")
            for ((name, distanceCalculation) in allDistanceCalculationMethods()) {
                println("  Using distance method: $name")

                val learner = KNearestNeighbor(k, distanceCalculation)
                learner.learn(trainingData)

                // Calculate accuracy
                val correctClassifications = featureVectorsWithClassification.count { item ->
                    learner.classify(item.featureVector) == item.classification
                }
                val accuracy = correctClassifications.toDouble() / featureVectorsWithClassification.size

                // Safely add result
                mutex.lock()
                try {
                    results.add(Triple(k, name, accuracy))
                } finally {
                    mutex.unlock()
                }
            }
        }
    }

    // Wait for all jobs to finish
    jobs.forEach { it.join() }

    // Output results
    println("\nFinal Results:")
    results.sortedByDescending { it.third }.forEach { (k, name, accuracy) ->
        println("k = $k, Distance Method = $name, Accuracy = $accuracy")
    }

    // Aggregate and print average accuracy per method
    println("\nAverage Accuracy per Distance Method:")
    allDistanceCalculationMethods().forEach { (name, _) ->
        val avgAccuracy = results.filter { it.second == name }.map { it.third }.average()
        println("$name: $avgAccuracy")
    }

    // Write results to CSV
    writeResultsToCsv(results)
    println("Results written to CSV.")
}

// Define distance calculation methods
private fun allDistanceCalculationMethods(): List<Pair<String, KNearestNeighborDistanceCalculation>> {
    return listOf(
        "Euclidean" to KNearestNeighborDistanceCalculation { v1, v2 ->
            sqrt(v1.zip(v2).sumOf { (a, b) -> (a - b).pow(2) })
        },
        "Manhattan" to KNearestNeighborDistanceCalculation { v1, v2 ->
            v1.zip(v2).sumOf { (a, b) -> abs(a - b) }
        },
        "Minkowski (p=1.5)" to KNearestNeighborDistanceCalculation { v1, v2 ->
            val p = 1.5
            v1.zip(v2).sumOf { (a, b) -> abs(a - b).pow(p) }.pow(1 / p)
        },
        "Chebyshev" to KNearestNeighborDistanceCalculation { v1, v2 ->
            v1.zip(v2).maxOf { (a, b) -> abs(a - b) }
        },
        "Cosine" to KNearestNeighborDistanceCalculation { v1, v2 ->
            val dotProduct = v1.zip(v2).sumOf { (a, b) -> a * b }
            val magnitude1 = sqrt(v1.sumOf { it.pow(2) })
            val magnitude2 = sqrt(v2.sumOf { it.pow(2) })
            1 - (dotProduct / (magnitude1 * magnitude2))
        }
    )
}

// Generate a range of k values to test
private fun generateKValues(start: Int, max: Int): List<Int> {
    return (start..max).filter { it % 2 != 0 }
}

// Write results to a CSV file
private fun writeResultsToCsv(results: List<Triple<Int, String, Double>>) {
    val file = File("optimal_knn_results_500.csv")
    file.writeText("k,${allDistanceCalculationMethods().joinToString(",") { it.first }}\n")

    val groupedResults = results.groupBy { it.first }
    groupedResults.keys.sorted().forEach { k ->
        val row = groupedResults[k]?.joinToString(",") { it.third.toString() } ?: ""
        file.appendText("$k,$row\n")
    }
}

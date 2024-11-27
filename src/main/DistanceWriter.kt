import java.io.File

fun main() {
    val input = """
        k = 1, Distance Method = Euclidean, Accuracy = 0.912588592642592
        k = 1, Distance Method = Cosine, Accuracy = 0.912588592642592
    """.trimIndent()

    // Parse input lines
    val lines = input.lines()
    val results = mutableMapOf<Int, MutableMap<String, Double>>()
    val distanceMethods = mutableSetOf<String>()

    for (line in lines) {
        val parts = line.split(", ")
        val k = parts[0].substringAfter("k = ").toInt()
        val distanceMethod = parts[1].substringAfter("Distance Method = ")
        val accuracy = parts[2].substringAfter("Accuracy = ").toDouble()

        distanceMethods.add(distanceMethod)

        results.computeIfAbsent(k) { mutableMapOf() }[distanceMethod] = accuracy
    }

    // Prepare CSV file content
    val csvFile = File("output.csv")
    val sortedDistanceMethods = distanceMethods.sorted()
    val header = "k," + sortedDistanceMethods.joinToString(",")
    val rows = results.map { (k, accuracies) ->
        k.toString() + "," + sortedDistanceMethods.joinToString(",") { method ->
            accuracies[method]?.toString() ?: ""
        }
    }

    // Write to CSV file
    csvFile.printWriter().use { writer ->
        writer.println(header)
        rows.forEach { writer.println(it) }
    }

    println("CSV file written to ${csvFile.absolutePath}")
}

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

fun main(args: Array<String>) {
    val occurrences = mapWordOccurrences(
        ""
    )

    //prints the sorted word occurrences
    occurrences.sortedWith(
        compareBy({ it.target }, { it.distance })
    ).forEach {
        println(it.toString())
    }

    //allows user to keep entering a word
    val input = Scanner(System.`in`)
    while (true) {
        println("Enter a word:")
        val entry = input.nextLine().toLowerCase()

        getHighestMatch(entry, occurrences).forEach {
            println(" > $it")
        }
    }
}

private fun getHighestMatch(entry: String, occurrences: ArrayList<Occurrence>, singularReturn: Boolean = false): ArrayList<String> {
    val sample = entry.split(" ").reversed()
    val validWords = ArrayList<String>()

    occurrences.filter { occurrence ->
        occurrence.target == sample.first() && occurrence.distance == 1
    }.forEach { occurrence ->
        val checkWord = occurrence.comparator //fear
        var accepted = true
        sample.forEachIndexed { index, word -> //that, get
            val acceptedPhrases =
                occurrences.filter { occurrence -> occurrence.target == checkWord && occurrence.comparator == word && occurrence.distance == -(index + 1) }
            if (acceptedPhrases.size != 1) { //more than one found, no match for sure
                accepted = false
            }
        }
        if (accepted) {
            if (singularReturn) {
                validWords.add(checkWord)
            } else {
                val toAdd = getHighestMatch("$entry $checkWord", occurrences)
                if (toAdd.isEmpty()) {
                    validWords.add(checkWord)
                } else {
                    toAdd.forEach {
                        validWords.add("$checkWord $it")
                    }
                }
            }
        }

    }

    return validWords
}

const val PROTECT_NEWlINES = "<NL>"

/**
 * Finds the occurrences of words with new lines as boundaries
 */
fun mapWordOccurrences(text: String): ArrayList<Occurrence> {
    println("Create word network...")
    val occurrences = ArrayList<Occurrence>()
    val lines =
        text.toLowerCase()
            .replace("\n", PROTECT_NEWlINES)
            .replace(Regex("\\s?\\([^()]*\\)\\s?"), "")
            .trim()
            .replace(",", "")
            .replace("?", "")
            .split(PROTECT_NEWlINES)

    lines.forEach { line ->
        println("[$line]")
        line.split(" ").forEachIndexed { targetWordIndex, targetWord ->
            line.split(" ").forEachIndexed { checkWordIndex, checkWord ->
                if (checkWordIndex != targetWordIndex) {
                    println("Registering word occurrence between [$targetWord] and [$checkWord]")
                    registerWordOccurrence(
                        targetWord,
                        checkWord,
                        if (targetWordIndex > checkWordIndex) -(targetWordIndex - checkWordIndex) else abs(
                            targetWordIndex - checkWordIndex
                        ),
                        occurrences
                    )
                }
            }
        }
    }
    println("Word network created.")

    return occurrences
}

/**
 * Finds the occurrences of words with a range limit
 */
@Deprecated("New method which doesn't use limits, but uses new lines as boundaries")
fun mapWordOccurrences(text: String, limit: Int): ArrayList<Occurrence> {
    val occurrences = ArrayList<Occurrence>()
    val cleanTextNodes =
        text.replace(Regex("[(.*?)]"), "")
            .replace("\n", " ")
            .split(" ")

    cleanTextNodes.forEachIndexed { index, word ->
        val lowerIndex = if (index - limit < 0) 0 else index - limit
        val upperIndex = if (index + limit + 1 > cleanTextNodes.size) cleanTextNodes.lastIndex else index + limit + 1

        cleanTextNodes.subList(lowerIndex, upperIndex).forEachIndexed { index2, occurredWord ->
            if (index2 - limit != 0) {
                registerWordOccurrence(word, occurredWord, index2 - limit, occurrences)
            }
        }
    }

    return occurrences
}

fun registerWordOccurrence(target: String, comparator: String, distance: Int, occurrences: ArrayList<Occurrence>) {
    occurrences.forEach {
        if (it.target == target && it.comparator == comparator && it.distance == distance) {
            it.frequency++
            return
        }
    }
    occurrences.add(Occurrence(target, comparator, distance))
    return
}

data class Occurrence(val target: String, val comparator: String, val distance: Int, var frequency: Int = 1) {
    override fun toString(): String {
        return "=[$target]\t<$distance>\t[$comparator]\t->\t(x$frequency)"
    }
}
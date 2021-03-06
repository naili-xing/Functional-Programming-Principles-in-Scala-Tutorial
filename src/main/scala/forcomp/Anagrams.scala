package forcomp

object Anagrams {
  type Word = String
  type Sentence = List[Word]
  type Occurrences = List[(Char, Int)]

  val dictionary:List[Word] = loadDictionary

  def wordOccurrences(w:Word):Occurrences = {
    w.toLowerCase.toList.groupBy(identity).map{case (c,listc) => (c, listc.length)}.toList.sorted
  }

  /** make the sentence be a big string, and then count as a word */
  def  sentenceOccurrences(s: Sentence): Occurrences = wordOccurrences(s.foldLeft("")(_+_))

  lazy val dictionaryByOccurrences:Map[Occurrences, Sentence] =
    dictionary.groupBy(wordOccurrences(_)).withDefaultValue(List.empty[Word])

  /** Returns all the anagrams of a given word. */
  def wordAnagrams(word: Word): List[Word] = {
    dictionaryByOccurrences(wordOccurrences(word))
  }

  /** Returns the list of all subsets of the occurrence list.
   * This includes the occurrence itself, i.e. `List(('k', 1), ('o', 1))`
   * is a subset of `List(('k', 1), ('o', 1))`.
   * It also include the empty subset `List()`.
   *
   * Example: the subsets of the occurrence list `List(('a', 2), ('b', 2))` are:
   *
   * List(
   * List(),
   * List(('a', 1)),
   * List(('a', 2)),
   * List(('b', 1)),
   * List(('a', 1), ('b', 1)),
   * List(('a', 2), ('b', 1)),
   * List(('b', 2)),
   * List(('a', 1), ('b', 2)),
   * List(('a', 2), ('b', 2))
   * )
   *
   * Note that the order of the occurrence list subsets does not matter -- the subsets
   * in the example above could have been displayed in some other order.
   */
  def combinations(occurrences: Occurrences): List[Occurrences] = {
    if (occurrences.isEmpty) List(List())
    else (for {
      rest <- combinations(occurrences.tail)
      (c, frequency) = occurrences.head
      i <- (0 to frequency)
    } yield (if (i != 0) (c, i) :: rest else rest).sorted)
  }

  /** Subtracts occurrence list `y` from occurrence list `x`.
   *
   * The precondition is that the occurrence list `y` is a subset of
   * the occurrence list `x` -- any character appearing in `y` must
   * appear in `x`, and its frequency in `y` must be smaller or equal
   * than its frequency in `x`.
   *
   * Note: the resulting value is an occurrence - meaning it is sorted
   * and has no zero-entries.
   */
  def subtract(x: Occurrences, y: Occurrences): Occurrences = {
    def subtractFreq(freqMap: Map[Char, Int], occurence: (Char, Int)): Map[Char, Int] = {
      val (c, freqeuency) = occurence
      val newFreq = freqMap(c) - freqeuency
      if (newFreq == 0)
        freqMap - (c)
      else
        freqMap.updated(c, newFreq)
    }

    y.foldLeft(x.toMap)(subtractFreq).toList
  }

  /** Returns a list of all anagram sentences of the given sentence.
   *
   * An anagram of a sentence is formed by taking the occurrences of all the characters of
   * all the words in the sentence, and producing all possible combinations of words with those characters,
   * such that the words have to be from the dictionary.
   *
   * The number of words in the sentence and its anagrams does not have to correspond.
   * For example, the sentence `List("I", "love", "you")` is an anagram of the sentence `List("You", "olive")`.
   *
   * Also, two sentences with the same words but in a different order are considered two different anagrams.
   * For example, sentences `List("You", "olive")` and `List("olive", "you")` are different anagrams of
   * `List("I", "love", "you")`.
   *
   * Here is a full example of a sentence `List("Yes", "man")` and its anagrams for our dictionary:
   *
   * List(
   * List(en, as, my),
   * List(en, my, as),
   * List(man, yes),
   * List(men, say),
   * List(as, en, my),
   * List(as, my, en),
   * List(sane, my),
   * List(Sean, my),
   * List(my, en, as),
   * List(my, as, en),
   * List(my, sane),
   * List(my, Sean),
   * List(say, men),
   * List(yes, man)
   * )
   *
   * The different sentences do not have to be output in the order shown above - any order is fine as long as
   * all the anagrams are there. Every returned word has to exist in the dictionary.
   *
   * Note: in case that the words of the sentence are in the dictionary, then the sentence is the anagram of itself,
   * so it has to be returned in this list.
   *
   * Note: There is only one anagram of an empty sentence.
   */
  def sentenceAnagrams(sentence: Sentence): List[Sentence] = {
    if (sentence.isEmpty) List(List())
    else {
      def sentencesForOccurence(occurrences: Occurrences): List[Sentence] = {
        if (occurrences.isEmpty) List(List())
        else {
          val allCombinationsOfOccurences = combinations(occurrences)
          for {
            occurence <- allCombinationsOfOccurences
            remainingOccurences = subtract(occurrences, occurence)
            word <- dictionaryByOccurrences(occurence)
            sentence <- sentencesForOccurence(remainingOccurences)
          } yield word :: sentence
        }
      }

      sentencesForOccurence(sentenceOccurrences(sentence))
    }
  }
}








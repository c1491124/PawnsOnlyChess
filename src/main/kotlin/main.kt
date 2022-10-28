package chess

import java.util.*
import kotlin.math.absoluteValue

val board = List(8) { MutableList(8) { "   " } }
val colorNameMap = mapOf(0 to "white", 1 to "black")
val colorMap = mapOf(0 to " W ", 1 to " B ")
lateinit var firstPlayerName: String
lateinit var secondPlayerName: String
var ifEnPassant = false
val pawnPosition = listOf(mutableListOf<String>("01"), mutableListOf<String>())
fun main() {
    println(" Pawns-Only Chess")
    initName()
    initPosition()
    printBoard()
    play(firstPlayerName, secondPlayerName)
}

fun initPosition() {
    board[1].replaceAll { " B " }
    board[6].replaceAll { " W " }
}

fun printBoard() {
    board.forEachIndexed { index, rank ->
        println("  +---+---+---+---+---+---+---+---+")
        print("${8 - index} |")
        print(rank.joinToString("|"))
        println("|")
    }
    println("  +---+---+---+---+---+---+---+---+")
    println("    a   b   c   d   e   f   g   h")
}

fun initName() {
    println("First Player's name:")
    firstPlayerName = readln()
    println("Second Player's name:")
    secondPlayerName = readln()
}

data class ParsedCommand(val command: String) {
    var fileFrom = command[1].code - 48
    var fileTo = command[3].code - 48
    var fileDiff = fileTo - fileFrom
    var rankFrom = command[0]
    var rankTo = command[2]
    val rankFromIndex = rankFrom.code - 97
    val rankToIndex = rankTo.code - 97
}

fun updateGame(parsedCommand: ParsedCommand, color: Int) {
    updateTo(parsedCommand.fileTo, parsedCommand.rankToIndex, colorMap, color)
    updateFrom(parsedCommand.fileFrom, parsedCommand.rankFromIndex)
    printBoard()
}

fun checkCapture(parsedCommand: ParsedCommand, color: Int, lastTurnInfo: LastTurnInfo): Boolean {
    if (parsedCommand.fileDiff.absoluteValue == 1 &&
        (parsedCommand.rankToIndex - parsedCommand.rankFromIndex).absoluteValue == 1
    ) {
        // can not capture empty , except  "en passant"
        if (board[8 - parsedCommand.fileTo][parsedCommand.rankToIndex] == "   ") {
            return if (checkEnPassant(lastTurnInfo, parsedCommand.rankToIndex, parsedCommand.fileTo)) {
                ifEnPassant = true
                true
            } else {
                println("Invalid Input")
                false
            }
        }
        // can not capture our own piece
        if (board[8 - parsedCommand.fileTo][parsedCommand.rankToIndex] == colorMap[color]) {
            updateTo(parsedCommand.fileTo, parsedCommand.rankFromIndex, colorMap, color)
            updateFrom(parsedCommand.fileFrom, parsedCommand.rankFromIndex)
            println("Invalid Input")
            return false
        }
        //can only capture forward
        if (color % 2 == 0 && parsedCommand.fileDiff < 0) {
            println("Invalid Input")
            return false
        }
        if (color % 2 == 1 && parsedCommand.fileDiff > 0) {
            println("Invalid Input")
            return false
        }
        //successful capture
        return true
    } else {
        return false
    }
}

fun play(name1: String, name2: String) {
    var color = 0  // 0 means white ; 1 means black , can be used as map index
    val pattern = "[a-h][1-8][a-h][1-8]".toRegex()
    val playerMap = mapOf(0 to name1, 1 to name2)
    var lastTurnInfo = LastTurnInfo(0, 1, 1, 1)
    while (true) {
        println("${playerMap[color % 2]}'s turn:")
        val command = readln()
        if (command == "exit") {
            println("Bye!")
            break
        }
        if (!command.matches(pattern)) {
            println("Invalid Input")
            continue
        }
        val parsedCommand = ParsedCommand(command)
        //check move forward
        if (checkMove(parsedCommand, color)) {
            // updating position
            pawnPosition[color % 2] += "${parsedCommand.rankToIndex}+${parsedCommand.fileTo}"
            pawnPosition[color % 2] -= "${parsedCommand.rankFromIndex}+${parsedCommand.fileFrom}"
            updateGame(parsedCommand, color % 2)
            lastTurnInfo = LastTurnInfo(color, parsedCommand.fileFrom, parsedCommand.fileTo, parsedCommand.rankToIndex)
            if (checkEndGame(color % 2)) {
                break
            }
            color++
            continue
        } else if (checkCapture(parsedCommand, color, lastTurnInfo)) {
            //update position if en passant
            if (ifEnPassant) {
                updateBoardWithEnPassant(
                    color,
                    parsedCommand.fileFrom,
                    parsedCommand.fileTo,
                    parsedCommand.rankFromIndex,
                    parsedCommand.rankToIndex,
                    colorMap,
                    lastTurnInfo
                )
                //pawnPosition[(color+1) % 2]....
            } else {
                //normal capture update
                updateGame(parsedCommand, color % 2)
                //removePosition
            }
            pawnPosition[color % 2] += "${parsedCommand.rankToIndex}+${parsedCommand.fileTo}"
            pawnPosition[color % 2] -= "${parsedCommand.rankFromIndex}+${parsedCommand.fileFrom}"
            printBoard()
            lastTurnInfo = LastTurnInfo(color, parsedCommand.fileFrom, parsedCommand.fileTo, parsedCommand.rankToIndex)
            if (checkEndGame(color % 2)) {
                break
            }
            color++
            ifEnPassant = false
        }
    }
}


fun checkMove(parsedCommand: ParsedCommand, color: Int): Boolean {
    // check straight move .
    if (parsedCommand.rankFrom == parsedCommand.rankTo &&
        (parsedCommand.fileDiff.absoluteValue == 1 ||
                parsedCommand.fileDiff.absoluteValue == 2)
    ) {
        // You can only command your own pawn
        if (board[8 - parsedCommand.fileFrom][parsedCommand.rankFromIndex] != colorMap[color % 2]) {
            println("No ${colorNameMap[color % 2]} pawn at ${parsedCommand.command.substring(0, 2)}")
            return false
        }
        // Pawns can only step forward
        if (color % 2 == 0 && parsedCommand.fileDiff < 0) {
            println("Invalid Input")
            return false
        }
        if (color % 2 == 1 && parsedCommand.fileDiff > 0) {
            println("Invalid Input")
            return false
        }
        // Pawns can jump if only at the initial position
        if (parsedCommand.fileDiff.absoluteValue == 2) {
            if (color % 2 == 0 && parsedCommand.fileFrom != 2) {
                println("Invalid Input")
                return false
            }
            if (color % 2 == 1 && parsedCommand.fileFrom != 7) {
                println("Invalid Input")
                return false
            }
        }
        if (parsedCommand.rankFrom == parsedCommand.rankTo &&
            (parsedCommand.fileDiff == 0) &&
            board[8 - parsedCommand.fileFrom][parsedCommand.rankFromIndex] == "   "
        ) {
            println("No ${colorNameMap[color % 2]} pawn at ${parsedCommand.rankFrom}${parsedCommand.fileFrom}")
            return false
        }
        if (board[8 - parsedCommand.fileTo][parsedCommand.rankFromIndex] != "   ") {
            println("Invalid Input")
            return false
        }
        return true
    }
    return false
}

fun checkWin(color: Int): Boolean {
    val colorMap = mapOf(0 to " W ", 1 to " B ")
    if (board[0].contains(" W ") || board[7].contains(" B ")) {
        return true
    }
    if (board.all { it.all { grid -> grid != colorMap[(color + 1) % 2] } }) {
        return true
    }
    return false
}

fun checkDraw(color: Int): Boolean {
    return false
}

fun checkEndGame(color: Int): Boolean {
    if (checkDraw(color)) {
        println("Stalemate!")
        println("Bye!")
        return true
    }
    if (checkWin(color)) {
        val colorNameMap = mapOf(0 to "white", 1 to "black")
        println("${colorNameMap[color % 2]?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Wins!")
        println("Bye!")
        return true
    }
    return false
}

fun updateBoardWithEnPassant(
    color: Int,
    fileFrom: Int,
    fileTo: Int,
    rankFromIndex: Int,
    rankToIndex: Int,
    colorMap: Map<Int, String>,
    lastTurnInfo: LastTurnInfo
) {
    updateFrom(fileFrom, rankFromIndex)
    updateTo(fileTo, rankToIndex, colorMap, color)
    updateFrom(lastTurnInfo.fileTo, lastTurnInfo.rankToIndex)
}

private fun updateFrom(fileFrom: Int, rankIndex: Int) {
    board[8 - fileFrom][rankIndex] = "   "
}

private fun updateTo(
    fileTo: Int,
    rankIndex: Int,
    colorMap: Map<Int, String>,
    color: Int
) {
    board[8 - fileTo][rankIndex] = colorMap[color % 2] ?: "null"
}

data class LastTurnInfo(
    val color: Int,
    val fileFrom: Int,
    val fileTo: Int,
    val rankToIndex: Int
)


fun checkEnPassant(lastTurnInfo: LastTurnInfo, rankToIndex: Int, fileTo: Int): Boolean {
    val whiteJump = lastTurnInfo.fileFrom == 2 && lastTurnInfo.fileTo == 4
    val blackJump = lastTurnInfo.fileFrom == 7 && lastTurnInfo.fileTo == 5
    val theSameRank = lastTurnInfo.rankToIndex == rankToIndex
    val ifBlackBehind = lastTurnInfo.fileTo == 5 && fileTo == 6
    val ifWhiteBehind = lastTurnInfo.fileTo == 4 && fileTo == 3
    return when (lastTurnInfo.color % 2) {
        0 -> whiteJump && theSameRank && ifWhiteBehind
        1 -> blackJump && theSameRank && ifBlackBehind
        else -> false
    }
}

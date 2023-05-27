package tasklist
import kotlinx.datetime.*
import java.time.LocalTime
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

data class Task(
    var task: MutableList<String>,
    var prio: String,
    var date: String,
    var time: String,
    var due: String,
    )

var tasksList = mutableListOf<Task>()
var input = ""




fun main() {

    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val jsonAdapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))

    if (File("tasklist.json").exists()) {
        val jsonString = File("tasklist.json").readText()
        val tasksFromJson = jsonAdapter.fromJson(jsonString)
        if (tasksFromJson != null) {
            tasksList.addAll(tasksFromJson)
        }
    }

    while(true){
        println("Input an action (add, print, edit, delete, end):")
        input = readLine()!!.trim().lowercase()
        when(input){
            "add" -> addTask()
            "print" -> printTasks()
            "edit" -> editTaskList()
            "delete" -> deleteTask()
            "end" -> {
                println("Tasklist exiting!")
                val jsonFile = File("tasklist.json")
                jsonFile.writeText(jsonAdapter.toJson(tasksList))
                break
            }
            else -> println("The input action is invalid")
        }
    }

}

fun deleteTask(){

    if(tasksList.isEmpty()){
        println("No tasks have been input")
        return
    }
    printTasks()

    tasksList.removeAt(chooseTaskNumber() - 1)

    println("The task is deleted")
}

fun editTaskList(){

    if(tasksList.isEmpty()){
        println("No tasks have been input")
        return
    }
    printTasks()

    val taskNumber = chooseTaskNumber() - 1
    var fieldToEdit: String
    val validInputs = listOf("priority", "date", "time", "task")

    while(true){
        println("Input a field to edit (priority, date, time, task):")
        fieldToEdit = readLine()!!
        if (fieldToEdit in validInputs) break
        println("Invalid field")
    }

    when(fieldToEdit){
        "priority" -> tasksList[taskNumber].prio = setPriority()
        "date" -> {
            tasksList[taskNumber].date = setDate()
            tasksList[taskNumber].due = setDue(tasksList[taskNumber].date)
        }
        "time" -> tasksList[taskNumber].time = setTime()
        "task" -> tasksList[taskNumber].task = editTask()
    }

    println("The task is changed")

}

fun setDue(date: String): String {

    val dateParts = date.split("-")

    val taskDate = LocalDate(
        dateParts[0].toInt(),
        dateParts[1].toInt(),
        dateParts[2].toInt())

    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date

    val daysUntil = currentDate.daysUntil(taskDate)

    return when {
        daysUntil == 0 -> "T"
        daysUntil > 0 -> "I"
        else -> "O"
    }

}

fun chooseTaskNumber(): Int{

    var selection: String

    while(true){
        println("Input the task number (1-${tasksList.size}):")
        selection = readLine()!!
        if(selection.isNotBlank() && selection.toIntOrNull() in 1..tasksList.size) break
        println("Invalid task number")
    }

    return selection.toInt()
}

fun setPriority(): String{
    while(true){
        println("Input the task priority (C, H, N, L):")
        input = readLine()!!.trim().uppercase()

        val validInputs = listOf("C", "H", "N", "L")

        if (input in validInputs) {
            return input
        }
    }
}

fun setDate(): String {
    while(true) {
        println("Input the date (yyyy-mm-dd):")
        var date = readLine()!!
        try {
            val dateParts = date.split("-")

            if (dateParts.size == 3) {
                val year = dateParts[0].toInt()
                val month = dateParts[1].padStart(2, '0')
                val day = dateParts[2].padStart(2, '0')
                date = "$year-$month-$day"
                LocalDate.parse(date)
                return date
            }
            else{
                throw(IllegalArgumentException("Invalid date format"))
            }
        } catch (_: Exception) {
            println("The input date is invalid")

        }
    }

}

fun setTime(): String {
    while(true) {
        println("Input the time (hh:mm):")
        var time = readLine()!!
        try {
            val timeParts = time.split(":")

            if (timeParts.size == 2) {
                val hour = timeParts[0].padStart(2, '0')
                val minute = timeParts[1].padStart(2, '0')
                time = "$hour:$minute"
                LocalTime.parse(time)
                return time
            }
            else{
                throw(IllegalArgumentException("Invalid time format"))
            }

        } catch (_: Exception) {
            println("The input time is invalid")
        }
    }
}

fun addTask(){

    val prio = setPriority()
    val date = setDate()
    val time = setTime()
    val due = setDue(date)


    println("Input a new task (enter a blank line to end):")
    input = readLine()!!.trim()

    val currentTask = mutableListOf<String>()

    while(input.isNotEmpty()){
        currentTask.add(input)
        input = readLine()!!.trim()
    }

    if(currentTask.isNotEmpty()){
        tasksList.add(Task(currentTask, prio, date, time, due))
    }
    else{
        println("The task is blank")
    }

}

fun editTask(): MutableList<String> {

    println("Input a new task (enter a blank line to end):")
    input = readLine()!!.trim()

    val currentTask = mutableListOf<String>()

    while(input.isNotEmpty()){
        currentTask.add(input)
        input = readLine()!!.trim()
    }

    if(currentTask.isNotEmpty()){
        return currentTask
    }
    else{
        println("The task is blank")
    }

    return mutableListOf()

}

fun printTasks(){
    if(tasksList.isEmpty()){
        println("No tasks have been input")
    }
    else{
        println("+----+------------+-------+---+---+--------------------------------------------+\n" +
                "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                "+----+------------+-------+---+---+--------------------------------------------+")
        for(i in tasksList.indices){

            val prioColor = when(tasksList[i].prio){
                "C" -> "\u001B[101m \u001B[0m"
                "H" -> "\u001B[103m \u001B[0m"
                "N" -> "\u001B[102m \u001B[0m"
                else -> "\u001B[104m \u001B[0m"
            }
            val dueColor = when(tasksList[i].due){
                "I" -> "\u001B[102m \u001B[0m"
                "T" -> "\u001B[103m \u001B[0m"
                else -> "\u001B[101m \u001B[0m"
            }

            print("| ${i + 1}${if(i + 1 < 10) "  " else " "}| ${tasksList[i].date} |" +
                    " ${tasksList[i].time} | $prioColor | $dueColor |")

            for(j in 0 until tasksList[i].task.size){

                if(j > 0){
                    print("|    |            |       |   |   |")
                }

                var counter = 0

                for(l in 0 until tasksList[i].task[j].length){
                    print(tasksList[i].task[j][l])

                    counter += 1

                    if(counter == 44 && l != tasksList[i].task[j].length - 1){
                        println("|")
                        print("|    |            |       |   |   |")
                        counter = 0
                    }
                }

                for(x in counter until 44){
                    print(" ")
                }
                println("|")
            }

            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
    }
}






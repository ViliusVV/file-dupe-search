import kotlinx.coroutines.delay
import viliusvv.FileEntry
import viliusvv.fileEntries
import viliusvv.fileQueue
import viliusvv.filesTableModel
import viliusvv.infoModel
import java.io.File
import java.security.MessageDigest

fun calcFileHash(filePath: String): String {
    val digestInstance = MessageDigest.getInstance("MD5")
    val file = File(filePath)
    val bytes = file.readBytes()
    val hashBytes = digestInstance.digest(bytes)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun listSubdirAndFiles(dirs: List<File>) {
    infoModel.statusMessage = "Begins scanning directories..."
    for (root in dirs) {
        if (!root.exists() || !root.isDirectory) {
            println("Directory does not exist: $root")
            return
        }

        root.walkTopDown().forEach { file ->
            if (file.isDirectory) {
                println("Dir: ${file.absolutePath}")
                infoModel.statusMessage = "Scanning: ${file.absolutePath}"
            } else {
//            println("  File: ${file.name} (${file.length()} bytes)")
                fileQueue.add(file)
            }
        }

        infoModel.statusMessage = "Finished scanning root: $root"
    }

    infoModel.statusMessage = "Finished collecting files"
}

suspend fun addFilesToTable() {
    var remainingIdle = 1000
    while (infoModel.inProgress && remainingIdle >= 0) {
        if(fileQueue.isEmpty()) {
            delay(100)
            remainingIdle -= 100
            continue
        }

        val file = fileQueue.poll()
        val hash = if (infoModel.compareHash) calcFileHash(file.absolutePath) else "-"
        val entry = FileEntry(
            fileName = file.name,
            filePath = file.absolutePath,
            size = file.length(),
            modified = file.lastModified(),
            hash = hash
        )
        filesTableModel.addRow(entry)
        infoModel.processedFiles += 1
    }
}



val fileNameBucket = mutableMapOf<String, MutableList<FileEntry>>()
val sizeBucket = mutableMapOf<Long, MutableList<FileEntry>>()
val hashBucket = mutableMapOf<String, MutableList<FileEntry>>()

fun findIdenticalFileNames(): Map<String, List<FileEntry>> {
    for (entry in fileEntries) {
        val list = fileNameBucket.getOrPut(entry.fileName) { mutableListOf() }
        list.add(entry)
    }
    return fileNameBucket.filter { it.value.size > 1 }
}

fun findIdenticalFileSizes(): Map<Long, List<FileEntry>> {
    for (entry in fileEntries) {
        val list = sizeBucket.getOrPut(entry.size) { mutableListOf() }
        list.add(entry)
    }
    return sizeBucket.filter { it.value.size > 1 }
}

fun findIdenticalFiles(): Map<String, List<FileEntry>> {
    val dupeFileNames = fileNameBucket.filter { it.value.size > 1 }
    val dupeSizes = sizeBucket.filter { it.value.size > 1 }

    // get where both name and size match
    val sizeNameSame = dupeFileNames.mapValues { entry ->
        entry.value.filter { fileEntry ->
            dupeSizes.containsKey(fileEntry.size)
        }}

    for(same in sizeNameSame) {
        for(f in same.value) {
            f.hash = calcFileHash(f.filePath)
            val list = hashBucket.getOrPut(f.hash) { mutableListOf() }
            list.add(f)
        }
    }

    return hashBucket.filter { it.value.size > 1 }
}

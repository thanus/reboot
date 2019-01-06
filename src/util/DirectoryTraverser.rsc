module util::DirectoryTraverser

import IO;
import String;

public list[loc] getAllFilesFor(loc projectLocation, str extension = ".java") {
  list[loc] files = [];
  
  for (entry <- listEntries(projectLocation)) {
    if (endsWith(entry, extension)) {
      files += (projectLocation + entry);
    } elseif (isDirectory(projectLocation + entry)) {
      files += getAllFilesFor(projectLocation + entry, extension = extension);
    }
  }

  return files;
}

void checkIfLocationExistsAndIsDirectory(loc project) {
  if (!exists(project) && !isDirectory(project)) {
    throw "Location doesn\'t exist or is not a directory";
  }
}

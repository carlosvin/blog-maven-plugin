package com.maven.plugins.blog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

	public static Path change(Path inputDir, Path inputFile, Path outputDir) {
		Path subpath = subpath(inputDir, inputFile);
		return outputDir.resolve(subpath);
	}

	public static Path subpath(Path inputDir, Path inputFile) {
		int dirParts = inputDir.getNameCount();
		int fileParts = inputFile.getNameCount();
		if (fileParts > dirParts) {
			return subpath(inputDir.toString(), inputFile.toString());
		} else {
			return inputFile;
		}
	}

	private static Path subpath(String inputDir, String inputFile) {

		int index = inputFile.indexOf(inputDir);
		if (index == -1) {
			return Paths.get(inputFile);
		}
		String path = inputFile.substring(index + inputDir.length());

		if (path.startsWith("/") || path.startsWith("\\")) {
			return Paths.get(".", path).normalize();
		}
		return Paths.get(path);
	}

	public static Path switchExtension(Path file, String newExtension) {
		if (!newExtension.startsWith(".")) {
			newExtension = "." + newExtension;
		}
		String fileName = file.getFileName().toString();
		int i = fileName.lastIndexOf('.');
		if (i == -1) {
			return Paths.get(file.toString() + newExtension);
		} else {
			return file.resolveSibling(fileName.substring(0, i) + newExtension);
		}
	}
}

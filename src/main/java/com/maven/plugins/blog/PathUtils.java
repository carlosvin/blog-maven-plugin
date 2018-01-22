package com.maven.plugins.blog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
	public static Path change(Path inputdir, Path inputFile, Path outputDir) {
		Path subpath = subpath(inputdir, inputFile);
		return outputDir.resolve(subpath);
	}

	public static Path subpath(Path inputdir, Path inputFile) {
		return subpath(inputdir.toString(), inputFile.toString());
	}

	public static Path subpath(String inputDir, String inputFile) {
		String path = inputFile.replace(inputDir, "");
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
